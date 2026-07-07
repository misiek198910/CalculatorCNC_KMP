package kalkulator.cnc

import androidx.compose.runtime.remember // <--- PAMIĘTAJ O IMPORCIE
import androidx.compose.ui.window.ComposeUIViewController
import kalkulator.cnc.data.local.createRoomDatabase
import kalkulator.cnc.data.local.getDatabaseBuilder
import kalkulator.cnc.ui.components.AdBanner

@Suppress("UNUSED")
fun MainViewController(
    onShowStartAd: (onComplete: () -> Unit) -> Unit
) = ComposeUIViewController {

    val database = remember { createRoomDatabase(getDatabaseBuilder()) }

    App(
        database = database,
        adContent = {
            AdBanner(isPremium = false)
        },
        showInterstitialAd = {
            AdMobConfigBridge.showInterstitial?.invoke()
        },
        onShowStartAd = onShowStartAd
    )
}