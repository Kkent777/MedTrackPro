package com.chenglin.medtrackpro.data.repository

import android.content.Context
import com.chenglin.medtrackpro.data.local.MedTrackDatabase
import com.chenglin.medtrackpro.data.local.dao.MedicationDao
import com.chenglin.medtrackpro.data.local.dao.PatientDao
import com.chenglin.medtrackpro.data.local.dao.SymptomDao
import com.chenglin.medtrackpro.data.remote.GeminiApiService
import com.chenglin.medtrackpro.data.remote.GeminiContent
import com.chenglin.medtrackpro.data.remote.GeminiErrorResponse
import com.chenglin.medtrackpro.data.remote.GeminiGenerateContentRequest
import com.chenglin.medtrackpro.data.remote.GeminiPart
import com.chenglin.medtrackpro.data.remote.RetrofitProvider
import com.google.gson.Gson
import java.io.IOException
import java.util.Locale
import retrofit2.HttpException

// A3: Clinician dashboard aggregate statistics and Gemini insights
class ClinicianDashboardRepository(
    private val patientDao: PatientDao,
    private val medicationDao: MedicationDao,
    private val symptomDao: SymptomDao,
    private val geminiApiService: GeminiApiService
) {
    private val gson = Gson()

    suspend fun loadStatistics(): ClinicianStats {
        return ClinicianStats(
            totalPatients = patientDao.getPatientCount(),
            averageMedicationsPerPatient = medicationDao.getAverageMedicationCountPerPatient() ?: 0.0,
            mostCommonSymptomCategory = symptomDao.getMostCommonSymptomName() ?: "Not available",
            averageSymptomSeverity = symptomDao.getAverageSymptomSeverity() ?: 0.0
        )
    }

    suspend fun findPatterns(stats: ClinicianStats): ClinicianPatternResult {
        val apiKey = MedCoachRepository.configuredGeminiApiKey()
        if (apiKey.isBlank()) {
            return ClinicianPatternResult.MissingApiKey
        }

        return try {
            val response = geminiApiService.generateContent(
                model = GEMINI_MODEL,
                apiKey = apiKey,
                request = GeminiGenerateContentRequest(
                    contents = listOf(
                        GeminiContent(
                            parts = listOf(GeminiPart(text = buildPatternPrompt(stats)))
                        )
                    )
                )
            )
            val text = response.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text
                ?.trim()
                .orEmpty()
            val insights = parseInsights(text)

            if (insights.isEmpty()) {
                ClinicianPatternResult.EmptyResponse
            } else {
                ClinicianPatternResult.Success(insights)
            }
        } catch (_: IOException) {
            ClinicianPatternResult.NetworkError
        } catch (exception: HttpException) {
            ClinicianPatternResult.ApiError(exception.toFriendlyGeminiMessage())
        } catch (_: Exception) {
            ClinicianPatternResult.NetworkError
        }
    }

    private fun buildPatternPrompt(stats: ClinicianStats): String {
        return """
            You are helping a clinician review aggregate medication-tracking app data.
            Use only the aggregate statistics below. Do not identify individual patients, diagnose, or recommend dose changes.

            Aggregate data:
            - Total patients: ${stats.totalPatients}
            - Average medications per patient: ${stats.averageMedicationsPerPatient.formatStat()}
            - Most common symptom category: ${stats.mostCommonSymptomCategory}
            - Average symptom severity out of 10: ${stats.averageSymptomSeverity.formatStat()}

            Return exactly 3 concise, data-driven observations or patterns.
            Format each observation as a short numbered list item.
        """.trimIndent()
    }

    private fun parseInsights(text: String): List<String> {
        return text
            .lines()
            .map { line ->
                line.trim()
                    .removePrefix("-")
                    .replace(Regex("^\\d+[.)]\\s*"), "")
                    .trim()
            }
            .filter { line -> line.isNotBlank() }
            .take(3)
    }

    private fun Double.formatStat(): String {
        return String.format(Locale.US, "%.1f", this)
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
        private const val GEMINI_MODEL = "gemini-2.5-flash"

        fun fromContext(context: Context): ClinicianDashboardRepository {
            val database = MedTrackDatabase.getDatabase(context.applicationContext)
            return ClinicianDashboardRepository(
                patientDao = database.patientDao(),
                medicationDao = database.medicationDao(),
                symptomDao = database.symptomDao(),
                geminiApiService = RetrofitProvider.geminiApiService
            )
        }
    }
}

data class ClinicianStats(
    val totalPatients: Int,
    val averageMedicationsPerPatient: Double,
    val mostCommonSymptomCategory: String,
    val averageSymptomSeverity: Double
)

sealed interface ClinicianPatternResult {
    data class Success(val insights: List<String>) : ClinicianPatternResult
    data object MissingApiKey : ClinicianPatternResult
    data object EmptyResponse : ClinicianPatternResult
    data object NetworkError : ClinicianPatternResult
    data class ApiError(val message: String) : ClinicianPatternResult
}
