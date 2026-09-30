package uz.meduza.anime.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uz.meduza.anime.data.models.AnimeDto
import uz.meduza.anime.data.models.UpcomingAnimeDto
import uz.meduza.anime.data.models.WatchHistoryItemDto
import uz.meduza.anime.data.repository.AnimeRepository
import uz.meduza.anime.data.repository.LibraryRepository
import uz.meduza.anime.data.repository.ScheduleRepository

data class HomeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val popularAnimes: List<AnimeDto> = emptyList(),
    val latestAnimes: List<AnimeDto> = emptyList(),
    val continueWatchingList: List<WatchHistoryItemDto> = emptyList(),
    val upcomingPremieres: List<UpcomingAnimeDto> = emptyList(),
    val errorMessage: String? = null,
    val hasData: Boolean = false
) {
    val trendingAnimes: List<AnimeDto> get() = popularAnimes
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val animeRepo = AnimeRepository(application)
    private val scheduleRepo = ScheduleRepository(application)
    private val libraryRepo = LibraryRepository(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (_uiState.value.hasData && forceRefresh) {
                _uiState.value = _uiState.value.copy(isRefreshing = true)
            } else if (!_uiState.value.hasData) {
                _uiState.value = _uiState.value.copy(isLoading = true)
            }

            val maxAttempts = 3
            var lastError: Throwable? = null

            for (attempt in 1..maxAttempts) {
                try {
                    coroutineScope {
                        val popularDeferred = async { animeRepo.getTrendingAnimes(10) }
                        val latestDeferred = async { animeRepo.getLatestAnimes(10) }
                        val historyDeferred = async { libraryRepo.getWatchHistory() }
                        val scheduleDeferred = async { scheduleRepo.getWeeklySchedule() }

                        val popularResult = popularDeferred.await()
                        val latestResult = latestDeferred.await()
                        val historyResult = historyDeferred.await()
                        val scheduleResult = scheduleDeferred.await()

                        val popular = popularResult.getOrDefault(_uiState.value.popularAnimes)
                        val latest = latestResult.getOrDefault(_uiState.value.latestAnimes)
                        val history = historyResult.getOrDefault(_uiState.value.continueWatchingList)
                        val upcoming = scheduleResult.getOrNull()?.upcomingPremieres?.animes
                            ?: _uiState.value.upcomingPremieres

                        val hasError = popularResult.isFailure && latestResult.isFailure

                        _uiState.value = HomeUiState(
                            isLoading = false,
                            isRefreshing = false,
                            popularAnimes = popular,
                            latestAnimes = latest,
                            continueWatchingList = history,
                            upcomingPremieres = upcoming,
                            hasData = popular.isNotEmpty() || latest.isNotEmpty(),
                            errorMessage = if (hasError && popular.isEmpty() && latest.isEmpty())
                                (popularResult.exceptionOrNull()?.message ?: "Ma'lumotlarni yuklab bo'lmadi")
                            else null
                        )
                    }
                    return@launch
                } catch (e: Exception) {
                    lastError = e
                    if (attempt < maxAttempts) {
                        delay(600L * attempt)
                    }
                }
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isRefreshing = false,
                errorMessage = if (!_uiState.value.hasData)
                    (lastError?.message ?: "Ulanishda muammo yuz berdi. Internet aloqasini tekshiring.")
                else null
            )
        }
    }

    fun refresh() = loadHomeData(forceRefresh = true)
}
