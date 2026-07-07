package kalkulator.cnc

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import kalkulator.cnc.data.MainRepository
import kalkulator.cnc.data.local.AppDatabase
import kalkulator.cnc.data.viewmodel.AppViewModel
import kalkulator.cnc.remote.RemoteClient
import kalkulator.cnc.ui.screens.AddToolScreen
import kalkulator.cnc.ui.screens.BillingController
import kalkulator.cnc.ui.screens.MainScreen
import kalkulator.cnc.ui.screens.MillingScreen
import kalkulator.cnc.ui.screens.MyToolsScreen
import kalkulator.cnc.ui.screens.NewsScreen
import kalkulator.cnc.ui.screens.PrivacyPolicyScreen
import kalkulator.cnc.ui.screens.SettingsScreen
import kalkulator.cnc.ui.screens.SplashScreen
import kalkulator.cnc.ui.screens.TurningScreen
import kalkulator.cnc.ui.screens.SubscriptionScreen
import kalkulator.cnc.ui.screens.TablesScreen
import kalkulator.cnc.ui.screens.TolerancesScreen

sealed class Screen {
    object Main : Screen()
    object Milling : Screen()
    object Turning : Screen()
    object News : Screen()
    object Settings : Screen()
    object Subscription : Screen()
    object Tables : Screen()
    object Tolerances : Screen()
    object Privacy : Screen()
    object Tools : Screen()
    data class AddTools(val toolId: Int? = null) : Screen()
    object Splash : Screen()
}

private var isCoilInitialized = false

@Composable
fun App(
    database: AppDatabase,
    billingController: BillingController? = null,
    adContent: @Composable () -> Unit = {},
    showInterstitialAd: () -> Unit = {},
    onRequestConsent: (onComplete: () -> Unit) -> Unit = { onComplete -> onComplete() },
    onShowStartAd: (onComplete: () -> Unit) -> Unit = { it() }
) {
    if (!isCoilInitialized) {
        setSingletonImageLoaderFactory { context ->
            ImageLoader.Builder(context)
                .components {
                    add(KtorNetworkFetcherFactory())
                }
                .crossfade(true)
                .build()
        }
        isCoilInitialized = true
    }

    val repository = remember {
        MainRepository(
            apiService = RemoteClient.apiService,
            cncToolDao = database.cncToolDao(),
            subscriptionDao = database.subscriptionDao(),
            threadDao = database.threadDao(),
            userToolDao = database.userToolDao()
        )
    }

    val viewModel = remember { AppViewModel(repository) }
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (val screen = currentScreen) {
            is Screen.Splash -> {
                SplashScreen(
                    viewModel = viewModel,
                    onRequestConsent = onRequestConsent,
                    onShowStartAd = onShowStartAd,
                    onNavigateToMain = { currentScreen = Screen.Main }
                )
            }

            is Screen.Main -> {
                MainScreen(
                    onNavigateToMilling = { currentScreen = Screen.Milling },
                    onNavigateToTurning = { currentScreen = Screen.Turning },
                    onNavigateToTools = { currentScreen = Screen.Tools },
                    onNavigateToTables = { currentScreen = Screen.Tables },
                    onNavigateToTolerances = { currentScreen = Screen.Tolerances },
                    onNavigateToSettings = { currentScreen = Screen.Settings },
                    onNavigateToNews = { currentScreen = Screen.News },
                    onNavigateToSubscription = { currentScreen = Screen.Subscription },
                    hasNewNews = false,
                    adContent = adContent
                )
            }

            is Screen.Milling -> {
                MillingScreen(
                    onBack = { currentScreen = Screen.Main },
                    repository = repository,
                    adContent = adContent,
                    showInterstitialAd = showInterstitialAd
                )
            }
            is Screen.Turning -> {
                TurningScreen(
                    onBack = { currentScreen = Screen.Main },
                    repository = repository,
                    adContent = adContent,
                    showInterstitialAd = showInterstitialAd
                )
            }
            is Screen.News -> {
                NewsScreen(
                    onBack = { currentScreen = Screen.Main },
                    repository = repository,
                    adContent = adContent
                )
            }
            is Screen.Settings -> {
                SettingsScreen(
                    onBack = { currentScreen = Screen.Main },
                    onNavigateToPremium = { currentScreen = Screen.Subscription },
                    onNavigateToPrivacy = { currentScreen = Screen.Privacy },
                    onLanguageChanged = { currentScreen = Screen.Splash },
                    repository = repository,
                    adContent = adContent
                )
            }
            is Screen.Subscription -> {
                SubscriptionScreen(
                    onBack = { currentScreen = Screen.Settings },
                    repository = repository,
                    billingController = billingController
                )
            }
            is Screen.Tables -> {
                TablesScreen(
                    onBack = { currentScreen = Screen.Main },
                    repository = repository,
                    adContent = adContent,
                    showInterstitialAd = showInterstitialAd
                )
            }
            is Screen.Tolerances -> {
                TolerancesScreen(
                    onBack = { currentScreen = Screen.Main },
                    repository = repository,
                    onNavigateToPremium = { currentScreen = Screen.Subscription },
                    adContent = adContent,
                    showInterstitialAd = showInterstitialAd
                )
            }
            is Screen.Privacy -> {
                PrivacyPolicyScreen(
                    onBack = { currentScreen = Screen.Settings },
                    viewModel = viewModel
                )
            }
            is Screen.Tools ->{
                MyToolsScreen(
                    onBack = { currentScreen = Screen.Main },
                    repository = repository,
                    onNavigateToAddTool = { id -> currentScreen = Screen.AddTools(id) },
                    onNavigateToPremium = { currentScreen = Screen.Subscription },
                    adContent = adContent
                )
            }
            is Screen.AddTools -> {
                AddToolScreen(
                    toolId = screen.toolId,
                    onBack = { currentScreen = Screen.Tools },
                    repository = repository,
                    adContent = adContent
                )
            }
        }
    }
}