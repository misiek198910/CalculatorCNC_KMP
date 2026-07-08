package kalkulator.cnc.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import calculatorcnc.shared.generated.resources.*
import kalkulator.cnc.data.MainRepository
import kalkulator.cnc.data.domain.CncCategory
import kalkulator.cnc.data.viewmodel.ToolViewModel
import kalkulator.cnc.data.domain.TurningCalculationResult
import kalkulator.cnc.data.domain.TurningCalculator
import kalkulator.cnc.data.domain.TurningResultMode
import kalkulator.cnc.ui.components.ParameterTuningSection
import kalkulator.cnc.ui.components.ResultDialog
import kalkulator.cnc.ui.components.ToolCascadeSelector
import kalkulator.cnc.ui.theme.ApplicationNewBlue
import kalkulator.cnc.utils.LogScreenView
import kalkulator.cnc.utils.getSystemPreference
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.pow
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TurningScreen(
    onBack: () -> Unit,
    repository: MainRepository,
    adContent: @Composable () -> Unit = {},
    showInterstitialAd: () -> Unit = {}
) {
    LogScreenView(screenName = "TurningScreen")

    val isPremium by repository.isPremiumFlow.collectAsState(initial = false)
    var selectedOperationIndex by remember { mutableStateOf(0) }
    var inputValues by remember { mutableStateOf(List(5) { "" }) }
    var result by remember { mutableStateOf<TurningCalculationResult?>(null) }
    var expandedDropdown by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showPremiumDialog by remember { mutableStateOf(false) }
    var isToolCardExpanded by remember { mutableStateOf(true) }
    var showResultDialog by remember { mutableStateOf(false) }
    val operations = stringArrayResource(Res.array.tourning_spinner1_data)
    val fieldLabels = getHintsForTurningOperation(selectedOperationIndex)
    val isMetric = remember { getSystemPreference() == 0 }
    val toolViewModel: ToolViewModel = viewModel { ToolViewModel(repository) }
    val selectedTool by toolViewModel.toolParameters.collectAsState()
    val allCategories by toolViewModel.categories.collectAsState()
    val selectedModel by toolViewModel.selectedModel.collectAsState()
    var isDatabaseLoaded by remember { mutableStateOf(false) }

    val filteredCategories = remember(allCategories, selectedOperationIndex) {
        val techKeywords = when (selectedOperationIndex) {
            0 -> listOf("TURNING", "TURNING_MINI", "TURNING_CROSS", "FREETURN", "INSERT")
            1 -> listOf("BORING")
            2 -> listOf("TURNING_THREAD", "THREAD_TAP", "THREAD_TAP_EG", "THREAD_FORM", "INSERT")
            else -> emptyList()
        }

        if (techKeywords.isEmpty()) emptyList()
        else allCategories.filter { it.code in techKeywords }.sortedBy { it.name }
    }

    fun Double.format(digits: Int): String {
        val factor = 10.0.pow(digits)
        val rounded = (this * factor).roundToInt() / factor
        return rounded.toString()
    }

    val isCategoryPremium: (String) -> Boolean = { categoryName ->
        val code = filteredCategories.find { it.name == categoryName }?.code ?: ""
        code == "FREETURN"
    }

    val isDiameterPremium: (Double) -> Boolean = { false }
    val isModelPremium: (String) -> Boolean = { false }

    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(Unit) {
        isDatabaseLoaded = repository.hasTools()
    }

    LaunchedEffect(selectedOperationIndex) {
        inputValues = List(fieldLabels.size) { "" }
        result = null
        toolViewModel.resetSelection()
    }

    LaunchedEffect(selectedTool) {
        selectedTool?.let { tool ->
            val newValues = inputValues.toMutableList()
            val optVc = ((tool.vcMin + tool.vcMax) / 2).toInt().toString()

            val vcIndex = fieldLabels.indexOfFirst { it.contains("Vc", true) }
            val dcIndex = fieldLabels.indexOfFirst { it.contains("DM", true) || it.contains("D ", true) }
            val fnIndex = fieldLabels.indexOfFirst { it.contains("Fn", true) || it.contains("f ", true) }
            val jtIndex = fieldLabels.indexOfFirst { it.contains("JT", true) }

            if (vcIndex != -1) newValues[vcIndex] = optVc
            if (dcIndex != -1 && selectedOperationIndex == 1) newValues[dcIndex] = tool.diameter.toString()
            if (fnIndex != -1) newValues[fnIndex] = tool.feedStep.toString()
            if (jtIndex != -1) newValues[jtIndex] = tool.feedStep.toString()

            inputValues = newValues
            isToolCardExpanded = false
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            inputValues = List(fieldLabels.size) { "" }
            toolViewModel.resetSelection()
            toolViewModel.setIsoGroup("")
            result = null
        }
    }

    // --- KOMPONENTY UI ---

    val operationSpinner = @Composable {
        ExposedDropdownMenuBox(
            expanded = expandedDropdown,
            onExpandedChange = { expandedDropdown = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = operations[selectedOperationIndex],
                onValueChange = {},
                readOnly = true,
                label = { Text("Rodzaj obliczeń:") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedDropdown) },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0x1AFFFFFF),
                    focusedContainerColor = Color(0x1AFFFFFF),
                    unfocusedBorderColor = Color(0x40FFFFFF),
                    focusedBorderColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color(0xFFBDBDBD)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedDropdown,
                onDismissRequest = { expandedDropdown = false },
                modifier = Modifier.background(Color(0xFF1E1E1E))
            ) {
                operations.forEachIndexed { index, operation ->
                    val isLocked = !isPremium && index in 3..6

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(operation, color = Color.White)
                                if (isLocked) {
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = "Premium",
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        onClick = {
                            expandedDropdown = false
                            if (isLocked) {
                                showPremiumDialog = true
                            } else {
                                selectedOperationIndex = index
                            }
                        }
                    )
                }
            }
        }
    }

    val toolBaseCard = @Composable {
        if (selectedOperationIndex in 0..2) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x26FFFFFF))
                    .border(1.dp, Color(0x40FFFFFF), RoundedCornerShape(12.dp))
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = isDatabaseLoaded) {
                                isToolCardExpanded = !isToolCardExpanded
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when {
                                !isDatabaseLoaded -> "Baza narzędzi obecnie niedostępna"
                                !selectedModel.isNullOrEmpty() -> selectedModel!!
                                else -> stringResource(Res.string.tool_select_base)
                            },
                            color = if (isDatabaseLoaded) Color.White else Color.Gray,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )

                        if (isDatabaseLoaded) {
                            Icon(
                                imageVector = if (isToolCardExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }

                    if (isDatabaseLoaded && isToolCardExpanded) {
                        ToolCascadeSelector(
                            labelStep1 = Res.string.step1_tourning_system,
                            labelStep2 = Res.string.step2_tourning_radius,
                            isVisible = isToolCardExpanded,
                            viewModel = toolViewModel,
                            isPremium = isPremium,
                            filteredCategories = filteredCategories.map { it.name },
                            isCategoryPremium = isCategoryPremium,
                            isDiameterPremium = isDiameterPremium,
                            isModelPremium = isModelPremium,
                            onPremiumClick = { showPremiumDialog = true },
                            onModelSelected = { _ ->
                                isToolCardExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }

    val inputFields = @Composable { isLandscape: Boolean ->
        val vcIndex = fieldLabels.indexOfFirst { it.contains("Vc", true) }
        val verticalPadding = if (isLandscape) 4.dp else 8.dp

        fieldLabels.forEachIndexed { index, label ->
            if (index == vcIndex && selectedTool != null) {
                ParameterTuningSection(
                    tool = selectedTool,
                    vcValue = inputValues.getOrElse(index) { "" },
                    onVcValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            val newValues = inputValues.toMutableList()
                            newValues[index] = newValue
                            inputValues = newValues
                        }
                    },
                    isThreading = (selectedOperationIndex == 2), // Index 2 to Gwintowanie
                    modifier = Modifier.padding(bottom = verticalPadding)
                )
            } else {
                OutlinedTextField(
                    value = inputValues.getOrElse(index) { "" },
                    onValueChange = { newValue ->
                        val sanitizedValue = newValue.replace(',', '.')
                        if (sanitizedValue.isEmpty() || sanitizedValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            val newValues = inputValues.toMutableList()
                            newValues[index] = sanitizedValue
                            inputValues = newValues
                        }
                    },
                    label = { Text(label) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color(0x40FFFFFF),
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color(0xFFBDBDBD)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = verticalPadding)
                )
            }
        }
        result?.error?.let { errorMsg ->
            Text(
                text = errorMsg,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )
        }
    }

    val actionButtons = @Composable { isLandscape: Boolean ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = if (isLandscape) 8.dp else 12.dp, horizontal = if (isLandscape) 0.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledIconButton(
                onClick = {
                    inputValues = List(fieldLabels.size) { "" }
                    toolViewModel.resetSelection()
                    toolViewModel.setIsoGroup("")
                    result = null
                },
                modifier = Modifier.size(if (isLandscape) 48.dp else 56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = ApplicationNewBlue, contentColor = Color.White)
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = stringResource(Res.string.clear))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = {
                    val numericValues = inputValues.map { it.toDoubleOrNull() ?: 0.0 }
                    result = TurningCalculator.calculate(selectedOperationIndex, numericValues, isMetric)
                    isToolCardExpanded = false
                    if (result != null) showResultDialog = true
                },
                modifier = Modifier.weight(1f).height(if (isLandscape) 48.dp else 56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ApplicationNewBlue)
            ) {
                Text(stringResource(Res.string.calculate), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(12.dp))

            FilledIconButton(
                onClick = { showInfoDialog = true },
                modifier = Modifier.size(if (isLandscape) 48.dp else 56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = ApplicationNewBlue, contentColor = Color.White)
            ) {
                Icon(imageVector = Icons.Default.Info, contentDescription = stringResource(Res.string.informationtext))
            }
        }
    }

    // --- GŁÓWNY WIDOK EKRANU ---

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                focusManager.clearFocus()
            }
    ) {
        val isLandscape = maxWidth > maxHeight
        val backgroundImage = if (isLandscape) {
            Res.drawable.background_landscape
        } else {
            Res.drawable.background_portrait
        }

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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
        ) {
            // --- NAGŁÓWEK ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ApplicationNewBlue)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = stringResource(Res.string.back), tint = Color.White)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(stringResource(Res.string.main_button2), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(12.dp))
                    Image(painter = painterResource(Res.drawable.turning), contentDescription = null, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.weight(1f))
                }

                operationSpinner()
            }

            // --- ZAWARTOŚĆ ---
            if (isLandscape) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // LEWA KOLUMNA (Pola tekstowe)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                    ) {
                        inputFields(true)
                    }

                    // PRAWA KOLUMNA (Baza narzędziowa i przyciski)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                    ) {
                        toolBaseCard()
                        Spacer(modifier = Modifier.height(16.dp))
                        actionButtons(true)
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    toolBaseCard()
                    Spacer(modifier = Modifier.height(16.dp))
                    inputFields(false)
                }

                actionButtons(false)

                // Reklama tylko w trybie pionowym
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

    // --- DIALOGI ---
    if (showResultDialog && result != null) {
        val res = result!!
        val resultsList = mutableListOf<Pair<String, String>>()

        when (res.mode) {
            TurningResultMode.STANDARD -> {
                resultsList.add(stringResource(Res.string.res_f_m) to res.f.format(2))
                resultsList.add(stringResource(Res.string.res_n_m) to res.s.toInt().toString())
            }
            TurningResultMode.TIME -> {
                resultsList.add(stringResource(Res.string.tc_colon) to res.tc.format(2) + " min")
            }
            TurningResultMode.VOLUME -> {
                resultsList.add(stringResource(Res.string.q_colon) to res.q.format(2) + " cm³/min")
            }
            TurningResultMode.ROUGHNESS -> {
                resultsList.add(stringResource(Res.string.res_ra) to res.ra.format(3))
            }
            TurningResultMode.TAPER -> {
                resultsList.add(stringResource(Res.string.res_angle) to res.angle.format(2) + "°")
            }
            TurningResultMode.NONE -> {}
        }

        ResultDialog(
            title = stringResource(Res.string.calculateResult),
            results = resultsList,
            highlightedResult = null,
            onClose = { showResultDialog = false
                showInterstitialAd()}
        )
    }

    if (showInfoDialog) {
        val infoResId = getTurningInfoResId(selectedOperationIndex, isMetric)

        val rawText = stringResource(infoResId)
            .replace("<br>", "\n")
            .replace("</br>", "\n")
            .replace("<br/>", "\n")
            .replace("<br />", "\n")

        val annotatedText = buildAnnotatedString {
            var currentIndex = 0
            var isBold = false
            var isItalic = false

            while (currentIndex < rawText.length) {
                val nextTagIndex = rawText.indexOf("<", currentIndex)

                if (nextTagIndex == -1) {
                    val text = rawText.substring(currentIndex)
                    withStyle(
                        SpanStyle(
                            fontWeight = if (isBold) FontWeight.Bold else null,
                            fontStyle = if (isItalic) FontStyle.Italic else null,
                            color = if (isBold) Color.White else Color.Unspecified
                        )
                    ) {
                        append(text)
                    }
                    break
                }

                if (nextTagIndex > currentIndex) {
                    val text = rawText.substring(currentIndex, nextTagIndex)
                    withStyle(SpanStyle(
                        fontWeight = if (isBold) FontWeight.Bold else null,
                        fontStyle = if (isItalic) FontStyle.Italic else null,
                        color = if (isBold) Color.White else Color.Unspecified
                    )) {
                        append(text)
                    }
                }

                when {
                    rawText.startsWith("<b>", nextTagIndex, ignoreCase = true) -> {
                        isBold = true
                        currentIndex = nextTagIndex + 3
                    }
                    rawText.startsWith("</b>", nextTagIndex, ignoreCase = true) -> {
                        isBold = false
                        currentIndex = nextTagIndex + 4
                    }
                    rawText.startsWith("<i>", nextTagIndex, ignoreCase = true) -> {
                        isItalic = true
                        currentIndex = nextTagIndex + 3
                    }
                    rawText.startsWith("</i>", nextTagIndex, ignoreCase = true) -> {
                        isItalic = false
                        currentIndex = nextTagIndex + 4
                    }
                    else -> {
                        withStyle(SpanStyle(
                            fontWeight = if (isBold) FontWeight.Bold else null,
                            fontStyle = if (isItalic) FontStyle.Italic else null,
                            color = if (isBold) Color.White else Color.Unspecified
                        )) {
                            append("<")
                        }
                        currentIndex = nextTagIndex + 1
                    }
                }
            }
        }

        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            containerColor = ApplicationNewBlue,
            title = {
                Text(stringResource(Res.string.informationtext), color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                    Text(
                        text = annotatedText,
                        color = Color(0xFFBDBDBD),
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "💡 ${stringResource(Res.string.vc_expert_title)}",
                        color = Color(0xFFFFD740),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Text(text = "• ${stringResource(Res.string.vc_expert_min)}", color = Color(0xFFBDBDBD), fontSize = 14.sp)
                    Text(text = "• ${stringResource(Res.string.vc_expert_opt)}", color = Color(0xFFBDBDBD), fontSize = 14.sp)
                    Text(text = "• ${stringResource(Res.string.vc_expert_max)}", color = Color(0xFFBDBDBD), fontSize = 14.sp)
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
            title = { Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFFFD700))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(Res.string.premium_required_text), color = Color.White, fontWeight = FontWeight.Bold)
            }},
            text = {
                Text(
                    text = stringResource(Res.string.premium_required_title),
                    color = Color(0xFFBDBDBD),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showPremiumDialog = false }) {
                    Text(stringResource(Res.string.app_restart_dialog_ok), color = Color.White)
                }
            }
        )
    }
}


@Composable
fun getHintsForTurningOperation(index: Int): List<String> {
    return when (index) {
        0 -> listOf(stringResource(Res.string.DM), stringResource(Res.string.VC), stringResource(Res.string.Fn), stringResource(Res.string.AP), stringResource(Res.string.KC))
        1 -> listOf(stringResource(Res.string.VC), stringResource(Res.string.DM), stringResource(Res.string.Fn))
        2 -> listOf(stringResource(Res.string.VC), stringResource(Res.string.DM), stringResource(Res.string.JT))
        3 -> listOf(stringResource(Res.string.LM), stringResource(Res.string.DM), stringResource(Res.string.VC), stringResource(Res.string.Fn))
        4 -> listOf(stringResource(Res.string.AP), stringResource(Res.string.Fn), stringResource(Res.string.VC))
        5 -> listOf(stringResource(Res.string.Fn), stringResource(Res.string.RE))
        6 -> listOf(stringResource(Res.string.D_uppercase), stringResource(Res.string.d_lowercase), stringResource(Res.string.L))
        else -> emptyList()
    }
}

fun getTurningInfoResId(index: Int, isMetric: Boolean): StringResource {
    return when (index) {
        0 -> if (isMetric) Res.string.info_8_m else Res.string.info_8_inch
        1 -> if (isMetric) Res.string.info_9_m else Res.string.info_9_inch
        2 -> if (isMetric) Res.string.info_13_m else Res.string.info_13_inch
        3 -> if (isMetric) Res.string.info_10_m else Res.string.info_10_inch
        4 -> if (isMetric) Res.string.info_11_m else Res.string.info_11_inch
        5 -> if (isMetric) Res.string.info_14_m else Res.string.info_14_inch
        6 -> if (isMetric) Res.string.info_15_m else Res.string.info_15_inch
        else -> Res.string.info_8_m
    }
}