package kalkulator.cnc.billing

import android.app.Activity
import android.content.Intent
import androidx.core.net.toUri
import kalkulator.cnc.ui.screens.BillingController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class AndroidBillingController(
    private val activity: Activity,
    private val billingManager: BillingManager
) : BillingController {

    private val scope = CoroutineScope(Dispatchers.Main)

    override val monthlyPrice = MutableStateFlow("")
    override val yearlyPrice = MutableStateFlow("")

    init {
        scope.launch {
            billingManager.productDetails.collect { details ->
                if (details != null) {
                    monthlyPrice.value = billingManager.getPlanOfferInfo(details, BillingManager.SKU_REMOVE_ADS_MONTH)
                    yearlyPrice.value = billingManager.getPlanOfferInfo(details, BillingManager.BASE_PLAN_YEARLY_TRIAL)
                }
            }
        }
    }

    override fun buyMonthly() {
        billingManager.productDetails.value?.let { details ->
            billingManager.launchPurchaseFlow(activity, details, BillingManager.SKU_REMOVE_ADS_MONTH)
        }
    }

    override fun buyYearly() {
        billingManager.productDetails.value?.let { details ->
            billingManager.launchPurchaseFlow(activity, details, BillingManager.BASE_PLAN_YEARLY_TRIAL)
        }
    }

    override fun restore() {
        billingManager.queryPurchasesAsync()
    }

    override fun manage() {
        val url = "https://play.google.com/store/account/subscriptions?package=${activity.packageName}"
        activity.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    }
}