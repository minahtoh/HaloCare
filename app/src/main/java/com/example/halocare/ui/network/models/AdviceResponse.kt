package com.example.halocare.ui.network.models


data class AdviceResponse(
    val slip: AdviceSlip
)

data class AdviceSlip(
    val id: Int,
    val advice: String
)


