package kalkulator.cnc.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import calculatorcnc.shared.generated.resources.*
import calculatorcnc.shared.generated.resources.Res
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kalkulator.cnc.ui.theme.ApplicationNewBlue
import kalkulator.cnc.utils.changeLanguage
import org.jetbrains.compose.resources.stringArrayResource
import kalkulator.cnc.utils.getLanguagePreference
import kalkulator.cnc.utils.getSystemPreference
import kalkulator.cnc.utils.saveSystemPreference

@Composable
fun SettingsScreen(onBack: () -> Unit, onNavigateToPremium: () -> Unit, onNavigateToPrivacy: () -> Unit, adContent: @Composable () -> Unit = {}) {
    val supportedLanguages = listOf(
        "pl" to "Polski",
        "en" to "English"
    )

    val uriHandler = LocalUriHandler.current

    var currentLanguageTag by remember { mutableStateOf(getLanguagePreference()) }
    var currentSystemIndex by remember { mutableStateOf(getSystemPreference()) }
    val currentLanguageIndex = supportedLanguages.indexOfFirst { it.first == currentLanguageTag }.coerceAtLeast(0)

    var showSystemDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    val systemOptions = stringArrayResource(Res.array.spinner1_items)
    val languageOptions = supportedLanguages.map { it.second }

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
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(Res.string.back),
                        tint = Color.White
                    )
                }
                Image(
                    painter = painterResource(Res.drawable.settings),
                    contentDescription = null,
                    modifier = Modifier.size(35.dp).padding(start = 8.dp)
                )
                Text(
                    text = stringResource(Res.string.main_button6),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {

                item {
                    SettingsCard(
                        iconRes = Res.drawable.calc,
                        title = stringResource(Res.string.calc_system),
                        subtitle = if (systemOptions.isNotEmpty()) systemOptions[currentSystemIndex] else "",
                        onClick = { showSystemDialog = true }
                    )
                }

                item {
                    SettingsCard(
                        iconRes = Res.drawable.language,
                        title = stringResource(Res.string.language),
                        subtitle = supportedLanguages[currentLanguageIndex].second,
                        onClick = { showLanguageDialog = true }
                    )
                }

                item {
                    SettingsCard(
                        iconRes = Res.drawable.privacy,
                        title = stringResource(Res.string.privacy),
                        onClick = onNavigateToPrivacy
                    )
                }

                item {
                    SettingsCard(
                        iconRes = Res.drawable.rate,
                        title = stringResource(Res.string.settings_button3),
                        onClick = {
                            uriHandler.openUri("https://play.google.com/store/apps/details?id=kalkulator.cnc")
                        }
                    )
                }

                item {
                    SettingsCard(
                        iconRes = Res.drawable.coffe,
                        title = stringResource(Res.string.settings_button4),
                        onClick = { uriHandler.openUri("https://buycoffee.to/mivs/MojaParafia") }
                    )
                }

                item {
                    SettingsCard(
                        iconRes = Res.drawable.ads,
                        title = stringResource(Res.string.settings_button5),
                        onClick = onNavigateToPremium
                    )
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

    // Okna dialogowe
    if (showSystemDialog) {
        CustomSelectionDialog(
            title = stringResource(Res.string.calc_system),
            options = systemOptions,
            selectedIndex = currentSystemIndex,
            onSelect = { index ->
                currentSystemIndex = index
                saveSystemPreference(index)
                showSystemDialog = false
            },
            onDismiss = { showSystemDialog = false }
        )
    }

    if (showLanguageDialog) {
        CustomSelectionDialog(
            title = stringResource(Res.string.language),
            options = languageOptions,
            selectedIndex = currentLanguageIndex,
            onSelect = { index ->
                val selectedTag = supportedLanguages[index].first

                if (selectedTag != currentLanguageTag) {
                    currentLanguageTag = selectedTag

                    changeLanguage(currentLanguageTag)
                }
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }
}

@Composable
fun SettingsCard(
    iconRes: org.jetbrains.compose.resources.DrawableResource,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x1AFFFFFF))
            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(50.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = Color(0xFFBDBDBD),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun CustomSelectionDialog(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ApplicationNewBlue,
        title = {
            Text(text = title, color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                options.forEachIndexed { index, option ->
                    val isSelected = index == selectedIndex
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0x33FFFFFF) else Color.Transparent)
                            .clickable { onSelect(index) }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = option,
                            color = if (isSelected) Color(0xFFFF9800) else Color.White,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 18.sp
                        )
                    }
                    if (index < options.size - 1) {
                        HorizontalDivider(color = Color(0x1AFFFFFF), thickness = 1.dp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.close), color = Color.White)
            }
        }
    )
}