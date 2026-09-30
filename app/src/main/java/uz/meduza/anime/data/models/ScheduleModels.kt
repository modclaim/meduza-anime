package uz.meduza.anime.data.models

import com.google.gson.annotations.SerializedName

data class ScheduleResponse(
    @SerializedName("currentServerTime") val currentServerTime: String,
    @SerializedName("weeklyCalendar") val weeklyCalendar: Map<String, List<ScheduledDayAnimeDto>> = emptyMap(),
    @SerializedName("upcomingPremieres") val upcomingPremieres: UpcomingPremieresDto
)

data class ScheduledDayAnimeDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("originalTitle") val originalTitle: String? = null,
    @SerializedName("slug") val slug: String,
    @SerializedName("posterUrl") val posterUrl: String,
    @SerializedName("releaseTime") val releaseTime: String? = "18:00",
    @SerializedName("rating") val rating: Double = 0.0,
    @SerializedName("genres") val genres: List<GenreDto> = emptyList()
)

data class DayScheduleResponse(
    @SerializedName("day") val day: String,
    @SerializedName("count") val count: Int,
    @SerializedName("animes") val animes: List<ScheduledDayAnimeDto>
)

data class UpcomingPremieresDto(
    @SerializedName("animes") val animes: List<UpcomingAnimeDto> = emptyList(),
    @SerializedName("episodes") val episodes: List<UpcomingEpisodeDto> = emptyList()
)

data class UpcomingAnimeDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("originalTitle") val originalTitle: String? = null,
    @SerializedName("slug") val slug: String,
    @SerializedName("posterUrl") val posterUrl: String,
    @SerializedName("bannerUrl") val bannerUrl: String? = null,
    @SerializedName("synopsis") val synopsis: String? = null,
    @SerializedName("type") val type: String = "MOVIE",
    @SerializedName("scheduledReleaseDate") val scheduledReleaseDate: String,
    @SerializedName("secondsRemaining") val secondsRemaining: Long = 0,
    @SerializedName("hoursRemaining") val hoursRemaining: Double = 0.0,
    @SerializedName("badgeText") val badgeText: String? = null,
    @SerializedName("genres") val genres: List<GenreDto> = emptyList()
)

data class UpcomingEpisodeDto(
    @SerializedName("id") val id: String,
    @SerializedName("episodeNumber") val episodeNumber: Int,
    @SerializedName("title") val title: String,
    @SerializedName("thumbnailUrl") val thumbnailUrl: String? = null,
    @SerializedName("isPremium") val isPremium: Boolean = false,
    @SerializedName("releaseDate") val releaseDate: String,
    @SerializedName("secondsRemaining") val secondsRemaining: Long = 0,
    @SerializedName("hoursRemaining") val hoursRemaining: Double = 0.0,
    @SerializedName("anime") val anime: AnimeSimpleDto,
    @SerializedName("season") val season: SeasonSimpleDto? = null
)

// Library Models
data class WatchHistoryItemDto(
    @SerializedName("id") val id: String,
    @SerializedName("stoppedAt") val stoppedAt: Int,
    @SerializedName("duration") val duration: Int,
    @SerializedName("progressPercent") val progressPercent: Int = 0,
    @SerializedName("isCompleted") val isCompleted: Boolean = false,
    @SerializedName("watchedAt") val watchedAt: String,
    @SerializedName("episode") val episode: HistoryEpisodeDto
)

data class HistoryEpisodeDto(
    @SerializedName("id") val id: String,
    @SerializedName("episodeNumber") val episodeNumber: Int,
    @SerializedName("title") val title: String,
    @SerializedName("thumbnailUrl") val thumbnailUrl: String? = null,
    @SerializedName("duration") val duration: Int = 0,
    @SerializedName("isPremium") val isPremium: Boolean = false,
    @SerializedName("season") val season: SeasonSimpleDto? = null,
    @SerializedName("anime") val anime: AnimeSimpleDto
)

data class BookmarkItemDto(
    @SerializedName("id") val id: String,
    @SerializedName("status") val status: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("anime") val anime: AnimeDto
)

data class BookmarkRequest(
    @SerializedName("status") val status: String // WATCHING, PLAN_TO_WATCH, COMPLETED, ON_HOLD, DROPPED, REMOVE
)

// Comment Models
data class CommentDto(
    @SerializedName("id") val id: String,
    @SerializedName("content") val content: String,
    @SerializedName("isSpoiler") val isSpoiler: Boolean = false,
    @SerializedName("likesCount") val likesCount: Int = 0,
    @SerializedName("isLikedByMe") val isLikedByMe: Boolean = false,
    @SerializedName("user") val user: UserDto,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("replies") val replies: List<CommentDto>? = emptyList()
)

data class CreateCommentRequest(
    @SerializedName("content") val content: String,
    @SerializedName("parentId") val parentId: String? = null,
    @SerializedName("isSpoiler") val isSpoiler: Boolean = false
)

data class ToggleLikeResponse(
    @SerializedName("commentId") val commentId: String,
    @SerializedName("isLiked") val isLiked: Boolean,
    @SerializedName("likesCount") val likesCount: Int
)

data class LibraryHistoryResponse(
    @SerializedName("items") val items: List<WatchHistoryItemDto> = emptyList(),
    @SerializedName("meta") val meta: MetaDto? = null
)

data class LibraryBookmarksResponse(
    @SerializedName("items") val items: List<BookmarkItemDto> = emptyList(),
    @SerializedName("meta") val meta: MetaDto? = null
)

data class CommentsPageResponse(
    @SerializedName("items") val items: List<CommentDto> = emptyList(),
    @SerializedName("meta") val meta: MetaDto? = null
)
