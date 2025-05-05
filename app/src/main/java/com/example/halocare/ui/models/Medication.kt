package com.example.halocare.ui.models

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity("medications_table")
data class Medication(
    @PrimaryKey(autoGenerate = true) val medicationId : Int = 0,
    val name: String,
    val dosage: Int,
    val frequency: Int,
    val dosesUsedToday : Int,
    val prescribedDays : List<LocalDate>,
    val isReminderOn: Boolean,
    val color: Int = Color.Red.toArgb(),
    val firstDoseTime: LocalTime = LocalTime.now()
)