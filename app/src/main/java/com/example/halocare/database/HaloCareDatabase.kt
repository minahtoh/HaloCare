package com.example.halocare.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.halocare.ui.models.ExerciseData
import com.example.halocare.ui.models.HaloMoodEntry
import com.example.halocare.ui.models.SleepData
import com.example.halocare.ui.models.User


@Database(
    entities = [User::class, HaloMoodEntry::class, ExerciseData::class, SleepData::class],
    version = 4,
    exportSchema = false
)
abstract class HaloCareDatabase : RoomDatabase() {
    abstract fun userDao() : UserDao
    abstract fun moodEntryDao(): MoodEntryDao
    abstract fun exerciseDao(): ExerciseTrackerDao
    abstract fun sleepDao(): SleepDao
}