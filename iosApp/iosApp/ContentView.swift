
import SwiftUI
import Shared

extension Shared.Weather {
    var sfSymbolName: String {
        switch self {
        case .clearSky: return "sun.max.fill"
        case .partlyCloudy: return "cloud.sun.fill"
        case .fog: return "cloud.fog.fill"
        case .drizzle: return "cloud.drizzle.fill"
        case .rain: return "cloud.rain.fill"
        case .snow: return "snow"
        case .showers: return "cloud.heavyrain.fill"
        case .thunderstorm: return "cloud.bolt.rain.fill"
        case .unknown: return "questionmark.circle.fill"
        default:
            print("Unknown weather enum case: \(self.name)")
            return "questionmark.circle.fill"
        }
    }

    var userFriendlyDescription: String {
        switch self {
        case .clearSky: return "Clear Sky"
        case .partlyCloudy: return "Partly Cloudy"
        case .fog: return "Fog"
        case .drizzle: return "Drizzle"
        case .rain: return "Rain"
        case .snow: return "Snow"
        case .showers: return "Showers"
        case .thunderstorm: return "Thunderstorm"
        case .unknown: return "Unknown Weather"
        default:
            return self.name
        }
    }
}

func formatDate(_ kmpLocalDate: Shared.Kotlinx_datetimeLocalDate) -> String {
    let dateFormatter = DateFormatter()
    var components = DateComponents()
    components.year = Int(kmpLocalDate.year)
    components.month = Int(kmpLocalDate.month.ordinal) + 1
    components.day = Int(kmpLocalDate.day)

    if let date = Calendar.current.date(from: components) {
        dateFormatter.dateFormat = "MMM d (EEE)"
        return dateFormatter.string(from: date)
    }
    return kmpLocalDate.description()
}

func formatTime(_ kmpLocalDateTime: Shared.Kotlinx_datetimeLocalDateTime, hourlyFormat: Bool = false) -> String {
    let dateFormatter = DateFormatter()
    var components = DateComponents()
    components.year = Int(kmpLocalDateTime.year)
    components.month = Int(kmpLocalDateTime.month.ordinal) + 1
    components.day = Int(kmpLocalDateTime.day)
    components.hour = Int(kmpLocalDateTime.hour)
    components.minute = Int(kmpLocalDateTime.minute)

    if let date = Calendar.current.date(from: components) {
        dateFormatter.dateFormat = hourlyFormat ? "h a" : "h:mm a"
        return dateFormatter.string(from: date)
    }
    let minuteString = String(format: "%02d", kmpLocalDateTime.minute)
    return "\(kmpLocalDateTime.hour):\(minuteString)"
}

struct ContentView: View {
    @StateObject private var viewModelStoreOwner = IosViewModelStoreOwner()
    @State private var kmpViewModel: Shared.WeatherViewModel? = nil
    @State private var uiState: Shared.WeatherUiState? = nil
    @State private var collectionTask: Task<(), Never>? = nil

    var body: some View {
        NavigationView {
            Group {
                if let state = uiState {
                    if state.isLoading && state.forecast == nil {
                        ProgressView("Loading Weather...")
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                    } else if let errorMsg = state.error {
                        VStack(spacing: 20) {
                            Image(systemName: "exclamationmark.triangle.fill")
                                .font(.system(size: 50))
                                .foregroundColor(.red)
                            Text("Error")
                                .font(.title)
                            Text(errorMsg)
                                .font(.callout)
                                .foregroundColor(.gray)
                                .multilineTextAlignment(.center)
                                .padding(.horizontal)
                            Button("Retry") {
                                kmpViewModel?.fetchTokyoWeather()
                            }
                            .buttonStyle(.borderedProminent)
                        }
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                    } else if let forecast = state.forecast {
                        ScrollView {
                            VStack(alignment: .center, spacing: 24) {
                                CurrentWeatherViewSwiftUI(currentWeather: forecast.currentWeather)
                                HourlyWeatherViewSwiftUI(hourlyForecast: forecast.hourlyForecast)
                                WeeklyWeatherViewSwiftUI(weeklyForecast: forecast.weeklyForecast)
                            }
                            .padding()
                        }
                        .transition(.opacity.combined(with: .scale))
                    } else {
                        Text("Welcome! Pull to refresh.")
                            .font(.headline)
                            .foregroundColor(.secondary)
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                    }
                } else {
                    ProgressView("Initializing...")
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                }
            }
            .navigationTitle("KMM Weather")
            .onAppear {
                if kmpViewModel == nil {
                    let factory = Shared.WeatherViewModelKt.weatherViewModelFactory
                    let specificVm: Shared.WeatherViewModel = viewModelStoreOwner.viewModel(
                        factory: factory,
                        extras: Shared.CreationExtras.Empty.shared
                    )
                    self.kmpViewModel = specificVm
                    
                    collectionTask = Task {
                        guard let vm = self.kmpViewModel else { return }
                        let stateFlow = vm.uiState
                        
                        let stream = AsyncStream<Shared.WeatherUiState> { continuation in
                            let collector = SwiftFlowCollector<Shared.WeatherUiState> { value in
                                continuation.yield(value)
                            }
                            let cancellable = stateFlow.collect(collector: collector, completionHandler: { error in
                                if let error = error {
                                    print("Error collecting uiState: \(error.localizedDescription)")
                                }
                                continuation.finish()
                            })
                            continuation.onTermination = { @Sendable _ in
                                print("AsyncStream for uiState terminated.")
                            }
                        }
                        
                        for await newState in stream {
                            DispatchQueue.main.async {
                               self.uiState = newState
                            }
                        }
                    }
                    kmpViewModel?.fetchTokyoWeather()
                }
            }
            .onDisappear() {
                collectionTask?.cancel()
            }
            .refreshable {
                kmpViewModel?.fetchTokyoWeather()
            }
        }
    }
}

private struct CurrentWeatherViewSwiftUI: View {
    let currentWeather: Shared.CurrentWeather

    var body: some View {
        VStack(spacing: 16) {
            Text("Current Weather")
                .font(.title2)
                .fontWeight(.semibold)
            
            Image(systemName: currentWeather.weather.sfSymbolName)
                .font(.system(size: 80))
                .symbolRenderingMode(.multicolor)
                .padding(.bottom, 5)

            Text("\(String(format: "%.1f", currentWeather.temperature))°C")
                .font(.system(size: 50, weight: .bold))

            Text(currentWeather.weather.userFriendlyDescription)
                .font(.title3)
                .multilineTextAlignment(.center)
        }
        .padding()
        .background(.thinMaterial)
        .cornerRadius(16)
        .shadow(color: .black.opacity(0.1), radius: 5, x: 0, y: 2)
    }
}

private struct HourlyWeatherItemSwiftUI: View {
    let hourlyWeather: Shared.HourlyWeather

    var body: some View {
        VStack(spacing: 8) {
            Text(formatTime(hourlyWeather.time, hourlyFormat: true))
                .font(.caption)
            Image(systemName: hourlyWeather.weather.sfSymbolName)
                .font(.title3)
                .symbolRenderingMode(.multicolor)
            Text("\(String(format: "%.0f", hourlyWeather.temperature))°")
                .font(.footnote)
                .fontWeight(.medium)
        }
        .padding(EdgeInsets(top: 8, leading: 10, bottom: 8, trailing: 10))
        .background(.ultraThinMaterial)
        .cornerRadius(10)
    }
}

private struct HourlyWeatherViewSwiftUI: View {
    let hourlyForecast: [Shared.HourlyWeather]

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("Hourly Forecast")
                .font(.title3)
                .fontWeight(.semibold)
                .padding(.horizontal)

            if hourlyForecast.isEmpty {
                Text("No hourly data available.")
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .padding(.horizontal)
                    .frame(height: 50) 
            } else {
                ScrollView(.horizontal, showsIndicators: false) {
                    LazyHStack(spacing: 10) {
                        ForEach(hourlyForecast, id: \.time) { weatherItem in
                            HourlyWeatherItemSwiftUI(hourlyWeather: weatherItem)
                        }
                    }
                    .padding(.horizontal)
                }
            }
        }
    }
}

private struct WeeklyWeatherViewSwiftUI: View {
    let weeklyForecast: [Shared.DailyWeather]

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("7-Day Forecast")
                .font(.title2)
                .fontWeight(.semibold)
                .padding(.horizontal)

            ForEach(weeklyForecast, id: \.date) { dailyWeather in 
                DailyWeatherRowSwiftUI(dailyWeather: dailyWeather)
            }
        }
    }
}

private struct DailyWeatherRowSwiftUI: View {
    let dailyWeather: Shared.DailyWeather

    var body: some View {
        HStack(spacing: 16) {
            VStack(alignment: .leading) {
                Text(formatDate(dailyWeather.date))
                    .font(.headline)
                Text(dailyWeather.weather.userFriendlyDescription)
                    .font(.subheadline)
                    .foregroundColor(.gray)
                 Text("Sunrise: \(formatTime(dailyWeather.sunrise)), Sunset: \(formatTime(dailyWeather.sunset))")
                    .font(.caption)
                    .foregroundColor(.gray)
            }

            Spacer()
            
            Image(systemName: dailyWeather.weather.sfSymbolName)
                .font(.title2)
                .symbolRenderingMode(.multicolor)

            Text("\(String(format: "%.0f", dailyWeather.maxTemperature))° / \(String(format: "%.0f", dailyWeather.minTemperature))°")
                .font(.callout)
                .fontWeight(.medium)
        }
        .padding(EdgeInsets(top: 10, leading: 12, bottom: 10, trailing: 12))
        .background(.ultraThinMaterial)
        .cornerRadius(12)
    }
}

class SwiftFlowCollector<T>: NSObject, Shared.Kotlinx_coroutines_coreFlowCollector {
    let callback: (T) -> Void

    init(callback: @escaping (T) -> Void) {
        self.callback = callback
    }

    func emit(value: Any?, completionHandler: @escaping ((any Error)?) -> Void) {
        guard let typedValue = value as? T else {
            let error = NSError(domain: "SwiftFlowCollectorError", code: 1, userInfo: [NSLocalizedDescriptionKey: "Emitted value type mismatch (expected \(String(describing: T.self)), got \(String(describing: value.self))) or nil for non-nullable T"])
            completionHandler(error)
            return
        }
        callback(typedValue)
        completionHandler(nil)
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
