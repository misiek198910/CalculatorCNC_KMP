package kalkulator.cnc.utils

import platform.Foundation.NSUserDefaults

actual fun changeLanguage(languageIso: String) {

    NSUserDefaults.standardUserDefaults.setObject(languageIso, forKey = "language_iso")
    NSUserDefaults.standardUserDefaults.setObject(listOf(languageIso), forKey = "AppleLanguages")
    NSUserDefaults.standardUserDefaults.synchronize()
}

actual fun getLanguagePreference(): String {
    return NSUserDefaults.standardUserDefaults.stringForKey("language_iso") ?: "pl"
}

actual fun saveSystemPreference(systemIndex: Int) {
    platform.Foundation.NSUserDefaults.standardUserDefaults.setInteger(systemIndex.toLong(), forKey = "system_index")
    platform.Foundation.NSUserDefaults.standardUserDefaults.synchronize()
}

actual fun getSystemPreference(): Int {
    return platform.Foundation.NSUserDefaults.standardUserDefaults.integerForKey("system_index").toInt()
}