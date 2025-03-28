package com.example.halocare.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.halocare.network.NetworkRepository
import com.example.halocare.network.models.WeatherResponse
import com.example.halocare.network.models.WeatherResponseHourly
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LoadingState{LOADING, SUCCESSFUL, ERROR, IDLE}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val networkRepository: NetworkRepository
): ViewModel() {
    private val _weatherData = MutableStateFlow<WeatherResponse?>(null)
    val weatherData = _weatherData.asStateFlow()
    private val _weatherLoadingState = MutableStateFlow(LoadingState.IDLE)
    val weatherLoadingState = _weatherLoadingState.asStateFlow()
    private val _hourlyWeatherData = MutableStateFlow<WeatherResponseHourly?>(null)
    val hourlyWeatherData = _hourlyWeatherData.asStateFlow()
    private val _hourlyWeatherLoadingState = MutableStateFlow(LoadingState.IDLE)
    val hourlyWeatherLoadingState = _hourlyWeatherLoadingState.asStateFlow()

    init {
        getTodayWeather("Lagos")
    }

    fun getTodayWeather(location: String){
        viewModelScope.launch {
            _weatherLoadingState.value = LoadingState.LOADING
            val weatherCall = networkRepository.getTodayWeather(location)
            if (weatherCall.isSuccess){
                _weatherData.value = weatherCall.getOrNull()
                _weatherLoadingState.value = LoadingState.SUCCESSFUL
            }
            if (weatherCall.isFailure){
                val errorData = weatherCall.exceptionOrNull()
                Log.d("WEATHER DATA", "getTodayWeather: ${errorData?.message}")
                _weatherLoadingState.value = LoadingState.ERROR
            }
        }
    }

    fun getHourlyWeather(location: String){
        viewModelScope.launch {
            _hourlyWeatherLoadingState.value = LoadingState.LOADING
            delay(1500)
            val hourlyWeatherCall = networkRepository.getHourlyWeather(location)
            if (hourlyWeatherCall.isSuccess){
                _hourlyWeatherData.value = hourlyWeatherCall.getOrNull()
                _hourlyWeatherLoadingState.value = LoadingState.SUCCESSFUL
            }
            if(hourlyWeatherCall.isFailure){
                val errorData = hourlyWeatherCall.exceptionOrNull()
                Log.d("WEATHER DATA", "getTodayWeather: ${errorData?.message}")
                _hourlyWeatherLoadingState.value = LoadingState.ERROR
            }
        }
    }
}