package com.example.yasuda.weathers

import WeatherViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.yasuda.weathers.model.CurrentWeather
import com.example.yasuda.weathers.model.DailyWeather
import com.example.yasuda.weathers.model.HourlyWeather
import com.example.yasuda.weathers.model.Weather
import com.example.yasuda.weathers.viewmodel.WeatherUiState
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.todayIn
import weatherViewModelFactory
import kotlin.math.roundToInt
import kotlin.time.ExperimentalTime

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
    val state = rememberPullToRefreshState()
    PullToRefreshBox(
        state = state,
        onRefresh = onRefresh,
        isRefreshing = uiState.isLoading && uiState.forecast != null,
        indicator = {
            PullToRefreshDefaults.LoadingIndicator(
                state = state,
                isRefreshing = uiState.isLoading && uiState.forecast != null,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        },
        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.isLoading && uiState.forecast == null) {
                LoadingIndicator(modifier = Modifier.size(64.dp))
            } else {
                uiState.forecast?.let { forecast ->
                    CurrentWeather(forecast.currentWeather)
                    HourlyWeather(forecast.hourlyForecast)
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CurrentWeather(currentWeather: CurrentWeather) {
    Card(
        modifier = Modifier.padding(16.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = largeShape,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
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
                    style = MaterialTheme.typography.displayLargeEmphasized
                )
            }
        }
    }
}

@Composable
fun HourlyWeather(weathers: List<HourlyWeather>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = "Hourly Forecast",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, bottom = 2.dp)
        )
        HourlyWeathers(weathers = weathers)
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun HourlyWeathers(
    weathers: List<HourlyWeather>
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(weathers.size) { index ->
            HourlyWeatherItem(
                hourlyWeather = weathers[index],
                shape = when (index) {
                    0 -> smallShape.copy(
                        topStart = largeShape.topStart,
                        bottomStart = largeShape.bottomStart
                    )
                    weathers.lastIndex -> smallShape.copy(
                        topEnd = largeShape.topEnd,
                        bottomEnd = largeShape.bottomEnd
                    )
                    else -> smallShape
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HourlyWeatherItem(
    hourlyWeather: HourlyWeather,
    shape: Shape
) {
    Card(
        modifier = Modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        shape = shape,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = hourlyWeather.time.format(hourMinuteFormat),
                style = MaterialTheme.typography.titleSmallEmphasized
            )
            Spacer(modifier = Modifier.height(4.dp))
            Icon(
                painter = painterResource(id = hourlyWeather.weather.iconRes()),
                contentDescription = hourlyWeather.weather.name,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${hourlyWeather.temperature.roundToInt()}°C",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun WeeklyWeather(weathers: List<DailyWeather>) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp) 
    ) {
        Text(
            text = "Weekly Forecast",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        weathers.forEachIndexed { index, dailyWeather ->
            DailyWeatherItem(
                dailyWeather = dailyWeather,
                shape = when (index) {
                    0 -> smallShape.copy(
                        topStart = largeShape.topStart,
                        topEnd = largeShape.topEnd
                    )
                    weathers.lastIndex -> smallShape.copy(
                        bottomStart = largeShape.bottomStart,
                        bottomEnd = largeShape.bottomEnd
                    )
                    else -> smallShape
                }
            )
        }
    }
}

@OptIn(ExperimentalTime::class)
private fun formatDateToDayDisplay(date: LocalDate): String {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    return when (date) {
        today -> "Today"
        else -> {
            when (date.dayOfWeek) {
                kotlinx.datetime.DayOfWeek.MONDAY -> "Mon"
                kotlinx.datetime.DayOfWeek.TUESDAY -> "Tue"
                kotlinx.datetime.DayOfWeek.WEDNESDAY -> "Wed"
                kotlinx.datetime.DayOfWeek.THURSDAY -> "Thu"
                kotlinx.datetime.DayOfWeek.FRIDAY -> "Fri"
                kotlinx.datetime.DayOfWeek.SATURDAY -> "Sat"
                kotlinx.datetime.DayOfWeek.SUNDAY -> "Sun"
                else -> date.dayOfWeek.name.take(3)
            }
        }
    }
}

@Composable
fun DailyWeatherItem(
    dailyWeather: DailyWeather,
    shape: Shape
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        shape = shape,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween 
        ) {
            Text(
                text = formatDateToDayDisplay(dailyWeather.date), 
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f) 
            )
            Icon(
                painter = painterResource(id = dailyWeather.weather.iconRes()),
                contentDescription = dailyWeather.weather.name,
                modifier = Modifier.size(36.dp) 
            )
            Text(
                text = "${dailyWeather.maxTemperature.roundToInt()}° / ${dailyWeather.minTemperature.roundToInt()}°",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium, 
                textAlign = TextAlign.End, 
                modifier = Modifier.weight(1f) 
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

@OptIn(FormatStringsInDatetimeFormats::class)
private val hourMinuteFormat = LocalDateTime.Format { byUnicodePattern("HH:mm") }

private val largeShape = ShapeDefaults.ExtraLarge
private val smallShape = ShapeDefaults.ExtraSmall
