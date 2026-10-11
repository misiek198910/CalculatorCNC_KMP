package kalkulator.cnc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import calculatorcnc.shared.generated.resources.*
import kalkulator.cnc.data.viewmodel.AppViewModel
import kalkulator.cnc.ui.theme.ApplicationNewBlue
import kalkulator.cnc.utils.LogScreenView
import org.jetbrains.compose.resources.stringResource

@Composable
fun PrivacyPolicyScreen(
    onBack: () -> Unit,
    viewModel: AppViewModel
) {
    LogScreenView(screenName = "PrivacyPolicyScreen")

    val policyText by viewModel.privacyPolicy.collectAsState()
    val isLoading by viewModel.isLoadingPolicy.collectAsState()
    val isError by viewModel.policyErrorMessage.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadPrivacyPolicy("kalkulator-cnc")
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
                .windowInsetsPadding(WindowInsets.systemBars)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ApplicationNewBlue)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = ApplicationNewBlue
                        )
                    }
                    isError -> {
                        Text(
                            text = stringResource(Res.string.app_database_download_eror),
                            color = Color.Red,
                            fontSize = 16.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    policyText != null -> {
                        Text(
                            text = policyText!!,
                            color = Color(0xFFE0E0E0),
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState())
                        )
                    }
                }
            }
        }
    }
}