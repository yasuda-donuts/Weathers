package com.example.yasuda.weathers

import WeatherViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.yasuda.weathers.model.CurrentWeather
import com.example.yasuda.weathers.model.DailyWeather
import com.example.yasuda.weathers.model.Weather
import com.example.yasuda.weathers.viewmodel.WeatherUiState
import weatherViewModelFactory

@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = viewModel(factory = weatherViewModelFactory)
) {
    val uiState by viewModel.uiState.collectAsState()
    WeatherScreenContent(
        uiState = uiState,
        onRefresh = viewModel::fetchTokyoWeather
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WeatherScreenContent(
    uiState: WeatherUiState,
    onRefresh: () -> Unit = { }
) {
    PullToRefreshBox(
        onRefresh = onRefresh,
        isRefreshing = uiState.isLoading && uiState.forecast != null,
        modifier = Modifier.fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.isLoading && uiState.forecast == null) {
                CircularWavyProgressIndicator(modifier = Modifier.size(64.dp))
            } else {
                uiState.forecast?.let { forecast ->
                    CurrentWeather(forecast.currentWeather)
                    WeeklyWeather(forecast.weeklyForecast)
                }
            }
            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun CurrentWeather(currentWeather: CurrentWeather) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Current Weather",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    painter = painterResource(id = currentWeather.weather.iconRes()),
                    contentDescription = currentWeather.weather.name,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "${currentWeather.temperature}°C",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun WeeklyWeather(weathers: List<DailyWeather>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Weekly Forecast",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        weathers.forEach {
            dailyWeather ->
            DailyWeatherItem(dailyWeather)
        }
    }
}

@Composable
fun DailyWeatherItem(dailyWeather: DailyWeather) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = dailyWeather.date.toString(), // Consider formatting this date
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    painter = painterResource(id = dailyWeather.weather.iconRes()),
                    contentDescription = dailyWeather.weather.name,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Max: ${dailyWeather.maxTemperature}°C, Min: ${dailyWeather.minTemperature}°C",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Sunrise: ${dailyWeather.sunrise}, Sunset: ${dailyWeather.sunset}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun Weather.iconRes() = when (this) {
    Weather.CLEAR_SKY -> R.drawable.ico_day
    Weather.PARTLY_CLOUDY -> R.drawable.ico_partly_cloudy_day
    Weather.FOG -> R.drawable.ico_foggy
    Weather.DRIZZLE, Weather.RAIN, Weather.SHOWERS -> R.drawable.ico_rainy
    Weather.SNOW -> R.drawable.ico_snowy
    Weather.THUNDERSTORM -> R.drawable.ico_thunderstorm
    Weather.UNKNOWN -> R.drawable.ico_unknown_med
}
