package kalkulator.cnc.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kalkulator.cnc.data.MainRepository
import kalkulator.cnc.entity.NewsItem
import kalkulator.cnc.remote.RemoteClient
import kalkulator.cnc.utils.getLanguagePreference // <--- ZAKŁADAM, ŻE MASZ TEN IMPORT DOSTĘPNY
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

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading = _isDownloading.asStateFlow()

    private val _privacyPolicy = MutableStateFlow<String?>(null)
    val privacyPolicy = _privacyPolicy.asStateFlow()

    private val _isLoadingPolicy = MutableStateFlow(false)
    val isLoadingPolicy = _isLoadingPolicy.asStateFlow()

    private val _policyErrorMessage = MutableStateFlow<Boolean>(false)
    val policyErrorMessage = _policyErrorMessage.asStateFlow()


    fun initializeApp() {
        viewModelScope.launch {
            val appLang = getLanguagePreference()

            if (repository.hasTools()) {
                println("[APP] Baza już istnieje, pomijam synchronizację.")
                _isDbReady.value = true

                loadNews()
            } else {
                println("[APP] Baza jest pusta, pobieram bazę dla języka: $appLang")
                syncDatabase(appLang)
                loadNews()
            }
        }
    }

    private suspend fun syncDatabase(language: String) {
        _dbErrorMessage.value = null
        _isDbReady.value = false
        _dbSyncProgress.value = 0
        _isDownloading.value = true

        val success = repository.syncDatabase(language) { progress ->
            _dbSyncProgress.value = progress
        }

        _isDownloading.value = false

        if (success) {
            _isDbReady.value = true
        } else {
            _dbErrorMessage.value = "Błąd serwera. Pracujesz w trybie offline."
        }
    }

    fun loadNews() {
        viewModelScope.launch {
            _isLoadingNews.value = true
            _newsErrorMessage.value = null
            try {
                println("[DEBUG] Próba pobrania newsów z API...")
                val responseList = RemoteClient.apiService.getNewsFeed()

                if (responseList != null) {
                    println("[DEBUG] Otrzymano listę newsów o rozmiarze: ${responseList.size}")

                    _newsList.value = responseList.map { apiItem ->
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

    fun loadPrivacyPolicy(serviceName: String) {
        viewModelScope.launch {
            _isLoadingPolicy.value = true
            _policyErrorMessage.value = false

            try {
                val policy = repository.getPrivacyPolicy(serviceName)
                if (policy != null) {
                    _privacyPolicy.value = policy
                } else {
                    _policyErrorMessage.value = true
                }
            } catch (e: Exception) {
                println("[DEBUG] Wyjątek w loadPrivacyPolicy: ${e.message}")
                _policyErrorMessage.value = true
            } finally {
                _isLoadingPolicy.value = false
            }
        }
    }
}