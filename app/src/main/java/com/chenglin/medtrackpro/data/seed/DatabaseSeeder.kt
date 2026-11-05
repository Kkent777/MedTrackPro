package com.chenglin.medtrackpro.data.seed

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import com.chenglin.medtrackpro.data.csv.AssetsCsvReader
import com.chenglin.medtrackpro.data.local.MedTrackDatabase
import com.chenglin.medtrackpro.data.local.entity.MedicationEntity
import com.chenglin.medtrackpro.data.local.entity.PatientEntity
import com.chenglin.medtrackpro.data.local.entity.SymptomEntity
import com.chenglin.medtrackpro.data.model.Medication
import com.chenglin.medtrackpro.data.model.Patient
import com.chenglin.medtrackpro.data.model.Symptom
import com.chenglin.medtrackpro.data.prefs.MedTrackPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

// A3: First-launch CSV seeding into Room
object DatabaseSeeder {
    private const val TAG = "DatabaseSeeder"

    private val seedMutex = Mutex()

    suspend fun seedIfNeeded(context: Context) {
        val appContext = context.applicationContext

        withContext(Dispatchers.IO) {
            seedMutex.withLock {
                val preferences = MedTrackPreferences(appContext)
                if (preferences.isDatabaseSeeded()) {
                    return@withLock
                }

                try {
                    val csvReader = AssetsCsvReader()
                    val patients = csvReader.readPatients(appContext).map { it.toEntity() }
                    val medications = csvReader.readMedications(appContext).map { it.toEntity() }
                    val symptoms = csvReader.readSymptoms(appContext).map { it.toEntity() }
                    val database = MedTrackDatabase.getDatabase(appContext)

                    database.withTransaction {
                        database.patientDao().insertPatients(patients)
                        database.medicationDao().insertMedications(medications)
                        database.symptomDao().insertSymptoms(symptoms)
                    }

                    preferences.setDatabaseSeeded(true)
                } catch (exception: Exception) {
                    Log.e(TAG, "First-launch database seeding failed.", exception)
                }
            }
        }
    }

    private fun Patient.toEntity(): PatientEntity {
        val fullName = listOf(firstName, lastName)
            .filter { it.isNotBlank() }
            .joinToString(separator = " ")

        return PatientEntity(
            patientId = patientId,
            firstName = firstName,
            lastName = lastName,
            fullName = fullName,
            dateOfBirth = dateOfBirth,
            gender = gender,
            phoneNumber = phoneNumber,
            password = ""
        )
    }

    private fun Medication.toEntity(): MedicationEntity {
        return MedicationEntity(
            medicationId = medicationId,
            patientId = patientId,
            medicationName = medicationName,
            dosage = dosage,
            frequency = frequency,
            scheduledTime = scheduledTime,
            medicationType = medicationType,
            startDate = startDate,
            notes = notes
        )
    }

    private fun Symptom.toEntity(): SymptomEntity {
        return SymptomEntity(
            symptomId = symptomId,
            patientId = patientId,
            symptomName = symptomName,
            severity = severity.toIntOrNull() ?: 0,
            recordedDateTime = recordedDate,
            notes = notes
        )
    }
}
