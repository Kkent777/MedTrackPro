package com.chenglin.medtrackpro.ui.screens.clinician

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chenglin.medtrackpro.data.repository.ClinicianDashboardRepository
import com.chenglin.medtrackpro.data.repository.ClinicianPatternResult
import com.chenglin.medtrackpro.data.repository.ClinicianStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// A3: Clinician dashboard aggregate statistics and Gemini insights
class ClinicianDashboardViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val repository = ClinicianDashboardRepository.fromContext(application)
    private val _uiState = MutableStateFlow(ClinicianDashboardUiState())
    val uiState: StateFlow<ClinicianDashboardUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    fun loadStatistics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingStats = true, statsErrorMessage = null) }
            val stats = repository.loadStatistics()
            _uiState.update {
                it.copy(
                    isLoadingStats = false,
                    stats = stats
                )
            }
        }
    }

    fun findPatterns() {
        val stats = uiState.value.stats ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isFindingPatterns = true,
                    patternErrorMessage = null,
                    insights = emptyList()
                )
            }

            when (val result = repository.findPatterns(stats)) {
                is ClinicianPatternResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isFindingPatterns = false,
                            insights = result.insights
                        )
                    }
                }

                ClinicianPatternResult.MissingApiKey -> {
                    _uiState.update {
                        it.copy(
                            isFindingPatterns = false,
                            patternErrorMessage = "Gemini API key is missing. Add gemini.api.key to local.properties."
                        )
                    }
                }

                ClinicianPatternResult.EmptyResponse -> {
                    _uiState.update {
                        it.copy(
                            isFindingPatterns = false,
                            patternErrorMessage = "Gemini did not return any patterns. Please try again."
                        )
                    }
                }

                ClinicianPatternResult.NetworkError -> {
                    _uiState.update {
                        it.copy(
                            isFindingPatterns = false,
                            patternErrorMessage = "Unable to reach Gemini. Check your connection and try again."
                        )
                    }
                }

                is ClinicianPatternResult.ApiError -> {
                    _uiState.update {
                        it.copy(
                            isFindingPatterns = false,
                            patternErrorMessage = result.message
                        )
                    }
                }
            }
        }
    }
}

data class ClinicianDashboardUiState(
    val isLoadingStats: Boolean = true,
    val stats: ClinicianStats? = null,
    val statsErrorMessage: String? = null,
    val isFindingPatterns: Boolean = false,
    val insights: List<String> = emptyList(),
    val patternErrorMessage: String? = null
)
