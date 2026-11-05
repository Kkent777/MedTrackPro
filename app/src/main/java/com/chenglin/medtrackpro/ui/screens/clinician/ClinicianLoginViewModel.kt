package com.chenglin.medtrackpro.ui.screens.clinician

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ClinicianLoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ClinicianLoginUiState())
    val uiState: StateFlow<ClinicianLoginUiState> = _uiState.asStateFlow()

    fun onAccessKeyChange(value: String) {
        _uiState.update {
            it.copy(
                accessKey = value,
                errorMessage = null
            )
        }
    }

    fun submitAccessKey(): Boolean {
        val isCorrect = uiState.value.accessKey.trim() == CLINICIAN_ACCESS_KEY
        _uiState.update {
            it.copy(
                errorMessage = if (isCorrect) {
                    null
                } else {
                    "Access key is incorrect. Please try again."
                }
            )
        }
        return isCorrect
    }

    companion object {
        private const val CLINICIAN_ACCESS_KEY = "dollar-entry-apples"
    }
}

data class ClinicianLoginUiState(
    val accessKey: String = "",
    val errorMessage: String? = null
)
