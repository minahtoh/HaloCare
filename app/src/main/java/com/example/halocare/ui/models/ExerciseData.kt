package com.example.halocare.ui.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises_table")
data class ExerciseData(
    @PrimaryKey(autoGenerate = true) val exerciseId: Int = 0,
    val exerciseName: String,
    val timeElapsed : Float,
    val exerciseDate: String
)
