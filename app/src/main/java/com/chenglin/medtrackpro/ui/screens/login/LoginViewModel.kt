package com.chenglin.medtrackpro.ui.screens.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chenglin.medtrackpro.data.auth.AuthRepository
import com.chenglin.medtrackpro.data.auth.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val authRepository = AuthRepository.fromContext(application)
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onModeChange(mode: LoginMode) {
        _uiState.update {
            it.copy(
                mode = mode,
                message = null,
                patientIdError = null,
                phoneNumberError = null,
                passwordError = null
            )
        }
    }

    fun onPatientIdChange(value: String) {
        _uiState.update { it.copy(patientId = value, patientIdError = null, message = null) }
    }

    fun onPhoneNumberChange(value: String) {
        _uiState.update { it.copy(phoneNumber = value, phoneNumberError = null, message = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, message = null) }
    }

    fun onNewPasswordChange(value: String) {
        _uiState.update { it.copy(newPassword = value, passwordError = null, message = null) }
    }

    fun submit(onSuccess: () -> Unit) {
        val state = uiState.value
        val trimmedPatientId = state.patientId.trim()

        if (state.mode == LoginMode.Login) {
            submitLogin(trimmedPatientId, state.password, onSuccess)
        } else {
            submitClaim(trimmedPatientId, state.phoneNumber, state.newPassword, onSuccess)
        }
    }

    private fun submitLogin(patientId: String, password: String, onSuccess: () -> Unit) {
        val patientIdError = if (patientId.isBlank()) "Patient ID is required" else null
        val passwordError = if (password.isBlank()) "Password is required" else null
        if (patientIdError != null || passwordError != null) {
            _uiState.update {
                it.copy(patientIdError = patientIdError, passwordError = passwordError)
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            val result = authRepository.login(patientId, password)
            handleAuthResult(result, onSuccess)
        }
    }

    private fun submitClaim(
        patientId: String,
        phoneNumber: String,
        newPassword: String,
        onSuccess: () -> Unit
    ) {
        val normalizedPhoneNumber = phoneNumber.filter { it.isDigit() }
        val patientIdError = if (patientId.isBlank()) "Patient ID is required" else null
        val phoneNumberError = when {
            phoneNumber.trim().isBlank() -> "Phone number is required"
            normalizedPhoneNumber.length != 10 -> "Phone number must have exactly 10 digits"
            !normalizedPhoneNumber.startsWith("04") -> "Phone number must start with 04"
            else -> null
        }
        val passwordError = validatePassword(newPassword)

        if (patientIdError != null || phoneNumberError != null || passwordError != null) {
            _uiState.update {
                it.copy(
                    patientIdError = patientIdError,
                    phoneNumberError = phoneNumberError,
                    passwordError = passwordError
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            val result = authRepository.claimAccount(patientId, phoneNumber, newPassword)
            handleAuthResult(result, onSuccess)
        }
    }

    private fun handleAuthResult(result: AuthResult, onSuccess: () -> Unit) {
        when (result) {
            is AuthResult.Success -> {
                _uiState.update { it.copy(isLoading = false) }
                onSuccess()
            }

            AuthResult.PatientNotFound -> {
                _uiState.update { it.copy(isLoading = false, message = "No account found with this Patient ID") }
            }

            AuthResult.PhoneMismatch -> {
                _uiState.update { it.copy(isLoading = false, message = "Patient ID and phone number do not match") }
            }

            AuthResult.AccountAlreadyClaimed -> {
                _uiState.update { it.copy(isLoading = false, message = "This account is already claimed. Please log in.") }
            }

            AuthResult.AccountNotClaimed -> {
                _uiState.update { it.copy(isLoading = false, message = "First-time account setup is required before login") }
            }

            AuthResult.IncorrectPassword -> {
                _uiState.update { it.copy(isLoading = false, message = "Incorrect password") }
            }
        }
    }

    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Password is required"
            password.length < 8 -> "Password must be at least 8 characters"
            password.none { it.isLetter() } || password.none { it.isDigit() } -> {
                "Password must contain at least one letter and one number"
            }
            else -> null
        }
    }

}

data class LoginUiState(
    val mode: LoginMode = LoginMode.Login,
    val patientId: String = "",
    val phoneNumber: String = "",
    val password: String = "",
    val newPassword: String = "",
    val patientIdError: String? = null,
    val phoneNumberError: String? = null,
    val passwordError: String? = null,
    val message: String? = null,
    val isLoading: Boolean = false
)

enum class LoginMode {
    Login,
    Claim
}
