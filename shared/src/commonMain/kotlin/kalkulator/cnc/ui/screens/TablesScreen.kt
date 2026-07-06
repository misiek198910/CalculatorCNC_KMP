package kalkulator.cnc.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import calculatorcnc.shared.generated.resources.*
import kalkulator.cnc.data.MainRepository
import kalkulator.cnc.ui.theme.ApplicationNewBlue
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import kalkulator.cnc.entity.ThreadEntity


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TablesScreen(onBack: () -> Unit, repository: MainRepository, initialSpinnerIdx: Int = 0, adContent: @Composable () -> Unit = {}, showInterstitialAd: () -> Unit = {}) {
    var spinnerIdx by remember { mutableStateOf(initialSpinnerIdx) }
    var tableIdx by remember { mutableStateOf(0) }
    var expandedDropdown by remember { mutableStateOf(false) }
    val categories = stringArrayResource(Res.array.spinner3_items)
    val formingAbbr = stringResource(Res.string.forming_abbr)
    val mixedAbbr = stringResource(Res.string.mixed_abbr)
    val threadEg = stringResource(Res.string.thread_eg)
    val isPremium by repository.isPremiumFlow.collectAsState(initial = false)
    var isInitialized by remember { mutableStateOf(false) }
    var interactionCount by remember { mutableIntStateOf(0) }

    val buttonLabels = remember(spinnerIdx) {
        when (spinnerIdx) {
            0 -> listOf("M", "MF", "M $formingAbbr", "MF $formingAbbr")
            1 -> listOf("$threadEg M", "$threadEg MF", "$threadEg UNC", "$threadEg UNF", "$threadEg M $formingAbbr")
            2 -> listOf("UNC", "UNF", "UNEF", "UNC $formingAbbr", "UNF $formingAbbr", "UNEF $formingAbbr", "8-UN", "UN $mixedAbbr", "UNJC", "UNJF")
            3 -> listOf("NPT", "NPSM", "NPTF")
            4 -> listOf("G", "G $formingAbbr")
            5 -> listOf("BSW", "BSW $formingAbbr")
            6 -> listOf("MJ")
            7 -> listOf("PG")
            else -> emptyList()
        }
    }

    var threadsData by remember { mutableStateOf<List<ThreadEntity>>(emptyList()) }

    LaunchedEffect(spinnerIdx) {
        tableIdx = 0
    }

    LaunchedEffect(spinnerIdx, tableIdx) {

        threadsData = repository.getThreads(spinnerIdx, tableIdx)

        if (isInitialized && !isPremium) {
            interactionCount++
            if (interactionCount % 3 == 0) {
                showInterstitialAd()
            }
        }
        isInitialized = true
    }

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
            // HEADER
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
                    text = stringResource(Res.string.main_button4),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )

                Image(
                    painter = painterResource(Res.drawable.tolerances),
                    contentDescription = null,
                    modifier = Modifier.size(35.dp).padding(start = 6.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Box(modifier = Modifier.weight(1f)) {
                    ExposedDropdownMenuBox(
                        expanded = expandedDropdown,
                        onExpandedChange = { expandedDropdown = it }
                    ) {
                        OutlinedTextField(
                            value = if (categories.isNotEmpty()) categories[spinnerIdx] else "",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedDropdown) },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color(0x33000000),
                                focusedContainerColor = Color(0x33000000),
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.menuAnchor().fillMaxWidth().height(50.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false }
                        ) {
                            categories.forEachIndexed { index, category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = {
                                        spinnerIdx = index
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(buttonLabels) { index, label ->
                    val isSelected = index == tableIdx
                    Button(
                        onClick = { tableIdx = index },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) Color(0xFF1976D2) else Color(0x33FFFFFF)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text(text = label, color = Color.White, fontSize = 12.sp)
                    }
                }
            }

            val suffix = if (buttonLabels.isNotEmpty() && tableIdx < buttonLabels.size) buttonLabels[tableIdx] else ""
            Text(
                text = "${stringResource(Res.string.holeName)} $suffix",
                color = Color(0xFFFFD700), // Gold
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x33000000)) // bg_search_input
                    .border(1.dp, Color(0x40FFFFFF), RoundedCornerShape(8.dp))
            ) {

                val isInch = spinnerIdx in listOf(2, 3, 4, 5) || (spinnerIdx == 1 && tableIdx in listOf(2, 3))
                val pitchLabel = if (isInch) "Zwoje/cal" else stringResource(Res.string.holePitch)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF333333))
                        .padding(vertical = 8.dp)
                ) {
                    TableHeaderText("Nazwa", Modifier.weight(1f))
                    TableHeaderText(pitchLabel, Modifier.weight(1f))
                    TableHeaderText(stringResource(Res.string.holeMin), Modifier.weight(1f))
                    TableHeaderText(stringResource(Res.string.holeMax), Modifier.weight(1f))
                    TableHeaderText(stringResource(Res.string.hole), Modifier.weight(1f))
                }

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(threadsData) { thread ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TableDataText(thread.name, Color.White, Modifier.weight(1f))
                            TableDataText(thread.pitch, Color(0xFFFFEB3B), Modifier.weight(1f)) // Żółty
                            TableDataText(thread.holeMin, Color.White, Modifier.weight(1f))
                            TableDataText(thread.holeMax, Color.White, Modifier.weight(1f))
                            TableDataText(
                                text = thread.holeSize,
                                color = Color(0xFFFFEB3B),
                                modifier = Modifier.weight(1f),
                                isBold = true
                            )
                        }
                        HorizontalDivider(color = Color(0x1AFFFFFF), thickness = 1.dp)
                    }
                }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x1A000000))
                    .padding(top = 8.dp)
                    .defaultMinSize(minHeight = 50.dp),
                contentAlignment = Alignment.Center
            ) {
                adContent()
            }
        }
    }
}

@Composable
fun TableHeaderText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = Color.Yellow,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = modifier.padding(horizontal = 2.dp)
    )
}

@Composable
fun TableDataText(text: String, color: Color, modifier: Modifier = Modifier, isBold: Boolean = false) {
    Text(
        text = text,
        color = color,
        fontSize = 14.sp,
        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
        textAlign = TextAlign.Center,
        modifier = modifier.padding(horizontal = 2.dp, vertical = 6.dp)
    )
}