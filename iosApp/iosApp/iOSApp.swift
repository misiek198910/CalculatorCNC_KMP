import SwiftUI
import GoogleMobileAds
import UserMessagingPlatform
import Shared

// 1. Klasa delegata
class BannerAdLogger: NSObject, BannerViewDelegate {
    static let shared = BannerAdLogger()

    func bannerViewDidReceiveAd(_ bannerView: BannerView) {}

    func bannerView(_ bannerView: BannerView, didFailToReceiveAdWithError error: Error) {}
}

class ConsentManager {
    static let shared = ConsentManager()

    func gatherConsent(completion: @escaping () -> Void) {
        let parameters = RequestParameters()

        ConsentInformation.shared.requestConsentInfoUpdate(with: parameters) { error in
            if error != nil {
                completion()
                return
            }

            DispatchQueue.main.async {
                guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
                      let rootViewController = windowScene.windows.first?.rootViewController else {
                    completion()
                    return
                }

                ConsentForm.loadAndPresentIfRequired(from: rootViewController) { _ in
                    completion()
                }
            }
        }
    }
}

class InterstitialAdManager: NSObject, FullScreenContentDelegate {
    static let shared = InterstitialAdManager()
    private var interstitialAd: InterstitialAd?
    private var adUnitID: String = ""

    func loadAd(adUnitID: String) {
        self.adUnitID = adUnitID
        let request = Request()

        InterstitialAd.load(with: adUnitID, request: request) { [weak self] ad, error in
            if error != nil {
                return
            }
            self?.interstitialAd = ad
            self?.interstitialAd?.fullScreenContentDelegate = self
        }
    }

    func showAd() {
        guard let interstitialAd = interstitialAd else {
            loadAd(adUnitID: self.adUnitID)
            return
        }

        if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
           let rootVC = windowScene.windows.first?.rootViewController {
            interstitialAd.present(from: rootVC)
        }
    }

    func adDidDismissFullScreenContent(_ ad: FullScreenPresentingAd) {
        interstitialAd = nil
        loadAd(adUnitID: self.adUnitID)
    }
}

@main
struct iOSApp: App {
    init() {
        MobileAds.shared.requestConfiguration.testDeviceIdentifiers = [ "82f23e9a6a77246e35c57400f34c93d5" ]

        let adInlineID = Bundle.main.object(forInfoDictionaryKey: "AD_BANNER_INLINE_ID") as? String ?? ""
        let adBannerID = Bundle.main.object(forInfoDictionaryKey: "AD_BANNER_ID") as? String ?? ""

        //Banner inline
        AdMobConfigBridge.shared.createInlineBanner = {
            let width = UIScreen.main.bounds.width
            let banner = BannerView(adSize: inlineAdaptiveBanner(width: width, maxHeight: 250))

            banner.frame = CGRect(x: 0, y: 0, width: width, height: 250)
            banner.adUnitID = adInlineID
            banner.delegate = BannerAdLogger.shared

            if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
               let window = windowScene.windows.first,
               let rootVC = window.rootViewController {
                banner.rootViewController = rootVC
            }

            DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                if ConsentInformation.shared.canRequestAds {
                    banner.load(Request())
                }
            }
            return banner
        }

        //Zwykły banner
        AdBanner_iosKt.createIosAdBannerView = {
            let banner = BannerView(adSize: AdSizeBanner)

            banner.frame = CGRect(x: 0, y: 0, width: 320, height: 50)
            banner.adUnitID = adBannerID
            banner.delegate = BannerAdLogger.shared

            if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
               let window = windowScene.windows.first,
               let rootVC = window.rootViewController {
                banner.rootViewController = rootVC
            }

            DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                if ConsentInformation.shared.canRequestAds {
                    banner.load(Request())
                }
            }
            return banner
        }
        //Interstitial
        AdMobConfigBridge.shared.showInterstitial = {
            InterstitialAdManager.shared.showAd()
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onAppear {
                    ConsentManager.shared.gatherConsent {
                        if ConsentInformation.shared.canRequestAds {
                            MobileAds.shared.start(completionHandler: nil)

                            let adInterstitialID = Bundle.main.object(forInfoDictionaryKey: "AD_FULLSCREEN_ID") as? String ?? ""
                            InterstitialAdManager.shared.loadAd(adUnitID: adInterstitialID)
                        }
                    }
                }
        }
    }
}