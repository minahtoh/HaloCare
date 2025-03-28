package com.example.halocare.network

import com.example.halocare.network.models.WeatherResponse
import com.example.halocare.network.models.WeatherResponseHourly
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class NetworkRepository @Inject constructor(
    @Named("weatherApi") private val weatherCaller: Retrofit
) {
    private val weatherApi = weatherCaller.create(WeatherApi::class.java)

    suspend fun getTodayWeather(location:String): Result<WeatherResponse>{
        return try {
            val response = weatherApi.getCurrentWeather(location = location)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error: ${response.code()} ${response.message()}"))
            }
        }catch (e:Exception){
            Result.failure(e)
        }
    }

    suspend fun getHourlyWeather(location: String) : Result<WeatherResponseHourly>{
        return try {
            val response = weatherApi.getHourlyWeather(location = location)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error: ${response.code()} ${response.message()}"))
            }
        } catch (e:Exception){
            Result.failure(e)
        }
    }

}