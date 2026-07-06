package kalkulator.cnc.ui.components

import android.annotation.SuppressLint
import android.util.DisplayMetrics
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import kalkulator.cnc.shared.BuildConfig

@SuppressLint("MissingPermission")
@Composable
actual fun AdMobInline(modifier: Modifier) {
    AndroidView(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        factory = { context ->
            AdView(context).apply {
                val displayMetrics: DisplayMetrics = context.resources.displayMetrics
                val adWidth = (displayMetrics.widthPixels / displayMetrics.density).toInt()

                setAdSize(AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(context, adWidth))

                adUnitId = BuildConfig.ADMOB_INLINE_ID
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}