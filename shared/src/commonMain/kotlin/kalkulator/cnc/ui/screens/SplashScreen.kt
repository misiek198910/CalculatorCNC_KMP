package kalkulator.cnc.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import calculatorcnc.shared.generated.resources.Res
import calculatorcnc.shared.generated.resources.*
import kalkulator.cnc.data.viewmodel.AppViewModel
import org.jetbrains.compose.resources.stringResource

@Composable
fun SplashScreen(
    viewModel: AppViewModel,
    onNavigateToMain: () -> Unit
) {
    val isDbReady by viewModel.isDbReady.collectAsState()
    val errorMessage by viewModel.dbErrorMessage.collectAsState()
    val currentLanguage = Locale.current.language

    LaunchedEffect(Unit) {
        viewModel.initializeApp(currentLanguage)
    }

    LaunchedEffect(isDbReady) {
        if (isDbReady) {
            onNavigateToMain()
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
                    onClick = { viewModel.initializeApp(currentLanguage) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text(stringResource(Res.string.app_database_download_try_again), color = ApplicationNewBlue)
                }
            } else {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(Res.string.app_database_download),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}