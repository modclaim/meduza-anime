package uz.meduza.anime.ui.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uz.meduza.anime.data.models.*
import uz.meduza.anime.data.repository.EpisodeRepository
import timber.log.Timber

data class PlayerUiState(
    val isLoading: Boolean = true,
    val isPremiumRestricted: Boolean = false,
    val episodeDetail: EpisodeDetailDto? = null,
    val streamsResponse: EpisodeStreamsResponse? = null,
    val selectedStream: VideoStreamDto? = null,
    val selectedStreamUrl: String? = null,
    val selectedQuality: String = "AUTO_HLS",
    val selectedDub: DubTrackDto? = null,
    val selectedSubtitle: SubtitleTrackDto? = null,
    val comments: List<CommentDto> = emptyList(),
    val isPostingComment: Boolean = false,
    val savedProgressSeconds: Int = 0,
    val errorMessage: String? = null
)

class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val episodeRepo = EpisodeRepository(application)
    private var progressJob: Job? = null

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    fun loadEpisode(episodeId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                isPremiumRestricted = false
            )

            // Lokal xotiradan (Room DB va SharedPreferences) oxirgi ko'rilgan progressni tekshirish
            val localCache = try {
                uz.meduza.anime.data.local.MeduzaDatabase.getInstance(getApplication()).historyDao().getByEpisodeId(episodeId)
            } catch (e: Exception) {
                null
            }
            val sharedPrefs = getApplication<Application>().getSharedPreferences("meduza_progress", android.content.Context.MODE_PRIVATE)
            val prefsPos = sharedPrefs.getInt("pos_$episodeId", 0)
            val savedPos = maxOf(localCache?.stoppedAt ?: 0, prefsPos)

            // Parallel yuklash
            val detailResult = episodeRepo.getEpisodeDetail(episodeId)
            val streamsResult = episodeRepo.getEpisodeStreams(episodeId)
            val commentsResult = episodeRepo.getEpisodeComments(episodeId)

            val detail = detailResult.getOrNull()
            val comments = commentsResult.getOrDefault(emptyList())

            streamsResult.fold(
                onSuccess = { streamsData ->
                    // Optimal sifat tanlash: birinchi HD, keyin pastroq
                    val priorityOrder = listOf("AUTO_HLS", "P1080", "P720", "P480", "P360")
                    val bestStream = priorityOrder.firstNotNullOfOrNull { quality ->
                        streamsData.streams.firstOrNull { it.quality == quality }
                    } ?: streamsData.streams.firstOrNull()

                    val defaultDub = streamsData.dubs.firstOrNull { it.isDefault }
                        ?: streamsData.dubs.firstOrNull()
                    val defaultSub = streamsData.subtitles.firstOrNull { it.isDefault }

                    _uiState.value = PlayerUiState(
                        isLoading = false,
                        episodeDetail = detail,
                        streamsResponse = streamsData,
                        selectedStream = bestStream,
                        selectedStreamUrl = bestStream?.url,
                        selectedQuality = bestStream?.quality ?: "AUTO_HLS",
                        selectedDub = defaultDub,
                        selectedSubtitle = defaultSub,
                        comments = comments,
                        savedProgressSeconds = savedPos
                    )
                },
                onFailure = { err ->
                    val isPremium = err.message?.contains("Premium", ignoreCase = true) == true ||
                                   err.message?.contains("VIP", ignoreCase = true) == true
                    _uiState.value = PlayerUiState(
                        isLoading = false,
                        isPremiumRestricted = isPremium,
                        episodeDetail = detail,
                        comments = comments,
                        savedProgressSeconds = savedPos,
                        errorMessage = err.message
                    )
                }
            )
        }
    }

    fun selectQuality(stream: VideoStreamDto) {
        _uiState.value = _uiState.value.copy(
            selectedStream = stream,
            selectedStreamUrl = stream.url,
            selectedQuality = stream.quality
        )
    }

    fun selectDub(dub: DubTrackDto) {
        _uiState.value = _uiState.value.copy(selectedDub = dub)
    }

    fun selectSubtitle(sub: SubtitleTrackDto?) {
        _uiState.value = _uiState.value.copy(selectedSubtitle = sub)
    }

    /**
     * Progress tracking — har 10 soniyada saqlash.
     * Tarmoq yo'q bo'lsa Room DB va SharedPreferences ga lokal saqlanadi.
     */
    fun startProgressTracking(
        episodeId: String,
        getCurrentPosition: () -> Long,
        getDuration: () -> Long
    ) {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (true) {
                delay(10_000L)
                val posSec = (getCurrentPosition() / 1000).toInt()
                val durSec = (getDuration() / 1000).toInt()
                if (durSec > 0 && posSec > 0) {
                    saveCurrentProgress(episodeId, posSec, durSec)
                }
            }
        }
    }

    fun stopProgressTracking() {
        progressJob?.cancel()
        progressJob = null
    }

    fun saveCurrentProgress(episodeId: String, stoppedAt: Int, duration: Int) {
        try {
            val sharedPrefs = getApplication<Application>().getSharedPreferences("meduza_progress", android.content.Context.MODE_PRIVATE)
            sharedPrefs.edit().putInt("pos_$episodeId", stoppedAt).apply()
        } catch (_: Exception) {}
        viewModelScope.launch {
            episodeRepo.saveProgress(episodeId, stoppedAt, duration)
        }
    }

    fun postComment(episodeId: String, content: String, isSpoiler: Boolean) {
        if (content.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPostingComment = true)
            val result = episodeRepo.postComment(episodeId, content.trim(), null, isSpoiler)
            if (result.isSuccess) {
                val newComment = result.getOrNull()!!
                _uiState.value = _uiState.value.copy(
                    isPostingComment = false,
                    comments = listOf(newComment) + _uiState.value.comments
                )
            } else {
                _uiState.value = _uiState.value.copy(isPostingComment = false)
            }
        }
    }

    fun toggleCommentLike(commentId: String) {
        viewModelScope.launch {
            try {
                // Optimistic UI update for immediate response
                val currentComments = _uiState.value.comments
                val targetComment = currentComments.firstOrNull { it.id == commentId } ?: return@launch
                val willLike = !targetComment.isLikedByMe
                val adjustedCount = if (willLike) targetComment.likesCount + 1 else (targetComment.likesCount - 1).coerceAtLeast(0)

                _uiState.value = _uiState.value.copy(
                    comments = currentComments.map {
                        if (it.id == commentId) it.copy(isLikedByMe = willLike, likesCount = adjustedCount) else it
                    }
                )

                val result = episodeRepo.toggleCommentLike(commentId)
                if (result.isSuccess) {
                    val data = result.getOrNull()
                    if (data != null) {
                        _uiState.value = _uiState.value.copy(
                            comments = _uiState.value.comments.map {
                                if (it.id == commentId) it.copy(isLikedByMe = data.isLiked, likesCount = data.likesCount) else it
                            }
                        )
                    }
                } else {
                    // Revert on failure
                    _uiState.value = _uiState.value.copy(comments = currentComments)
                }
            } catch (e: Exception) {
                Timber.e(e, "toggleCommentLike failed for comment %s", commentId)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        progressJob?.cancel()
    }
}
