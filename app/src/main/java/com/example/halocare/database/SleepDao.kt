package com.example.halocare.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.halocare.ui.models.SleepData
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepData(sleepData: SleepData)

    @Query("SELECT * FROM sleep_table ORDER BY sleepId DESC")
    fun getAllSleepData(): Flow<List<SleepData>>

    @Query("DELETE FROM sleep_table")
    suspend fun clearAllSleepData()
}
