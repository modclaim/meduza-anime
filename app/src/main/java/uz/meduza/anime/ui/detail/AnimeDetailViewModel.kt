package uz.meduza.anime.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uz.meduza.anime.data.models.*
import uz.meduza.anime.data.repository.AnimeRepository
import uz.meduza.anime.data.repository.EpisodeRepository
import uz.meduza.anime.data.repository.LibraryRepository

data class AnimeDetailUiState(
    val isLoading: Boolean = true,
    val anime: AnimeDto? = null,
    val episodes: List<EpisodeDto> = emptyList(),
    val selectedSeason: Int = 1,
    val isBookmarked: Boolean = false,
    val userRating: Int? = null,
    val errorMessage: String? = null
)

class AnimeDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val animeRepo = AnimeRepository(application)
    private val episodeRepo = EpisodeRepository(application)
    private val libraryRepo = LibraryRepository(application)

    private val _uiState = MutableStateFlow(AnimeDetailUiState())
    val uiState: StateFlow<AnimeDetailUiState> = _uiState.asStateFlow()

    fun loadAnimeDetail(slug: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val animeResult = animeRepo.getAnimeDetail(slug)
            var epsResult = episodeRepo.getAnimeEpisodes(slug)

            // Retry once if episodes failed
            if (epsResult.isFailure) {
                epsResult = episodeRepo.getAnimeEpisodes(slug)
            }

            animeResult.fold(
                onSuccess = { anime ->
                    val localBookmark = libraryRepo.getBookmarkBySlug(slug)
                    val isBookmarked = (anime.userInteraction?.bookmarkStatus != null) || (localBookmark != null)
                    val userScore = anime.userInteraction?.rating
                    val eps = epsResult.getOrNull()?.episodes ?: emptyList()

                    _uiState.value = AnimeDetailUiState(
                        isLoading = false,
                        anime = anime,
                        episodes = eps,
                        selectedSeason = 1,
                        isBookmarked = isBookmarked,
                        userRating = userScore
                    )
                },
                onFailure = { err ->
                    _uiState.value = AnimeDetailUiState(
                        isLoading = false,
                        errorMessage = err.message ?: "Anime ma'lumotlarini yuklab bo'lmadi"
                    )
                }
            )
        }
    }

    fun selectSeason(slug: String, seasonNumber: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(selectedSeason = seasonNumber)
            val result = episodeRepo.getAnimeEpisodes(slug, seasonNumber)
            if (result.isSuccess) {
                val eps = result.getOrNull()?.episodes ?: emptyList()
                _uiState.value = _uiState.value.copy(episodes = eps)
            }
        }
    }

    fun toggleBookmark(slug: String, newStatus: String = "PLAN_TO_WATCH") {
        val currentBookmarked = _uiState.value.isBookmarked
        val anime = _uiState.value.anime

        viewModelScope.launch {
            // Optimistic update
            _uiState.value = _uiState.value.copy(isBookmarked = !currentBookmarked)

            if (currentBookmarked) {
                libraryRepo.removeBookmark(slug)
            } else {
                libraryRepo.setBookmark(
                    slug = slug,
                    status = newStatus,
                    title = anime?.title ?: "",
                    posterUrl = anime?.posterUrl ?: "",
                    type = anime?.type ?: "TV",
                    rating = anime?.rating ?: 0.0
                )
            }
        }
    }

    fun setBookmarkStatus(slug: String, status: String) {
        viewModelScope.launch {
            val result = libraryRepo.setBookmark(slug, status)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isBookmarked = true)
            }
        }
    }

    fun rateAnime(slug: String, score: Int) {
        viewModelScope.launch {
            val result = animeRepo.rateAnime(slug, score)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(userRating = score)
            }
        }
    }
}
