package com.example.halocare.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.halocare.ui.models.JournalEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface JournalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournal(journal: JournalEntry)

    @Delete
    suspend fun deleteJournal(journal: JournalEntry)

    @Query("SELECT * FROM journals_table ORDER BY date DESC")
    fun getAllJournals(): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journals_table WHERE date = :selectedDate LIMIT 1")
    suspend fun getJournalByDate(selectedDate: LocalDate): JournalEntry?

    @Query("SELECT * FROM journals_table WHERE journalType = :type ORDER BY date DESC")
    fun getJournalsByType(type: String): Flow<List<JournalEntry>>

    @Query("DELETE FROM journals_table")
    suspend fun clearAll()
}