package com.chenglin.medtrackpro.data.model

data class Patient(
    val patientId: String,
    val firstName: String = "",
    val lastName: String = "",
    val dateOfBirth: String = "",
    val gender: String = "",
    val phoneNumber: String = "",
    val password: String
)
