import Foundation
import GoogleMobileAds
import UIKit

class AppOpenAdHelper: NSObject, FullScreenContentDelegate {
    static let shared = AppOpenAdHelper()

    var onComplete: (() -> Void)?
    var appOpenAd: AppOpenAd?

    private var isAdShowing = false
    private var hasFinished = false

    func loadAndShow(completion: @escaping () -> Void) {
        self.onComplete = completion
        self.isAdShowing = false
        self.hasFinished = false

        MobileAds.shared.start(completionHandler: nil)

        // TIMEOUT
        DispatchQueue.main.asyncAfter(deadline: .now() + 4.0) { [weak self] in
            guard let self = self else { return }
            if !self.isAdShowing && !self.hasFinished {
                print("Timeout reklamy startowej. Przepuszczam dalej.")
                self.completeAdProcess()
            }
        }

        let request = Request()

        AppOpenAd.load(with: "ca-app-pub-3940256099942544/5575463023",
                       request: request) { [weak self] (ad: AppOpenAd?, error: Error?) in

            guard let self = self else { return }

            if self.hasFinished { return }

            if let error = error {
                print("Błąd pobierania reklamy startowej: \(error.localizedDescription)")
                self.completeAdProcess()
                return
            }

            self.appOpenAd = ad
            self.appOpenAd?.fullScreenContentDelegate = self

            if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
               let rootVC = windowScene.windows.first?.rootViewController {
                self.isAdShowing = true
                self.appOpenAd?.present(from: rootVC)
            } else {
                print("Błąd wyświetlania: Nie znaleziono rootVC")
                self.completeAdProcess()
            }
        }
    }

    func adDidDismissFullScreenContent(_ ad: FullScreenPresentingAd) {
        print("Użytkownik zamknął reklamę.")
        self.appOpenAd = nil
        completeAdProcess()
    }

    func ad(_ ad: FullScreenPresentingAd, didFailToPresentFullScreenContentWithError error: Error) {
        print("Błąd podczas wyświetlania reklamy: \(error.localizedDescription)")
        self.appOpenAd = nil
        completeAdProcess()
    }

    // GWARANCJA GŁÓWNEGO WĄTKU DLA KOTLINA
    private func completeAdProcess() {
        DispatchQueue.main.async { [weak self] in
            guard let self = self else { return }
            if !self.hasFinished {
                self.hasFinished = true
                print("Wysyłam sygnał onComplete do Kotlin Multiplatform!")
                self.onComplete?()
                self.onComplete = nil
            }
        }
    }
}