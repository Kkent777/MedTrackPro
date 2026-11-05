package com.chenglin.medtrackpro.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.chenglin.medtrackpro.data.local.entity.MedicationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications WHERE PatientID = :patientId ORDER BY ScheduledTime, MedicationName")
    fun observeMedicationsForPatient(patientId: String): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE PatientID = :patientId ORDER BY ScheduledTime, MedicationName")
    suspend fun getMedicationsForPatient(patientId: String): List<MedicationEntity>

    @Query(
        """
        SELECT AVG(medication_count) FROM (
            SELECT COUNT(medications.MedicationID) AS medication_count
            FROM patients
            LEFT JOIN medications ON patients.PatientID = medications.PatientID
            GROUP BY patients.PatientID
        )
        """
    )
    suspend fun getAverageMedicationCountPerPatient(): Double?

    @Query("SELECT * FROM medications WHERE MedicationID = :medicationId LIMIT 1")
    suspend fun getMedicationById(medicationId: String): MedicationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: MedicationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedications(medications: List<MedicationEntity>)

    @Update
    suspend fun updateMedication(medication: MedicationEntity)

    @Query(
        """
        UPDATE medications
        SET TakenToday = :isTaken,
            TakenDate = :takenDate
        WHERE MedicationID = :medicationId
            AND PatientID = :patientId
        """
    )
    suspend fun updateTakenState(
        medicationId: String,
        patientId: String,
        isTaken: Boolean,
        takenDate: String
    )

    @Delete
    suspend fun deleteMedication(medication: MedicationEntity)
}
