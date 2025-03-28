package com.example.halocare.network.models

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("location") val location: Location,
    @SerializedName("current") val current: CurrentWeather
) {
    data class Location(
        @SerializedName("name") val name: String,
        @SerializedName("country") val country: String,
        @SerializedName("localtime") val localtime: String
    )

    data class CurrentWeather(
        @SerializedName("temp_c") val tempCelsius: Float,
        @SerializedName("temp_f") val tempFahrenheit: Float,
        @SerializedName("condition") val condition: Condition,
        @SerializedName("is_day") val isDayRaw: Int,
        @SerializedName("wind_kph") val windSpeed: Float,
        @SerializedName("wind_dir") val windDirection: String,
        @SerializedName("feelslike_c") val feelsLikeCelsius: Float,
        @SerializedName("feelslike_f") val feelsLikeFahrenheit: Float,
        @SerializedName("cloud") val cloudCoverage: Int,
        @SerializedName("precip_mm") val precipitationMm: Float
    ) {
        val isDay: Boolean
            get() = isDayRaw == 1
        data class Condition(
            @SerializedName("text") val text: String,
            @SerializedName("icon") val iconRaw: String
        ){
            val icon: String
                get() = "https:$iconRaw"
        }
    }
}

