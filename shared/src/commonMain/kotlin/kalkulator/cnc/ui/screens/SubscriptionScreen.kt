package kalkulator.cnc.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import calculatorcnc.shared.generated.resources.*
import kalkulator.cnc.ads.AdFreeManager
import kalkulator.cnc.data.MainRepository
import kalkulator.cnc.ui.theme.ApplicationNewBlue
import kalkulator.cnc.utils.LogScreenView
import kalkulator.cnc.utils.termsOfServiceUrl
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
interface BillingController {
    val monthlyPrice: StateFlow<String>
    val yearlyPrice: StateFlow<String>
    fun buyMonthly()
    fun buyYearly()
    fun restore()
    fun manage()
}

@Composable
fun SubscriptionScreen(
    onBack: () -> Unit,
    repository: MainRepository,
    billingController: BillingController?,
    showRewardedAd: (onResult: (rewarded: Boolean) -> Unit) -> Unit = { it(false) }
) {

    LogScreenView(screenName = "SubscriptionScreen")

    val isPremium by repository.isPremiumFlow.collectAsState(initial = false)
    val loadingText = stringResource(Res.string.loading)
    val monthlyPriceRaw by billingController?.monthlyPrice?.collectAsState() ?: remember { mutableStateOf("") }
    val yearlyPriceRaw by billingController?.yearlyPrice?.collectAsState() ?: remember { mutableStateOf("") }
    val monthlyPrice = monthlyPriceRaw.ifBlank { loadingText }
    val yearlyPrice = yearlyPriceRaw.ifBlank { loadingText }
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(Res.drawable.background_portrait),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ApplicationNewBlue.copy(alpha = 0.80f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ApplicationNewBlue)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Image(
                    painter = painterResource(Res.drawable.ads),
                    contentDescription = null,
                    modifier = Modifier.size(35.dp).padding(start = 8.dp)
                )
                Text(
                    text = stringResource(Res.string.settings_subs),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xE61A1A1A)),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color(0xFF1A5276)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(Res.string.settings_subs),
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = stringResource(Res.string.text_info1),
                            color = Color(0xFFBDBDBD),
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 12.dp, bottom = 24.dp)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x33000000))
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFFFFD700)) // Gold
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(stringResource(Res.string.no_ads), color = Color.White, fontSize = 16.sp)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFFFFD700))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(stringResource(Res.string.premium_function), color = Color.White, fontSize = 16.sp)
                                }
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 24.dp),
                            color = Color(0x33FFFFFF)
                        )

                        Text(
                            text = stringResource(Res.string.status),
                            color = Color(0xFFBDBDBD),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = if (isPremium) stringResource(Res.string.subs_active) else stringResource(Res.string.subs_deactive),
                            color = if (isPremium) Color(0xFF4CAF50) else Color(0xFFFF5252),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(32.dp))
                        
                        if (billingController == null) {
                            Text(
                                text = "System subskrypcji dla systemu iOS jest w trakcie przygotowywania. Funkcje premium będą wkrótce dostępne.",
                                color = Color(0xFFBDBDBD),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 16.dp)
                            )

                            if (!isPremium) {
                                RewardedAdOffer(showRewardedAd = showRewardedAd)
                            }
                        } else {
                            if (isPremium) {
                                Button(
                                    onClick = { billingController.manage() },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ApplicationNewBlue),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(stringResource(Res.string.settings_subs), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = { billingController.buyMonthly() },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ApplicationNewBlue),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(stringResource(Res.string.subs_monthly_button), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                        Text(monthlyPrice, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = { billingController.buyYearly() },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ApplicationNewBlue),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(stringResource(Res.string.subs_annual_button), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                        Text(yearlyPrice, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                TextButton(
                                    onClick = { billingController.restore() },
                                    modifier = Modifier.fillMaxWidth().height(50.dp)
                                ) {
                                    Text(stringResource(Res.string.buy_back), color = Color.White, fontSize = 14.sp)
                                }

                                RewardedAdOffer(showRewardedAd = showRewardedAd)

                                Text(
                                    text = stringResource(Res.string.subs_info),
                                    color = Color(0xFF9E9E9E),
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp, start = 16.dp, end = 16.dp),
                                    lineHeight = 14.sp
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(onClick = { uriHandler.openUri(termsOfServiceUrl()) }) {
                                        Text(stringResource(Res.string.statute), color = Color(0xFFBDBDBD), fontSize = 10.sp)
                                    }
                                    Text(
                                        text = "|",
                                        color = Color(0xFFBDBDBD),
                                        fontSize = 10.sp
                                    )
                                    TextButton(onClick = { uriHandler.openUri("https://privacypolicy.mivs.dev/kalkulator-cnc.html") }) {
                                        Text(stringResource(Res.string.privacy), color = Color(0xFFBDBDBD), fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RewardedAdOffer(showRewardedAd: (onResult: (rewarded: Boolean) -> Unit) -> Unit) {
    val adFreeUntil by AdFreeManager.adFreeUntil.collectAsState()
    val isAdFree by AdFreeManager.isAdFreeFlow.collectAsState(initial = AdFreeManager.isAdFreeNow())
    var isLoading by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    val grantedText = stringResource(Res.string.reward_granted)
    val unavailableText = stringResource(Res.string.reward_unavailable)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x33000000))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(Res.string.reward_title),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(Res.string.reward_desc),
            color = Color(0xFFBDBDBD),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
        )

        if (isAdFree) {
            Text(
                text = stringResource(Res.string.reward_active_until, formatDateTime(adFreeUntil)),
                color = Color(0xFF4CAF50),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        } else {
            OutlinedButton(
                onClick = {
                    isLoading = true
                    resultMessage = null
                    showRewardedAd { rewarded ->
                        isLoading = false
                        if (rewarded) {
                            AdFreeManager.grantReward()
                            resultMessage = grantedText
                        } else {
                            resultMessage = unavailableText
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                border = BorderStroke(1.dp, Color(0xFFFFD700)),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color(0xFFFFD700),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(Res.string.reward_loading), color = Color.White, fontSize = 14.sp)
                } else {
                    Text(stringResource(Res.string.reward_button), color = Color(0xFFFFD700), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        resultMessage?.let {
            Text(
                text = it,
                color = Color(0xFFBDBDBD),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

private fun formatDateTime(epochMillis: Long): String {
    val dt = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.currentSystemDefault())
    fun Int.pad() = toString().padStart(2, '0')
    return "${dt.dayOfMonth.pad()}.${dt.monthNumber.pad()} ${dt.hour.pad()}:${dt.minute.pad()}"
}
