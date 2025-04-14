package com.example.halocare.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.halocare.ui.models.HaloMoodEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoodEntry(entry: HaloMoodEntry)

    @Query("SELECT * FROM mood_entries WHERE timeLogged BETWEEN :startTime AND :endTime ORDER BY timeLogged ASC")
    fun getEntriesForDay(startTime: Long, endTime: Long): Flow<List<HaloMoodEntry>>
}