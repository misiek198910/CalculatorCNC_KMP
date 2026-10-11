package kalkulator.cnc.utils

expect fun changeLanguage(languageIso: String)
expect fun getLanguagePreference(): String

expect fun saveSystemPreference(systemIndex: Int)

expect fun getSystemPreference(): Int

expect fun saveAdFreeUntil(epochMillis: Long)

expect fun getAdFreeUntil(): Long

expect fun openStoreRating()

expect fun termsOfServiceUrl(): String
