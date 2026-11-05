package com.chenglin.medtrackpro.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// A3: MedCoach tip history stored and loaded from Room
@Entity(
    tableName = "medcoach_tips",
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
data class MedCoachTipEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "TipID")
    val id: Long = 0,

    @ColumnInfo(name = "PatientID")
    val patientId: String,

    @ColumnInfo(name = "TipText")
    val tipText: String,

    @ColumnInfo(name = "Timestamp")
    val timestamp: Long
)
