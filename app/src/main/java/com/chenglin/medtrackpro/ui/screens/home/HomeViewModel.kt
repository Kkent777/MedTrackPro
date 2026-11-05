package com.chenglin.medtrackpro.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chenglin.medtrackpro.data.model.Medication
import com.chenglin.medtrackpro.data.repository.MedicationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val medicationRepository = MedicationRepository.fromContext(application)
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    // A3: Home loads current patient medications from Room
    fun refresh() {
        viewModelScope.launch {
            val homeData = medicationRepository.getHomeData()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    patientName = homeData.patientName,
                    medications = homeData.medications
                )
            }
        }
    }

    // A3: Taken toggle persistence with daily reset
    fun onTakenChange(medicationId: String, isTaken: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                medications = currentState.medications.map { medication ->
                    if (medication.medicationId == medicationId) {
                        medication.copy(isTakenToday = isTaken)
                    } else {
                        medication
                    }
                }
            )
        }

        viewModelScope.launch {
            medicationRepository.updateTakenState(
                medicationId = medicationId,
                isTaken = isTaken
            )
            refresh()
        }
    }

}

data class HomeUiState(
    val isLoading: Boolean = true,
    val patientName: String = "Patient",
    val medications: List<Medication> = emptyList()
)

val HomeUiState.takenCount: Int
    get() = medications.count { medication -> medication.isTakenToday }

val HomeUiState.medicationCount: Int
    get() = medications.size
