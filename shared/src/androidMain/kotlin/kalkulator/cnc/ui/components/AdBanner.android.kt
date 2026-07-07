package kalkulator.cnc.ui.components

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import kalkulator.cnc.shared.BuildConfig

@SuppressLint("MissingPermission")
@Composable
actual fun AdBanner(modifier: Modifier, isPremium: Boolean) {
    if (isPremium) return
    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            factory = { ctx ->
                AdView(ctx).apply {
                    val density = resources.displayMetrics.density
                    val adWidth = (resources.displayMetrics.widthPixels / density).toInt()
                    setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(ctx, adWidth))
                    adUnitId = BuildConfig.AD_BANNER_ID
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            Log.e(
                                "AdMobBanner",
                                "BŁĄD: Baner nie załadował się! Kod: ${error.code}, Wiadomość: ${error.message}"
                            )
                        }
                    }

                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}