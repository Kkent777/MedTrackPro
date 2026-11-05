package com.chenglin.medtrackpro.navigation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chenglin.medtrackpro.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// A3: Session persistence using logged_in_patient_id
class SessionViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val authRepository = AuthRepository.fromContext(application)
    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val patient = authRepository.getCurrentLoggedInPatient()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    hasValidSession = patient != null
                )
            }
        }
    }

    fun logout() {
        authRepository.clearSession()
        _uiState.update {
            it.copy(
                isLoading = false,
                hasValidSession = false
            )
        }
    }

}

data class SessionUiState(
    val isLoading: Boolean = true,
    val hasValidSession: Boolean = false
)
