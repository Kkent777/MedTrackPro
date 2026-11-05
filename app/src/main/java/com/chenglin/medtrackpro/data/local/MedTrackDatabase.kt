package com.chenglin.medtrackpro.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.chenglin.medtrackpro.data.local.dao.MedCoachTipDao
import com.chenglin.medtrackpro.data.local.dao.MedicationDao
import com.chenglin.medtrackpro.data.local.dao.PatientDao
import com.chenglin.medtrackpro.data.local.dao.SymptomDao
import com.chenglin.medtrackpro.data.local.entity.MedCoachTipEntity
import com.chenglin.medtrackpro.data.local.entity.MedicationEntity
import com.chenglin.medtrackpro.data.local.entity.PatientEntity
import com.chenglin.medtrackpro.data.local.entity.SymptomEntity

// A3: Room database setup for patients, medications, symptoms, and MedCoach tips
@Database(
    entities = [
        PatientEntity::class,
        MedicationEntity::class,
        SymptomEntity::class,
        MedCoachTipEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class MedTrackDatabase : RoomDatabase() {
    abstract fun patientDao(): PatientDao
    abstract fun medicationDao(): MedicationDao
    abstract fun symptomDao(): SymptomDao
    abstract fun medCoachTipDao(): MedCoachTipDao

    companion object {
        @Volatile
        private var instance: MedTrackDatabase? = null

        fun getDatabase(context: Context): MedTrackDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    MedTrackDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { database ->
                    instance = database
                }
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `medcoach_tips` (
                        `TipID` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `PatientID` TEXT NOT NULL,
                        `TipText` TEXT NOT NULL,
                        `Timestamp` INTEGER NOT NULL,
                        FOREIGN KEY(`PatientID`) REFERENCES `patients`(`PatientID`)
                            ON UPDATE CASCADE ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                connection.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_medcoach_tips_PatientID` ON `medcoach_tips` (`PatientID`)"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    "ALTER TABLE `medications` ADD COLUMN `TakenToday` INTEGER NOT NULL DEFAULT 0"
                )
                connection.execSQL(
                    "ALTER TABLE `medications` ADD COLUMN `TakenDate` TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        private const val DATABASE_NAME = "medtrack_database"
    }
}
