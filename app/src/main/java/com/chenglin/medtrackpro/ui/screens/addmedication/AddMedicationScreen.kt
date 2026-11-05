package com.chenglin.medtrackpro.ui.screens.addmedication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val frequencyOptions = listOf(
    "Once daily",
    "Twice daily",
    "Three times daily",
    "As needed"
)

private val medicationTypeOptions = listOf(
    "Tablet",
    "Capsule",
    "Liquid",
    "Injection",
    "Topical",
    "Other"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: AddMedicationViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val timePickerState = rememberTimePickerState(
        initialHour = 8,
        initialMinute = 0,
        is24Hour = false
    )

    Scaffold(
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Add Medication",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Enter the medication details below.",
                style = MaterialTheme.typography.bodyMedium
            )

            OutlinedTextField(
                value = uiState.medicationName,
                onValueChange = viewModel::onMedicationNameChange,
                label = {
                    Text("Medication Name")
                },
                singleLine = true,
                isError = uiState.medicationNameError != null,
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState.medicationNameError != null) {
                Text(
                    text = uiState.medicationNameError.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlinedTextField(
                value = uiState.dosage,
                onValueChange = viewModel::onDosageChange,
                label = {
                    Text("Dosage")
                },
                placeholder = {
                    Text("e.g. 500mg")
                },
                singleLine = true,
                isError = uiState.dosageError != null,
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState.dosageError != null) {
                Text(
                    text = uiState.dosageError.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            SimpleDropdownField(
                label = "Frequency",
                selectedValue = uiState.frequency,
                options = frequencyOptions,
                onValueSelected = viewModel::onFrequencyChange
            )

            Text(
                text = "Time",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            OutlinedButton(
                onClick = {
                    viewModel.setTimePickerVisible(true)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = uiState.scheduledTime.ifBlank { "Select time" },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (uiState.scheduledTimeError != null) {
                Text(
                    text = uiState.scheduledTimeError.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            SimpleDropdownField(
                label = "Medication Type",
                selectedValue = uiState.medicationType,
                options = medicationTypeOptions,
                onValueSelected = viewModel::onMedicationTypeChange
            )

            OutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::onNotesChange,
                label = {
                    Text("Notes")
                },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState.generalError != null) {
                Text(
                    text = uiState.generalError.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.saveMedication { saved ->
                            if (saved) {
                                onBackClick()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }

                OutlinedButton(
                    onClick = {
                        viewModel.clearForm()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }
            }

            TextButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Back")
            }
        }
    }

    if (uiState.showTimePicker) {
        AlertDialog(
            onDismissRequest = {
                viewModel.setTimePickerVisible(false)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onScheduledTimeChange(
                            formatSelectedTime(
                                hour = timePickerState.hour,
                                minute = timePickerState.minute
                            )
                        )
                        viewModel.setTimePickerVisible(false)
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.setTimePickerVisible(false)
                    }
                ) {
                    Text("Cancel")
                }
            },
            title = {
                Text("Select Time")
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}

@Composable
private fun SimpleDropdownField(
    label: String,
    selectedValue: String,
    options: List<String>,
    onValueSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = {
                    expanded = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = selectedValue,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(option)
                        },
                        onClick = {
                            onValueSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

private fun formatSelectedTime(hour: Int, minute: Int): String {
    return LocalTime.of(hour, minute)
        .format(DateTimeFormatter.ofPattern("h:mm a"))
}
