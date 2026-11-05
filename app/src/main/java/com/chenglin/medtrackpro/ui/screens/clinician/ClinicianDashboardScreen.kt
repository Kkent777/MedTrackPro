package com.chenglin.medtrackpro.ui.screens.clinician

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chenglin.medtrackpro.data.repository.ClinicianStats
import java.util.Locale

// A3: Clinician dashboard aggregate statistics and Gemini insights
@Composable
fun ClinicianDashboardScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: ClinicianDashboardViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Clinician Dashboard",
                style = MaterialTheme.typography.headlineMedium
            )
            TextButton(onClick = onBackClick) {
                Text("Back")
            }
        }

        if (uiState.isLoadingStats) {
            CircularProgressIndicator()
        } else {
            uiState.stats?.let { stats ->
                StatsGrid(stats = stats)
                Button(
                    onClick = viewModel::findPatterns,
                    enabled = !uiState.isFindingPatterns,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (uiState.isFindingPatterns) "Finding Patterns" else "Find Patterns")
                }
            }
        }

        uiState.statsErrorMessage?.let { message ->
            ErrorText(message = message)
        }

        if (uiState.isFindingPatterns) {
            CircularProgressIndicator()
        }

        uiState.patternErrorMessage?.let { message ->
            ErrorText(message = message)
        }

        if (uiState.insights.isNotEmpty()) {
            InsightsCard(insights = uiState.insights)
        }
    }
}

@Composable
private fun StatsGrid(stats: ClinicianStats) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            label = "Total Patients",
            value = stats.totalPatients.toString()
        )
        StatCard(
            label = "Average Medications per Patient",
            value = stats.averageMedicationsPerPatient.formatStat()
        )
        StatCard(
            label = "Most Common Symptom Category",
            value = stats.mostCommonSymptomCategory
        )
        StatCard(
            label = "Average Symptom Severity",
            value = "${stats.averageSymptomSeverity.formatStat()} / 10"
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}

@Composable
private fun InsightsCard(insights: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Gemini Patterns",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            insights.forEachIndexed { index, insight ->
                Text(
                    text = "${index + 1}. $insight",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ErrorText(message: String) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium
    )
}

private fun Double.formatStat(): String {
    return String.format(Locale.US, "%.1f", this)
}
