package com.example.halocare.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.halocare.ui.models.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE uid = :userId LIMIT 1")
    suspend fun getUser(userId: String): User?

    @Query("DELETE FROM users WHERE uid = :userId")
    suspend fun deleteUser(userId: String)
}