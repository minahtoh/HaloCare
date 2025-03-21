package com.example.halocare.ui.models

import com.example.halocare.R
import java.time.LocalDate


data class Professional(
    val name: String,
    val specialty: String,
    val picture : Int = R.drawable.baseline_person_3_24,
    val price : Double = 20.00,
    val availableDates: List<LocalDate>
) {
    fun isAvailable(date: LocalDate): Boolean {
        return availableDates.contains(date)
    }
}
