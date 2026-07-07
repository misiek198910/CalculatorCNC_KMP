package kalkulator.cnc.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.analytics.analytics
import dev.gitlive.firebase.analytics.logEvent

@Composable
fun LogScreenView(screenName: String) {
    LaunchedEffect(screenName) {
        try {
            Firebase.analytics.logEvent("screen_view") {
                param("screen_name", screenName)
                param("screen_class", screenName)
            }
            println("[ANALYTICS] Zarejestrowano widok: $screenName")
        } catch (e: Exception) {
            println("[ANALYTICS] Błąd logowania ekranu: ${e.message}")
        }
    }
}