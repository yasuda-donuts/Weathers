package com.example.yasuda.weathers.data

import WeatherRepository
import com.example.yasuda.weathers.model.CurrentWeather
import com.example.yasuda.weathers.model.DailyWeather
import com.example.yasuda.weathers.model.WeatherForecast
import com.example.yasuda.weathers.model.WeatherInfo
import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import kotlinx.datetime.LocalDate

class OpenMeteoRepositoryImpl(private val httpClient: HttpClient) : WeatherRepository {
    private val forecastApiUrl = "https://api.open-meteo.com/v1/forecast"

    override suspend fun getWeatherData(latitude: Double, longitude: Double): NetworkResult<WeatherForecast> {
        val result = httpClient.safeRequest<OpenMeteoResponseDto> {
            url(forecastApiUrl)
            parameter("latitude", latitude)
            parameter("longitude", longitude)
            parameter("current", "temperature_2m,weather_code")
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
    val daily = dailyData?: return null

    val weeklyData = daily.time.indices.mapNotNull { i ->
        DailyWeather(
            date = LocalDate.parse(daily.time[i]),
            maxTemperature = daily.temperatureMax[i],
            minTemperature = daily.temperatureMin[i],
            weatherInfo = mapWmoCodeToWeatherInfo(daily.weatherCode[i]),
            sunrise = daily.sunrise[i],
            sunset = daily.sunset[i]
        )
    }

    return WeatherForecast(
        currentWeather = CurrentWeather(
            temperature = current.temperature,
            weatherInfo = mapWmoCodeToWeatherInfo(current.weatherCode)
        ),
        weeklyForecast = weeklyData
    )
}

fun mapWmoCodeToWeatherInfo(code: Int): WeatherInfo {
    val (description, iconIdentifier) = when (code) {
        0 -> "Clear sky" to "ic_clear_sky"
        1, 2, 3 -> "Mainly clear, partly cloudy, and overcast" to "ic_partly_cloudy"
        45, 48 -> "Fog and depositing rime fog" to "ic_fog"
        51, 53, 55 -> "Drizzle: Light, moderate, and dense intensity" to "ic_drizzle"
        61, 63, 65 -> "Rain: Slight, moderate and heavy intensity" to "ic_rain"
        71, 73, 75 -> "Snow fall: Slight, moderate, and heavy intensity" to "ic_snow"
        80, 81, 82 -> "Rain showers: Slight, moderate, and violent" to "ic_showers"
        95, 96, 99 -> "Thunderstorm" to "ic_thunderstorm"
        else -> "Unknown" to "ic_unknown"
    }
    return WeatherInfo(code, description, iconIdentifier)
}