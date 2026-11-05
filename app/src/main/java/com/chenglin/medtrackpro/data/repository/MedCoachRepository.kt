package com.chenglin.medtrackpro.data.repository

import android.content.Context
import com.chenglin.medtrackpro.BuildConfig
import com.chenglin.medtrackpro.data.local.MedTrackDatabase
import com.chenglin.medtrackpro.data.local.dao.MedCoachTipDao
import com.chenglin.medtrackpro.data.local.dao.MedicationDao
import com.chenglin.medtrackpro.data.local.dao.PatientDao
import com.chenglin.medtrackpro.data.local.dao.SymptomDao
import com.chenglin.medtrackpro.data.local.entity.MedCoachTipEntity
import com.chenglin.medtrackpro.data.local.entity.MedicationEntity
import com.chenglin.medtrackpro.data.local.entity.PatientEntity
import com.chenglin.medtrackpro.data.local.entity.SymptomEntity
import com.chenglin.medtrackpro.data.prefs.MedTrackPreferences
import com.chenglin.medtrackpro.data.remote.GeminiApiService
import com.chenglin.medtrackpro.data.remote.GeminiContent
import com.chenglin.medtrackpro.data.remote.GeminiErrorResponse
import com.chenglin.medtrackpro.data.remote.GeminiGenerateContentRequest
import com.chenglin.medtrackpro.data.remote.GeminiPart
import com.chenglin.medtrackpro.data.remote.OpenFdaApiService
import com.chenglin.medtrackpro.data.remote.OpenFdaDrugLabel
import com.chenglin.medtrackpro.data.remote.RetrofitProvider
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.io.IOException
import com.google.gson.Gson
import retrofit2.HttpException

class MedCoachRepository(
    private val openFdaApiService: OpenFdaApiService,
    private val geminiApiService: GeminiApiService,
    private val medicationDao: MedicationDao,
    private val patientDao: PatientDao,
    private val symptomDao: SymptomDao,
    private val medCoachTipDao: MedCoachTipDao,
    private val preferences: MedTrackPreferences
) {
    private val gson = Gson()

    suspend fun getCurrentPatientMedicationNames(): List<String> {
        val patientId = preferences.getLoggedInPatientId().orEmpty()
        if (patientId.isBlank()) {
            return emptyList()
        }

        return medicationDao.getMedicationsForPatient(patientId)
            .map { medication -> medication.medicationName }
            .filter { medicationName -> medicationName.isNotBlank() }
            .distinct()
            .sorted()
    }

    // A3: OpenFDA drug information lookup through Retrofit
    suspend fun lookupDrugInfo(medicationName: String): DrugInfoLookupResult {
        val trimmedMedicationName = medicationName.trim()
        if (trimmedMedicationName.isBlank()) {
            return DrugInfoLookupResult.EmptyInput
        }

        return try {
            val brandResult = findFirstLabel("openfda.brand_name:\"$trimmedMedicationName\"")
            val label = brandResult ?: findFirstLabel("openfda.generic_name:\"$trimmedMedicationName\"")

            if (label == null) {
                DrugInfoLookupResult.NotFound
            } else {
                DrugInfoLookupResult.Success(label.toDrugInfo(trimmedMedicationName))
            }
        } catch (_: IOException) {
            DrugInfoLookupResult.NetworkError
        } catch (_: HttpException) {
            DrugInfoLookupResult.NotFound
        } catch (_: Exception) {
            DrugInfoLookupResult.NetworkError
        }
    }

    // A3: Gemini medication tip generation using patient-specific context
    suspend fun generateMedicationTip(apiKey: String): GenerateTipResult {
        if (apiKey.isBlank()) {
            return GenerateTipResult.MissingApiKey
        }

        val patientId = preferences.getLoggedInPatientId().orEmpty()
        if (patientId.isBlank()) {
            return GenerateTipResult.NoLoggedInPatient
        }

        val patient = patientDao.getPatientById(patientId) ?: return GenerateTipResult.NoLoggedInPatient
        val medications = medicationDao.getMedicationsForPatient(patientId)
        val symptoms = symptomDao.getSymptomsForPatient(patientId).take(5)
        val prompt = buildMedicationTipPrompt(
            patient = patient,
            medications = medications,
            symptoms = symptoms
        )

        return try {
            val response = geminiApiService.generateContent(
                model = GEMINI_MODEL,
                apiKey = apiKey,
                request = GeminiGenerateContentRequest(
                    contents = listOf(
                        GeminiContent(
                            parts = listOf(GeminiPart(text = prompt))
                        )
                    )
                )
            )
            val tipText = response.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text
                ?.trim()
                .orEmpty()

            if (tipText.isBlank()) {
                GenerateTipResult.EmptyResponse
            } else {
                medCoachTipDao.insertTip(
                    MedCoachTipEntity(
                        patientId = patientId,
                        tipText = tipText,
                        timestamp = System.currentTimeMillis()
                    )
                )
                GenerateTipResult.Success(tipText)
            }
        } catch (_: IOException) {
            GenerateTipResult.NetworkError
        } catch (exception: HttpException) {
            GenerateTipResult.ApiError(exception.toFriendlyGeminiMessage())
        } catch (_: Exception) {
            GenerateTipResult.NetworkError
        }
    }

    // A3: MedCoach tip history stored and loaded from Room
    suspend fun getCurrentPatientTips(): List<MedCoachTip> {
        val patientId = preferences.getLoggedInPatientId().orEmpty()
        if (patientId.isBlank()) {
            return emptyList()
        }

        return medCoachTipDao.getTipsForPatient(patientId).map { tip ->
            tip.toModel()
        }
    }

    private suspend fun findFirstLabel(search: String): OpenFdaDrugLabel? {
        return try {
            openFdaApiService.searchDrugLabel(search = search, limit = 1)
                .results
                ?.firstOrNull()
        } catch (exception: HttpException) {
            if (exception.code() == 404) {
                null
            } else {
                throw exception
            }
        }
    }

    private fun OpenFdaDrugLabel.toDrugInfo(searchName: String): DrugInfo {
        return DrugInfo(
            displayName = openfda?.brandName.firstDisplayValue()
                .takeUnless { it == NOT_AVAILABLE }
                ?: searchName,
            genericName = openfda?.genericName.firstDisplayValue(),
            purpose = purpose.firstDisplayValue(),
            warnings = warnings.firstDisplayValue(),
            dosageAndAdministration = dosageAndAdministration.firstDisplayValue(),
            activeIngredient = activeIngredient.firstDisplayValue(),
            indicationsAndUsage = indicationsAndUsage.firstDisplayValue()
        )
    }

    private fun List<String>?.firstDisplayValue(): String {
        return this
            ?.firstOrNull { value -> value.isNotBlank() }
            ?.trim()
            ?: NOT_AVAILABLE
    }

    private fun buildMedicationTipPrompt(
        patient: PatientEntity,
        medications: List<MedicationEntity>,
        symptoms: List<SymptomEntity>
    ): String {
        val patientName = patient.fullName
            .takeIf { it.isNotBlank() }
            ?: listOf(patient.firstName, patient.lastName)
                .filter { it.isNotBlank() }
                .joinToString(" ")
                .ifBlank { "this patient" }
        val medicationSummary = medications
            .takeIf { it.isNotEmpty() }
            ?.joinToString(separator = "\n") { medication ->
                "- ${medication.medicationName}, ${medication.dosage}, ${medication.frequency}, scheduled ${medication.scheduledTime.ifBlank { "not set" }}"
            }
            ?: "- No medications recorded"
        val symptomSummary = symptoms
            .takeIf { it.isNotEmpty() }
            ?.joinToString(separator = "\n") { symptom ->
                "- ${symptom.symptomName}, severity ${symptom.severity}/10, ${symptom.recordedDateTime}, notes: ${symptom.notes.ifBlank { "none" }}"
            }
            ?: "- No recent symptoms recorded"

        return """
            You are MedCoach in a medication tracking app. Write one concise, friendly medication-management tip for $patientName.
            Use the patient context below, but do not diagnose, do not recommend changing prescribed doses, and advise contacting a clinician for concerning symptoms.

            Current medications:
            $medicationSummary

            Recent symptom history:
            $symptomSummary

            Keep the response under 120 words and make it personalised to the medication/symptom context.
        """.trimIndent()
    }

    private fun MedCoachTipEntity.toModel(): MedCoachTip {
        return MedCoachTip(
            id = id,
            tipText = tipText,
            timestamp = timestamp,
            formattedTimestamp = Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy, h:mm a"))
        )
    }

    private fun HttpException.toFriendlyGeminiMessage(): String {
        val responseMessage = response()
            ?.errorBody()
            ?.string()
            ?.let { body ->
                runCatching {
                    gson.fromJson(body, GeminiErrorResponse::class.java)
                        ?.error
                        ?.message
                        ?.takeIf { message -> message.isNotBlank() }
                }.getOrNull()
            }
        val fallback = when (code()) {
            400 -> "Gemini rejected the request. Check that the API key and model are valid for this project."
            401, 403 -> "Gemini rejected the API key or project permissions."
            404 -> "The selected Gemini model was not found for this API version."
            429 -> "Gemini rate limit reached. Please try again later."
            else -> "Gemini returned an error. Please try again later."
        }

        return responseMessage ?: fallback
    }

    companion object {
        private const val NOT_AVAILABLE = "Not available"
        private const val GEMINI_MODEL = "gemini-2.5-flash"

        fun fromContext(context: Context): MedCoachRepository {
            val appContext = context.applicationContext
            val database = MedTrackDatabase.getDatabase(appContext)
            return MedCoachRepository(
                openFdaApiService = RetrofitProvider.openFdaApiService,
                geminiApiService = RetrofitProvider.geminiApiService,
                medicationDao = database.medicationDao(),
                patientDao = database.patientDao(),
                symptomDao = database.symptomDao(),
                medCoachTipDao = database.medCoachTipDao(),
                preferences = MedTrackPreferences(appContext)
            )
        }

        fun configuredGeminiApiKey(): String {
            return BuildConfig.GEMINI_API_KEY
        }
    }
}

sealed interface DrugInfoLookupResult {
    data class Success(val drugInfo: DrugInfo) : DrugInfoLookupResult
    data object EmptyInput : DrugInfoLookupResult
    data object NotFound : DrugInfoLookupResult
    data object NetworkError : DrugInfoLookupResult
}

data class DrugInfo(
    val displayName: String,
    val genericName: String,
    val purpose: String,
    val warnings: String,
    val dosageAndAdministration: String,
    val activeIngredient: String,
    val indicationsAndUsage: String
)

sealed interface GenerateTipResult {
    data class Success(val tipText: String) : GenerateTipResult
    data object MissingApiKey : GenerateTipResult
    data object NoLoggedInPatient : GenerateTipResult
    data object EmptyResponse : GenerateTipResult
    data object NetworkError : GenerateTipResult
    data class ApiError(val message: String) : GenerateTipResult
}

data class MedCoachTip(
    val id: Long,
    val tipText: String,
    val timestamp: Long,
    val formattedTimestamp: String
)
