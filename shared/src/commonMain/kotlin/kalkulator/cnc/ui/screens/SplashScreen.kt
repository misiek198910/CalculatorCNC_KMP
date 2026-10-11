package kalkulator.cnc.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import calculatorcnc.shared.generated.resources.Res
import calculatorcnc.shared.generated.resources.*
import kalkulator.cnc.data.viewmodel.AppViewModel
import kalkulator.cnc.ui.theme.ApplicationNewBlue
import kalkulator.cnc.utils.LogScreenView
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    viewModel: AppViewModel,
    onRequestConsent: (onComplete: () -> Unit) -> Unit,
    onShowStartAd: (onComplete: () -> Unit) -> Unit,
    onNavigateToMain: () -> Unit
) {
    LogScreenView(screenName = "SplashScreen")

    val isDbReady by viewModel.isDbReady.collectAsState()
    val errorMessage by viewModel.dbErrorMessage.collectAsState()
    val dbSyncProgress by viewModel.dbSyncProgress.collectAsState()

    var minTimeElapsed by remember { mutableStateOf(false) }
    val isDownloading by viewModel.isDownloading.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.initializeApp()
        delay(2000.milliseconds)
        minTimeElapsed = true
    }

    LaunchedEffect(isDbReady, minTimeElapsed) {
        if (isDbReady && minTimeElapsed) {
            onRequestConsent {
                onShowStartAd {
                    scope.launch {
                        onNavigateToMain()
                    }
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ApplicationNewBlue
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.app_name),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(64.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: stringResource(Res.string.app_database_download_eror),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        onShowStartAd {
                            onNavigateToMain()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 48.dp)
                ) {
                    Text(stringResource(Res.string.try_offline), color = Color.LightGray)
                }
            } else {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                val loadingText = if (isDownloading) {
                    if (dbSyncProgress > 0) {
                        "${stringResource(Res.string.downloading_database)} $dbSyncProgress%"
                    } else {
                        stringResource(Res.string.downloading_database)
                    }
                } else {
                    stringResource(Res.string.loading)
                }

                Text(
                    text = loadingText,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}