package uz.meduza.anime.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import uz.meduza.anime.data.local.entities.AnimeEntity
import uz.meduza.anime.data.local.entities.BookmarkEntity
import uz.meduza.anime.data.local.entities.WatchHistoryEntity

@Dao
interface AnimeDao {
    @Query("SELECT * FROM animes_cache ORDER BY cachedAt DESC LIMIT :limit")
    fun getCachedAnimes(limit: Int = 20): Flow<List<AnimeEntity>>

    @Query("SELECT * FROM animes_cache WHERE isTrending = 1 ORDER BY rating DESC LIMIT :limit")
    fun getTrendingAnimes(limit: Int = 10): Flow<List<AnimeEntity>>

    @Query("SELECT * FROM animes_cache WHERE slug = :slug LIMIT 1")
    suspend fun getAnimeBySlug(slug: String): AnimeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnimes(animes: List<AnimeEntity>)

    @Query("DELETE FROM animes_cache WHERE cachedAt < :expireTimestamp")
    suspend fun clearOldCache(expireTimestamp: Long)

    @Query("SELECT COUNT(*) FROM animes_cache")
    suspend fun getCacheCount(): Int
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM watch_history_cache ORDER BY watchedAt DESC")
    fun getWatchHistory(): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history_cache WHERE episodeId = :episodeId LIMIT 1")
    suspend fun getByEpisodeId(episodeId: String): WatchHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(item: WatchHistoryEntity)

    @Query("DELETE FROM watch_history_cache WHERE episodeId = :episodeId")
    suspend fun deleteItem(episodeId: String)

    @Query("DELETE FROM watch_history_cache")
    suspend fun clearHistory()

    @Query("SELECT COUNT(*) FROM watch_history_cache")
    suspend fun getHistoryCount(): Int
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks_cache ORDER BY updatedAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks_cache WHERE status = :status ORDER BY updatedAt DESC")
    fun getBookmarksByStatus(status: String): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks_cache WHERE animeSlug = :slug LIMIT 1")
    suspend fun getBookmarkBySlug(slug: String): BookmarkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setBookmark(item: BookmarkEntity)

    @Query("DELETE FROM bookmarks_cache WHERE animeSlug = :slug")
    suspend fun deleteBookmark(slug: String)

    @Query("DELETE FROM bookmarks_cache")
    suspend fun clearAll()
}
