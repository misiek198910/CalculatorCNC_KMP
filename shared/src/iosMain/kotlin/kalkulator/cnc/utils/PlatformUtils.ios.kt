package kalkulator.cnc.utils

import platform.Foundation.NSBundle
import platform.Foundation.NSURL
import platform.Foundation.NSUserDefaults
import platform.StoreKit.SKStoreReviewController
import platform.UIKit.UIApplication
import platform.UIKit.UIWindowScene

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

actual fun saveAdFreeUntil(epochMillis: Long) {
    NSUserDefaults.standardUserDefaults.setInteger(epochMillis, forKey = "ad_free_until")
    NSUserDefaults.standardUserDefaults.synchronize()
}

actual fun getAdFreeUntil(): Long {
    return NSUserDefaults.standardUserDefaults.integerForKey("ad_free_until")
}

actual fun openStoreRating() {
    // Z ID z App Store otwieramy stronę pisania opinii - systemowe okno ma limit wyświetleń i może się nie pokazać
    val appStoreId = (NSBundle.mainBundle.objectForInfoDictionaryKey("APP_STORE_ID") as? String)
        ?.trim()
        .orEmpty()
    if (appStoreId.isNotEmpty()) {
        val url = NSURL.URLWithString("https://apps.apple.com/app/id$appStoreId?action=write-review")
        if (url != null) {
            UIApplication.sharedApplication.openURL(url, options = emptyMap<Any?, Any>(), completionHandler = null)
            return
        }
    }

    val scene = UIApplication.sharedApplication.connectedScenes
        .firstOrNull { it is UIWindowScene } as? UIWindowScene
    if (scene != null) {
        SKStoreReviewController.requestReviewInScene(scene)
    }
}

actual fun termsOfServiceUrl(): String = "https://www.apple.com/legal/internet-services/itunes/dev/stdeula/"
