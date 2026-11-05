package com.chenglin.medtrackpro.data.auth

import android.content.Context
import com.chenglin.medtrackpro.data.local.MedTrackDatabase
import com.chenglin.medtrackpro.data.local.dao.PatientDao
import com.chenglin.medtrackpro.data.local.entity.PatientEntity
import com.chenglin.medtrackpro.data.prefs.MedTrackPreferences

class AuthRepository(
    private val patientDao: PatientDao,
    private val preferences: MedTrackPreferences
) {
    suspend fun patientExists(patientId: String): Boolean {
        return patientDao.getPatientById(patientId.trim()) != null
    }

    suspend fun seededAccountHasNoPassword(patientId: String): Boolean {
        val patient = patientDao.getPatientById(patientId.trim()) ?: return false
        return patient.password.isBlank()
    }

    // A3: Room-backed account claiming using PatientID, phone number, and new password
    suspend fun claimAccount(
        patientId: String,
        phoneNumber: String,
        newPassword: String
    ): AuthResult {
        val trimmedPatientId = patientId.trim()
        val normalizedPhoneNumber = normalizePhoneNumber(phoneNumber)
        val patient = patientDao.getPatientById(trimmedPatientId) ?: return AuthResult.PatientNotFound

        if (normalizePhoneNumber(patient.phoneNumber) != normalizedPhoneNumber) {
            return AuthResult.PhoneMismatch
        }

        if (patient.password.isNotBlank()) {
            return AuthResult.AccountAlreadyClaimed
        }

        patientDao.updatePassword(trimmedPatientId, newPassword)
        preferences.saveLoggedInPatientId(trimmedPatientId)
        return AuthResult.Success(trimmedPatientId)
    }

    // A3: Room-backed login using PatientID and password
    suspend fun login(patientId: String, password: String): AuthResult {
        val trimmedPatientId = patientId.trim()
        val patient = patientDao.getPatientById(trimmedPatientId) ?: return AuthResult.PatientNotFound

        if (patient.password.isBlank()) {
            return AuthResult.AccountNotClaimed
        }

        val authenticatedPatient = patientDao.getPatientByIdAndPassword(trimmedPatientId, password)
            ?: return AuthResult.IncorrectPassword

        preferences.saveLoggedInPatientId(authenticatedPatient.patientId)
        return AuthResult.Success(authenticatedPatient.patientId)
    }

    suspend fun registerNewUser(
        fullName: String,
        phoneNumber: String,
        password: String
    ): RegisterResult {
        val normalizedPhoneNumber = normalizePhoneNumber(phoneNumber)
        val existingPhone = patientDao.getPatientByPhoneNumber(normalizedPhoneNumber)
        if (existingPhone != null) {
            return RegisterResult.PhoneAlreadyExists
        }

        val patientId = generateNextPatientId(patientDao.getAllPatientIds())
        val nameParts = fullName.trim().split(Regex("\\s+"), limit = 2)
        val patient = PatientEntity(
            patientId = patientId,
            firstName = nameParts.getOrElse(0) { "" },
            lastName = nameParts.getOrElse(1) { "" },
            fullName = fullName.trim(),
            phoneNumber = normalizedPhoneNumber,
            password = password
        )

        patientDao.insertPatient(patient)
        return RegisterResult.Success(patientId)
    }

    suspend fun getCurrentLoggedInPatient(): PatientEntity? {
        val patientId = preferences.getLoggedInPatientId()?.takeIf { it.isNotBlank() } ?: return null
        return patientDao.getPatientById(patientId)
    }

    fun getLoggedInPatientId(): String? {
        return preferences.getLoggedInPatientId()
    }

    fun clearSession() {
        preferences.clearLoggedInPatientId()
    }

    private fun generateNextPatientId(existingIds: List<String>): String {
        val highestParsedId = existingIds
            .mapNotNull { patientId -> parsePatientId(patientId) }
            .maxByOrNull { parsedId -> parsedId.number }

        if (highestParsedId == null) {
            return "P1001"
        }

        val nextNumber = highestParsedId.number + 1
        return "${highestParsedId.prefix}${nextNumber.toString().padStart(highestParsedId.numberWidth, '0')}"
    }

    private fun parsePatientId(patientId: String): ParsedPatientId? {
        val match = PATIENT_ID_PATTERN.matchEntire(patientId.trim()) ?: return null
        val number = match.groupValues[2].toIntOrNull() ?: return null

        return ParsedPatientId(
            prefix = match.groupValues[1],
            number = number,
            numberWidth = match.groupValues[2].length
        )
    }

    private fun normalizePhoneNumber(phoneNumber: String): String {
        return phoneNumber.filter { it.isDigit() }
    }

    companion object {
        private val PATIENT_ID_PATTERN = Regex("^([A-Za-z]+)(\\d+)$")

        fun fromContext(context: Context): AuthRepository {
            val appContext = context.applicationContext
            return AuthRepository(
                patientDao = MedTrackDatabase.getDatabase(appContext).patientDao(),
                preferences = MedTrackPreferences(appContext)
            )
        }
    }
}

sealed interface AuthResult {
    data class Success(val patientId: String) : AuthResult
    data object PatientNotFound : AuthResult
    data object PhoneMismatch : AuthResult
    data object AccountAlreadyClaimed : AuthResult
    data object AccountNotClaimed : AuthResult
    data object IncorrectPassword : AuthResult
}

sealed interface RegisterResult {
    data class Success(val patientId: String) : RegisterResult
    data object PhoneAlreadyExists : RegisterResult
}

private data class ParsedPatientId(
    val prefix: String,
    val number: Int,
    val numberWidth: Int
)
