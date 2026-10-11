import UIKit
import SwiftUI
import Shared

struct ComposeView: UIViewControllerRepresentable {
    // Obserwujemy instancję StoreKitManagera przekazaną z widoku SwiftUI
    @ObservedObject var storeKitManager: StoreKitManager

    func makeUIViewController(context: Self.Context) -> UIViewController {
        // Konfigurujemy most między Swiftem a Kotlinem
        // Status premium z StoreKit 2 zapisywany jest przez most do wspólnej bazy (subscription_status)
        let billingController = storeKitManager.setupBridge()
        
        // Zwracamy MainViewController podając oba wymagane parametry
        return MainViewControllerKt.MainViewController(
            billingController: billingController
        ) { kotlinCallback in
            
            // Logika Twojej reklamy startowej
            AppOpenAdHelper.shared.loadAndShow {
                DispatchQueue.main.async {
                    if let closure = kotlinCallback as? () -> Void {
                        closure()
                    } else if let closureWithUnit = kotlinCallback as? () -> KotlinUnit {
                        _ = closureWithUnit()
                    } else {
                        (kotlinCallback as AnyObject).invoke?()
                    }
                }
            }
        }
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Self.Context) {}
}

struct ContentView: View {
    // Inicjalizujemy zarządcę płatności jako główny stan widoku
    @StateObject private var storeKitManager = StoreKitManager()
    
    var body: some View {
        ComposeView(storeKitManager: storeKitManager)
            .ignoresSafeArea()
            .onReceive(NotificationCenter.default.publisher(for: UIApplication.willEnterForegroundNotification)) { _ in
                // Wykrywa wygaśnięcie lub odnowienie subskrypcji po powrocie do aplikacji
                storeKitManager.refreshPremiumStatus()
            }
    }
}
