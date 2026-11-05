package com.chenglin.medtrackpro.ui.screens.addmedication

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

class AddMedicationViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val medicationRepository = MedicationRepository.fromContext(application)
    private val _uiState = MutableStateFlow(AddMedicationUiState())
    val uiState: StateFlow<AddMedicationUiState> = _uiState.asStateFlow()

    fun onMedicationNameChange(value: String) {
        _uiState.update {
            it.copy(medicationName = value, medicationNameError = null, generalError = null)
        }
    }

    fun onDosageChange(value: String) {
        _uiState.update {
            it.copy(dosage = value, dosageError = null, generalError = null)
        }
    }

    fun onFrequencyChange(value: String) {
        _uiState.update { it.copy(frequency = value) }
    }

    fun onMedicationTypeChange(value: String) {
        _uiState.update { it.copy(medicationType = value) }
    }

    fun onScheduledTimeChange(value: String) {
        _uiState.update {
            it.copy(scheduledTime = value, scheduledTimeError = null)
        }
    }

    fun onNotesChange(value: String) {
        _uiState.update { it.copy(notes = value) }
    }

    fun setTimePickerVisible(isVisible: Boolean) {
        _uiState.update { it.copy(showTimePicker = isVisible) }
    }

    fun clearForm() {
        _uiState.value = AddMedicationUiState()
    }

    // A3: Add Medication saves new medication records to Room
    fun saveMedication(onComplete: (Boolean) -> Unit) {
        val state = uiState.value
        val trimmedMedicationName = state.medicationName.trim()
        val trimmedDosage = state.dosage.trim()
        val medicationNameError = if (trimmedMedicationName.isEmpty()) {
            "Medication name is required"
        } else {
            null
        }
        val dosageError = when {
            trimmedDosage.isEmpty() -> "Dosage is required"
            !DOSAGE_PATTERN.matches(trimmedDosage.lowercase()) -> {
                "Dosage must be like 500mg, 5ml, or 1.5g"
            }
            else -> null
        }
        val scheduledTimeError = if (state.scheduledTime.isBlank()) {
            "Time is required"
        } else {
            null
        }

        if (medicationNameError != null || dosageError != null || scheduledTimeError != null) {
            _uiState.update {
                it.copy(
                    medicationNameError = medicationNameError,
                    dosageError = dosageError,
                    scheduledTimeError = scheduledTimeError,
                    generalError = null
                )
            }
            return
        }

        val patientId = medicationRepository.getLoggedInPatientId().orEmpty()
        if (patientId.isBlank()) {
            _uiState.update { it.copy(generalError = "No logged in patient found") }
            onComplete(false)
            return
        }

        viewModelScope.launch {
            medicationRepository.addMedication(
                Medication(
                    medicationId = "MED${System.currentTimeMillis()}",
                    patientId = patientId,
                    medicationName = trimmedMedicationName,
                    dosage = trimmedDosage.lowercase(),
                    frequency = state.frequency,
                    medicationType = state.medicationType,
                    scheduledTime = state.scheduledTime,
                    notes = state.notes.trim()
                )
            )
            onComplete(true)
        }
    }

    private companion object {
        val DOSAGE_PATTERN = Regex("^\\d+(\\.\\d+)?(mg|ml|g)$")
    }
}

data class AddMedicationUiState(
    val medicationName: String = "",
    val dosage: String = "",
    val frequency: String = "Once daily",
    val scheduledTime: String = "",
    val medicationType: String = "Tablet",
    val notes: String = "",
    val showTimePicker: Boolean = false,
    val medicationNameError: String? = null,
    val dosageError: String? = null,
    val scheduledTimeError: String? = null,
    val generalError: String? = null
)
