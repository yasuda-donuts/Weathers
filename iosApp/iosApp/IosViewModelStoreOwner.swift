
import Foundation
import Shared

// Conforms to Shared.ViewModelStoreOwner from the swiftExport generated header
class IosViewModelStoreOwner: ObservableObject, Shared.ViewModelStoreOwner {

    // Use Shared.ViewModelStore from the swiftExport generated header
    let viewModelStore = Shared.ViewModelStore()

    /// This function allows retrieving the androidx ViewModel from the store.
    /// It uses the utility function to pass the generic type T to shared code.
    func viewModel<T: Shared.ViewModel>( // T conforms to Shared.ViewModel
        key: String? = nil,
        factory: Shared.ViewModelProviderFactory, // Use Shared.ViewModelProviderFactory
        extras: Shared.CreationExtras? = nil     // Use Shared.CreationExtras
    ) -> T {
        do {
            // Call the resolveViewModel from the swiftExport generated header
            // modelClass expects AnyClass, so T.self should work.
            let resolvedVm = try viewModelStore.resolveViewModel(
                modelClass: T.self, // T.self is AnyClass
                factory: factory,
                key: key, // Pass the key if provided
                extras: extras ?? Shared.CreationExtras.Empty.shared // Use empty extras if nil
            )

            guard let specificVm = resolvedVm as? T else {
                // This should ideally not happen if types align and factory is correct
                fatalError("Failed to cast ViewModel to type \(T.self). Resolved ViewModel was \(resolvedVm) of type \(type(of: resolvedVm))")
            }
            return specificVm
        } catch {
            fatalError("Failed to create ViewModel of type \(T.self): \(error.localizedDescription) with underlying error: \(error)")
        }
    }

    /// This is called when this class is used as a `@StateObject`
    deinit {
        viewModelStore.clear()
    }
}
