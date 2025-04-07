package com.example.halocare.ui.models

import androidx.annotation.Keep
import com.example.halocare.R
import java.time.LocalDate

@Keep
data class Professional(
    val id: String = "",
    val name: String = "",
    val specialty: String= "",
    val bio: String= "",
    val picture: String = "",
    val rating: Double = 0.0,
    val location: String = "",
    val consultationPrice: Int = 0,
    val availableDates: List<String> = emptyList(),
    val language: List<String> = emptyList()
) {
    val parsedAvailableDates: List<LocalDate>
        get() = availableDates.mapNotNull {
            try {
                LocalDate.parse(it)
            } catch (e: Exception) {
                null
            }
        }

    fun isAvailable(date: String): Boolean {
        return availableDates.contains(date)
    }

    fun isAvailable(date: LocalDate): Boolean {
        return parsedAvailableDates.contains(date)
    }
}
