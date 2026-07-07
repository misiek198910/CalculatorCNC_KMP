package kalkulator.cnc.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import calculatorcnc.shared.generated.resources.*
import kalkulator.cnc.data.MainRepository
import kalkulator.cnc.data.domain.TolerancesCalculator
import kalkulator.cnc.data.domain.FitResult
import kalkulator.cnc.data.domain.FitType
import kalkulator.cnc.ui.theme.ApplicationNewBlue
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.material.icons.filled.ContentCopy
import kalkulator.cnc.utils.LogScreenView
import kotlinx.coroutines.launch
import kotlin.math.round
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TolerancesScreen(onBack: () -> Unit, repository: MainRepository, onNavigateToPremium: () -> Unit, adContent: @Composable () -> Unit = {}, showInterstitialAd: () -> Unit = {}) {

    LogScreenView(screenName = "TolerancesScreen")

    val isPremium by repository.isPremiumFlow.collectAsState(initial = false)
    val clipboardManager = LocalClipboardManager.current
    val availableHoles = TolerancesCalculator.getHoleLabels()
    val availableShafts = TolerancesCalculator.getShaftLabels()

    val freeHoles = listOf("H7", "H8")
    val freeShafts = listOf("h6", "h7", "g6")

    var diameterInput by remember { mutableStateOf("") }
    var selectedHole by remember { mutableStateOf("H7") }
    var selectedShaft by remember { mutableStateOf("h6") }
    var fitResult by remember { mutableStateOf<FitResult?>(null) }

    var expandedHoleDropdown by remember { mutableStateOf(false) }
    var expandedShaftDropdown by remember { mutableStateOf(false) }

    var showInfoDialog by remember { mutableStateOf(false) }
    var showPremiumDialog by remember { mutableStateOf(false) }

    val isMetric by repository.isMetricFlow.collectAsState(initial = true)

    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }

    val coroutineScope = rememberCoroutineScope()
    var isCopied by remember { mutableStateOf(false) }

    fun calculateFit() {
        val dia = diameterInput.replace(',', '.').toDoubleOrNull()

        if (dia != null && dia > 0 && dia <= 500) {
            fitResult = TolerancesCalculator.calculateFit(dia, selectedHole, selectedShaft)
        } else {
            fitResult = null
        }
    }



    LaunchedEffect(diameterInput, selectedHole, selectedShaft) {
        calculateFit()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {

                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent()
                    }
                }
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                focusManager.clearFocus()
            }
    ) {
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
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ApplicationNewBlue)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = stringResource(Res.string.back), tint = Color.White)
                }
                Text(
                    text = stringResource(Res.string.tolerances),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
                Image(
                    painter = painterResource(Res.drawable.tolerances_iso),
                    contentDescription = null,
                    modifier = Modifier.size(45.dp).padding(start = 12.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(Res.drawable.rotate),
                        contentDescription = null,
                        modifier = Modifier.size(45.dp).padding(end = 12.dp)
                    )
                    OutlinedTextField(
                        value = diameterInput,
                        onValueChange = { newValue ->
                            val sanitized = newValue.replace(',', '.')
                            if (sanitized.isEmpty() || sanitized.matches(Regex("^\\d*\\.?\\d*$"))) {
                                diameterInput = newValue
                            }
                        },
                        label = { Text(stringResource(Res.string.diameter_hint)) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF1976D2),
                            unfocusedBorderColor = Color(0xFF1976D2),
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color(0xFFBDBDBD),
                            unfocusedContainerColor = Color(0x33000000),
                            focusedContainerColor = Color(0x33000000)
                        ),
                        modifier = Modifier.fillMaxWidth().height(64.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    ExposedDropdownMenuBox(
                        expanded = expandedHoleDropdown,
                        onExpandedChange = { expandedHoleDropdown = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedHole,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(Res.string.hole_hint)) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expandedHoleDropdown
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF1976D2),
                                unfocusedBorderColor = Color(0xFF1976D2),
                                focusedLabelColor = Color.White,
                                unfocusedLabelColor = Color(0xFFBDBDBD),
                                unfocusedContainerColor = Color(0x33000000),
                                focusedContainerColor = Color(0x33000000)
                            ),
                            modifier = Modifier.menuAnchor().fillMaxWidth().height(64.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedHoleDropdown,
                            onDismissRequest = { expandedHoleDropdown = false }
                        ) {
                            availableHoles.forEach { hole ->
                                val isLocked = !isPremium && hole !in freeHoles
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(hole)
                                            if (isLocked) {
                                                Spacer(modifier = Modifier.weight(1f))
                                                Icon(
                                                    Icons.Default.Lock,
                                                    contentDescription = null,
                                                    tint = Color(0xFFFFD700),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        expandedHoleDropdown = false
                                        if (isLocked) showPremiumDialog = true else selectedHole =
                                            hole
                                    }
                                )
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = expandedShaftDropdown,
                        onExpandedChange = { expandedShaftDropdown = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedShaft,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(Res.string.shaft_hint)) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expandedShaftDropdown
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF1976D2),
                                unfocusedBorderColor = Color(0xFF1976D2),
                                focusedLabelColor = Color.White,
                                unfocusedLabelColor = Color(0xFFBDBDBD),
                                unfocusedContainerColor = Color(0x33000000),
                                focusedContainerColor = Color(0x33000000)
                            ),
                            modifier = Modifier.menuAnchor().fillMaxWidth().height(64.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedShaftDropdown,
                            onDismissRequest = { expandedShaftDropdown = false }
                        ) {
                            availableShafts.forEach { shaft ->
                                val isLocked = !isPremium && shaft !in freeShafts
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(shaft)
                                            if (isLocked) {
                                                Spacer(modifier = Modifier.weight(1f))
                                                Icon(
                                                    Icons.Default.Lock,
                                                    contentDescription = null,
                                                    tint = Color(0xFFFFD700),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        expandedShaftDropdown = false
                                        if (isLocked) showPremiumDialog = true else selectedShaft =
                                            shaft
                                    }
                                )
                            }
                        }
                    }
                }

                if (fitResult != null) {
                    val res = fitResult!!

                    val (statusText, statusColor) = when (res.type) {
                        FitType.CLEARANCE -> stringResource(Res.string.status_clearance) to Color(
                            0xFF8BC34A
                        )

                        FitType.TRANSITION -> stringResource(Res.string.status_transition) to Color(
                            0xFFFFB74D
                        )

                        FitType.INTERFERENCE -> stringResource(Res.string.status_interference) to Color(
                            0xFFE57373
                        )

                        else -> "" to Color.White
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0x33000000)),
                        border = BorderStroke(1.dp, Color(0x40FFFFFF)),
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = statusText,
                                color = statusColor,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = Color(0xFF1976D2)
                            )

                            val unit = if (isMetric) "mm" else "in"
                            val divider = if (isMetric) 1.0 else 25.4
                            val decimals =
                                if (isMetric) 1000.0 else 10000.0

                            fun formatNum(value: Double): String {
                                return (round((value / divider) * decimals) / decimals).toString()
                            }

                            // Dynamiczne stringi przypisane do zmiennych
                            val holeValuesTxt = stringResource(
                                Res.string.fit_hole_dynamic,
                                formatNum(res.holeMin),
                                formatNum(res.holeMax),
                                unit
                            )

                            val shaftValuesTxt = stringResource(
                                Res.string.fit_shaft_dynamic,
                                formatNum(res.shaftMin),
                                formatNum(res.shaftMax),
                                unit
                            )

                            val clearanceTxt = if (res.minClearance >= 0) {
                                stringResource(
                                    Res.string.fit_clearance_dynamic,
                                    formatNum(res.minClearance),
                                    formatNum(res.maxClearance),
                                    unit
                                )
                            } else {
                                stringResource(
                                    Res.string.fit_interference_dynamic,
                                    formatNum(res.minClearance),
                                    formatNum(res.maxClearance),
                                    unit
                                )
                            }

                            Text(text = holeValuesTxt, color = Color.White, fontSize = 17.sp)
                            Text(
                                text = shaftValuesTxt,
                                color = Color.White,
                                fontSize = 17.sp,
                                modifier = Modifier.padding(top = 6.dp)
                            )

                            Text(
                                text = clearanceTxt,
                                color = Color(0xFFBDBDBD),
                                fontSize = 16.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                modifier = Modifier.padding(top = 12.dp)
                            )

                            IconButton(
                                onClick = {
                                    focusManager.clearFocus()
                                    val copyString = "$statusText\n$holeValuesTxt\n$shaftValuesTxt\n$clearanceTxt"
                                    clipboardManager.setText(AnnotatedString(copyString))

                                    isCopied = true

                                    coroutineScope.launch {
                                        kotlinx.coroutines.delay(2000.milliseconds)
                                        isCopied = false
                                    }
                                },
                                modifier = Modifier.padding(top = 15.dp).size(40.dp)
                            ) {
                                Icon(
                                    imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                    contentDescription = stringResource(Res.string.copy_action),
                                    // Dynamiczna zmiana koloru: zielony na potwierdzenie, biały domyślnie
                                    tint = if (isCopied) Color.Green else Color.White
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Button(
                    onClick = {
                        diameterInput = ""
                        selectedHole = "H7"
                        selectedShaft = "h6"
                        fitResult = null

                        if (!isPremium) {
                            showInterstitialAd()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ApplicationNewBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(56.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(Res.string.reset),
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = { showInfoDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = ApplicationNewBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(56.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(text = "i", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            containerColor = ApplicationNewBlue,
            title = { Text(stringResource(Res.string.informationtext), color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(text = stringResource(Res.string.status_clearance) + ":", color = Color.White, fontWeight = FontWeight.Bold)
                    Text(text = stringResource(Res.string.desc_clearance), color = Color(0xFFBDBDBD), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = stringResource(Res.string.status_transition) + ":", color = Color.White, fontWeight = FontWeight.Bold)
                    Text(text = stringResource(Res.string.desc_transition), color = Color(0xFFBDBDBD), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = stringResource(Res.string.status_interference) + ":", color = Color.White, fontWeight = FontWeight.Bold)
                    Text(text = stringResource(Res.string.desc_interference), color = Color(0xFFBDBDBD), fontSize = 14.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text(stringResource(Res.string.close), color = Color.White)
                }
            }
        )
    }

    if (showPremiumDialog) {
        AlertDialog(
            onDismissRequest = { showPremiumDialog = false },
            containerColor = ApplicationNewBlue,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFFFD700))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(Res.string.premium_required_text), color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = stringResource(Res.string.premium_required_title),
                    color = Color(0xFFBDBDBD),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showPremiumDialog = false
                    onNavigateToPremium()
                }) {
                    Text(stringResource(Res.string.unlocked), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPremiumDialog = false }) {
                    Text(stringResource(Res.string.cancel), color = Color.White)
                }
            }
        )
    }
}