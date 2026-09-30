package uz.meduza.anime.data.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import uz.meduza.anime.core.network.ApiClient
import uz.meduza.anime.core.network.MeduzaApiService
import uz.meduza.anime.core.session.SessionManager
import uz.meduza.anime.data.local.CacheManager
import uz.meduza.anime.data.local.MeduzaDatabase
import uz.meduza.anime.data.local.entities.BookmarkEntity
import uz.meduza.anime.data.local.entities.WatchHistoryEntity
import uz.meduza.anime.data.models.*

// ==================== ScheduleRepository ====================
class ScheduleRepository(context: Context) {
    private val api: MeduzaApiService = ApiClient.getService(context)

    suspend fun getWeeklySchedule(): Result<ScheduleResponse> = withContext(Dispatchers.IO) {
        val memKey = "schedule_weekly"
        CacheManager.getMemory<ScheduleResponse>(memKey)?.let {
            return@withContext Result.success(it)
        }

        return@withContext try {
            val response = api.getWeeklySchedule()
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data!!
                CacheManager.putMemory(memKey, data, CacheManager.TTL_SCHEDULE)
                CacheManager.markScheduleCached()
                Result.success(data)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Jadvalni yuklab bo'lmadi"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getScheduleByDay(day: String): Result<DayScheduleResponse> = withContext(Dispatchers.IO) {
        val memKey = "schedule_day_$day"
        CacheManager.getMemory<DayScheduleResponse>(memKey)?.let {
            return@withContext Result.success(it)
        }

        return@withContext try {
            val response = api.getScheduleByDay(day)
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data!!
                CacheManager.putMemory(memKey, data, CacheManager.TTL_SCHEDULE)
                Result.success(data)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Jadval yuklab bo'lmadi"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// ==================== LibraryRepository ====================
class LibraryRepository(private val context: Context) {
    private val api: MeduzaApiService = ApiClient.getService(context)
    private val db: MeduzaDatabase = MeduzaDatabase.getInstance(context)
    private val historyDao = db.historyDao()
    private val bookmarkDao = db.bookmarkDao()
    private val animeDao = db.animeDao()

    // -------------------- Watch History --------------------

    /**
     * Ko'rish tarixi — Avval serverdan yuklanadi, keyin Room DB ga saqlanadi.
     * Offline bo'lsa Room DB dan qaytariladi.
     */
    suspend fun getWatchHistory(): Result<List<WatchHistoryItemDto>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = api.getWatchHistory(limit = 50)
            if (response.isSuccessful && response.body()?.success == true) {
                val items = response.body()?.data ?: emptyList()
                // Room DB ga sync qilish
                syncHistoryToLocal(items)
                Result.success(items)
            } else {
                // Offline fallback — Room DB dan
                val localHistory = runCatching { historyDao.getWatchHistory().first() }.getOrDefault(emptyList())
                Result.success(localHistory.map { it.toDto() })
            }
        } catch (e: Exception) {
            // Network xatolik — Room DB dan qaytarish
            val localHistory = runCatching { historyDao.getWatchHistory().first() }.getOrDefault(emptyList())
            Result.success(localHistory.map { it.toDto() })
        }
    }

    /** Room DB dan real-time stream (Flow) */
    fun getLocalWatchHistory(): Flow<List<WatchHistoryItemDto>> {
        return historyDao.getWatchHistory().map { list -> list.map { it.toDto() } }
    }

    suspend fun deleteHistoryItem(episodeId: String): Result<Unit> = withContext(Dispatchers.IO) {
        // Avval lokal
        historyDao.deleteItem(episodeId)
        // Keyin server
        return@withContext try {
            val response = api.deleteHistoryItem(episodeId)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception(response.body()?.message ?: "O'chirib bo'lmadi"))
        } catch (e: Exception) {
            // Lokal o'chirilgan, server xatolik — success qaytaramiz
            Result.success(Unit)
        }
    }

    suspend fun clearHistory(): Result<Unit> = withContext(Dispatchers.IO) {
        // Avval lokal
        historyDao.clearHistory()
        // Keyin server
        return@withContext try {
            val response = api.clearHistory()
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception(response.body()?.message ?: "Tarixni tozalab bo'lmadi"))
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    // -------------------- Bookmarks --------------------

    suspend fun getBookmarks(status: String? = null): Result<List<BookmarkItemDto>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = api.getBookmarks(status = status, limit = 50)
            if (response.isSuccessful && response.body()?.success == true) {
                val items = response.body()?.data ?: emptyList()
                // Room DB ga sync
                syncBookmarksToLocal(items)
                Result.success(items)
            } else {
                // Offline fallback — agar status bo'lmasa barchasi olinadi
                val localBookmarks = if (!status.isNullOrBlank()) {
                    runCatching { bookmarkDao.getBookmarksByStatus(status).first() }.getOrDefault(emptyList())
                } else {
                    runCatching { bookmarkDao.getAllBookmarks().first() }.getOrDefault(emptyList())
                }
                Result.success(localBookmarks.map { it.toDto() })
            }
        } catch (e: Exception) {
            val localBookmarks = if (!status.isNullOrBlank()) {
                runCatching { bookmarkDao.getBookmarksByStatus(status).first() }.getOrDefault(emptyList())
            } else {
                runCatching { bookmarkDao.getAllBookmarks().first() }.getOrDefault(emptyList())
            }
            Result.success(localBookmarks.map { it.toDto() })
        }
    }

    /** Room DB dan real-time bookmark stream */
    fun getLocalBookmarks(status: String? = null): Flow<List<BookmarkItemDto>> {
        return (if (!status.isNullOrBlank()) bookmarkDao.getBookmarksByStatus(status) else bookmarkDao.getAllBookmarks())
            .map { list -> list.map { it.toDto() } }
    }

    suspend fun getBookmarkBySlug(slug: String): BookmarkEntity? = withContext(Dispatchers.IO) {
        return@withContext runCatching { bookmarkDao.getBookmarkBySlug(slug) }.getOrNull()
    }

    suspend fun setBookmark(
        slug: String,
        status: String,
        title: String = "",
        posterUrl: String = "",
        type: String = "TV",
        rating: Double = 0.0
    ): Result<Unit> = withContext(Dispatchers.IO) {
        // 1. Har doim lokal Room DB ga saqlash
        runCatching {
            val cached = animeDao.getAnimeBySlug(slug)
            bookmarkDao.setBookmark(
                BookmarkEntity(
                    animeSlug = slug,
                    animeTitle = if (title.isNotBlank()) title else (cached?.title ?: slug),
                    posterUrl = if (posterUrl.isNotBlank()) posterUrl else (cached?.posterUrl ?: ""),
                    type = if (type.isNotBlank()) type else (cached?.type ?: "TV"),
                    rating = if (rating > 0.0) rating else (cached?.rating ?: 0.0),
                    status = status,
                    updatedAt = System.currentTimeMillis()
                )
            )
            CacheManager.invalidateAnimeDetail(slug)
        }

        // 2. Agar login bo'lgan bo'lsa serverga ham sync qilish
        val sessionManager = SessionManager.getInstance(context)
        if (sessionManager.isLoggedInSync()) {
            runCatching {
                api.setBookmark(SetBookmarkRequest(animeSlug = slug, status = status))
            }
        }
        return@withContext Result.success(Unit)
    }

    suspend fun removeBookmark(slug: String): Result<Unit> = withContext(Dispatchers.IO) {
        // 1. Lokal o'chirish
        runCatching {
            bookmarkDao.deleteBookmark(slug)
            CacheManager.invalidateAnimeDetail(slug)
        }
        // 2. Serverga xabar berish
        val sessionManager = SessionManager.getInstance(context)
        if (sessionManager.isLoggedInSync()) {
            runCatching { api.removeBookmark(slug) }
        }
        return@withContext Result.success(Unit)
    }

    // -------------------- Sync helpers --------------------

    private suspend fun syncHistoryToLocal(items: List<WatchHistoryItemDto>) {
        try {
            items.forEach { item ->
                historyDao.saveProgress(
                    WatchHistoryEntity(
                        episodeId = item.episode.id,
                        animeSlug = item.episode.anime.slug,
                        animeTitle = item.episode.anime.title,
                        posterUrl = item.episode.anime.posterUrl ?: "",
                        episodeNumber = item.episode.episodeNumber,
                        episodeTitle = item.episode.title,
                        thumbnailUrl = item.episode.thumbnailUrl,
                        stoppedAt = item.stoppedAt,
                        duration = item.duration,
                        progressPercent = item.progressPercent,
                        watchedAt = System.currentTimeMillis()
                    )
                )
            }
        } catch (e: Exception) {
            // Sync xatolik — ignored
        }
    }

    private suspend fun syncBookmarksToLocal(items: List<BookmarkItemDto>) {
        try {
            items.forEach { item ->
                bookmarkDao.setBookmark(
                    BookmarkEntity(
                        animeSlug = item.anime.slug,
                        animeTitle = item.anime.title,
                        posterUrl = item.anime.posterUrl,
                        type = item.anime.type,
                        rating = item.anime.rating,
                        status = item.status,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        } catch (e: Exception) {
            // Sync xatolik — ignored
        }
    }
}

// ==================== UserRepository ====================
class UserRepository(private val context: Context) {
    private val api: MeduzaApiService = ApiClient.getService(context)
    private val sessionManager = SessionManager.getInstance(context)

    suspend fun getMe(forceRefresh: Boolean = false): Result<UserDto> = withContext(Dispatchers.IO) {
        val memKey = "user_me"
        if (forceRefresh) {
            CacheManager.invalidateMemory(memKey)
        } else {
            CacheManager.getMemory<UserDto>(memKey)?.let {
                return@withContext Result.success(it)
            }
        }

        return@withContext try {
            val response = api.getMe()
            if (response.isSuccessful && response.body()?.success == true) {
                val user = response.body()!!.data!!
                sessionManager.updatePremiumStatus(user.isPremium)
                CacheManager.putMemory(memKey, user, 5 * 60 * 1000L) // 5 daqiqa
                Result.success(user)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Profil yuklab bo'lmadi"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(
        username: String? = null,
        avatar: String? = null,
        currentPass: String? = null,
        newPass: String? = null
    ): Result<UserDto> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = api.updateProfile(UpdateProfileRequest(username, avatar, currentPass, newPass))
            if (response.isSuccessful && response.body()?.success == true) {
                CacheManager.invalidateMemory("user_me")
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Profilni yangilab bo'lmadi"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun upgradePremium(days: Int = 30): Result<UserDto> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = api.upgradePremium(UpgradePremiumRequest(days))
            if (response.isSuccessful && response.body()?.success == true) {
                val user = response.body()!!.data!!
                sessionManager.updatePremiumStatus(true)
                CacheManager.invalidateMemory("user_me")
                Result.success(user)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Premium faollashtirb bo'lmadi"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAccount(): Result<Boolean> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = api.deleteAccount()
            if (response.isSuccessful && response.body()?.success == true) {
                sessionManager.clearSession()
                CacheManager.clearAllMemory()
                CacheManager.invalidateAll()
                uz.meduza.anime.core.sync.SyncWorker.cancelAll(context)
                Result.success(true)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Hisobni o'chirishda xatolik yuz berdi"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// ==================== Entity -> DTO Converters ====================

private fun WatchHistoryEntity.toDto(): WatchHistoryItemDto {
    return WatchHistoryItemDto(
        id = episodeId,
        stoppedAt = stoppedAt,
        duration = duration,
        progressPercent = progressPercent,
        isCompleted = progressPercent >= 90,
        watchedAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault())
            .format(java.util.Date(watchedAt)),
        episode = HistoryEpisodeDto(
            id = episodeId,
            episodeNumber = episodeNumber,
            title = episodeTitle,
            thumbnailUrl = thumbnailUrl,
            duration = duration,
            isPremium = false,
            season = null,
            anime = AnimeSimpleDto(
                id = animeSlug,
                title = animeTitle,
                slug = animeSlug,
                posterUrl = posterUrl
            )
        )
    )
}

private fun BookmarkEntity.toDto(): BookmarkItemDto {
    return BookmarkItemDto(
        id = animeSlug,
        status = status,
        updatedAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault())
            .format(java.util.Date(updatedAt)),
        anime = AnimeDto(
            id = animeSlug,
            title = animeTitle,
            slug = animeSlug,
            posterUrl = posterUrl,
            type = type,
            rating = rating,
            status = status
        )
    )
}
