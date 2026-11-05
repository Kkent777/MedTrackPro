package com.chenglin.medtrackpro.ui.screens.medcoach

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chenglin.medtrackpro.data.repository.DrugInfo
import com.chenglin.medtrackpro.data.repository.DrugInfoLookupResult
import com.chenglin.medtrackpro.data.repository.GenerateTipResult
import com.chenglin.medtrackpro.data.repository.MedCoachRepository
import com.chenglin.medtrackpro.data.repository.MedCoachTip
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MedCoachViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val medCoachRepository = MedCoachRepository.fromContext(application)
    private val _uiState = MutableStateFlow(MedCoachUiState())
    val uiState: StateFlow<MedCoachUiState> = _uiState.asStateFlow()

    init {
        loadMedicationNames()
        loadTipHistory()
    }

    fun loadMedicationNames() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    medicationNames = medCoachRepository.getCurrentPatientMedicationNames()
                )
            }
        }
    }

    fun onMedicationNameChange(value: String) {
        _uiState.update {
            it.copy(
                medicationName = value,
                errorMessage = null,
                drugInfo = null
            )
        }
    }

    fun searchDrugInfo() {
        val medicationName = uiState.value.medicationName.trim()

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    drugInfo = null
                )
            }

            when (val result = medCoachRepository.lookupDrugInfo(medicationName)) {
                is DrugInfoLookupResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            drugInfo = result.drugInfo
                        )
                    }
                }

                DrugInfoLookupResult.EmptyInput -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Enter or select a medication name first."
                        )
                    }
                }

                DrugInfoLookupResult.NotFound -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "No OpenFDA drug label was found for this medication."
                        )
                    }
                }

                DrugInfoLookupResult.NetworkError -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Unable to reach OpenFDA. Check your internet connection and try again."
                        )
                    }
                }
            }
        }
    }

    // A3: Gemini medication tip generation using patient-specific context
    fun generateMedicationTip() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isGeneratingTip = true,
                    tipErrorMessage = null
                )
            }

            when (
                val result = medCoachRepository.generateMedicationTip(
                    apiKey = MedCoachRepository.configuredGeminiApiKey()
                )
            ) {
                is GenerateTipResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isGeneratingTip = false,
                            generatedTipText = result.tipText
                        )
                    }
                    loadTipHistory()
                }

                GenerateTipResult.MissingApiKey -> {
                    _uiState.update {
                        it.copy(
                            isGeneratingTip = false,
                            tipErrorMessage = "Gemini API key is missing. Add GEMINI_API_KEY or gemini.api.key to local.properties."
                        )
                    }
                }

                GenerateTipResult.NoLoggedInPatient -> {
                    _uiState.update {
                        it.copy(
                            isGeneratingTip = false,
                            tipErrorMessage = "No logged-in patient was found."
                        )
                    }
                }

                GenerateTipResult.EmptyResponse -> {
                    _uiState.update {
                        it.copy(
                            isGeneratingTip = false,
                            tipErrorMessage = "Gemini did not return a medication tip. Please try again."
                        )
                    }
                }

                GenerateTipResult.NetworkError -> {
                    _uiState.update {
                        it.copy(
                            isGeneratingTip = false,
                            tipErrorMessage = "Unable to generate a tip right now. Check your connection and try again."
                        )
                    }
                }

                is GenerateTipResult.ApiError -> {
                    _uiState.update {
                        it.copy(
                            isGeneratingTip = false,
                            tipErrorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun loadTipHistory() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(tipHistory = medCoachRepository.getCurrentPatientTips())
            }
        }
    }

    fun setHistoryDialogVisible(isVisible: Boolean) {
        _uiState.update { it.copy(isHistoryDialogVisible = isVisible) }
    }
}

data class MedCoachUiState(
    val medicationName: String = "",
    val medicationNames: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val drugInfo: DrugInfo? = null,
    val errorMessage: String? = null,
    val generatedTipText: String = "",
    val isGeneratingTip: Boolean = false,
    val tipErrorMessage: String? = null,
    val tipHistory: List<MedCoachTip> = emptyList(),
    val isHistoryDialogVisible: Boolean = false
)
