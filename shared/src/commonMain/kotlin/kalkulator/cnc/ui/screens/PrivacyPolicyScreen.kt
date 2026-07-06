package kalkulator.cnc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import calculatorcnc.shared.generated.resources.*
import kalkulator.cnc.data.MainRepository
import kalkulator.cnc.ui.theme.ApplicationNewBlue
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalResourceApi::class)
@Composable
fun PrivacyPolicyScreen(
    onBack: () -> Unit,
    repository: MainRepository
) {
    val systemLanguage = Locale.current.language.lowercase()

    val fileName = if (systemLanguage == "pl") "privacy_pl.html" else "privacy_en.html"

    var formattedContent by remember { mutableStateOf<AnnotatedString?>(null) }
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(fileName) {
        val bytes = Res.readBytes("files/$fileName")
        val rawHtml = bytes.decodeToString()
        formattedContent = parsePrivacyHtml(rawHtml)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsTopHeight(WindowInsets.systemBars)
                .background(ApplicationNewBlue)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars) // Odsuwamy UI od pasków
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().background(ApplicationNewBlue).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    text = stringResource(Res.string.privacy),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1E1E1E))) {
                if (formattedContent != null) {
                    ClickableText(
                        text = formattedContent!!,
                        style = TextStyle(
                            color = Color(0xFFE0E0E0),
                            fontSize = 16.sp,
                            lineHeight = 24.sp
                        ),
                        onClick = { offset ->
                            formattedContent!!.getStringAnnotations(tag = "URL", start = offset, end = offset)
                                .firstOrNull()?.let { annotation ->
                                    uriHandler.openUri(annotation.item)
                                }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    )
                } else {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = ApplicationNewBlue)
                }
            }
        }
    }
}

fun parsePrivacyHtml(html: String): AnnotatedString {
    val bodyStart = html.indexOf("<body>").takeIf { it != -1 }?.plus(6) ?: 0
    val bodyEnd = html.indexOf("</body>").takeIf { it != -1 } ?: html.length
    var body = html.substring(bodyStart, bodyEnd).trim()

    body = body.replace(Regex("<p>\\s*"), "")
        .replace(Regex("\\s*</p>"), "\n\n")
        .replace(Regex("<ul>\\s*"), "\n")
        .replace(Regex("\\s*</ul>"), "\n")
        .replace(Regex("<li>\\s*"), "• ")
        .replace(Regex("\\s*</li>"), "\n")
        .replace("<br>", "\n")
        .replace(Regex("\n{3,}"), "\n\n")

    return buildAnnotatedString {
        var currentIndex = 0
        val tagRegex = Regex("<(/?)strong>|<a href=\"([^\"]+)\">|</a>")
        val matches = tagRegex.findAll(body)

        for (match in matches) {
            append(body.substring(currentIndex, match.range.first))

            val tag = match.value
            when {
                tag == "<strong>" -> pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color.White))
                tag == "</strong>" -> pop()
                tag.startsWith("<a ") -> {
                    val url = match.groupValues[2]
                    pushStyle(SpanStyle(color = Color(0xFF64B5F6), textDecoration = TextDecoration.Underline))
                    pushStringAnnotation(tag = "URL", annotation = url)
                }
                tag == "</a>" -> pop()
            }
            currentIndex = match.range.last + 1
        }
        append(body.substring(currentIndex))
    }
}