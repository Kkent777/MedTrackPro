package com.chenglin.medtrackpro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.chenglin.medtrackpro.data.local.entity.PatientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientDao {
    @Query("SELECT * FROM patients ORDER BY PatientID")
    fun observePatients(): Flow<List<PatientEntity>>

    @Query("SELECT * FROM patients WHERE PatientID = :patientId LIMIT 1")
    fun observePatient(patientId: String): Flow<PatientEntity?>

    @Query("SELECT * FROM patients WHERE PatientID = :patientId LIMIT 1")
    suspend fun getPatientById(patientId: String): PatientEntity?

    @Query("SELECT * FROM patients WHERE PatientID = :patientId AND PhoneNumber = :phoneNumber LIMIT 1")
    suspend fun getPatientByIdAndPhone(patientId: String, phoneNumber: String): PatientEntity?

    @Query("SELECT * FROM patients WHERE PatientID = :patientId AND Password = :password LIMIT 1")
    suspend fun getPatientByIdAndPassword(patientId: String, password: String): PatientEntity?

    @Query("SELECT * FROM patients WHERE PhoneNumber = :phoneNumber LIMIT 1")
    suspend fun getPatientByPhoneNumber(phoneNumber: String): PatientEntity?

    @Query("SELECT PatientID FROM patients")
    suspend fun getAllPatientIds(): List<String>

    @Query("SELECT COUNT(*) FROM patients")
    suspend fun getPatientCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: PatientEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatients(patients: List<PatientEntity>)

    @Query("UPDATE patients SET Password = :password WHERE PatientID = :patientId")
    suspend fun updatePassword(patientId: String, password: String)

    @Update
    suspend fun updatePatient(patient: PatientEntity)
}
