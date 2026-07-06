package kalkulator.cnc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import calculatorcnc.shared.generated.resources.Res
import calculatorcnc.shared.generated.resources.close
import kalkulator.cnc.ui.theme.ApplicationNewBlue
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ResultDialog(title: String, results: List<Pair<String, String>>, highlightedResult: Pair<String, String>? = null, onClose: () -> Unit) {
    val clipboardManager = LocalClipboardManager.current
    val containerColor = Color(0xE61A1A1A)
    val borderColor = Color(0xFF1976D2)
    
    var isCopied by remember { mutableStateOf(false) }

    LaunchedEffect(isCopied) {
        if (isCopied) {
            delay(2000.milliseconds)
            isCopied = false
        }
    }

    AlertDialog(
        onDismissRequest = onClose,
        containerColor = containerColor,
        modifier = Modifier.border(1.dp, borderColor, RoundedCornerShape(20.dp)),
        title = {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = title.uppercase(),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
                IconButton(
                    onClick = {
                        val textToCopy = buildString {
                            results.forEach { append("${it.first}: ${it.second}\n") }
                            highlightedResult?.let { append("\n${it.first}: ${it.second}") }
                        }.trim()

                        clipboardManager.setText(AnnotatedString(textToCopy))
                        isCopied = true
                    },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = if (isCopied) Color.Green else Color.White
                    )
                }
            }
        },
        text = {
            Column {
                results.forEach { (label, value) ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = label, color = Color(0xFFBDBDBD), fontSize = 15.sp)
                        Text(text = value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }

                highlightedResult?.let { (label, value) ->
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color(0x33FFFFFF))
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = label, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(text = value, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth().height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ApplicationNewBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(Res.string.close),
                    color = Color.White
                )
            }
        }
    )
}