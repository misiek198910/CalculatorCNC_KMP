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

    private fun loadNews() {
        viewModelScope.launch {
            _isLoadingNews.value = true
            _newsErrorMessage.value = null
            try {
                val responseList = RemoteClient.apiService.getNewsFeed()

                if (responseList != null) {
                    _newsList.value = responseList.map { apiItem ->
                        NewsItem(
                            title = apiItem.title ?: "Brak tytułu",
                            content = apiItem.content ?: "",
                            isVisible = apiItem.is_visible ?: true,
                            actionLink = apiItem.action_link,
                            imageUrl = apiItem.image_url,
                            timestamp = try {
                                apiItem.publish_date?.let { Instant.parse(it).toEpochMilliseconds() }
                            } catch (e: Exception) {
                                null
                            }
                        )
                    }
                } else {
                    _newsErrorMessage.value = "Błąd: Brak danych z serwera"
                }
            } catch (e: Exception) {
                _newsErrorMessage.value = "Błąd połączenia: ${e.message ?: "Nieznany błąd"}"
            } finally {
                _isLoadingNews.value = false
            }
        }
    }
}