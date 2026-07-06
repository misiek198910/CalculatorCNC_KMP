package kalkulator.cnc.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kalkulator.cnc.data.MainRepository
import kalkulator.cnc.entity.NewsItem
import kalkulator.cnc.remote.RemoteClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.toInstant

class AppViewModel(
    private val repository: MainRepository
) : ViewModel() {

    private val _newsList = MutableStateFlow<List<NewsItem>>(emptyList())
    val newsList = _newsList.asStateFlow()

    private val _isLoadingNews = MutableStateFlow(false)
    val isLoadingNews = _isLoadingNews.asStateFlow()

    private val _newsErrorMessage = MutableStateFlow<String?>(null)
    val newsErrorMessage = _newsErrorMessage.asStateFlow()

    private val _isDbReady = MutableStateFlow(false)
    val isDbReady = _isDbReady.asStateFlow()

    private val _dbSyncProgress = MutableStateFlow(0)
    val dbSyncProgress = _dbSyncProgress.asStateFlow()

    private val _dbErrorMessage = MutableStateFlow<String?>(null)
    val dbErrorMessage = _dbErrorMessage.asStateFlow()


    fun initializeApp(systemLanguage: String) {
        viewModelScope.launch {
            loadNews()

            if (repository.hasTools()) {
                println("[APP] Baza już istnieje, pomijam synchronizację.")
                _isDbReady.value = true
            } else {
                println("[APP] Baza jest pusta, rozpoczynam synchronizację.")
                syncDatabase(systemLanguage)
            }
        }
    }

    private suspend fun syncDatabase(language: String) {
        _dbErrorMessage.value = null
        _isDbReady.value = false
        _dbSyncProgress.value = 0

        val success = repository.syncDatabase(language) { progress ->
            _dbSyncProgress.value = progress
        }

        if (success) {
            _isDbReady.value = true
        } else {
            _dbErrorMessage.value = "Nie udało się pobrać bazy danych narzędzi. Sprawdź połączenie z internetem."
        }
    }

     fun loadNews() {
        viewModelScope.launch {
            _isLoadingNews.value = true
            _newsErrorMessage.value = null
            try {
                // 1. Logujemy próbę połączenia
                println("[DEBUG] Próba pobrania newsów z API...")
                val responseList = RemoteClient.apiService.getNewsFeed()

                // 2. Sprawdzamy co przyszło
                if (responseList != null) {
                    println("[DEBUG] Otrzymano listę newsów o rozmiarze: ${responseList.size}")

                    _newsList.value = responseList.map { apiItem ->
                        // Logujemy datę dla każdego elementu, żeby sprawdzić format
                        println("[DEBUG] Parsowanie newsa: ${apiItem.title}, data: ${apiItem.publish_date}")

                        NewsItem(
                            title = apiItem.title ?: "Brak tytułu",
                            content = apiItem.content ?: "",
                            isVisible = apiItem.is_visible ?: true,
                            actionLink = apiItem.action_link,
                            imageUrl = apiItem.image_url,
                            timestamp = try {
                                apiItem.publish_date?.let { dateStr ->
                                    val isoDate = dateStr.replace(" ", "T")
                                    kotlinx.datetime.LocalDateTime.parse(isoDate)
                                        .toInstant(kotlinx.datetime.TimeZone.UTC)
                                        .toEpochMilliseconds()
                                }
                            } catch (e: Exception) {
                                println("[CNC_LOG] Błąd parsowania daty: ${e.message}")
                                null
                            }
                        )
                    }
                } else {
                    println("[DEBUG] API zwróciło NULL (błąd połączenia lub pusta odpowiedź)")
                    _newsErrorMessage.value = "Błąd: Brak danych z serwera"
                }
            } catch (e: Exception) {
                println("[DEBUG] Wyjątek w loadNews: ${e.message}")
                _newsErrorMessage.value = "Błąd połączenia: ${e.message}"
            } finally {
                _isLoadingNews.value = false
            }
        }
    }
}