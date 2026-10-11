package kalkulator.cnc

import androidx.compose.ui.window.ComposeUIViewController
import kalkulator.cnc.data.local.sharedDatabase
import kalkulator.cnc.ui.components.AdBanner
import kalkulator.cnc.ui.screens.BillingController // Dodany import

@Suppress("UNUSED")
fun MainViewController(
    billingController: BillingController,
    onShowStartAd: (onComplete: () -> Unit) -> Unit
) = ComposeUIViewController {

    App(
        database = sharedDatabase,
        billingController = billingController,
        adContent = {
            AdBanner(isPremium = false)
        },
        showInterstitialAd = {
            AdMobConfigBridge.showInterstitial?.invoke()
        },
        showRewardedAd = { onResult ->
            AdMobConfigBridge.requestRewarded(onResult)
        },
        onShowStartAd = onShowStartAd
    )
}