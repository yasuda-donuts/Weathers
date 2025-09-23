import SwiftUI
import Shared

struct ContentView: View {
    @State private var showContent = false
    @StateObject private var viewModelStoreOwner = IosViewModelStoreOwner()

    var body: some View {
        let weatherViewModel: WeatherViewModel = viewModelStoreOwner.viewModel(
            factory: WeatherViewModelKt.weatherViewModelFactory
        )

        VStack {
            
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
        .padding()
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
