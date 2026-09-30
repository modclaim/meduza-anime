package uz.meduza.anime.domain.usecase

import android.content.Context
import uz.meduza.anime.data.models.*
import uz.meduza.anime.data.repository.EpisodeRepository
import uz.meduza.anime.data.repository.LibraryRepository
import uz.meduza.anime.data.repository.ScheduleRepository

// Episode Use Cases
class GetEpisodeStreamsUseCase(context: Context) {
    private val repository = EpisodeRepository(context)
    suspend operator fun invoke(episodeId: String): Result<EpisodeStreamsResponse> =
        repository.getEpisodeStreams(episodeId)
}

class SaveWatchProgressUseCase(context: Context) {
    private val repository = EpisodeRepository(context)
    suspend operator fun invoke(episodeId: String, stoppedAt: Int, duration: Int): Result<Unit> =
        repository.saveProgress(episodeId, stoppedAt, duration)
}

class GetEpisodeCommentsUseCase(context: Context) {
    private val repository = EpisodeRepository(context)
    suspend operator fun invoke(episodeId: String): Result<List<CommentDto>> =
        repository.getEpisodeComments(episodeId)
}

class PostCommentUseCase(context: Context) {
    private val repository = EpisodeRepository(context)
    suspend operator fun invoke(episodeId: String, content: String, isSpoiler: Boolean): Result<CommentDto> =
        repository.postComment(episodeId, content, null, isSpoiler)
}

// Library Use Cases
class GetBookmarksUseCase(context: Context) {
    private val repository = LibraryRepository(context)
    suspend operator fun invoke(status: String? = null): Result<List<BookmarkItemDto>> =
        repository.getBookmarks(status)
}

class SetBookmarkUseCase(context: Context) {
    private val repository = LibraryRepository(context)
    suspend operator fun invoke(slug: String, status: String): Result<Unit> =
        repository.setBookmark(slug, status)
}

class GetWatchHistoryUseCase(context: Context) {
    private val repository = LibraryRepository(context)
    suspend operator fun invoke(): Result<List<WatchHistoryItemDto>> =
        repository.getWatchHistory()
}

// Schedule Use Cases
class GetWeeklyScheduleUseCase(context: Context) {
    private val repository = ScheduleRepository(context)
    suspend operator fun invoke(): Result<ScheduleResponse> =
        repository.getWeeklySchedule()
}
