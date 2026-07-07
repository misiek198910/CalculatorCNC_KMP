package kalkulator.cnc.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear // DODANY IMPORT
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import calculatorcnc.shared.generated.resources.*
import kalkulator.cnc.data.MainRepository
import kalkulator.cnc.data.params.getMaterialsList
import kalkulator.cnc.entity.UserToolEntity
import kalkulator.cnc.ui.theme.ApplicationNewBlue
import kalkulator.cnc.utils.LogScreenView
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToolScreen(toolId: Int?, onBack: () -> Unit, repository: MainRepository, adContent: @Composable () -> Unit = {}) {

    LogScreenView(screenName = "AddToolScreen")

    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val isEditMode = toolId != null
    var name by remember { mutableStateOf("") }
    var sValue by remember { mutableStateOf("") }
    var fValue by remember { mutableStateOf("") }
    var workpiece by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var showMaterialDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(isEditMode) }

    LaunchedEffect(toolId) {
        if (toolId != null) {
            val tool = repository.getUserToolById(toolId)
            if (tool != null) {
                name = tool.name
                sValue = tool.s ?: ""
                fValue = tool.f ?: ""
                workpiece = tool.workpiece ?: ""
                notes = tool.notes ?: ""
            }
            isLoading = false
        }
    }

    fun getIsoColor(isoGroup: String): Color {
        return when (isoGroup) {
            "P" -> Color(0xFF0091EA)
            "M" -> Color(0xFFFFEA00)
            "K" -> Color(0xFFD50000)
            "N" -> Color(0xFF00C853)
            "S" -> Color(0xFFFF6D00)
            "H" -> Color(0xFF9E9E9E)
            else -> Color.Transparent
        }
    }

    fun saveTool(otherLabel: String) {
        val finalName = name.trim()
        if (finalName.isEmpty()) {
            showErrorDialog = true
            return
        }

        val finalWorkpiece = if (workpiece.trim().isEmpty()) otherLabel else workpiece.trim()

        val toolToSave = UserToolEntity(
            id = toolId ?: 0,
            name = finalName,
            f = fValue.trim(),
            s = sValue.trim(),
            workpiece = finalWorkpiece,
            notes = notes.trim()
        )

        coroutineScope.launch {
            if (isEditMode) {
                repository.updateUserTool(toolToSave)
            } else {
                repository.insertUserTool(toolToSave)
            }
            onBack()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(Res.drawable.background_portrait),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier.fillMaxSize().background(ApplicationNewBlue.copy(alpha = 0.80f))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsTopHeight(WindowInsets.systemBars)
                .background(ApplicationNewBlue)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
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
                    Icon(Icons.Default.ArrowBack, contentDescription = stringResource(Res.string.back), tint = Color.White)
                }
                Text(
                    text = if (isEditMode) stringResource(Res.string.edit) else stringResource(Res.string.add),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                        .padding(16.dp)
                ) {

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
                        Image(
                            painter = painterResource(Res.drawable.tools),
                            contentDescription = null,
                            modifier = Modifier.size(45.dp).padding(end = 12.dp)
                        )
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text(text = stringResource(Res.string.toolsName), fontSize = 11.sp, maxLines = 1, softWrap = false)},
                            colors = getTextFieldColors(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            trailingIcon = {
                                if (name.isNotEmpty()) {
                                    IconButton(onClick = { name = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Wyczyść", tint = Color.White)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(64.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(Res.drawable.rotate),
                                contentDescription = null,
                                modifier = Modifier.size(45.dp).padding(end = 8.dp)
                            )
                            OutlinedTextField(
                                value = sValue,
                                onValueChange = { sValue = it },
                                label = { Text(text = stringResource(Res.string.s_hint), fontSize = 11.sp, maxLines = 1, softWrap = false)},
                                colors = getTextFieldColors(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                trailingIcon = {
                                    if (sValue.isNotEmpty()) {
                                        IconButton(onClick = { sValue = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Wyczyść", tint = Color.White)
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(64.dp)
                            )
                        }
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(Res.drawable.feed),
                                contentDescription = null,
                                modifier = Modifier.size(45.dp).padding(end = 8.dp)
                            )
                            OutlinedTextField(
                                value = fValue,
                                onValueChange = { fValue = it },
                                label = { Text(text = stringResource(Res.string.f_hint), fontSize = 11.sp, maxLines = 1, softWrap = false)},
                                colors = getTextFieldColors(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                trailingIcon = {
                                    if (fValue.isNotEmpty()) {
                                        IconButton(onClick = { fValue = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Wyczyść", tint = Color.White)
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(64.dp)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
                        Image(
                            painter = painterResource(Res.drawable.material),
                            contentDescription = null,
                            modifier = Modifier.size(45.dp).padding(end = 12.dp)
                        )
                        Box(modifier = Modifier.weight(1f).height(64.dp)) {
                            OutlinedTextField(
                                value = workpiece,
                                onValueChange = {},
                                readOnly = true,
                                enabled = false,
                                label = { Text(text = stringResource(Res.string.material), fontSize = 11.sp, maxLines = 1, softWrap = false)},
                                colors = getTextFieldColors().copy(
                                    disabledTextColor = Color.White,
                                    disabledLabelColor = Color(0xFFBDBDBD),
                                    disabledIndicatorColor = Color.Transparent,
                                    disabledContainerColor = Color(0x33000000)
                                ),
                                modifier = Modifier.fillMaxSize()
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(end = if (workpiece.isNotEmpty()) 48.dp else 0.dp)
                                    .clickable {
                                        focusManager.clearFocus()
                                        showMaterialDialog = true
                                    }
                            )

                            if (workpiece.isNotEmpty()) {
                                IconButton(
                                    onClick = { workpiece = "" },
                                    modifier = Modifier.align(Alignment.CenterEnd)
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = "Wyczyść", tint = Color.White)
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(text = stringResource(Res.string.notes_hint), fontSize = 11.sp, maxLines = 1, softWrap = false)},
                        colors = getTextFieldColors(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        trailingIcon = {
                            if (notes.isNotEmpty()) {
                                IconButton(onClick = { notes = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Wyczyść", tint = Color.White)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(150.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .imePadding(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val otherLabel = stringResource(Res.string.other)

                    Button(
                        onClick = { saveTool(otherLabel) },
                        colors = ButtonDefaults.buttonColors(containerColor = ApplicationNewBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Text(stringResource(Res.string.save), color = Color.White, fontSize = 16.sp)
                    }
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = ApplicationNewBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Text(stringResource(Res.string.close), color = Color.White, fontSize = 16.sp)
                    }
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

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            containerColor = ApplicationNewBlue,
            title = { Text(stringResource(Res.string.tools_error_code), color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(Res.string.tools_error), color = Color(0xFFBDBDBD)) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text(stringResource(Res.string.app_restart_dialog_ok), color = Color.White)
                }
            }
        )
    }

    if (showMaterialDialog) {
        val materials = getMaterialsList()

        AlertDialog(
            onDismissRequest = { showMaterialDialog = false },
            containerColor = ApplicationNewBlue,
            title = { Text(stringResource(Res.string.select_material_iso), color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(materials) { material ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x33000000))
                                .clickable {
                                    workpiece = material.name
                                    showMaterialDialog = false
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(8.dp)
                                    .height(48.dp)
                                    .background(getIsoColor(material.isoGroup))
                            )
                            Text(
                                text = "${material.name} (${material.isoGroup})",
                                color = Color.White,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(12.dp).align(Alignment.CenterVertically)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMaterialDialog = false }) {
                    Text(stringResource(Res.string.close), color = Color.White)
                }
            }
        )
    }
}
@Composable
fun getTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedContainerColor = Color(0x33000000),
    unfocusedContainerColor = Color(0x33000000),
    focusedBorderColor = Color.Transparent,
    unfocusedBorderColor = Color.Transparent,
    focusedLabelColor = Color.White,
    unfocusedLabelColor = Color(0xFFBDBDBD)
)