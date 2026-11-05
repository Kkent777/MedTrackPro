package com.chenglin.medtrackpro.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medications",
    foreignKeys = [
        ForeignKey(
            entity = PatientEntity::class,
            parentColumns = ["PatientID"],
            childColumns = ["PatientID"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["PatientID"])
    ]
)
data class MedicationEntity(
    @PrimaryKey
    @ColumnInfo(name = "MedicationID")
    val medicationId: String,

    @ColumnInfo(name = "PatientID")
    val patientId: String,

    @ColumnInfo(name = "MedicationName")
    val medicationName: String,

    @ColumnInfo(name = "Dosage")
    val dosage: String,

    @ColumnInfo(name = "Frequency")
    val frequency: String,

    @ColumnInfo(name = "ScheduledTime")
    val scheduledTime: String = "",

    @ColumnInfo(name = "MedicationType")
    val medicationType: String = "",

    @ColumnInfo(name = "StartDate")
    val startDate: String = "",

    @ColumnInfo(name = "Notes")
    val notes: String = "",

    // A3: Taken toggle persistence with daily reset
    @ColumnInfo(name = "TakenToday")
    val takenToday: Boolean = false,

    @ColumnInfo(name = "TakenDate")
    val takenDate: String = ""
)
