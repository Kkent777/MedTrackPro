package com.chenglin.medtrackpro.ui.screens.symptoms

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.drawscope.Stroke
import com.chenglin.medtrackpro.data.model.Symptom
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

private val symptomCategoryOptions = listOf(
    "Pain",
    "Headache",
    "Nausea",
    "Dizziness",
    "Fatigue",
    "Skin Reaction",
    "Other"
)

private const val NOTES_MAX_LENGTH = 200

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val viewModel: SymptomsViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState(
        initialHour = 9,
        initialMinute = 0,
        is24Hour = false
    )

    var selectedCategory by rememberSaveable { mutableStateOf("") }
    var severity by rememberSaveable { mutableFloatStateOf(5f) }
    var notes by rememberSaveable { mutableStateOf("") }
    var selectedDate by rememberSaveable { mutableStateOf("") }
    var selectedTime by rememberSaveable { mutableStateOf("") }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }

    var categoryError by rememberSaveable { mutableStateOf<String?>(null) }
    var dateError by rememberSaveable { mutableStateOf<String?>(null) }
    var timeError by rememberSaveable { mutableStateOf<String?>(null) }

    fun clearForm() {
        selectedCategory = ""
        severity = 5f
        notes = ""
        selectedDate = ""
        selectedTime = ""
        categoryError = null
        dateError = null
        timeError = null
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Symptoms",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "Log Symptom",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            SimpleDropdownField(
                label = "Symptom Category",
                selectedValue = selectedCategory.ifBlank { "Select category" },
                options = symptomCategoryOptions,
                onValueSelected = {
                    selectedCategory = it
                    categoryError = null
                }
            )

            if (categoryError != null) {
                Text(
                    text = categoryError.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = "Severity: ${severity.toInt()}",
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = severity,
                onValueChange = {
                    severity = it
                },
                valueRange = 1f..10f,
                steps = 8,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = notes,
                onValueChange = {
                    notes = it.take(NOTES_MAX_LENGTH)
                },
                label = {
                    Text("Additional Notes")
                },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "${notes.length}/$NOTES_MAX_LENGTH",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Date",
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedButton(
                onClick = {
                    showDatePicker = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = selectedDate.ifBlank { "Select date" },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (dateError != null) {
                Text(
                    text = dateError.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = "Time",
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedButton(
                onClick = {
                    showTimePicker = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = selectedTime.ifBlank { "Select time" },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (timeError != null) {
                Text(
                    text = timeError.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        categoryError = if (selectedCategory.isBlank()) {
                            "Please select a symptom category"
                        } else {
                            null
                        }

                        dateError = if (selectedDate.isBlank()) {
                            "Date is required"
                        } else {
                            null
                        }

                        timeError = if (selectedTime.isBlank()) {
                            "Time is required"
                        } else {
                            null
                        }

                        if (categoryError != null || dateError != null || timeError != null) {
                            return@Button
                        }

                        val recordedDateTime = "$selectedDate $selectedTime"
                        viewModel.saveSymptom(
                            symptomName = selectedCategory,
                            severity = severity.toInt(),
                            recordedDateTime = recordedDateTime,
                            notes = notes.trim()
                        ) { saved ->
                            if (saved) {
                                clearForm()
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Symptom saved successfully")
                                }
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("No logged in patient found")
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }

                OutlinedButton(
                    onClick = {
                        clearForm()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            SeverityTrendChartCard(entries = uiState.severityTrend)

            Text(
                text = "Symptom History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (uiState.symptomHistory.isEmpty()) {
                Text(
                    text = "No symptoms logged yet.",
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                uiState.symptomHistory.forEach { symptom ->
                    SymptomHistoryCard(symptom = symptom)
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

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDate = datePickerState.selectedDateMillis?.let { millis ->
                            Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        }.orEmpty()
                        dateError = null
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = {
                showTimePicker = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedTime = formatSelectedTime(
                            hour = timePickerState.hour,
                            minute = timePickerState.minute
                        )
                        timeError = null
                        showTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showTimePicker = false
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

// A3: Original feature - Symptom Severity Trend Chart
@Composable
private fun SeverityTrendChartCard(entries: List<SymptomSeverityTrendPoint>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Severity Trend",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (entries.isEmpty()) {
                Text(
                    text = "No symptom history yet. Log symptoms to see your severity trend.",
                    style = MaterialTheme.typography.bodyMedium
                )
                return@Column
            }

            Text(
                text = "Latest ${entries.size} entries, oldest to newest",
                style = MaterialTheme.typography.bodySmall
            )

            val lineColor = MaterialTheme.colorScheme.primary
            val gridColor = MaterialTheme.colorScheme.outlineVariant
            val pointColor = MaterialTheme.colorScheme.secondary

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                val leftPadding = 18.dp.toPx()
                val rightPadding = 8.dp.toPx()
                val topPadding = 12.dp.toPx()
                val bottomPadding = 20.dp.toPx()
                val chartWidth = size.width - leftPadding - rightPadding
                val chartHeight = size.height - topPadding - bottomPadding
                val stepX = if (entries.size == 1) {
                    0f
                } else {
                    chartWidth / (entries.size - 1)
                }

                listOf(0f, 0.5f, 1f).forEach { fraction ->
                    val y = topPadding + chartHeight * fraction
                    drawLine(
                        color = gridColor,
                        start = Offset(leftPadding, y),
                        end = Offset(leftPadding + chartWidth, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                val points = entries.mapIndexed { index, entry ->
                    val x = leftPadding + stepX * index
                    val normalizedSeverity = (entry.severity.coerceIn(1, 10) - 1) / 9f
                    val y = topPadding + chartHeight * (1f - normalizedSeverity)
                    Offset(x, y)
                }

                if (points.size > 1) {
                    val path = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        points.drop(1).forEach { point ->
                            lineTo(point.x, point.y)
                        }
                    }
                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }

                points.forEach { point ->
                    drawCircle(
                        color = pointColor,
                        radius = 5.dp.toPx(),
                        center = point
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                entries.forEach { entry ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = entry.label,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = entry.severity.toString(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Text(
                text = "Entry labels show order; numbers show severity from 1 to 10.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SymptomHistoryCard(symptom: Symptom) {
    val severityInfo = symptom.severity.toSeverityInfo()

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = symptom.symptomName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text("Date/Time: ${symptom.recordedDate}")
            Text(
                text = "${severityInfo.label} (${symptom.severity})",
                color = severityInfo.color,
                fontWeight = FontWeight.SemiBold
            )
            if (symptom.notes.isNotBlank()) {
                Text("Notes: ${symptom.notes}")
            }
        }
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

private data class SeverityInfo(
    val label: String,
    val color: Color
)

private fun String.toSeverityInfo(): SeverityInfo {
    val severityValue = toIntOrNull() ?: 0

    return when (severityValue) {
        in 1..3 -> SeverityInfo(
            label = "Mild",
            color = Color(0xFF2E7D32)
        )
        in 4..6 -> SeverityInfo(
            label = "Moderate",
            color = Color(0xFFEF6C00)
        )
        in 7..10 -> SeverityInfo(
            label = "Severe",
            color = Color(0xFFC62828)
        )
        else -> SeverityInfo(
            label = "Unknown",
            color = Color(0xFF616161)
        )
    }
}
