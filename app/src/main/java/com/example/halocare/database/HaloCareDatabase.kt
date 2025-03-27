package com.example.halocare.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.halocare.ui.models.User


@Database(entities = [User::class], version = 1, exportSchema = false)
abstract class HaloCareDatabase : RoomDatabase() {
    abstract fun userDao() : UserDao
}