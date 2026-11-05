package com.chenglin.medtrackpro.data.repository

import android.content.Context
import com.chenglin.medtrackpro.data.local.MedTrackDatabase
import com.chenglin.medtrackpro.data.local.dao.MedicationDao
import com.chenglin.medtrackpro.data.local.dao.PatientDao
import com.chenglin.medtrackpro.data.local.entity.MedicationEntity
import com.chenglin.medtrackpro.data.model.Medication
import com.chenglin.medtrackpro.data.prefs.MedTrackPreferences
import java.time.LocalDate

class MedicationRepository(
    private val medicationDao: MedicationDao,
    private val patientDao: PatientDao,
    private val preferences: MedTrackPreferences
) {
    // A3: Home loads current patient medications from Room
    suspend fun getHomeData(): HomeData {
        val patientId = preferences.getLoggedInPatientId().orEmpty()
        if (patientId.isBlank()) {
            return HomeData(patientName = "Patient", medications = emptyList())
        }

        val patient = patientDao.getPatientById(patientId)
        val medications = medicationDao.getMedicationsForPatient(patientId).map { it.toModel() }
        val patientName = patient?.fullName
            ?.takeIf { it.isNotBlank() }
            ?: listOf(patient?.firstName.orEmpty(), patient?.lastName.orEmpty())
                .filter { it.isNotBlank() }
                .joinToString(" ")
                .ifBlank { patientId }

        return HomeData(
            patientName = patientName,
            medications = medications
        )
    }

    // A3: Add Medication saves new medication records to Room
    suspend fun addMedication(medication: Medication) {
        medicationDao.insertMedication(medication.toEntity())
    }

    // A3: Taken toggle persistence with daily reset
    suspend fun updateTakenState(medicationId: String, isTaken: Boolean) {
        val patientId = preferences.getLoggedInPatientId().orEmpty()
        if (patientId.isBlank()) {
            return
        }

        medicationDao.updateTakenState(
            medicationId = medicationId,
            patientId = patientId,
            isTaken = isTaken,
            takenDate = LocalDate.now().toString()
        )
    }

    fun getLoggedInPatientId(): String? {
        return preferences.getLoggedInPatientId()
    }

    private fun MedicationEntity.toModel(): Medication {
        return Medication(
            medicationId = medicationId,
            patientId = patientId,
            medicationName = medicationName,
            dosage = dosage,
            frequency = frequency,
            medicationType = medicationType,
            scheduledTime = scheduledTime,
            startDate = startDate,
            notes = notes,
            isTakenToday = takenToday && takenDate == LocalDate.now().toString()
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
            notes = notes,
            takenToday = isTakenToday,
            takenDate = if (isTakenToday) LocalDate.now().toString() else ""
        )
    }

    companion object {
        fun fromContext(context: Context): MedicationRepository {
            val appContext = context.applicationContext
            val database = MedTrackDatabase.getDatabase(appContext)
            return MedicationRepository(
                medicationDao = database.medicationDao(),
                patientDao = database.patientDao(),
                preferences = MedTrackPreferences(appContext)
            )
        }
    }
}

data class HomeData(
    val patientName: String,
    val medications: List<Medication>
)
