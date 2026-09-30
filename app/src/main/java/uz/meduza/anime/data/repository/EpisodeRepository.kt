package uz.meduza.anime.data.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uz.meduza.anime.core.network.ApiClient
import uz.meduza.anime.core.network.MeduzaApiService
import uz.meduza.anime.data.local.CacheManager
import uz.meduza.anime.data.local.MeduzaDatabase
import uz.meduza.anime.data.models.*

/**
 * EpisodeRepository — Episode va stream ma'lumotlarini cache-first strategiya bilan boshqaradi.
 * Episodlar 1 soat davomida keshlanadi, stream ma'lumotlari keshlanmaydi (real-time kerak).
 */
class EpisodeRepository(context: Context) {
    private val api: MeduzaApiService = ApiClient.getService(context)
    private val db: MeduzaDatabase = MeduzaDatabase.getInstance(context)

    // ==================== Anime Episodes ====================
    suspend fun getAnimeEpisodes(slug: String, season: Int? = null): Result<AnimeEpisodesResponse> = withContext(Dispatchers.IO) {
        val memKey = "episodes_${slug}_${season ?: "all"}"

        // 1. Memory cache
        CacheManager.getMemory<AnimeEpisodesResponse>(memKey)?.let {
            return@withContext Result.success(it)
        }

        // 2. Network (episodlar ko'p o'zgarmaydi ama har doim fresh olamiz, keyin cache)
        return@withContext try {
            val response = api.getAnimeEpisodes(slug, season)
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data!!
                CacheManager.putMemory(memKey, data, CacheManager.TTL_EPISODES)
                CacheManager.markEpisodesCached(slug)
                Result.success(data)
            } else {
                // Memory cache bo'lsa (muddati o'tgan bo'lsa ham) qaytarish
                val staleCache = CacheManager.getMemory<AnimeEpisodesResponse>(memKey)
                if (staleCache != null) Result.success(staleCache)
                else Result.failure(Exception(response.body()?.message ?: "Qismlarni yuklab bo'lmadi"))
            }
        } catch (e: Exception) {
            val staleCache = CacheManager.getMemory<AnimeEpisodesResponse>(memKey)
            if (staleCache != null) Result.success(staleCache)
            else Result.failure(e)
        }
    }

    // ==================== Episode Detail ====================
    suspend fun getEpisodeDetail(episodeId: String): Result<EpisodeDetailDto> = withContext(Dispatchers.IO) {
        val memKey = "ep_detail_$episodeId"
        CacheManager.getMemory<EpisodeDetailDto>(memKey)?.let {
            return@withContext Result.success(it)
        }

        return@withContext try {
            val response = api.getEpisodeDetail(episodeId)
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data!!
                CacheManager.putMemory(memKey, data, 30 * 60 * 1000L) // 30 daqiqa
                Result.success(data)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Qism topilmadi"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== Episode Streams ====================
    /**
     * Stream URL lari keshlanmaydi — premium cheklov real-time tekshirilishi kerak.
     * Lekin 2 daqiqalik memory cache qo'yamiz tez-tez qayta yuklashni oldini olish uchun.
     */
    suspend fun getEpisodeStreams(episodeId: String): Result<EpisodeStreamsResponse> = withContext(Dispatchers.IO) {
        val memKey = "streams_$episodeId"
        CacheManager.getMemory<EpisodeStreamsResponse>(memKey)?.let {
            return@withContext Result.success(it)
        }

        return@withContext try {
            val response = api.getEpisodeStreams(episodeId)
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data!!
                CacheManager.putMemory(memKey, data, 2 * 60 * 1000L) // 2 daqiqa
                Result.success(data)
            } else {
                val errorMsg = response.body()?.message ?: when (response.code()) {
                    403 -> "Ushbu qism faqat Premium obunachilar uchun mavjud. Iltimos, Premium obunani faollashtiring!"
                    404 -> "Qism topilmadi yoki hali premyera qilinmagan"
                    401 -> "Iltimos, tizimga kiring"
                    else -> "Video oqimlarini yuklab bo'lmadi (${response.code()})"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== Save Progress ====================
    suspend fun saveProgress(episodeId: String, stoppedAt: Int, duration: Int): Result<Unit> = withContext(Dispatchers.IO) {
        // Avval lokal DB ga saqlash (offline support)
        try {
            db.historyDao().saveProgress(
                uz.meduza.anime.data.local.entities.WatchHistoryEntity(
                    episodeId = episodeId,
                    animeSlug = "",
                    animeTitle = "",
                    posterUrl = "",
                    episodeNumber = 0,
                    episodeTitle = "",
                    thumbnailUrl = null,
                    stoppedAt = stoppedAt,
                    duration = duration,
                    progressPercent = if (duration > 0) ((stoppedAt.toFloat() / duration) * 100).toInt() else 0,
                    watchedAt = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            // DB xatolik — davom etamiz (server ga yuboramiz)
        }

        // Server ga yuborish
        return@withContext try {
            val response = api.saveProgress(
                SaveProgressRequest(episodeId = episodeId, stoppedAt = stoppedAt, duration = duration)
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Progress saqlab bo'lmadi"))
            }
        } catch (e: Exception) {
            // Network xatolik — lokal saqlash muvaffaqiyatli bo'lgan, error qaytarmaymiz
            Result.success(Unit)
        }
    }

    // ==================== Comments ====================
    suspend fun getEpisodeComments(episodeId: String, page: Int = 1): Result<List<CommentDto>> = withContext(Dispatchers.IO) {
        val memKey = "comments_${episodeId}_$page"
        CacheManager.getMemory<List<CommentDto>>(memKey)?.let {
            return@withContext Result.success(it)
        }

        return@withContext try {
            val response = api.getEpisodeComments(episodeId, page = page)
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data ?: emptyList()
                CacheManager.putMemory(memKey, data, 60 * 1000L) // 1 daqiqa
                Result.success(data)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Izohlarni yuklab bo'lmadi"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun postComment(
        episodeId: String,
        content: String,
        parentId: String? = null,
        isSpoiler: Boolean = false
    ): Result<CommentDto> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = api.postComment(episodeId, CreateCommentRequest(content, parentId, isSpoiler))
            if (response.isSuccessful && response.body()?.success == true) {
                // Izohlar cache ni tozalash
                CacheManager.invalidateMemoryByPrefix("comments_$episodeId")
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Izoh qoldirib bo'lmadi"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleCommentLike(commentId: String): Result<ToggleLikeResponse> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = api.toggleCommentLike(commentId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Like qo'yib bo'lmadi"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
