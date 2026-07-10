package kalkulator.cnc.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import calculatorcnc.shared.generated.resources.Res
import calculatorcnc.shared.generated.resources.*
import kalkulator.cnc.data.MainRepository
import kalkulator.cnc.data.domain.CalculationResult
import kalkulator.cnc.data.domain.MillingCalculator
import kalkulator.cnc.data.viewmodel.ToolViewModel
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
fun MillingScreen(
    onBack: () -> Unit,
    repository: MainRepository,
    adContent: @Composable () -> Unit = {},
    showInterstitialAd: () -> Unit = {}
) {
    LogScreenView(screenName = "MillingScreen")

    val isPremium by repository.isPremiumFlow.collectAsState(initial = false)
    var selectedOperationIndex by remember { mutableStateOf(0) }
    var inputValues by remember { mutableStateOf(List(4) { "" }) }
    var result by remember { mutableStateOf<CalculationResult?>(null) }
    var expandedDropdown by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showPremiumDialog by remember { mutableStateOf(false) }
    var isToolCardExpanded by rememberSaveable { mutableStateOf(true) }
    val isMetric = remember { getSystemPreference() == 0 }
    var showResultDialog by remember { mutableStateOf(false) }
    val operations = stringArrayResource(Res.array.miling_spinner1_data)
    val fieldLabels = getHintsForOperation(selectedOperationIndex)
    val toolViewModel: ToolViewModel = viewModel { ToolViewModel(repository) }
    val selectedTool by toolViewModel.toolParameters.collectAsState()
    val allCategories by toolViewModel.categories.collectAsState()
    val selectedModel by toolViewModel.selectedModel.collectAsState()
    var isDatabaseLoaded by remember { mutableStateOf(false) }

    val filteredCategories = remember(allCategories, selectedOperationIndex) {
        val techKeywords = when (selectedOperationIndex) {
            0 -> listOf(
                "MILL_HEAD", "MILL_VHM", "MILL_HSS", "MILL_OTHER",
                "DRILL_HSSE", "DRILL_HSS", "DRILL_VHM", "DRILL_OTHER",
                "REAMER", "COUNTERSINK", "MULTITOOL"
            )
            1 -> listOf("THREAD_TAP", "THREAD_TAP_EG", "THREAD_FORM")
            7 -> listOf("BORING", "MULTITOOL")
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
        code == "MULTITOOL" || code == "FREETURN"
    }

    val isDiameterPremium: (Double) -> Boolean = { dia ->
        val categoryName = toolViewModel.selectedCategory.value ?: ""
        val code = allCategories.find { it.name == categoryName }?.code ?: ""

        val isPrecision = code.startsWith("THREAD_") || code == "REAMER"
        val isHssOrVhm = code == "DRILL_HSS" || code == "DRILL_HSSE" || code == "DRILL_VHM"

        when {
            code == "MILL_HEAD" -> dia >= 63.0
            isPrecision -> dia >= 10.0
            isHssOrVhm -> dia >= 10.0
            else -> dia > 20.0
        }
    }

    val isModelPremium: (String) -> Boolean = { model ->
        val categoryName = toolViewModel.selectedCategory.value ?: ""
        val code = allCategories.find { it.name == categoryName }?.code ?: ""

        if (code.startsWith("DRILL_")) {
            false
        } else {
            val match = Regex("(\\d+)xD").find(model)
            val depth = match?.groupValues?.get(1)?.toInt() ?: 0
            depth > 5
        }
    }

    val focusManager = LocalFocusManager.current
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
            val dcIndex = fieldLabels.indexOfFirst { it.contains("DC", true) || it.contains("DM", true) }
            val fzIndex = fieldLabels.indexOfFirst { it.contains("Fz", true) || it.contains("Fn", true) }
            val zIndex = fieldLabels.indexOfFirst { it == "Z" }
            val jtIndex = fieldLabels.indexOfFirst { it.contains("JT", true) }

            if (vcIndex != -1) newValues[vcIndex] = optVc
            if (dcIndex != -1 && selectedOperationIndex != 7) newValues[dcIndex] = tool.diameter.toString()
            if (fzIndex != -1) newValues[fzIndex] = tool.feedStep.toString()
            if (zIndex != -1) newValues[zIndex] = tool.flutesCount.toString()
            if (jtIndex != -1) newValues[jtIndex] = tool.feedStep.toString()

            inputValues = newValues
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            inputValues = List(fieldLabels.size) { "" }
            toolViewModel.resetSelection()
        }
    }

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
                modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedDropdown,
                onDismissRequest = { expandedDropdown = false },
                modifier = Modifier.background(Color(0xFF1E1E1E))
            ) {
                operations.forEachIndexed { index, operation ->
                    val isLocked = !isPremium && index in 6..8

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(operation, color = Color.White)
                                if (isLocked) {
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = stringResource(Res.string.premium_required_text),
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
        if (selectedOperationIndex == 0 || selectedOperationIndex == 1 || selectedOperationIndex == 7) {
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
                            labelStep1 = Res.string.step1_milling_category,
                            labelStep2 = Res.string.step2_milling_diameter,
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
        val verticalPadding = if (isLandscape) 4.dp else 8.dp // Gęstszy układ w poziomie

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
                    isThreading = (selectedOperationIndex == 1),
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
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Done
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
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
        result?.errorRes?.let { resId ->
            val errorMsg = if (result!!.errorArg != null) {
                stringResource(resId).formatKmp(result!!.errorArg!!)
            } else {
                stringResource(resId)
            }

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
                .padding(vertical = if (isLandscape) 0.dp else 12.dp, horizontal = if (isLandscape) 0.dp else 16.dp),
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
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = ApplicationNewBlue,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(Res.string.clear)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = {
                    val numericValues = inputValues.map { it.toDoubleOrNull() ?: 0.0 }
                    result = MillingCalculator.calculate(
                        selectedOperationIndex,
                        numericValues,
                        isMetric
                    )
                    isToolCardExpanded = false
                    if (result != null) showResultDialog = true
                },
                modifier = Modifier.weight(1f).height(if (isLandscape) 48.dp else 56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ApplicationNewBlue)
            ) {
                Text(
                    stringResource(Res.string.calculate),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            FilledIconButton(
                onClick = { showInfoDialog = true },
                modifier = Modifier.size(if (isLandscape) 48.dp else 56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = ApplicationNewBlue,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = stringResource(Res.string.informationtext)
                )
            }
        }
    }

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
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.back),
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        stringResource(Res.string.main_button1),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Image(
                        painter = painterResource(Res.drawable.spindle),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }

                operationSpinner()
            }

            if (isLandscape) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                    ) {
                        inputFields(true)
                    }

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

        if (res.isHardnessMode) {
            resultsList.add(stringResource(Res.string.Rm) to res.rm.toString())
            resultsList.add(stringResource(Res.string.HV) to res.hv.toString())
            resultsList.add(stringResource(Res.string.HB) to res.hb.toString())
            if (res.hrc > 0) resultsList.add(stringResource(Res.string.HRC) to res.hrc.toString())
        } else if (res.isCenterMode) {
            resultsList.add(stringResource(Res.string.calc_center) to res.center.toString())
        } else if (res.isSawMode) {
            resultsList.add(stringResource(Res.string.FZ) to res.fz.toString())
        } else {
            resultsList.add(stringResource(Res.string.f_hint) to res.f.format(2))
            resultsList.add(stringResource(Res.string.s_hint) to res.s.toInt().toString())
        }

        ResultDialog(
            title = stringResource(Res.string.calculateResult),
            results = resultsList,
            highlightedResult = if (!res.isHardnessMode && res.fz > 0) stringResource(Res.string.FZ) to res.fz.format(
                3
            ) else null,
            onClose = {
                showResultDialog = false
                showInterstitialAd()
            }
        )
    }

    if (showInfoDialog) {
        val infoResId = getMillingInfoResId(selectedOperationIndex, isMetric)

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
                    withStyle(
                        SpanStyle(
                            fontWeight = if (isBold) FontWeight.Bold else null,
                            fontStyle = if (isItalic) FontStyle.Italic else null,
                            color = if (isBold) Color.White else Color.Unspecified
                        )
                    ) {
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
                        withStyle(
                            SpanStyle(
                                fontWeight = if (isBold) FontWeight.Bold else null,
                                fontStyle = if (isItalic) FontStyle.Italic else null,
                                color = if (isBold) Color.White else Color.Unspecified
                            )
                        ) {
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
                Text(
                    stringResource(Res.string.informationtext),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = annotatedText,
                        color = Color(0xFFBDBDBD),
                        fontSize = 14.sp
                    )

                    if (selectedOperationIndex != 9) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "💡 ${stringResource(Res.string.vc_expert_title)}",
                            color = Color(0xFFFFD740),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        Text(
                            text = "• ${stringResource(Res.string.vc_expert_min)}",
                            color = Color(0xFFBDBDBD),
                            fontSize = 14.sp
                        )
                        Text(
                            text = "• ${stringResource(Res.string.vc_expert_opt)}",
                            color = Color(0xFFBDBDBD),
                            fontSize = 14.sp
                        )
                        Text(
                            text = "• ${stringResource(Res.string.vc_expert_max)}",
                            color = Color(0xFFBDBDBD),
                            fontSize = 14.sp
                        )
                    }
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
                    Text(
                        stringResource(Res.string.premium_required_text),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
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
                TextButton(onClick = { showPremiumDialog = false }) {
                    Text(stringResource(Res.string.app_restart_dialog_ok), color = Color.White)
                }
            }
        )
    }
}

@Composable
fun getHintsForOperation(index: Int): List<String> {
    return when (index) {
        0 -> listOf(
            stringResource(Res.string.VC),
            stringResource(Res.string.DC),
            stringResource(Res.string.FZ),
            stringResource(Res.string.Z)
        )
        1 -> listOf(
            stringResource(Res.string.VC),
            stringResource(Res.string.DC),
            stringResource(Res.string.JT)
        )
        2 -> listOf(
            stringResource(Res.string.AS),
            stringResource(Res.string.T),
            stringResource(Res.string.VC),
            stringResource(Res.string.L)
        )
        3 -> listOf(
            stringResource(Res.string.VF),
            stringResource(Res.string.VC),
            stringResource(Res.string.Z),
            stringResource(Res.string.LB)
        )
        4 -> listOf(
            stringResource(Res.string.AS),
            stringResource(Res.string.VC),
            stringResource(Res.string.D_uppercase),
            stringResource(Res.string.Z),
            stringResource(Res.string.L)
        )
        5 -> listOf(
            stringResource(Res.string.VF),
            stringResource(Res.string.VC),
            stringResource(Res.string.Z),
            stringResource(Res.string.D_uppercase)
        )
        6 -> listOf(
            stringResource(Res.string.HD),
            stringResource(Res.string.TD),
            stringResource(Res.string.F)
        )
        7 -> listOf(
            stringResource(Res.string.VC),
            stringResource(Res.string.DM),
            stringResource(Res.string.Fn)
        )
        8 -> listOf(stringResource(Res.string.PointA), stringResource(Res.string.PointB))
        9 -> listOf(
            stringResource(Res.string.hardness_rm),
            stringResource(Res.string.hardness_hv),
            stringResource(Res.string.hardness_hb),
            stringResource(Res.string.hardness_hrc)
        )
        else -> emptyList()
    }
}

fun getMillingInfoResId(index: Int, isMetric: Boolean): StringResource {
    return when (index) {
        0 -> if (isMetric) Res.string.info_0_m else Res.string.info_0_inch
        1 -> if (isMetric) Res.string.info_13_m else Res.string.info_13_inch
        2 -> if (isMetric) Res.string.info_4_m else Res.string.info_4_inch
        3 -> if (isMetric) Res.string.info_5_m else Res.string.info_5_inch
        4 -> if (isMetric) Res.string.info_6_m else Res.string.info_6_inch
        5 -> if (isMetric) Res.string.info_7_m else Res.string.info_7_inch
        6 -> if (isMetric) Res.string.info_2_m else Res.string.info_2_inch
        7 -> if (isMetric) Res.string.info_3_m else Res.string.info_3_inch
        8 -> if (isMetric) Res.string.info_12_m else Res.string.info_12_inch
        9 -> Res.string.info_0_m
        else -> Res.string.info_0_m
    }
}