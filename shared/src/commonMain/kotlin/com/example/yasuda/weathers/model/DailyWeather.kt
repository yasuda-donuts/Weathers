package com.example.yasuda.weathers.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

data class DailyWeather(
    val date: LocalDate,
    val maxTemperature: Double,
    val minTemperature: Double,
    val weather: Weather,
    val sunrise: LocalDateTime,
    val sunset: LocalDateTime
)
