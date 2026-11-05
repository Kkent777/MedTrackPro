package com.chenglin.medtrackpro.ui.screens.symptoms

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chenglin.medtrackpro.data.model.Symptom
import com.chenglin.medtrackpro.data.repository.SymptomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// A3: Symptoms screen loads and saves patient symptom records through Room
class SymptomsViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val symptomRepository = SymptomRepository.fromContext(application)
    private val _uiState = MutableStateFlow(SymptomsUiState())
    val uiState: StateFlow<SymptomsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val symptoms = symptomRepository.getSymptomsForCurrentPatient()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    symptomHistory = symptoms,
                    severityTrend = symptoms.toSeverityTrendPoints()
                )
            }
        }
    }

    fun saveSymptom(
        symptomName: String,
        severity: Int,
        recordedDateTime: String,
        notes: String,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val saved = symptomRepository.addSymptom(
                symptomName = symptomName,
                severity = severity,
                recordedDateTime = recordedDateTime,
                notes = notes
            )
            refresh()
            onComplete(saved)
        }
    }

}

data class SymptomsUiState(
    val isLoading: Boolean = true,
    val symptomHistory: List<Symptom> = emptyList(),
    val severityTrend: List<SymptomSeverityTrendPoint> = emptyList()
)

data class SymptomSeverityTrendPoint(
    val label: String,
    val severity: Int,
    val recordedDateTime: String
)

// A3: Original feature - Symptom Severity Trend Chart
private fun List<Symptom>.toSeverityTrendPoints(): List<SymptomSeverityTrendPoint> {
    return take(SYMPTOM_TREND_LIMIT)
        .asReversed()
        .mapIndexed { index, symptom ->
            SymptomSeverityTrendPoint(
                label = "#${index + 1}",
                severity = symptom.severity.toIntOrNull() ?: 0,
                recordedDateTime = symptom.recordedDate
            )
        }
}

private const val SYMPTOM_TREND_LIMIT = 10
