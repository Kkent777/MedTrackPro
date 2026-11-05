package com.chenglin.medtrackpro.ui.screens.signup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chenglin.medtrackpro.data.auth.AuthRepository
import com.chenglin.medtrackpro.data.auth.RegisterResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignUpViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val authRepository = AuthRepository.fromContext(application)
    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun onFullNameChange(value: String) {
        _uiState.update { it.copy(fullName = value, fullNameError = null, generalMessage = null) }
    }

    fun onPhoneNumberChange(value: String) {
        _uiState.update { it.copy(phoneNumber = value, phoneNumberError = null, generalMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, generalMessage = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value, confirmPasswordError = null, generalMessage = null) }
    }

    fun register() {
        val state = uiState.value
        val normalizedPhoneNumber = state.phoneNumber.filter { it.isDigit() }
        val fullNameError = if (state.fullName.trim().isBlank()) "Full name is required" else null
        val phoneNumberError = when {
            state.phoneNumber.trim().isBlank() -> "Phone number is required"
            normalizedPhoneNumber.length != 10 -> "Phone number must have exactly 10 digits"
            !normalizedPhoneNumber.startsWith("04") -> "Phone number must start with 04"
            else -> null
        }
        val passwordError = validatePassword(state.password)
        val confirmPasswordError = when {
            state.confirmPassword.isBlank() -> "Confirm password is required"
            state.confirmPassword != state.password -> "Passwords do not match"
            else -> null
        }

        if (
            fullNameError != null ||
            phoneNumberError != null ||
            passwordError != null ||
            confirmPasswordError != null
        ) {
            _uiState.update {
                it.copy(
                    fullNameError = fullNameError,
                    phoneNumberError = phoneNumberError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmPasswordError
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalMessage = null) }
            when (
                val result = authRepository.registerNewUser(
                    fullName = state.fullName,
                    phoneNumber = state.phoneNumber,
                    password = state.password
                )
            ) {
                is RegisterResult.Success -> {
                    _uiState.update {
                        SignUpUiState(
                            createdPatientId = result.patientId,
                            generalMessage = "Sign up successful. Your Patient ID is ${result.patientId}."
                        )
                    }
                }

                RegisterResult.PhoneAlreadyExists -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            phoneNumberError = "Phone number already exists"
                        )
                    }
                }
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

data class SignUpUiState(
    val fullName: String = "",
    val phoneNumber: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val fullNameError: String? = null,
    val phoneNumberError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val generalMessage: String? = null,
    val createdPatientId: String? = null,
    val isLoading: Boolean = false
)
