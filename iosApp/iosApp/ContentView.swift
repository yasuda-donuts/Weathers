import SwiftUI
import Foundation
import Shared

struct ContentView: View {
    @StateObject private var viewModelStoreOwner = IosViewModelStoreOwner()
    @State private var kmpViewModel: Shared.WeatherViewModel? = nil
    @State private var uiState: Shared.WeatherUiState? = nil
    @State private var collectionTask: Task<(), Never>? = nil

    var body: some View {
        ZStack {
            BackgroundView(weather: uiState?.forecast?.currentWeather.weather)

            NavigationView {
                ZStack {
                    Color.clear

                    if let state = uiState {
                        if state.isLoading && state.forecast == nil {
                            loadingView
                        } else if let errorMsg = state.error {
                            errorView(message: errorMsg)
                        } else if let forecast = state.forecast {
                            weatherContentView(forecast: forecast)
                        } else {
                            placeholderView
                        }
                    } else {
                        loadingView
                    }
                }
                .navigationTitle("Tokyo Weather")
                .navigationBarTitleDisplayMode(.inline)
                .toolbarBackground(.visible, for:.navigationBar)
                .toolbarBackground(.ultraThinMaterial, for:.navigationBar)
            }
            .preferredColorScheme(.dark)
        }
        .onAppear(perform: setupViewModel)
        .onDisappear {
            collectionTask?.cancel()
        }
    }

    private func setupViewModel() {
        if kmpViewModel == nil {
            let factory = Shared.weatherViewModelFactory // SKIE: Direct access
            let specificVm: Shared.WeatherViewModel = viewModelStoreOwner.viewModel(
                factory: factory,
                extras: Shared.CreationExtras.Empty.shared
            )
            self.kmpViewModel = specificVm

            collectionTask = Task {
                guard let vm = self.kmpViewModel else { return }
                // SKIE: Collect StateFlow, attempting to call uiState as a function
                for await newState in vm.uiState {
                    await MainActor.run {
                        self.uiState = newState
                    }
                }
            }
            kmpViewModel?.fetchTokyoWeather()
        }
    }
}

extension ContentView {
    private var loadingView: some View {
        ProgressView("Fetching Weather...")
            .tint(.white)
            .foregroundStyle(.white)
    }

    private var placeholderView: some View {
        VStack(spacing: 8) {
            Image(systemName: "cloud.sun.rain.fill")
                .font(.largeTitle)
            Text("Welcome!")
            Text("Pull down to refresh.")
                .font(.caption)
        }
        .foregroundStyle(.secondary)
    }

    private func errorView(message: String) -> some View {
        VStack(spacing: 20) {
            Image(systemName: "exclamationmark.triangle.fill")
                .font(.system(size: 50))
                .foregroundColor(.yellow)
            Text("Error")
                .font(.title)
            Text(message)
                .font(.callout)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal)
            Button("Retry") {
                kmpViewModel?.fetchTokyoWeather()
            }
            .buttonStyle(.borderedProminent)
            .tint(.yellow)
        }
        .padding()
    }

    private func weatherContentView(forecast: Shared.WeatherForecast) -> some View {
        ScrollView {
            if #available(iOS 26.0, *) { // Preserving original iOS version check logic
                GlassEffectContainer(spacing: 20.0) {
                    weatherCards(forecast: forecast)
                }
            } else {
                weatherCards(forecast: forecast)
            }
        }
        .refreshable {
            kmpViewModel?.fetchTokyoWeather()
        }
        .transition(.opacity.animation(.easeInOut))
    }

    @ViewBuilder
    private func weatherCards(forecast: Shared.WeatherForecast) -> some View {
        VStack(spacing: 28) {
            CurrentWeatherView(currentWeather: forecast.currentWeather)
            HourlyWeatherView(hourlyForecast: forecast.hourlyForecast)
            WeeklyWeatherView(weeklyForecast: forecast.weeklyForecast)
        }
        .padding()
    }
}

private struct BackgroundView: View {
    let weather: Shared.Weather?

    var body: some View {
        let topColor = weather?.gradientColors.top ?? Color.black
        let bottomColor = weather?.gradientColors.bottom ?? Color.gray

        LinearGradient(colors: [topColor, bottomColor], startPoint:.top, endPoint:.bottom)
            .blur(radius: 60)
            .ignoresSafeArea()
            .animation(.easeInOut(duration: 1.0), value: weather)
    }
}

private struct CurrentWeatherView: View {
    let currentWeather: Shared.CurrentWeather

    var body: some View {
        VStack(spacing: 8) {
            Text(currentWeather.weather.userFriendlyDescription)
                .font(.title2).fontWeight(.medium)

            Text(String(format: "%.1f°", currentWeather.temperature))
                .font(.system(size: 80, weight:.bold))

            Image(systemName: currentWeather.weather.sfSymbolName)
                .font(.largeTitle)
                .symbolRenderingMode(.multicolor)
                .padding(.top, 8)
        }
        .foregroundColor(.white)
        .shadow(radius: 5)
    }
}

private struct HourlyWeatherView: View {
    let hourlyForecast: [Shared.HourlyWeather]

    var body: some View {
        VStack(alignment:.leading, spacing: 12) {
            Text("HOURLY FORECAST")
                .font(.caption)
                .fontWeight(.bold)
                .foregroundColor(.white.opacity(0.7))
                .padding(.leading)

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 16) {
                    ForEach(hourlyForecast, id: \.time) { weatherItem in
                        HourlyWeatherItem(hourlyWeather: weatherItem)
                    }
                }
                .padding(.horizontal)
            }
        }
        .padding()
        .glassedEffect(in: RoundedRectangle(cornerRadius: 20))
    }
}

private struct HourlyWeatherItem: View {
    let hourlyWeather: Shared.HourlyWeather

    var body: some View {
        VStack(spacing: 12) {
            Text(formatTime(hourlyWeather.time, hourlyFormat: true))
                .font(.caption.weight(.bold))
            Image(systemName: hourlyWeather.weather.sfSymbolName)
                .font(.title2)
                .symbolRenderingMode(.multicolor)
            Text("\(String(format: "%.0f", hourlyWeather.temperature))°")
                .font(.title3.weight(.semibold))
        }
        .foregroundColor(.white)
    }
}

private struct WeeklyWeatherView: View {
    let weeklyForecast: [Shared.DailyWeather]

    var body: some View {
        VStack(alignment:.leading, spacing: 12) {
            Text("7-DAY FORECAST")
                .font(.caption)
                .fontWeight(.bold)
                .foregroundColor(.white.opacity(0.7))
                .padding(.leading)

            VStack(spacing: 16) {
                ForEach(weeklyForecast, id: \.date) { dailyWeather in
                    DailyWeatherRow(dailyWeather: dailyWeather)
                }
            }
        }
        .padding()
        .glassedEffect(in: RoundedRectangle(cornerRadius: 20))
    }
}

private struct DailyWeatherRow: View {
    let dailyWeather: Shared.DailyWeather

    var body: some View {
        HStack(spacing: 16) {
            Text(formatDate(dailyWeather.date))
                .font(.headline)
                .frame(width: 120, alignment:.leading)

            Image(systemName: dailyWeather.weather.sfSymbolName)
                .font(.title2)
                .symbolRenderingMode(.multicolor)
                .frame(maxWidth:.infinity)

            Text("\(String(format: "%.0f", dailyWeather.maxTemperature))° / \(String(format: "%.0f", dailyWeather.minTemperature))°")
                .font(.headline)
                .frame(width: 100, alignment:.trailing)
        }
        .foregroundColor(.white)
    }
}

extension Shared.Weather {
    var sfSymbolName: String {
        switch self {
        case.clearSky: "sun.max.fill"
        case.partlyCloudy: "cloud.sun.fill"
        case.fog: "cloud.fog.fill"
        case.drizzle: "cloud.drizzle.fill"
        case.rain: "cloud.rain.fill"
        case.snow: "snowflake"
        case.showers: "cloud.heavyrain.fill"
        case.thunderstorm: "cloud.bolt.rain.fill"
        default: "questionmark.diamond.fill"
        }
    }

    var userFriendlyDescription: String {
        switch self {
        case.clearSky: "Clear Sky"
        case.partlyCloudy: "Partly Cloudy"
        case.fog: "Fog"
        case.drizzle: "Drizzle"
        case.rain: "Rain"
        case.snow: "Snow"
        case.showers: "Showers"
        case.thunderstorm: "Thunderstorm"
        default: "Unknown"
        }
    }

    var gradientColors: (top: Color, bottom: Color) {
        switch self {
        case.clearSky: (.blue, Color(red: 0.4, green: 0.8, blue: 1.0))
        case.partlyCloudy,.fog: (Color(red: 0.3, green: 0.5, blue: 0.7),.gray)
        case.rain,.drizzle,.showers: (.indigo,.gray)
        case.snow: (Color(red: 0.6, green: 0.8, blue: 0.9),.teal)
        case.thunderstorm: (.indigo,.black)
        default: (.black,.gray)
        }
    }
}

func formatDate(_ kmpLocalDate: Shared.Kotlinx_datetimeLocalDate) -> String {
    let dateFormatter = DateFormatter()
    var components = DateComponents()
    components.year = Int(kmpLocalDate.year)
    components.month = Int(kmpLocalDate.monthNumber) // Corrected access for LocalDate
    components.day = Int(kmpLocalDate.dayOfMonth)    // Corrected access for LocalDate

    if let date = Calendar.current.date(from: components) {
        if Calendar.current.isDateInToday(date) {
            return "Today"
        }
        dateFormatter.dateFormat = "EEE"
        return dateFormatter.string(from: date)
    }
    return kmpLocalDate.description()
}

func formatTime(_ kmpLocalDateTime: Shared.Kotlinx_datetimeLocalDateTime, hourlyFormat: Bool = false) -> String {
    let dateFormatter = DateFormatter()
    var components = DateComponents()
    components.year = Int(kmpLocalDateTime.year)
    components.month = Int(kmpLocalDateTime.monthNumber)
    components.day = Int(kmpLocalDateTime.dayOfMonth) // Corrected access for LocalDateTime
    components.hour = Int(kmpLocalDateTime.hour)
    components.minute = Int(kmpLocalDateTime.minute)

    if let date = Calendar.current.date(from: components) {
        dateFormatter.dateFormat = hourlyFormat ? "h a" : "h:mm a"
        return dateFormatter.string(from: date)
    }
    return "\(kmpLocalDateTime.hour):\(String(format: "%02d", kmpLocalDateTime.minute))"
}

// SwiftFlowCollector class removed

extension View {
    @ViewBuilder
    func glassedEffect(in shape: some Shape, interactive: Bool = false) -> some View {
        if #available(iOS 26.0, *) {
            self.glassEffect(interactive ? .regular.interactive() : .regular, in: shape)
        } else {
            self.background {
                shape.glassed()
            }
        }
    }
}

extension Shape {
    func glassed() -> some View {
        self
            .fill(.ultraThinMaterial)
            .fill(
                .linearGradient(
                    colors: [
                        .primary.opacity(0.08),
                        .primary.opacity(0.05),
                        .primary.opacity(0.01),
                        .clear,
                        .clear,
                        .clear
                    ],
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
            )
            .stroke(.primary.opacity(0.2), lineWidth: 0.7)
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
