package com.example.halocare.network.models

import com.google.gson.annotations.SerializedName

data class WeatherResponseHourly(
    @SerializedName("location") val location: Location,
    @SerializedName("forecast") val forecast: Forecast
) {
    data class Location(
        @SerializedName("name") val name: String,
        @SerializedName("country") val country: String,
        @SerializedName("localtime") val localtime: String
    )

    data class Forecast(
        @SerializedName("forecastday") val days: List<DayForecast>
    ) {
        data class DayForecast(
            @SerializedName("date") val date: String,
            @SerializedName("hour") val hourly: List<HourlyForecast>
        )
    }

    data class HourlyForecast(
        @SerializedName("time") val time: String,
        @SerializedName("temp_c") val tempCelsius: Float,
        @SerializedName("temp_f") val tempFahrenheit: Float,
        @SerializedName("feelslike_c") val feelsLikeCelsius: Float,
        @SerializedName("feelslike_f") val feelsLikeFahrenheit: Float,
        @SerializedName("condition") val condition: Condition,
        @SerializedName("wind_kph") val windSpeed: Float,
        @SerializedName("wind_dir") val windDirection: String,
        @SerializedName("chance_of_rain") val chanceOfRain: Int
    ) {
        data class Condition(
            @SerializedName("text") val text: String,
            @SerializedName("icon") val iconUrlRaw: String
        ) {
            val iconUrl: String
                get() = "https:$iconUrlRaw"
        }
    }
}

