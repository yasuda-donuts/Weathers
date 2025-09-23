package com.example.yasuda.weathers

import WeatherViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.yasuda.weathers.model.CurrentWeather
import com.example.yasuda.weathers.model.DailyWeather
import com.example.yasuda.weathers.viewmodel.WeatherUiState
import weatherViewModelFactory

@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = viewModel(factory = weatherViewModelFactory)
) {
    val uiState by viewModel.uiState.collectAsState()

}
