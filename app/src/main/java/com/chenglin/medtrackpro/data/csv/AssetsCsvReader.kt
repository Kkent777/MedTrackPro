package com.chenglin.medtrackpro.data.csv

import android.content.Context
import com.chenglin.medtrackpro.data.model.Medication
import com.chenglin.medtrackpro.data.model.Patient
import com.chenglin.medtrackpro.data.model.Symptom

class AssetsCsvReader {

    fun readPatients(context: Context, fileName: String = "patients.csv"): List<Patient> {
        val rows = readCsvRows(context, fileName)

        return rows.mapNotNull { row ->
            val patientId = row.firstValue("patient_id", "patientid", "id")
            val password = row.firstValue("password")
            val combinedName = row.firstValue("name", "full_name", "fullname")

            if (patientId.isBlank() || password.isBlank()) {
                return@mapNotNull null
            }

            Patient(
                patientId = patientId,
                firstName = row.firstValue("first_name", "firstname", "given_name")
                    .ifBlank { combinedName },
                lastName = row.firstValue("last_name", "lastname", "family_name"),
                dateOfBirth = row.firstValue("date_of_birth", "dob", "birth_date"),
                gender = row.firstValue("gender", "sex"),
                phoneNumber = row.firstValue("phone_number", "phonenumber", "phone", "mobile"),
                password = password
            )
        }
    }

    fun readMedications(context: Context, fileName: String = "medications.csv"): List<Medication> {
        val rows = readCsvRows(context, fileName)

        return rows.mapIndexedNotNull { index, row ->
            val patientId = row.firstValue("patient_id", "patientid")
            val medicationName = row.firstValue(
                "medication_name",
                "medicationname",
                "name",
                "medication"
            )

            if (patientId.isBlank() || medicationName.isBlank()) {
                return@mapIndexedNotNull null
            }

            Medication(
                medicationId = row.firstValue("medication_id", "medicationid", "id")
                    .ifBlank { "medication_${index + 1}" },
                patientId = patientId,
                medicationName = medicationName,
                dosage = row.firstValue("dosage", "dose"),
                frequency = row.firstValue("frequency", "schedule", "times_per_day"),
                medicationType = row.firstValue("medication_type", "medicationtype", "type"),
                scheduledTime = row.firstValue("scheduled_time", "time", "scheduledtime"),
                startDate = row.firstValue("start_date", "date_started", "date"),
                notes = row.firstValue("notes", "note", "instructions")
            )
        }
    }

    fun readSymptoms(context: Context, fileName: String = "symptoms.csv"): List<Symptom> {
        val rows = readCsvRows(context, fileName)

        return rows.mapIndexedNotNull { index, row ->
            val patientId = row.firstValue("patient_id", "patientid")
            val symptomName = row.firstValue("symptom_name", "category", "symptom", "name")

            if (patientId.isBlank() || symptomName.isBlank()) {
                return@mapIndexedNotNull null
            }

            Symptom(
                symptomId = row.firstValue("symptom_id", "symptomid", "id")
                    .ifBlank { "symptom_${index + 1}" },
                patientId = patientId,
                symptomName = symptomName,
                severity = row.firstValue("severity", "level"),
                recordedDate = row.firstValue("recorded_date", "datetime", "date", "logged_date"),
                notes = row.firstValue("notes", "note", "description")
            )
        }
    }

    // Reads the asset file, skips the header row, and returns each row as a header/value map.
    private fun readCsvRows(context: Context, fileName: String): List<Map<String, String>> {
        val lines = try {
            context.assets.open(fileName).bufferedReader().use { reader ->
                reader.readLines().filter { it.isNotBlank() }
            }
        } catch (_: Exception) {
            return emptyList()
        }

        if (lines.size <= 1) {
            return emptyList()
        }

        val headers = parseCsvLine(lines.first()).map { header ->
            header.trim().lowercase()
        }

        return lines.drop(1).mapNotNull { line ->
            val values = parseCsvLine(line)

            if (values.all { it.isBlank() }) {
                return@mapNotNull null
            }

            headers.mapIndexed { index, header ->
                header to values.getOrElse(index) { "" }.trim()
            }.toMap()
        }
    }

    // Simple CSV parser that supports quoted values and escaped quotes.
    private fun parseCsvLine(line: String): List<String> {
        val values = mutableListOf<String>()
        val currentValue = StringBuilder()
        var insideQuotes = false
        var index = 0

        while (index < line.length) {
            val character = line[index]

            when {
                character == '"' && insideQuotes && index + 1 < line.length && line[index + 1] == '"' -> {
                    currentValue.append('"')
                    index++
                }

                character == '"' -> {
                    insideQuotes = !insideQuotes
                }

                character == ',' && !insideQuotes -> {
                    values.add(currentValue.toString().trim())
                    currentValue.clear()
                }

                else -> {
                    currentValue.append(character)
                }
            }

            index++
        }

        values.add(currentValue.toString().trim())
        return values
    }

    private fun Map<String, String>.firstValue(vararg possibleKeys: String): String {
        return possibleKeys.firstNotNullOfOrNull { key ->
            this[key]
        }.orEmpty()
    }
}
