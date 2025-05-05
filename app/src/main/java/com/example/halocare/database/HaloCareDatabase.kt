package com.example.halocare.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.halocare.ui.models.ExerciseData
import com.example.halocare.ui.models.HaloMoodEntry
import com.example.halocare.ui.models.JournalEntry
import com.example.halocare.ui.models.Medication
import com.example.halocare.ui.models.SleepData
import com.example.halocare.ui.models.User


@Database(
    entities = [
        User::class,
        HaloMoodEntry::class,
        ExerciseData::class,
        SleepData::class,
        JournalEntry::class,
        Medication::class],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class HaloCareDatabase : RoomDatabase() {
    abstract fun userDao() : UserDao
    abstract fun moodEntryDao(): MoodEntryDao
    abstract fun exerciseDao(): ExerciseTrackerDao
    abstract fun sleepDao(): SleepDao
    abstract fun journalDao(): JournalDao
    abstract fun medicationsDao(): MedicationsDao
}
