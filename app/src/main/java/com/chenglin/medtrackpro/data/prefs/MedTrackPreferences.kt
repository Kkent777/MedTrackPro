package com.chenglin.medtrackpro.data.prefs

import android.content.Context

// A3: Session persistence using logged_in_patient_id
class MedTrackPreferences(context: Context) {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveLoggedInPatientId(patientId: String) {
        preferences.edit()
            .putString(KEY_LOGGED_IN_PATIENT_ID, patientId)
            .apply()
    }

    fun getLoggedInPatientId(): String? {
        return preferences.getString(KEY_LOGGED_IN_PATIENT_ID, null)
    }

    fun clearSession() {
        preferences.edit()
            .remove(KEY_LOGGED_IN_PATIENT_ID)
            .apply()
    }

    fun clearLoggedInPatientId() {
        clearSession()
    }

    fun isDatabaseSeeded(): Boolean {
        return preferences.getBoolean(KEY_DB_SEEDED, false)
    }

    fun setDatabaseSeeded(isSeeded: Boolean) {
        preferences.edit()
            .putBoolean(KEY_DB_SEEDED, isSeeded)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "medtrack_prefs"
        const val KEY_LOGGED_IN_PATIENT_ID = "logged_in_patient_id"
        const val KEY_DB_SEEDED = "db_seeded"
    }
}
