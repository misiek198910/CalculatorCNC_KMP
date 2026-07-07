package kalkulator.cnc.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import calculatorcnc.shared.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.Dp
import kalkulator.cnc.utils.LogScreenView
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val ApplicationNewBlue = Color(0xFF0A192F)

data class MenuCardData(
    val titleRes: StringResource,
    val iconRes: DrawableResource,
    val cardHeight: Dp,
    val iconSize: Dp,
    val showRedDot: Boolean = false,
    val onClick: () -> Unit
)

@Composable
fun MainScreen(
    onNavigateToMilling: () -> Unit,
    onNavigateToTurning: () -> Unit,
    onNavigateToTools: () -> Unit,
    onNavigateToTables: () -> Unit,
    onNavigateToTolerances: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToNews: () -> Unit,
    onNavigateToSubscription: () -> Unit,
    hasNewNews: Boolean,
    adContent: @Composable () -> Unit = {}
) {
    LogScreenView(screenName = "MainScreen")

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight

        val backgroundImage = if (isLandscape) {
            Res.drawable.background_landscape
        } else {
            Res.drawable.background_portrait
        }

        Box(modifier = Modifier.fillMaxSize()) {

            Image(
                painter = painterResource(backgroundImage),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ApplicationNewBlue.copy(alpha = 0.80f))
            )

            // 3. Główna zawartość
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars)
            ) {

                val mainCardHeight = if (isLandscape) 100.dp else 140.dp
                val secondaryCardHeight = if (isLandscape) 80.dp else 100.dp
                val iconSize = if (isLandscape) 48.dp else 60.dp
                val smallIconSize = if (isLandscape) 28.dp else 32.dp

                val cards = listOf(
                    MenuCardData(Res.string.main_button1, Res.drawable.spindle, mainCardHeight, iconSize, false, onNavigateToMilling),
                    MenuCardData(Res.string.main_button2, Res.drawable.turning, mainCardHeight, iconSize, false, onNavigateToTurning),
                    MenuCardData(Res.string.main_button3, Res.drawable.tools, mainCardHeight, iconSize, false, onNavigateToTools),
                    MenuCardData(Res.string.main_button4, Res.drawable.tolerances, mainCardHeight, iconSize, false, onNavigateToTables),
                    MenuCardData(Res.string.tolerances, Res.drawable.tolerances_iso, mainCardHeight, iconSize, false, onNavigateToTolerances),
                    MenuCardData(Res.string.main_button6, Res.drawable.settings, mainCardHeight, iconSize, false, onNavigateToSettings),
                    MenuCardData(Res.string.news, Res.drawable.notification, secondaryCardHeight, smallIconSize, hasNewNews, onNavigateToNews),
                    MenuCardData(Res.string.premium_function, Res.drawable.ads, secondaryCardHeight, smallIconSize, false, onNavigateToSubscription)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(if (isLandscape) 4 else 2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    items(cards) { card ->
                        XmlStyleDashboardCard(card)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x1A000000))
                        .defaultMinSize(minHeight = 50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    adContent()
                }
            }
        }
    }
}

@Composable
fun XmlStyleDashboardCard(card: MenuCardData) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(card.cardHeight)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x1AFFFFFF))
            .border(
                BorderStroke(1.dp, Color(0x33FFFFFF)),
                RoundedCornerShape(16.dp)
            )
            .clickable { card.onClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(card.iconRes),
                contentDescription = null,
                modifier = Modifier.size(card.iconSize),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(card.titleRes),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        if (card.showRedDot) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 12.dp, end = 12.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color.Red)
            )
        }
    }
}