package kalkulator.cnc

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kalkulator.cnc.billing.AndroidBillingController
import kalkulator.cnc.billing.BillingManager
import kalkulator.cnc.data.local.appContext
import kalkulator.cnc.data.local.createRoomDatabase
import kalkulator.cnc.data.local.getDatabaseBuilder
import kalkulator.cnc.ui.components.AdBanner
import java.util.Locale
import kalkulator.cnc.utils.UpdateHelper
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {

    private lateinit var billingManager: BillingManager

    private var mInterstitialAd: InterstitialAd? = null

    private lateinit var updateHelper: UpdateHelper

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

        appContext = applicationContext

        val database = createRoomDatabase(getDatabaseBuilder())
        billingManager = BillingManager(this, database.subscriptionDao())

        updateHelper = UpdateHelper(this)
        updateHelper.registerListener()
        updateHelper.checkForUpdates()


        setContent {

            val isPremiumState by billingManager.subscriptionStatus.collectAsState()
            val isPremium = isPremiumState == true
            val billingController = remember { AndroidBillingController(this, billingManager) }

            App(
                database = database,
                billingController = billingController,
                adContent = {
                    if (!isPremium) {
                        AdBanner(isPremium = false)
                    }
                },
                showInterstitialAd = {
                    if (!isPremium) {
                        showInterstitial()
                    }
                },
                onRequestConsent = { onComplete ->
                    val consentInformation =
                        UserMessagingPlatform.getConsentInformation(this@MainActivity)
                    val params = ConsentRequestParameters.Builder().build()

                    consentInformation.requestConsentInfoUpdate(
                        this@MainActivity,
                        params,
                        {
                            UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                                this@MainActivity
                            ) { formError ->
                                initializeMobileAds()
                                onComplete()
                            }
                        },
                        { requestConsentError ->
                            initializeMobileAds()
                            onComplete()
                        }
                    )
                },
                onShowStartAd = { onComplete ->

                    if (isPremium) {
                        onComplete()
                    } else {
                        var isCompleted = false

                        fun safeComplete() {
                            if (!isCompleted) {
                                isCompleted = true
                                onComplete()
                            }
                        }

                        val timeoutHandler = android.os.Handler(android.os.Looper.getMainLooper())
                        val timeoutRunnable = Runnable {
                            safeComplete()
                        }
                        timeoutHandler.postDelayed(timeoutRunnable, 4000L)

                        val request = AdRequest.Builder().build()

                        AppOpenAd.load(
                            this@MainActivity,
                            BuildConfig.AD_START_UNIT_ID,
                            request,
                            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                            object : AppOpenAdLoadCallback() {
                                override fun onAdLoaded(ad: AppOpenAd) {
                                    if (isCompleted) return

                                    timeoutHandler.removeCallbacks(timeoutRunnable)

                                    ad.fullScreenContentCallback =
                                        object : FullScreenContentCallback() {
                                            override fun onAdDismissedFullScreenContent() {
                                                safeComplete()
                                            }

                                            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                                                safeComplete()
                                            }
                                        }
                                    ad.show(this@MainActivity)
                                }

                                override fun onAdFailedToLoad(error: LoadAdError) {
                                    timeoutHandler.removeCallbacks(timeoutRunnable)
                                    safeComplete()
                                }
                            }
                        )
                    }
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (::updateHelper.isInitialized) {
            updateHelper.onResume()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::updateHelper.isInitialized) {
            updateHelper.unregisterListener()
        }
    }

    private fun initializeMobileAds() {
        MobileAds.initialize(this) {}
        loadInterstitialAd()
    }

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

    private fun showInterstitial() {
        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    mInterstitialAd = null
                    loadInterstitialAd()
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