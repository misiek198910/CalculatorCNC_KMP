package kalkulator.cnc.billing

import kalkulator.cnc.data.local.sharedDatabase
import kalkulator.cnc.entity.SubscriptionEntity
import kalkulator.cnc.ui.screens.BillingController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Ten kontroler tworzymy po stronie iOS (w pliku Swift)
class IosBillingController(
    private val onBuyMonthly: () -> Unit,
    private val onBuyYearly: () -> Unit,
    private val onRestore: () -> Unit,
    private val onManage: () -> Unit
) : BillingController {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _monthlyPrice = MutableStateFlow("")
    override val monthlyPrice: StateFlow<String> = _monthlyPrice.asStateFlow()

    private val _yearlyPrice = MutableStateFlow("")
    override val yearlyPrice: StateFlow<String> = _yearlyPrice.asStateFlow()

    // Funkcje wywoływane przez Swift w celu aktualizacji cen w UI
    fun updatePrices(monthly: String, yearly: String) {
        _monthlyPrice.value = monthly
        _yearlyPrice.value = yearly
    }

    // Wywoływane przez Swift po sprawdzeniu uprawnień w StoreKit - ten sam zapis co BillingManager na Androidzie
    fun updatePremiumStatus(isPremium: Boolean) {
        scope.launch {
            sharedDatabase.subscriptionDao().insertStatus(
                SubscriptionEntity(id = 1, isPremium = isPremium)
            )
        }
    }

    override fun buyMonthly() = onBuyMonthly()
    override fun buyYearly() = onBuyYearly()
    override fun restore() = onRestore()
    override fun manage() = onManage()
}