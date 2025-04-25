package com.example.halocare.ui.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sleep_table")
data class SleepData(
    val dayLogged : String,
    val sleepLength : Float,
    val sleepQuality : Int,
    @PrimaryKey(autoGenerate = true) val sleepId : Int = 0
)
