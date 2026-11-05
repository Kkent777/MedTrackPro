package com.chenglin.medtrackpro.data.model

data class Medication(
    val medicationId: String,
    val patientId: String,
    val medicationName: String,
    val dosage: String,
    val frequency: String,
    val medicationType: String = "",
    val scheduledTime: String = "",
    val startDate: String = "",
    val notes: String = "",
    val isTakenToday: Boolean = false
)
