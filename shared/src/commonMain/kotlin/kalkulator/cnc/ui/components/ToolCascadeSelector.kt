package kalkulator.cnc.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import calculatorcnc.shared.generated.resources.Res
import calculatorcnc.shared.generated.resources.*
import kalkulator.cnc.data.viewmodel.ToolViewModel
import kalkulator.cnc.ui.theme.*
import org.jetbrains.compose.resources.*

@Composable
fun ToolCascadeSelector(
    labelStep1: StringResource,
    labelStep2: StringResource,
    isVisible: Boolean,
    viewModel: ToolViewModel,
    isPremium: Boolean,
    filteredCategories: List<String>,
    isCategoryPremium: (String) -> Boolean,
    isDiameterPremium: (Double) -> Boolean,
    isModelPremium: (String) -> Boolean,
    onPremiumClick: () -> Unit,
    onModelSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedIso by viewModel.selectedMaterialGroup.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedDiameter by viewModel.selectedDiameter.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()

    val diameters by viewModel.availableDiameters.collectAsState()
    val models by viewModel.availableModels.collectAsState()

    AnimatedVisibility(visible = isVisible, modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                IsoButton(stringResource(Res.string.P), IsoPBlue, Modifier.weight(1f), isSelected = selectedIso == "P") { viewModel.setIsoGroup("P") }
                IsoButton(stringResource(Res.string.M), IsoMYellow, Modifier.weight(1f), textColor = Color.Black, isSelected = selectedIso == "M") { viewModel.setIsoGroup("M") }
                IsoButton(stringResource(Res.string.K), IsoKRed, Modifier.weight(1f), isSelected = selectedIso == "K") { viewModel.setIsoGroup("K") }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                IsoButton(stringResource(Res.string.N), IsoNGreen, Modifier.weight(1f), isSelected = selectedIso == "N") { viewModel.setIsoGroup("N") }
                IsoButton(stringResource(Res.string.S), IsoSOrange, Modifier.weight(1f), isSelected = selectedIso == "S") { viewModel.setIsoGroup("S") }
                IsoButton(stringResource(Res.string.H), IsoHGrey, Modifier.weight(1f), isSelected = selectedIso == "H") { viewModel.setIsoGroup("H") }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredCategories.isNotEmpty() && selectedIso.isNotEmpty()) {
                DropdownSelector(
                    label = stringResource(labelStep1),
                    items = filteredCategories,
                    selectedItem = selectedCategory ?: "",
                    isItemLocked = { !isPremium && isCategoryPremium(it) },
                    onItemSelected = { viewModel.setCategory(it) },
                    onLockedClick = onPremiumClick
                )
            }

            if (selectedCategory != null && diameters.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                DropdownSelector(
                    label = stringResource(labelStep2),
                    items = diameters.map { it.toString() },
                    selectedItem = selectedDiameter?.toString() ?: "",
                    isItemLocked = { !isPremium && isDiameterPremium(it.toDoubleOrNull() ?: 0.0) },
                    onItemSelected = { viewModel.setDiameter(it.toDouble()) },
                    onLockedClick = onPremiumClick
                )
            }

            if (selectedDiameter != null && models.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                DropdownSelector(
                    label = stringResource(Res.string.step3_model),
                    items = models,
                    selectedItem = selectedModel ?: "",
                    isItemLocked = { !isPremium && isModelPremium(it) },
                    onItemSelected = {
                        viewModel.setModel(it)
                        onModelSelected(it)
                    },
                    onLockedClick = onPremiumClick
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownSelector(
    label: String,
    items: List<String>,
    selectedItem: String,
    isItemLocked: (String) -> Boolean,
    onItemSelected: (String) -> Unit,
    onLockedClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedItem,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, color = Color.LightGray) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFFFFD700),
                unfocusedBorderColor = Color(0x40FFFFFF),
                unfocusedContainerColor = Color(0x1AFFFFFF),
                focusedContainerColor = Color(0x1AFFFFFF),
            ),
            modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color(0xFF1A2B4C))
        ) {
            items.forEach { selectionOption ->
                val locked = isItemLocked(selectionOption)
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = selectionOption,
                                color = if (locked) Color.Gray else Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            if (locked) {
                                Icon(Icons.Default.Lock, contentDescription = "Premium", tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    onClick = {
                        expanded = false
                        if (locked) onLockedClick() else onItemSelected(selectionOption)
                    }
                )
            }
        }
    }
}

@Composable
private fun IsoButton(
    text: String, color: Color, modifier: Modifier = Modifier, textColor: Color = Color.White, isSelected: Boolean, onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.padding(2.dp).height(35.dp),
        colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) Color.White else color),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text = text, color = if (isSelected) Color.Black else textColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}