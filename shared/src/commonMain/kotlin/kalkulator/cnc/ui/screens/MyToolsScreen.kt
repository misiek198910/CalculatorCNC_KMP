package kalkulator.cnc.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import calculatorcnc.shared.generated.resources.*
import kalkulator.cnc.data.MainRepository
import kalkulator.cnc.entity.UserToolEntity
import kalkulator.cnc.ui.components.AdMobInline
import kalkulator.cnc.ui.theme.ApplicationNewBlue
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyToolsScreen(
    onBack: () -> Unit,
    repository: MainRepository,
    onNavigateToAddTool: (toolId: Int?) -> Unit,
    onNavigateToPremium: () -> Unit,
    adContent: @Composable () -> Unit = {}
) {
    val isPremium by repository.isPremiumFlow.collectAsState(initial = false)
    val allTools by repository.getAllUserTools().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var searchQuery by remember { mutableStateOf("") }
    var toolToDelete by remember { mutableStateOf<UserToolEntity?>(null) }
    var showPremiumDialog by remember { mutableStateOf(false) }

    val filteredTools = allTools.filter { tool ->
        tool.name.contains(searchQuery, ignoreCase = true) ||
                (tool.workpiece?.contains(searchQuery, ignoreCase = true) ?: false)
    }

    // Tworzymy listę z wstrzykniętymi reklamami
    val mixedList = remember(filteredTools) { createMixedList(filteredTools) }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    focusManager.clearFocus()
                    // Zmiana limitu darmowych narzędzi z 3 na 6
                    if (!isPremium && allTools.size >= 6) {
                        showPremiumDialog = true
                    } else {
                        onNavigateToAddTool(null)
                    }
                },
                containerColor = Color(0xFFFF9800),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(Res.string.add))
            }
        },
        bottomBar = {
            // Globalny banner na samym dole - Scaffold sam podniesie FAB ponad to
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
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().background(ApplicationNewBlue)) {
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

            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                // Nagłówek i wyszukiwarka
                Row(
                    modifier = Modifier.fillMaxWidth().background(ApplicationNewBlue).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White) }
                    Text("Moje Narzędzia", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
                    Spacer(modifier = Modifier.weight(1f))
                    Text("${filteredTools.size}", color = Color(0xFFBDBDBD), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(stringResource(Res.string.tool_search), color = Color(0xFFBDBDBD)) },
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = Color(0x33000000), unfocusedContainerColor = Color(0x33000000), focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent),
                    shape = RoundedCornerShape(8.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    items(
                        items = mixedList,
                        span = { item ->
                            // Jeśli to reklama, zajmij całą szerokość linii
                            if (item is MyToolsListItem.Ad) GridItemSpan(maxLineSpan)
                            else GridItemSpan(1)
                        }
                    ) { item ->
                        when (item) {
                            is MyToolsListItem.Tool -> UserToolCard(item.tool, { onNavigateToAddTool(item.tool.id) }, { toolToDelete = item.tool })
                            is MyToolsListItem.Ad -> AdMobInline()
                        }
                    }
                }
            }
        }

        if (toolToDelete != null) {
            AlertDialog(
                onDismissRequest = { toolToDelete = null },
                containerColor = ApplicationNewBlue,
                title = { Text(stringResource(Res.string.confirm_delete_one), color = Color.White, fontWeight = FontWeight.Bold) },
                text = { Text(stringResource(Res.string.confirm_delete_tool, toolToDelete?.name ?: ""), color = Color(0xFFBDBDBD)) },
                confirmButton = {
                    TextButton(onClick = {
                        toolToDelete?.let {
                            coroutineScope.launch { repository.deleteUserTool(it) }
                        }
                        toolToDelete = null
                    }) {
                        Text(stringResource(Res.string.delete), color = Color(0xFFE57373))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { toolToDelete = null }) {
                        Text(stringResource(Res.string.cancel), color = Color.White)
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
                        Text(stringResource(Res.string.premium_function), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Text(
                        text = stringResource(Res.string.tools_limit),
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
}

@Composable
fun UserToolCard(tool: UserToolEntity, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0x33000000)),
        border = BorderStroke(1.dp, Color(0x40FFFFFF)),
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize()
        ) {
            Text(
                text = tool.name,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))


            Column(modifier = Modifier.weight(1f)) {
                if (!tool.workpiece.isNullOrBlank()) {
                    Text(
                        text = stringResource(Res.string.label_workpiece).formatKmp(tool.workpiece),
                        color = Color(0xFFBDBDBD),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (!tool.f.isNullOrBlank()) {
                    Text(
                        text = stringResource(Res.string.label_feed).formatKmp(tool.f),
                        color = Color(0xFFBDBDBD),
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }

                if (!tool.s.isNullOrBlank()) {
                    Text(
                        text = stringResource(Res.string.label_speed).formatKmp(tool.s),
                        color = Color(0xFFBDBDBD),
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }

                if (!tool.notes.isNullOrBlank()) {
                    Text(
                        text = stringResource(Res.string.label_notes).formatKmp(tool.notes),
                        color = Color(0xFFBDBDBD),
                        fontSize = 12.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = Color(0x1AFFFFFF))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEditClick, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(Res.string.edit_short), tint = Color.White, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(Res.string.delete), tint = Color(0xFFE57373), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

fun String.formatKmp(vararg args: Any): String {
    var result = this
    args.forEach { arg ->
        result = result.replaceFirst("%s", arg.toString())
    }
    return result
}

sealed class MyToolsListItem {
    data class Tool(val tool: UserToolEntity) : MyToolsListItem()
    object Ad : MyToolsListItem()
}

fun createMixedList(tools: List<UserToolEntity>): List<MyToolsListItem> {
    val result = mutableListOf<MyToolsListItem>()
    tools.forEachIndexed { index, tool ->
        result.add(MyToolsListItem.Tool(tool))
        // Zmiana modulo na 2, żeby dodawać reklamę po każdym pełnym rzędzie (co dwa elementy)
        if ((index + 1) % 4 == 0) {
            result.add(MyToolsListItem.Ad)
        }
    }
    return result
}