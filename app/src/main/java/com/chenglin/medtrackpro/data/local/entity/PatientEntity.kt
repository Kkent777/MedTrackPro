package com.chenglin.medtrackpro.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patients")
data class PatientEntity(
    @PrimaryKey
    @ColumnInfo(name = "PatientID")
    val patientId: String,

    @ColumnInfo(name = "FirstName")
    val firstName: String = "",

    @ColumnInfo(name = "LastName")
    val lastName: String = "",

    @ColumnInfo(name = "FullName")
    val fullName: String = "",

    @ColumnInfo(name = "DateOfBirth")
    val dateOfBirth: String = "",

    @ColumnInfo(name = "Gender")
    val gender: String = "",

    @ColumnInfo(name = "PhoneNumber")
    val phoneNumber: String = "",

    @ColumnInfo(name = "Password")
    val password: String
)
