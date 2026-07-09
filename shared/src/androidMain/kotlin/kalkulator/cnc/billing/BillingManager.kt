package kalkulator.cnc.billing

import android.app.Activity
import android.content.Context
import calculatorcnc.shared.generated.resources.Res
import calculatorcnc.shared.generated.resources.*
import com.android.billingclient.api.*
import kalkulator.cnc.data.dao.SubscriptionDao
import kalkulator.cnc.entity.SubscriptionEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.jetbrains.compose.resources.getString

class BillingManager(
    private val context: Context,
    private val dao: SubscriptionDao
) {
    private val billingClient: BillingClient
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _productDetails = MutableStateFlow<ProductDetails?>(null)

    private val _subscriptionStatus = MutableStateFlow<Boolean?>(null)
    val subscriptionStatus = _subscriptionStatus.asStateFlow()

    val productDetails = _productDetails.asStateFlow()

    interface BillingManagerListener {
        fun onPurchaseAcknowledged()
        fun onPurchaseError(error: String?)
    }

    var listener: BillingManagerListener? = null

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) { handlePurchase(purchase) }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            listener?.onPurchaseError("Anulowano zakup.") // Przywrócono z oryginalnego pliku
        } else {
            listener?.onPurchaseError("Błąd: ${billingResult.debugMessage}")
        }
    }

    init {
        scope.launch {
            val savedStatus = dao.getStatus()
            _subscriptionStatus.value = savedStatus?.isPremium == true
        }
        billingClient = BillingClient.newBuilder(context.applicationContext)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
            .build()
        connectToGooglePlay()
    }

    private fun connectToGooglePlay() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryPurchasesAsync()
                    queryProductDetails()
                }
            }
            override fun onBillingServiceDisconnected() { connectToGooglePlay() }
        })
    }

    fun queryProductDetails() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SKU_REMOVE_ADS_YEAR)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params)
        { billingResult, queryProductDetailsResult ->
            val productDetailsList = queryProductDetailsResult.productDetailsList

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && !productDetailsList.isNullOrEmpty()) {
                _productDetails.value = productDetailsList[0]
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity, productDetails: ProductDetails, basePlanId: String) {
        val offerDetails = productDetails.subscriptionOfferDetails?.find { it.basePlanId == basePlanId }

        if (offerDetails == null) {
            listener?.onPurchaseError("Nie znaleziono wybranej oferty ($basePlanId).")
            return
        }

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .setOfferToken(offerDetails.offerToken)
                    .build()
            )).build()

        billingClient.launchBillingFlow(activity, flowParams)
    }

    fun queryPurchasesAsync() {
        if (!billingClient.isReady) return
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                var hasPremium = false
                var token: String? = null
                purchases.forEach { purchase ->
                    if (purchase.products.contains(SKU_REMOVE_ADS_YEAR) && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        hasPremium = true
                        token = purchase.purchaseToken
                        if (!purchase.isAcknowledged) handlePurchase(purchase)
                    }
                }
                updateLocalStatus(hasPremium, token)
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
            billingClient.acknowledgePurchase(params) { result ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    updateLocalStatus(true, purchase.purchaseToken)
                    listener?.onPurchaseAcknowledged()
                } else {
                    listener?.onPurchaseError("Błąd autoryzacji: ${result.debugMessage}")
                }
            }
        } else if (purchase.isAcknowledged) {
            updateLocalStatus(true, purchase.purchaseToken)
        }
    }

    private fun updateLocalStatus(hasPremium: Boolean, token: String?) {
        _subscriptionStatus.value = hasPremium
        scope.launch {
            dao.insertStatus(
                SubscriptionEntity(
                    id = 1,
                    isPremium = hasPremium,
                    purchaseToken = token
                )
            )
        }
    }

    suspend fun getPlanOfferInfo(productDetails: ProductDetails?, basePlanId: String): String {
        val offer = productDetails?.subscriptionOfferDetails?.find { it.basePlanId == basePlanId }
        val trialPhase = offer?.pricingPhases?.pricingPhaseList?.find { it.priceAmountMicros == 0L }
        val basePhase = offer?.pricingPhases?.pricingPhaseList?.lastOrNull()
        val price = basePhase?.formattedPrice ?: ""

        return when {
            basePlanId == BASE_PLAN_YEARLY_TRIAL && trialPhase != null && basePhase != null -> {
                // Łączenie stringów bezpieczne dla Compose Multiplatform (bez formatowania %s)
                getString(Res.string.subs_trial_button) + " " + price
            }
            basePlanId == SKU_REMOVE_ADS_MONTH && basePhase != null -> {
                getString(Res.string.subs_monthly_button) + " " + price
            }
            basePlanId == BASE_PLAN_YEARLY_TRIAL && basePhase != null -> {
                getString(Res.string.subs_annual_button) + " " + price
            }
            else -> getString(Res.string.subs_buy_button)
        }
    }

    companion object {
        const val SKU_REMOVE_ADS_YEAR = "remove_ads_for_year"
        const val BASE_PLAN_YEARLY_TRIAL = "cnc-premium-auto"
        const val SKU_REMOVE_ADS_MONTH = "subscription-month"
    }
}