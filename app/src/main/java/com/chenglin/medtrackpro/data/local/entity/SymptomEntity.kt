package com.chenglin.medtrackpro.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "symptoms",
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
data class SymptomEntity(
    @PrimaryKey
    @ColumnInfo(name = "SymptomID")
    val symptomId: String,

    @ColumnInfo(name = "PatientID")
    val patientId: String,

    @ColumnInfo(name = "SymptomName")
    val symptomName: String,

    @ColumnInfo(name = "Severity")
    val severity: Int,

    @ColumnInfo(name = "RecordedDateTime")
    val recordedDateTime: String,

    @ColumnInfo(name = "Notes")
    val notes: String = ""
)
