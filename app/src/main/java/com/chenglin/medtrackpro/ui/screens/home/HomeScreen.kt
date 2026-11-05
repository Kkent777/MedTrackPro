package com.chenglin.medtrackpro.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.dp
import com.chenglin.medtrackpro.data.model.Medication
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeScreen(
    onAddMedicationClick: () -> Unit,
    onSymptomsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: HomeViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val todayDate = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
    }

    if (uiState.isLoading) {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Hello, ${uiState.patientName}",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = todayDate,
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = "${uiState.takenCount} of ${uiState.medicationCount} medications taken today",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Button(
            onClick = onAddMedicationClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Medication")
        }

        Button(
            onClick = onSymptomsClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Symptoms")
        }

        if (uiState.medications.isEmpty()) {
            Text(
                text = "No medications scheduled.",
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            uiState.medications.forEach { medication ->
                MedicationCard(
                    medication = medication,
                    isTaken = medication.isTakenToday,
                    onTakenChange = { isTaken ->
                        viewModel.onTakenChange(medication.medicationId, isTaken)
                    }
                )
            }
        }

        TextButton(
            onClick = onLogoutClick,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Logout")
        }
    }
}

@Composable
private fun MedicationCard(
    medication: Medication,
    isTaken: Boolean,
    onTakenChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = medication.medicationName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Dosage: ${medication.dosage}")
                Text("Frequency: ${medication.frequency}")
                Text(
                    text = "Scheduled time: ${medication.scheduledTime.ifBlank { "Not set" }}"
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Taken")
                Switch(
                    checked = isTaken,
                    onCheckedChange = onTakenChange
                )
            }
        }
    }
}
