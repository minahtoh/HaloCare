package com.example.halocare.database

import androidx.compose.ui.graphics.Color
import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class Converters {
    private val formatter = DateTimeFormatter.ISO_LOCAL_TIME

    @TypeConverter
    fun fromLocalTime(value: LocalTime?): String? {
        return value?.format(formatter)
    }

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? {
        return value?.let { LocalTime.parse(it, formatter) }
    }
    @TypeConverter
    fun fromLocalDate(date: LocalDate): String {
        return date.toString() // ISO-8601 format: "yyyy-MM-dd"
    }

    @TypeConverter
    fun toLocalDate(dateString: String): LocalDate {
        return LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
    }

    @TypeConverter
    fun fromColor(color: Color): Int = color.value.toInt()

    @TypeConverter
    fun toColor(colorInt: Int): Color = Color(colorInt)

    @TypeConverter
    fun fromLocalDateList(dates: List<LocalDate>): String =
        dates.joinToString(",") { it.toString() }

    @TypeConverter
    fun toLocalDateList(data: String): List<LocalDate> =
        if (data.isBlank()) emptyList()
        else data.split(",").map { LocalDate.parse(it) }
}