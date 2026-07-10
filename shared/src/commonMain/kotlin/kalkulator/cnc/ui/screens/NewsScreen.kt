package kalkulator.cnc.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import calculatorcnc.shared.generated.resources.Res
import calculatorcnc.shared.generated.resources.back
import calculatorcnc.shared.generated.resources.background_portrait
import calculatorcnc.shared.generated.resources.notification
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import kalkulator.cnc.data.MainRepository
import kalkulator.cnc.data.viewmodel.AppViewModel
import kalkulator.cnc.entity.NewsItem
import kalkulator.cnc.utils.LogScreenView
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource


@Composable
fun NewsScreen(onBack: () -> Unit, repository: MainRepository, adContent: @Composable () -> Unit = {}) {

    LogScreenView(screenName = "NewsScreen")

    val viewModel: AppViewModel = viewModel(factory = viewModelFactory {
        initializer {
            AppViewModel(repository)
        }
    })

    LaunchedEffect(Unit) {
        viewModel.loadNews()
    }

    val newsList by viewModel.newsList.collectAsState()
    val isLoading by viewModel.isLoadingNews.collectAsState()
    val errorMessage by viewModel.newsErrorMessage.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(Res.drawable.background_portrait),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(modifier = Modifier.fillMaxSize().background(ApplicationNewBlue.copy(alpha = 0.80f)))

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ApplicationNewBlue)
                        .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back), tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Image(painter = painterResource(Res.drawable.notification), contentDescription = null, modifier = Modifier.size(35.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Wiadomości", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = Color(0xFFFFD700)
                        )
                    } else if (!errorMessage.isNullOrEmpty()) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color.Red,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(newsList) { item ->
                                if (item.isVisible) {
                                    NewsItemCard(item)
                                }
                            }
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
    }
}

@Composable
fun NewsItemCard(item: NewsItem) {
    val formattedDate = remember(item.timestamp) {
        val ts = item.timestamp
        if (ts == null) {
            ""
        } else {
            try {
                val instant = Instant.fromEpochMilliseconds(ts)
                val timeZone: TimeZone = TimeZone.currentSystemDefault()
                val date = instant.toLocalDateTime(timeZone)

                val day = date.dayOfMonth.toString().padStart(2, '0')
                val month = date.monthNumber.toString().padStart(2, '0')
                val year = date.year

                "$day.$month.$year"
            } catch (e: Exception) {
                ""
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x1AFFFFFF)
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (!item.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = "News Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                    onState = { state ->
                        if (state is AsyncImagePainter.State.Error) {
                            println("[COIL_LOG] Błąd pobierania obrazka: ${state.result.throwable}")
                            state.result.throwable.printStackTrace()
                        }
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(
                text = item.title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            if (formattedDate.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formattedDate,
                    color = Color(0xFFBDBDBD),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.content,
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}