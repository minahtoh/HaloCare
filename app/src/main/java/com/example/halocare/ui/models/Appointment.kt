package com.example.halocare.ui.models

import com.google.errorprone.annotations.Keep
import kotlin.random.Random

@Keep
data class Appointment(
    val professionalId: String = "",
    val professionalName: String = "",
    val profilePicture: String = "",
    val occupation : String = "",
    val date: String = "",
    val time: String = "",
    val note: String? = null,
    val price: Double = 0.00,
    val bookedAt : Long = System.currentTimeMillis(),
    val status: String = ""
)
