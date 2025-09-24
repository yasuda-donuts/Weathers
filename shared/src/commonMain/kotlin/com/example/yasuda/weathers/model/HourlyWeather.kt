package com.example.yasuda.weathers.model

import kotlinx.datetime.LocalDateTime

data class HourlyWeather(
    val time: LocalDateTime,
    val temperature: Double,
    val precipitationProbability: Int,
    val weather: Weather
)