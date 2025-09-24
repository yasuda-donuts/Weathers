package com.example.yasuda.weathers.model

import kotlinx.datetime.LocalDate

data class DailyWeather(
    val date: LocalDate,
    val maxTemperature: Double,
    val minTemperature: Double,
    val weather: Weather,
    val sunrise: String,
    val sunset: String
)
