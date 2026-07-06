package kalkulator.cnc

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kalkulator.cnc.billing.AndroidBillingController
import kalkulator.cnc.billing.BillingManager
import kalkulator.cnc.data.local.appContext
import kalkulator.cnc.data.local.createRoomDatabase
import kalkulator.cnc.data.local.getDatabaseBuilder
import kalkulator.cnc.ui.components.AdBanner
import java.util.Locale

class MainActivity : ComponentActivity() {
    private lateinit var billingManager: BillingManager
    private var mInterstitialAd: InterstitialAd? = null

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("CncSettings", Context.MODE_PRIVATE)
        val lang = prefs.getString("language_iso", "pl") ?: "pl"

        val locale = Locale.forLanguageTag(lang)
        Locale.setDefault(locale)

        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)

        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        MobileAds.initialize(this) {}
        loadInterstitialAd() // Pierwsze załadowanie reklamy

        appContext = applicationContext

        val database = createRoomDatabase(getDatabaseBuilder())
        billingManager = BillingManager(this, database.subscriptionDao())

        setContent {
            val billingController = remember { AndroidBillingController(this, billingManager) }

            App(
                database = database,
                billingController = billingController,
                adContent = {
                    AdBanner(isPremium = false)
                },
                showInterstitialAd = { showInterstitial() }
            )
        }
    }

    // Metoda pobierająca reklamę pełnoekranową
    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            this,
            BuildConfig.ADMOB_FULLSCREEN_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                }
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                }
            }
        )
    }

    // Metoda wyświetlająca reklamę
    private fun showInterstitial() {
        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    mInterstitialAd = null
                    loadInterstitialAd() // Załaduj kolejną po zamknięciu
                }
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    mInterstitialAd = null
                }
            }
            mInterstitialAd?.show(this)
        } else {
            loadInterstitialAd()
        }
    }
}

@Composable
fun AdMobBanner(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = BuildConfig.AD_BANNER_ID
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}