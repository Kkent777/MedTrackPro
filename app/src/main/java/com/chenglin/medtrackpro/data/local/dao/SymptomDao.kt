package com.chenglin.medtrackpro.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.chenglin.medtrackpro.data.local.entity.SymptomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SymptomDao {
    @Query("SELECT * FROM symptoms WHERE PatientID = :patientId ORDER BY RecordedDateTime DESC")
    fun observeSymptomsForPatient(patientId: String): Flow<List<SymptomEntity>>

    @Query("SELECT * FROM symptoms WHERE PatientID = :patientId ORDER BY RecordedDateTime DESC")
    suspend fun getSymptomsForPatient(patientId: String): List<SymptomEntity>

    @Query(
        """
        SELECT SymptomName FROM symptoms
        GROUP BY SymptomName
        ORDER BY COUNT(*) DESC, SymptomName ASC
        LIMIT 1
        """
    )
    suspend fun getMostCommonSymptomName(): String?

    @Query("SELECT AVG(Severity) FROM symptoms")
    suspend fun getAverageSymptomSeverity(): Double?

    @Query("SELECT * FROM symptoms WHERE SymptomID = :symptomId LIMIT 1")
    suspend fun getSymptomById(symptomId: String): SymptomEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymptom(symptom: SymptomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymptoms(symptoms: List<SymptomEntity>)

    @Update
    suspend fun updateSymptom(symptom: SymptomEntity)

    @Delete
    suspend fun deleteSymptom(symptom: SymptomEntity)
}
