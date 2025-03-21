package com.example.halocare.ui.models

import kotlin.random.Random

data class Appointment(
    val tag : Int = Random.nextInt(),
    val doctorName: String,
    val doctorPicture: Int = 1,
    val occupation : String ="",
    val date: String,
    val time: String,
    val price: Double = 0.00,
    val status: String
)
