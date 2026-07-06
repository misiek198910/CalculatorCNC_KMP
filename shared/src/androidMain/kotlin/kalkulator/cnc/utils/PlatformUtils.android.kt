package kalkulator.cnc.utils

import android.content.Context
import android.content.Intent
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