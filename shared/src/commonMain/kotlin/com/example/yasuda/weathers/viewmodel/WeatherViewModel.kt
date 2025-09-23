import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.yasuda.weathers.data.NetworkResult
import com.example.yasuda.weathers.data.OpenMeteoRepositoryImpl
import com.example.yasuda.weathers.data.createHttpClient
import com.example.yasuda.weathers.viewmodel.WeatherUiState
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WeatherViewModel(private val repository: WeatherRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { fetchTokyoWeather() }
    }

    fun fetchTokyoWeather() = fetchWeather(latitude = 35.6892, longitude = 139.6917)

    fun fetchWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = repository.getWeatherData(latitude, longitude)) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, forecast = result.data)
                    }
                }
                is NetworkResult.Failure -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = result.message?: "An error occurred")
                    }
                }
            }
        }
    }
}

val weatherViewModelFactory = viewModelFactory {
    initializer {
        WeatherViewModel(repository = getWeatherRepository())
    }
}

fun getWeatherRepository(): WeatherRepository {
    return OpenMeteoRepositoryImpl(httpClient = createHttpClient())
}