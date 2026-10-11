import SwiftUI
import GoogleMobileAds
import UserMessagingPlatform
import Shared
import FirebaseCore
import AppTrackingTransparency
import StoreKit

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

class RewardedAdManager: NSObject, FullScreenContentDelegate {
    static let shared = RewardedAdManager()
    private var rewardedAd: RewardedAd?
    private var isLoading = false
    private var showWhenLoaded = false
    private var earnedReward = false

    private var adUnitID: String {
        Bundle.main.object(forInfoDictionaryKey: "AD_REWARDED_ID") as? String ?? ""
    }

    func loadAd() {
        guard rewardedAd == nil, !isLoading else { return }
        isLoading = true

        RewardedAd.load(with: adUnitID, request: Request()) { [weak self] ad, error in
            DispatchQueue.main.async {
                guard let self = self else { return }
                self.isLoading = false
                if error != nil {
                    self.rewardedAd = nil
                    if self.showWhenLoaded {
                        self.showWhenLoaded = false
                        AdMobConfigBridge.shared.onRewardedResult(rewarded: false)
                    }
                    return
                }
                self.rewardedAd = ad
                self.rewardedAd?.fullScreenContentDelegate = self
                if self.showWhenLoaded {
                    self.showWhenLoaded = false
                    self.showAd()
                }
            }
        }
    }

    func showAd() {
        guard let rewardedAd = rewardedAd else {
            // Reklama jeszcze się nie załadowała - pokażemy ją zaraz po załadowaniu
            showWhenLoaded = true
            loadAd()
            return
        }

        guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let rootVC = windowScene.windows.first?.rootViewController else {
            AdMobConfigBridge.shared.onRewardedResult(rewarded: false)
            return
        }

        earnedReward = false
        rewardedAd.present(from: rootVC) { [weak self] in
            self?.earnedReward = true
        }
    }

    func adDidDismissFullScreenContent(_ ad: FullScreenPresentingAd) {
        let rewarded = earnedReward
        rewardedAd = nil
        loadAd()
        AdMobConfigBridge.shared.onRewardedResult(rewarded: rewarded)
    }

    func ad(_ ad: FullScreenPresentingAd, didFailToPresentFullScreenContentWithError error: Error) {
        rewardedAd = nil
        loadAd()
        AdMobConfigBridge.shared.onRewardedResult(rewarded: false)
    }
}

@MainActor
class StoreKitManager: ObservableObject {
    private let productIds = ["cnc_premium_monthly", "cnc_premium_yearly"]
    
    // ZMIANA: Dodano przedrostek Shared.
    var billingBridge: Shared.IosBillingController?
    
    private var updateListenerTask: Task<Void, Never>? = nil

    init() {
        updateListenerTask = listenForTransactions()
    }

    deinit {
        updateListenerTask?.cancel()
    }
    
    // ZMIANA: Zwracamy Shared.IosBillingController
    func setupBridge() -> Shared.IosBillingController {
        let bridge = Shared.IosBillingController(
            onBuyMonthly: { [weak self] in self?.purchase(productId: "cnc_premium_monthly") },
            onBuyYearly: { [weak self] in self?.purchase(productId: "cnc_premium_yearly") },
            onRestore: { [weak self] in self?.restorePurchases() },
            onManage: { [weak self] in self?.manageSubscriptions() }
        )
        self.billingBridge = bridge
        Task { await fetchProducts() }
        Task { await updatePremiumStatus() }
        return bridge
    }

    private func fetchProducts() async {
        do {
            let storeProducts = try await Product.products(for: productIds)
            var monthly = ""
            var yearly = ""
            
            for product in storeProducts {
                if product.id == "cnc_premium_monthly" {
                    monthly = product.displayPrice
                } else if product.id == "cnc_premium_yearly" {
                    yearly = product.displayPrice
                }
            }
            
            billingBridge?.updatePrices(monthly: monthly, yearly: yearly)
        } catch {
            print("Failed to fetch products: \(error)")
        }
    }

    private func purchase(productId: String) {
        Task {
            guard let product = try? await Product.products(for: [productId]).first else { return }
            do {
                let result = try await product.purchase()
                switch result {
                case .success(let verification):
                    let transaction = try checkVerified(verification)
                    await transaction.finish()
                    await updatePremiumStatus()
                case .userCancelled, .pending:
                    break
                @unknown default:
                    break
                }
            } catch {
                print("Purchase failed: \(error)")
            }
        }
    }
    
    func restorePurchases() {
        Task {
            try? await AppStore.sync()
            await updatePremiumStatus()
        }
    }

    private func manageSubscriptions() {
        guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene else { return }
        Task {
            do {
                try await AppStore.showManageSubscriptions(in: windowScene)
            } catch {
                print("Failed to open subscription management")
            }
        }
    }

    func refreshPremiumStatus() {
        Task { await updatePremiumStatus() }
    }

    private func updatePremiumStatus() async {
        var hasActiveSubscription = false
        for await result in Transaction.currentEntitlements {
            do {
                let transaction = try checkVerified(result)
                if transaction.productType == .autoRenewable && transaction.revocationDate == nil {
                    if let expirationDate = transaction.expirationDate, expirationDate > Date() {
                        hasActiveSubscription = true
                    }
                }
            } catch {
                // Niezweryfikowana transakcja
            }
        }
        billingBridge?.updatePremiumStatus(isPremium: hasActiveSubscription)
    }

    private func listenForTransactions() -> Task<Void, Never> {
        return Task.detached {
            for await result in Transaction.updates {
                do {
                    let transaction = try await self.checkVerified(result)
                    await transaction.finish()
                    await self.updatePremiumStatus()
                } catch {
                    print("Transaction failed verification")
                }
            }
        }
    }

    private func checkVerified<T>(_ result: VerificationResult<T>) throws -> T {
        switch result {
        case .unverified:
            throw StoreError.failedVerification
        case .verified(let safe):
            return safe
        }
    }
    
    enum StoreError: Error {
        case failedVerification
    }
}

@main
struct iOSApp: App {
    init() {
        
        FirebaseApp.configure()
        
        //MobileAds.shared.requestConfiguration.testDeviceIdentifiers = [ "82f23e9a6a77246e35c57400f34c93d5" ]
        
        let adInlineID = Bundle.main.object(forInfoDictionaryKey: "AD_BANNER_INLINE_ID") as? String ?? ""
        let adBannerID = Bundle.main.object(forInfoDictionaryKey: "AD_BANNER_ID") as? String ?? ""
        
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
        
        AdMobConfigBridge.shared.showInterstitial = {
            InterstitialAdManager.shared.showAd()
        }

        AdMobConfigBridge.shared.showRewarded = {
            DispatchQueue.main.async {
                RewardedAdManager.shared.showAd()
            }
        }
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
                .onAppear {
                    ConsentManager.shared.gatherConsent {
                        ATTrackingManager.requestTrackingAuthorization { status in
                            DispatchQueue.main.async {
                                if ConsentInformation.shared.canRequestAds {
                                    MobileAds.shared.start(completionHandler: nil)
                                    
                                    let adInterstitialID = Bundle.main.object(forInfoDictionaryKey: "AD_FULLSCREEN_ID") as? String ?? ""
                                    InterstitialAdManager.shared.loadAd(adUnitID: adInterstitialID)
                                    RewardedAdManager.shared.loadAd()
                                }
                            }
                        }
                    }
                }
        }
    }
}
