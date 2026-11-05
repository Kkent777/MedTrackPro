package com.chenglin.medtrackpro.data.model

data class Symptom(
    val symptomId: String,
    val patientId: String,
    val symptomName: String,
    val severity: String,
    val recordedDate: String,
    val notes: String = ""
)
