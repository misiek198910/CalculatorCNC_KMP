package kalkulator.cnc

object AdMobConfigBridge {
    var createInlineBanner: (() -> Any)? = null
    var showInterstitial: (() -> Unit)? = null

    // Swift wyświetla reklamę z nagrodą i zgłasza wynik przez onRewardedResult
    var showRewarded: (() -> Unit)? = null
    private var pendingRewardedResult: ((Boolean) -> Unit)? = null

    fun requestRewarded(onResult: (Boolean) -> Unit) {
        val show = showRewarded
        if (show == null) {
            onResult(false)
            return
        }
        pendingRewardedResult?.invoke(false)
        pendingRewardedResult = onResult
        show()
    }

    fun onRewardedResult(rewarded: Boolean) {
        val callback = pendingRewardedResult
        pendingRewardedResult = null
        callback?.invoke(rewarded)
    }
}
