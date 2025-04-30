package com.example.halocare.database

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate): String {
        return date.toString() // ISO-8601 format: "yyyy-MM-dd"
    }

    @TypeConverter
    fun toLocalDate(dateString: String): LocalDate {
        return LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
    }
}