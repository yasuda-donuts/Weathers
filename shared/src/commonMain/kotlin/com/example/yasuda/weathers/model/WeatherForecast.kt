package com.example.yasuda.weathers.model

data class WeatherForecast(
    val currentWeather: CurrentWeather,
    val weeklyForecast: List<DailyWeather>,
    val hourlyForecast: List<HourlyWeather>,
)


