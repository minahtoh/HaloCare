package com.example.halocare.ui.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood_entries")
data class HaloMoodEntry(
    @PrimaryKey(autoGenerate = true) val id : Int = 0,
    val iconRes: Int,
    val gist: String,
    val timeLogged: Long
)
