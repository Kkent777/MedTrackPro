package com.chenglin.medtrackpro.data.repository

import android.content.Context
import com.chenglin.medtrackpro.data.local.MedTrackDatabase
import com.chenglin.medtrackpro.data.local.dao.SymptomDao
import com.chenglin.medtrackpro.data.local.entity.SymptomEntity
import com.chenglin.medtrackpro.data.model.Symptom
import com.chenglin.medtrackpro.data.prefs.MedTrackPreferences

// A3: Symptoms screen loads and saves patient symptom records through Room
class SymptomRepository(
    private val symptomDao: SymptomDao,
    private val preferences: MedTrackPreferences
) {
    suspend fun getSymptomsForCurrentPatient(): List<Symptom> {
        val patientId = preferences.getLoggedInPatientId().orEmpty()
        if (patientId.isBlank()) {
            return emptyList()
        }

        return symptomDao.getSymptomsForPatient(patientId).map { it.toModel() }
    }

    suspend fun addSymptom(
        symptomName: String,
        severity: Int,
        recordedDateTime: String,
        notes: String
    ): Boolean {
        val patientId = preferences.getLoggedInPatientId().orEmpty()
        if (patientId.isBlank()) {
            return false
        }

        symptomDao.insertSymptom(
            SymptomEntity(
                symptomId = "SYM${System.currentTimeMillis()}",
                patientId = patientId,
                symptomName = symptomName,
                severity = severity,
                recordedDateTime = recordedDateTime,
                notes = notes
            )
        )
        return true
    }

    private fun SymptomEntity.toModel(): Symptom {
        return Symptom(
            symptomId = symptomId,
            patientId = patientId,
            symptomName = symptomName,
            severity = severity.toString(),
            recordedDate = recordedDateTime,
            notes = notes
        )
    }

    companion object {
        fun fromContext(context: Context): SymptomRepository {
            val appContext = context.applicationContext
            return SymptomRepository(
                symptomDao = MedTrackDatabase.getDatabase(appContext).symptomDao(),
                preferences = MedTrackPreferences(appContext)
            )
        }
    }
}
