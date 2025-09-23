import com.example.yasuda.weathers.data.NetworkResult
import com.example.yasuda.weathers.model.WeatherForecast

interface WeatherRepository {
    suspend fun getWeatherData(latitude: Double, longitude: Double): NetworkResult<WeatherForecast>
}
