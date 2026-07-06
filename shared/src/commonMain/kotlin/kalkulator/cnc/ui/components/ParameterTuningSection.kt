package kalkulator.cnc.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import calculatorcnc.shared.generated.resources.Res
import androidx.compose.ui.text.input.KeyboardType
import calculatorcnc.shared.generated.resources.*
import kalkulator.cnc.entity.CncToolEntity
import kotlin.math.pow
import kotlin.math.roundToInt

@Composable
fun ParameterTuningSection(
    tool: CncToolEntity?,
    vcValue: String,
    onVcValueChange: (String) -> Unit,
    isThreading: Boolean,
    modifier: Modifier = Modifier
) {
    if (tool == null) return

    // Dodajemy stan, który śledzi, czy użytkownik już coś zmienił
    var isEdited by remember { mutableStateOf(false) }

    LaunchedEffect(vcValue) {
        if (vcValue.isEmpty()) {
            isEdited = false
        }
    }

    val min = tool.vcMin
    val max = tool.vcMax
    val opt = ((min + max) / 2).roundToInt()

    val currentVc = vcValue.toDoubleOrNull() ?: 0.0

    // Logika koloru: jeśli nie edytowano -> Biały, jeśli edytowano -> licz kolor
    val vcColor = if (!isEdited) {
        Color.White
    } else {
        when {
            currentVc >= (max - 0.5) -> Color(0xFFFF5252)
            currentVc <= (min + 0.5) -> Color(0xFFFFD740)
            else -> Color(0xFF4CAF50)
        }
    }

    fun Double.format(digits: Int): String {
        val factor = 10.0.pow(digits)
        val rounded = (this * factor).roundToInt() / factor
        return rounded.toString()
    }


    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = vcValue,
            onValueChange = {
                isEdited = true
                onVcValueChange(it)
            },
            label = { Text("Vc") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = vcColor,
                unfocusedTextColor = vcColor,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color(0x40FFFFFF),
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color(0xFFBDBDBD)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val fastSettings = listOf(
                Triple("MIN", min.toInt(), Color(0xFFFFD740)),
                Triple("OPT", opt, Color(0xFF4CAF50)),
                Triple("MAX", max.toInt(), Color(0xFFFF5252))
            )

            fastSettings.forEach { (label, value, color) ->
                TextButton(onClick = {
                    isEdited = true
                    onVcValueChange(value.toString())
                }) {
                    Text(text = label, color = color, fontWeight = FontWeight.Bold)
                }
            }
        }

        val isTap = tool.toolCategory.contains("Gwint", ignoreCase = true) ||
                tool.toolCategory.contains("Wygniat", ignoreCase = true)

        if (isThreading && isTap) {
            val drillSize = if (tool.toolCategory.contains("Wygniat", ignoreCase = true)) {
                tool.diameter - (0.5 * tool.feedStep)
            } else {
                tool.diameter - tool.feedStep
            }
            Text(
                text = "${stringResource(Res.string.drill_hint_label)} Ø${drillSize.format(2)}",
                color = Color(0xFFFFD740),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp).align(Alignment.CenterHorizontally)
            )
        }
    }
}