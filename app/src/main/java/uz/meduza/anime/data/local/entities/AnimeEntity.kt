package uz.meduza.anime.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "animes_cache")
data class AnimeEntity(
    @PrimaryKey val slug: String,
    val id: String,
    val title: String,
    val originalTitle: String?,
    val englishTitle: String?,
    val synopsis: String?,
    val posterUrl: String,
    val bannerUrl: String?,
    val type: String,
    val status: String,
    val year: Int?,
    val rating: Double,
    val ratingCount: Int,
    val genresJson: String,
    val isTrending: Boolean = false,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "watch_history_cache")
data class WatchHistoryEntity(
    @PrimaryKey val episodeId: String,
    val animeSlug: String,
    val animeTitle: String,
    val posterUrl: String,
    val episodeNumber: Int,
    val episodeTitle: String,
    val thumbnailUrl: String?,
    val stoppedAt: Int,
    val duration: Int,
    val progressPercent: Int,
    val watchedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookmarks_cache")
data class BookmarkEntity(
    @PrimaryKey val animeSlug: String,
    val animeTitle: String,
    val posterUrl: String,
    val type: String,
    val rating: Double,
    val status: String, // WATCHING, PLAN_TO_WATCH, COMPLETED
    val updatedAt: Long = System.currentTimeMillis()
)
