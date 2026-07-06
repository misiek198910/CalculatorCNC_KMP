package kalkulator.cnc

import androidx.compose.ui.window.ComposeUIViewController
import kalkulator.cnc.data.local.createRoomDatabase
import kalkulator.cnc.data.local.getDatabaseBuilder
import kalkulator.cnc.ui.components.AdBanner

@Suppress("UNUSED")
fun MainViewController() = ComposeUIViewController {

    val database = createRoomDatabase(getDatabaseBuilder())

    App(
        database = database,
        adContent = {
            AdBanner(isPremium = false)
        },
                showInterstitialAd = {
            AdMobConfigBridge.showInterstitial?.invoke()
        }
    )
}