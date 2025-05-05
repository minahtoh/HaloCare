package com.example.halocare.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.halocare.ui.models.Medication
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface MedicationsDao {

    @Query("SELECT * FROM medications_table")
    fun getAllMedications(): Flow<List<Medication>>

    @Query("SELECT * FROM medications_table WHERE :date IN (prescribedDays)")
    suspend fun getMedicationsForDate(date: LocalDate): List<Medication>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: Medication)

    @Update
    suspend fun updateMedication(medication: Medication)

    @Delete
    suspend fun deleteMedication(medication: Medication)
}
