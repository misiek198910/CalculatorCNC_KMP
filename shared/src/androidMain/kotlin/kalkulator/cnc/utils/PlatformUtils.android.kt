package kalkulator.cnc.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.edit
import kalkulator.cnc.data.local.appContext

actual fun changeLanguage(languageIso: String) {
    val prefs = appContext.getSharedPreferences("CncSettings", Context.MODE_PRIVATE)

    prefs.edit(commit = true) { putString("language_iso", languageIso) }

    val intent = appContext.packageManager.getLaunchIntentForPackage(appContext.packageName)
    intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    appContext.startActivity(intent)

    android.os.Process.killProcess(android.os.Process.myPid())
}

actual fun getLanguagePreference(): String {
    val prefs = appContext.getSharedPreferences("CncSettings", Context.MODE_PRIVATE)
    return prefs.getString("language_iso", "pl") ?: "pl"
}

actual fun saveSystemPreference(systemIndex: Int) {
    val prefs = appContext.getSharedPreferences("CncSettings", Context.MODE_PRIVATE)
    // Używamy commit = true, tak jak przy języku, aby zapisać od razu
    prefs.edit(commit = true) { putInt("system_index", systemIndex) }
}

actual fun getSystemPreference(): Int {
    val prefs = appContext.getSharedPreferences("CncSettings", Context.MODE_PRIVATE)
    return prefs.getInt("system_index", 0) // 0 to domyślnie metryczny
}

actual fun saveAdFreeUntil(epochMillis: Long) {
    val prefs = appContext.getSharedPreferences("CncSettings", Context.MODE_PRIVATE)
    prefs.edit(commit = true) { putLong("ad_free_until", epochMillis) }
}

actual fun getAdFreeUntil(): Long {
    val prefs = appContext.getSharedPreferences("CncSettings", Context.MODE_PRIVATE)
    return prefs.getLong("ad_free_until", 0L)
}

actual fun openStoreRating() {
    val packageName = appContext.packageName
    val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        appContext.startActivity(marketIntent)
    } catch (e: ActivityNotFoundException) {
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        appContext.startActivity(webIntent)
    }
}

actual fun termsOfServiceUrl(): String = "https://play.google.com/about/play-terms/"
