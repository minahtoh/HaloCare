package com.example.halocare.ui.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

data class JournalEntryData(
    val date: LocalDate,
    val entriesCount: Int
)

@Entity(tableName = "journals_table")
data class JournalEntry(
    val date: LocalDate,
    val entryText : String,
    val journalType : String,
    @PrimaryKey(autoGenerate = true) val journalId : Int = 0
)
