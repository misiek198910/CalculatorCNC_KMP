package kalkulator.cnc.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import kalkulator.cnc.data.MainRepository
import kalkulator.cnc.ui.theme.ApplicationNewBlue
import kotlinx.coroutines.flow.StateFlow
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
    billingController: BillingController? // Na iOS przekażemy null!
) {
    val isPremium by repository.isPremiumFlow.collectAsState(initial = false)

    // Odczytywanie cen z kontrolera (jeśli istnieje)
    val monthlyPrice by billingController?.monthlyPrice?.collectAsState(initial = "Ładowanie...") ?: remember { mutableStateOf("") }
    val yearlyPrice by billingController?.yearlyPrice?.collectAsState(initial = "Ładowanie...") ?: remember { mutableStateOf("") }

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
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ApplicationNewBlue)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
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

                        // Lista korzyści
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x33000000)) // Odpowiednik bg_search_input
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

                        // 🔥 ZAŚLEPKA DLA iOS LUB LOGIKA ANDROIDA
                        if (billingController == null) {
                            Text(
                                text = "System subskrypcji dla systemu iOS jest w trakcie przygotowywania. Funkcje premium będą wkrótce dostępne.",
                                color = Color(0xFFBDBDBD),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 16.dp)
                            )
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
                                    Text(monthlyPrice, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = { billingController.buyYearly() },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ApplicationNewBlue),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(yearlyPrice, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                TextButton(
                                    onClick = { billingController.restore() },
                                    modifier = Modifier.fillMaxWidth().height(50.dp)
                                ) {
                                    Text(stringResource(Res.string.buy_back), color = Color.White, fontSize = 14.sp)
                                }

                                Text(
                                    text = stringResource(Res.string.subs_info),
                                    color = Color(0xFF9E9E9E),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}