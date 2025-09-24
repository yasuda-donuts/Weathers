package com.example.yasuda.weathers.data

import WeatherRepository
import com.example.yasuda.weathers.model.CurrentWeather
import com.example.yasuda.weathers.model.DailyWeather
import com.example.yasuda.weathers.model.HourlyWeather
import com.example.yasuda.weathers.model.Weather
import com.example.yasuda.weathers.model.WeatherForecast
import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

class OpenMeteoRepositoryImpl(private val httpClient: HttpClient) : WeatherRepository {
    private val forecastApiUrl = "https://api.open-meteo.com/v1/forecast"

    override suspend fun getWeatherData(latitude: Double, longitude: Double): NetworkResult<WeatherForecast> {
        val result = httpClient.safeRequest<OpenMeteoResponseDto> {
            url(forecastApiUrl)
            parameter("latitude", latitude)
            parameter("longitude", longitude)
            parameter("current", "temperature_2m,weather_code")
            parameter("hourly", "temperature_2m,weather_code,precipitation_probability")
            parameter("daily", "weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset")
            parameter("timezone", "auto")
        }

        return when (result) {
            is NetworkResult.Success -> {
                val dto = result.data
                val domainModel = dto.toDomain()
                if (domainModel!= null) {
                    NetworkResult.Success(domainModel)
                } else {
                    NetworkResult.Failure(null, "Failed to parse weather data")
                }
            }
            is NetworkResult.Failure -> result
        }
    }
}

fun OpenMeteoResponseDto.toDomain(): WeatherForecast? {
    val current = currentData?: return null
    val hourly = hourlyData?: return null
    val daily = dailyData?: return null

    val hourlyData = hourly.time.indices.mapNotNull { i ->
        HourlyWeather(
            time = LocalDateTime.parse(hourly.time[i]),
            temperature = hourly.temperature[i],
            precipitationProbability = hourly.precipitationProbability[i],
            weather = Weather.fromWmoCode(hourly.weatherCode[i])
        )
    }

    val weeklyData = daily.time.indices.mapNotNull { i ->
        DailyWeather(
            date = LocalDate.parse(daily.time[i]),
            maxTemperature = daily.temperatureMax[i],
            minTemperature = daily.temperatureMin[i],
            weather = Weather.fromWmoCode(daily.weatherCode[i]),
            sunrise = LocalDateTime.parse(daily.sunrise[i]),
            sunset = LocalDateTime.parse(daily.sunset[i]),
        )
    }

    return WeatherForecast(
        currentWeather = CurrentWeather(
            temperature = current.temperature,
            weather = Weather.fromWmoCode(current.weatherCode)
        ),
        weeklyForecast = weeklyData,
        hourlyForecast = hourlyData,
    )
}
