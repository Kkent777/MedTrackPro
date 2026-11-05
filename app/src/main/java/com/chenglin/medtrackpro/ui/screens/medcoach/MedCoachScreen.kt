package com.chenglin.medtrackpro.ui.screens.medcoach

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chenglin.medtrackpro.data.repository.DrugInfo
import com.chenglin.medtrackpro.data.repository.MedCoachTip

@Composable
fun MedCoachScreen(
    modifier: Modifier = Modifier
) {
    val viewModel: MedCoachViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "MedCoach",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Search OpenFDA drug label information for a medication.",
            style = MaterialTheme.typography.bodyLarge
        )

        if (uiState.medicationNames.isNotEmpty()) {
            MedicationDropdown(
                medicationNames = uiState.medicationNames,
                onMedicationSelected = viewModel::onMedicationNameChange
            )
        }

        OutlinedTextField(
            value = uiState.medicationName,
            onValueChange = viewModel::onMedicationNameChange,
            label = {
                Text("Medication Name")
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = viewModel::searchDrugInfo,
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (uiState.isLoading) "Searching" else "Search Drug Info")
        }

        if (uiState.isLoading) {
            CircularProgressIndicator()
        }

        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        uiState.drugInfo?.let { drugInfo ->
            DrugInfoCard(drugInfo = drugInfo)
        }

        GenAiTipsSection(
            generatedTipText = uiState.generatedTipText,
            isGeneratingTip = uiState.isGeneratingTip,
            tipErrorMessage = uiState.tipErrorMessage,
            onGenerateTipClick = viewModel::generateMedicationTip,
            onShowAllTipsClick = {
                viewModel.loadTipHistory()
                viewModel.setHistoryDialogVisible(true)
            }
        )
    }

    if (uiState.isHistoryDialogVisible) {
        TipsHistoryDialog(
            tips = uiState.tipHistory,
            onDismiss = {
                viewModel.setHistoryDialogVisible(false)
            }
        )
    }
}

@Composable
private fun MedicationDropdown(
    medicationNames: List<String>,
    onMedicationSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Your Medications",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
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
                Text("Select a medication")
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                }
            ) {
                medicationNames.forEach { medicationName ->
                    DropdownMenuItem(
                        text = {
                            Text(medicationName)
                        },
                        onClick = {
                            onMedicationSelected(medicationName)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DrugInfoCard(drugInfo: DrugInfo) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = drugInfo.displayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            DrugInfoValue(label = "Generic Name", value = drugInfo.genericName)
            DrugInfoValue(label = "Active Ingredient", value = drugInfo.activeIngredient)
            DrugInfoValue(label = "Purpose", value = drugInfo.purpose)
            DrugInfoValue(label = "Warnings", value = drugInfo.warnings)
            DrugInfoValue(
                label = "Dosage and Administration",
                value = drugInfo.dosageAndAdministration
            )
            DrugInfoValue(label = "Indications and Usage", value = drugInfo.indicationsAndUsage)
        }
    }
}

@Composable
private fun DrugInfoValue(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun GenAiTipsSection(
    generatedTipText: String,
    isGeneratingTip: Boolean,
    tipErrorMessage: String?,
    onGenerateTipClick: () -> Unit,
    onShowAllTipsClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "GenAI Medication Tips",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Generate a personalised medication tip using your current medications and recent symptom history.",
            style = MaterialTheme.typography.bodyMedium
        )

        Button(
            onClick = onGenerateTipClick,
            enabled = !isGeneratingTip,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isGeneratingTip) "Generating" else "Generate Medication Tip")
        }

        OutlinedButton(
            onClick = onShowAllTipsClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Show All Tips")
        }

        if (isGeneratingTip) {
            CircularProgressIndicator()
        }

        if (tipErrorMessage != null) {
            Text(
                text = tipErrorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (generatedTipText.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Latest Tip",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = generatedTipText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun TipsHistoryDialog(
    tips: List<MedCoachTip>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        title = {
            Text("Previous Tips")
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (tips.isEmpty()) {
                    Text("No generated tips yet.")
                } else {
                    tips.forEach { tip ->
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = tip.formattedTimestamp,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = tip.tipText,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    )
}
