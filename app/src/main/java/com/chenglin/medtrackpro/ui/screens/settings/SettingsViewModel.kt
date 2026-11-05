package com.chenglin.medtrackpro.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chenglin.medtrackpro.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// A3: Settings screen displays current patient info and account actions
class SettingsViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val authRepository = AuthRepository.fromContext(application)
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            val patient = authRepository.getCurrentLoggedInPatient()
            val name = patient?.fullName
                ?.takeIf { it.isNotBlank() }
                ?: listOf(patient?.firstName.orEmpty(), patient?.lastName.orEmpty())
                    .filter { it.isNotBlank() }
                    .joinToString(" ")
                    .ifBlank { "Patient" }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    name = name,
                    phoneNumber = patient?.phoneNumber.orEmpty(),
                    patientId = patient?.patientId.orEmpty()
                )
            }
        }
    }
}

data class SettingsUiState(
    val isLoading: Boolean = true,
    val name: String = "Patient",
    val phoneNumber: String = "",
    val patientId: String = ""
)
