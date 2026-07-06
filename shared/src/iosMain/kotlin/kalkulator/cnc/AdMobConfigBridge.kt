package kalkulator.cnc

object AdMobConfigBridge {
    var createInlineBanner: (() -> Any)? = null
    var showInterstitial: (() -> Unit)? = null
}