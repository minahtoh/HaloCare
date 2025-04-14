package com.example.halocare.network

import com.example.halocare.network.models.AdviceResponse
import retrofit2.Response
import retrofit2.http.GET

interface AdviceApi {
    @GET("advice")
    suspend fun getRandomAdvice(): Response<AdviceResponse>
}