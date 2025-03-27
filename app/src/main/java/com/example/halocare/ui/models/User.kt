package com.example.halocare.ui.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val uid: String = "",
    val email: String = "",
    val name: String = "",
    val nickname: String? = null,
    val profilePicture: String? = null,
    val profession: String? = null,
    val dateCreated: Long = System.currentTimeMillis(),
    val dateOfBirth: String = ""
)
