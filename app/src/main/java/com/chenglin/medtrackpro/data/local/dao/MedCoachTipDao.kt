package com.chenglin.medtrackpro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chenglin.medtrackpro.data.local.entity.MedCoachTipEntity

// A3: MedCoach tip history stored and loaded from Room
@Dao
interface MedCoachTipDao {
    @Query("SELECT * FROM medcoach_tips WHERE PatientID = :patientId ORDER BY Timestamp DESC")
    suspend fun getTipsForPatient(patientId: String): List<MedCoachTipEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTip(tip: MedCoachTipEntity)
}
