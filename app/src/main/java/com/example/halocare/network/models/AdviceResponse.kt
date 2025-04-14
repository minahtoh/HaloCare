package com.example.halocare.network.models


data class AdviceResponse(
    val slip: AdviceSlip
)

data class AdviceSlip(
    val id: Int,
    val advice: String
)


