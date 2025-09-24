package com.example.halocare.ui.network

import com.example.halocare.ui.network.models.WeatherResponse
import com.example.halocare.ui.network.models.WeatherResponseHourly
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("current.json")
    suspend fun getCurrentWeather(
        @Query("key") apiKey: String = NetworkConstants.API_KEY,
        @Query("q") location: String,): Response<WeatherResponse>

    @GET("forecast.json")
    suspend fun getHourlyWeather(
        @Query("key") apiKey: String = NetworkConstants.API_KEY,
        @Query("q") location: String,
        @Query("days") days: Int = 1
    ) : Response<WeatherResponseHourly>

}