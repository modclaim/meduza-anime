package uz.meduza.anime.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uz.meduza.anime.core.session.SessionManager
import uz.meduza.anime.data.models.*
import uz.meduza.anime.data.repository.*

data class AnimeDetailUiState(
    val isLoading: Boolean = true,
    val anime: AnimeDto? = null,
    val episodes: List<EpisodeDto> = emptyList(),
    val selectedSeason: Int = 1,
    val isBookmarked: Boolean = false,
    val userRating: Int? = null,
    val errorMessage: String? = null,
    val episodesError: String? = null,
    val actionError: String? = null,
    val isLoadingEpisodes: Boolean = false,
    val isRating: Boolean = false,
    val isBookmarking: Boolean = false,
    val ratingSaved: Int = 0,
)

class AnimeDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val animeRepo = AnimeRepository(application)
    private val episodeRepo = EpisodeRepository(application)
    private val libraryRepo = LibraryRepository(application)
    private val session = SessionManager.getInstance(application)
    private val state = MutableStateFlow(AnimeDetailUiState())
    val uiState = state.asStateFlow()
    private var loadJob: Job? = null
    private var seasonJob: Job? = null

    fun loadAnimeDetail(slug: String, canViewEpisodes: Boolean = true) {
        loadJob?.cancel()
        seasonJob?.cancel()
        loadJob = viewModelScope.launch {
            state.value = AnimeDetailUiState()
            val episodes = async {
                if (canViewEpisodes && session.isLoggedInSync()) episodeRepo.getAnimeEpisodes(slug)
                else Result.success(null)
            }
            animeRepo
                .getAnimeDetail(slug)
                .fold(
                    onSuccess = { anime ->
                        val response = episodes.await()
                        val all = response.getOrNull()?.episodes.orEmpty()
                        state.value =
                            AnimeDetailUiState(
                                isLoading = false,
                                anime = anime,
                                episodes = all,
                                selectedSeason = anime.seasons.minOfOrNull { it.seasonNumber } ?: 1,
                                isBookmarked = anime.userInteraction?.bookmarkStatus != null,
                                userRating = anime.userInteraction?.rating,
                                episodesError = response.exceptionOrNull()?.message,
                            )
                    },
                    onFailure = {
                        state.value =
                            AnimeDetailUiState(
                                isLoading = false,
                                errorMessage =
                                    "Anime ma’lumotlari yuklanmadi. Internet aloqasini tekshiring.",
                            )
                    },
                )
        }
    }

    fun selectSeason(slug: String, seasonNumber: Int) {
        if (!session.isLoggedInSync()) return
        seasonJob?.cancel()
        seasonJob = viewModelScope.launch {
            state.value =
                state.value.copy(
                    selectedSeason = seasonNumber,
                    isLoadingEpisodes = true,
                    episodesError = null,
                )
            val result = episodeRepo.getAnimeEpisodes(slug, seasonNumber)
            state.value =
                state.value.copy(
                    episodes = result.getOrNull()?.episodes.orEmpty(),
                    isLoadingEpisodes = false,
                    episodesError =
                        if (result.isFailure) "Qismlar yuklanmadi. Qayta urinib ko‘ring." else null,
                )
        }
    }

    fun toggleBookmark(slug: String, newStatus: String = "PLAN_TO_WATCH") {
        if (!session.isLoggedInSync() || state.value.isBookmarking) return
        val previous = state.value.isBookmarked
        val anime = state.value.anime ?: return
        viewModelScope.launch {
            state.value = state.value.copy(isBookmarking = true, actionError = null)
            val result =
                if (previous) libraryRepo.removeBookmark(slug)
                else
                    libraryRepo.setBookmark(
                        slug,
                        newStatus,
                        anime.title,
                        anime.posterUrl,
                        anime.type,
                        anime.rating,
                    )
            state.value =
                state.value.copy(
                    isBookmarking = false,
                    isBookmarked = if (result.isSuccess) !previous else previous,
                    actionError =
                        if (result.isFailure) "Saqlab bo‘lmadi. Qayta urinib ko‘ring." else null,
                )
        }
    }

    fun rateAnime(slug: String, score: Int) {
        if (!session.isLoggedInSync() || state.value.isRating || score !in 1..10) return
        viewModelScope.launch {
            state.value = state.value.copy(isRating = true, actionError = null)
            state.value = state.value.withRatingResult(score, animeRepo.rateAnime(slug, score))
        }
    }

    fun clearActionError() {
        state.value = state.value.copy(actionError = null)
    }
}

fun AnimeDetailUiState.withRatingResult(
    score: Int,
    result: Result<RateAnimeResponse>,
): AnimeDetailUiState =
    result.fold(
        onSuccess = { saved ->
            copy(
                isRating = false,
                actionError = null,
                userRating = score,
                anime = anime?.copy(rating = saved.animeRating, ratingCount = saved.totalRatings),
                ratingSaved = ratingSaved + 1,
            )
        },
        onFailure = {
            copy(
                isRating = false,
                actionError =
                    "Baho saqlanmadi. Hisobingiz va internet aloqasini tekshirib, qayta urinib ko‘ring.",
            )
        },
    )
