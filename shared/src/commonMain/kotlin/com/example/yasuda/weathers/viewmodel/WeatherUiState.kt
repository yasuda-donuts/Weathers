package com.example.yasuda.weathers.viewmodel

import com.example.yasuda.weathers.model.WeatherForecast

data class WeatherUiState(
    val isLoading: Boolean = false,
    val forecast: WeatherForecast? = null,
    val error: String? = null
)