package uz.meduza.anime.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import uz.meduza.anime.core.network.ApiClient
import uz.meduza.anime.core.network.MeduzaApiService
import uz.meduza.anime.data.local.CacheManager
import uz.meduza.anime.data.local.MeduzaDatabase
import uz.meduza.anime.data.local.entities.AnimeEntity
import uz.meduza.anime.data.models.*

/**
 * AnimeRepository — Cache-First strategiya bilan:
 * 1. Avval xotira (memory) cache tekshiriladi
 * 2. Keyin Room DB (lokal) cache tekshiriladi
 * 3. Cache eskirgan yoki yo'q bo'lsa — networkdan yuklanadi va cache saqlanadi
 * 4. Network xatolik bo'lsa — mavjud cache qaytariladi (offline-first)
 */
class AnimeRepository(context: Context) {
    private val api: MeduzaApiService = ApiClient.getService(context)
    private val db: MeduzaDatabase = MeduzaDatabase.getInstance(context)
    private val animeDao = db.animeDao()
    private val gson = Gson()

    // ==================== Trending Animes ====================
    suspend fun getTrendingAnimes(limit: Int = 10): Result<List<AnimeDto>> = withContext(Dispatchers.IO) {
        // 1. Memory cache
        val memKey = "trending_$limit"
        CacheManager.getMemory<List<AnimeDto>>(memKey)?.let {
            return@withContext Result.success(it)
        }

        // 2. Room DB cache (agar fresh bo'lsa)
        if (CacheManager.isTrendingFresh()) {
            val cached = animeDao.getTrendingAnimes(limit).first()
            if (cached.isNotEmpty()) {
                val mapped = cached.map { it.toDto(gson) }
                CacheManager.putMemory(memKey, mapped, CacheManager.TTL_TRENDING)
                return@withContext Result.success(mapped)
            }
        }

        // 3. Network
        return@withContext try {
            val response = api.getTrendingAnimes(limit)
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data ?: emptyList()
                // Cache DB ga saqlash
                animeDao.insertAnimes(data.map { it.toEntity(isTrending = true) })
                CacheManager.markTrendingCached()
                CacheManager.putMemory(memKey, data, CacheManager.TTL_TRENDING)
                Result.success(data)
            } else {
                // Network xatolik — eski cache qaytarish
                val fallback = animeDao.getTrendingAnimes(limit).first().map { it.toDto(gson) }
                if (fallback.isNotEmpty()) Result.success(fallback)
                else Result.failure(Exception(response.body()?.message ?: "Trending animelarni yuklab bo'lmadi"))
            }
        } catch (e: Exception) {
            // Tarmoq yo'q — cache dan qaytarish
            val fallback = animeDao.getTrendingAnimes(limit).first().map { it.toDto(gson) }
            if (fallback.isNotEmpty()) Result.success(fallback)
            else Result.failure(e)
        }
    }

    // ==================== Latest Animes ====================
    suspend fun getLatestAnimes(limit: Int = 10): Result<List<AnimeDto>> = withContext(Dispatchers.IO) {
        val memKey = "latest_$limit"
        CacheManager.getMemory<List<AnimeDto>>(memKey)?.let {
            return@withContext Result.success(it)
        }

        if (CacheManager.isLatestFresh()) {
            val cached = animeDao.getCachedAnimes(limit).first()
            if (cached.isNotEmpty()) {
                val mapped = cached.map { it.toDto(gson) }
                CacheManager.putMemory(memKey, mapped, CacheManager.TTL_LATEST)
                return@withContext Result.success(mapped)
            }
        }

        return@withContext try {
            val response = api.getLatestAnimes(limit)
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data ?: emptyList()
                animeDao.insertAnimes(data.map { it.toEntity(isTrending = false) })
                CacheManager.markLatestCached()
                CacheManager.putMemory(memKey, data, CacheManager.TTL_LATEST)
                Result.success(data)
            } else {
                val fallback = animeDao.getCachedAnimes(limit).first().map { it.toDto(gson) }
                if (fallback.isNotEmpty()) Result.success(fallback)
                else Result.failure(Exception(response.body()?.message ?: "Yangi animelarni yuklab bo'lmadi"))
            }
        } catch (e: Exception) {
            val fallback = animeDao.getCachedAnimes(limit).first().map { it.toDto(gson) }
            if (fallback.isNotEmpty()) Result.success(fallback)
            else Result.failure(e)
        }
    }

    // ==================== Animes List ====================
    suspend fun getAnimes(
        page: Int = 1,
        limit: Int = 20,
        genre: String? = null,
        year: Int? = null,
        status: String? = null,
        type: String? = null,
        sortBy: String = "latest",
        order: String = "desc"
    ): Result<List<AnimeDto>> = withContext(Dispatchers.IO) {
        // Filter parameterlari bilan cache key
        val memKey = "animes_${page}_${limit}_${genre}_${year}_${status}_${type}_${sortBy}_${order}"
        CacheManager.getMemory<List<AnimeDto>>(memKey)?.let {
            return@withContext Result.success(it)
        }

        return@withContext try {
            val response = api.getAnimes(page, limit, genre, year, status, type, sortBy, order)
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data ?: emptyList()
                // Faqat birinchi sahifani, filter yo'q bo'lganda DB ga saqlash
                if (page == 1 && genre == null && status == null && type == null) {
                    animeDao.insertAnimes(data.map { it.toEntity(isTrending = false) })
                }
                CacheManager.putMemory(memKey, data, CacheManager.TTL_ANIMES_LIST)
                Result.success(data)
            } else {
                // Fallback — Room DB
                if (page == 1 && genre == null && status == null && type == null) {
                    val cached = animeDao.getCachedAnimes(limit).first().map { it.toDto(gson) }
                    if (cached.isNotEmpty()) return@withContext Result.success(cached)
                }
                Result.failure(Exception(response.body()?.message ?: "Animelarni yuklab bo'lmadi"))
            }
        } catch (e: Exception) {
            if (page == 1 && genre == null && status == null && type == null) {
                val cached = animeDao.getCachedAnimes(limit).first().map { it.toDto(gson) }
                if (cached.isNotEmpty()) return@withContext Result.success(cached)
            }
            Result.failure(e)
        }
    }

    // ==================== Anime Detail ====================
    suspend fun getAnimeDetail(slug: String): Result<AnimeDto> = withContext(Dispatchers.IO) {
        // 1. Memory cache
        val memKey = "anime_$slug"
        CacheManager.getMemory<AnimeDto>(memKey)?.let {
            return@withContext Result.success(it)
        }

        // 2. Room DB cache
        if (CacheManager.isAnimeDetailFresh(slug)) {
            val cached = animeDao.getAnimeBySlug(slug)
            if (cached != null) {
                val dto = cached.toDto(gson)
                CacheManager.putMemory(memKey, dto, CacheManager.TTL_ANIME_DETAIL)
                return@withContext Result.success(dto)
            }
        }

        // 3. Network
        return@withContext try {
            val response = api.getAnimeDetail(slug)
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data!!
                animeDao.insertAnimes(listOf(data.toEntity(isTrending = false)))
                CacheManager.markAnimeDetailCached(slug)
                CacheManager.putMemory(memKey, data, CacheManager.TTL_ANIME_DETAIL)
                Result.success(data)
            } else {
                // Fallback
                val cached = animeDao.getAnimeBySlug(slug)
                if (cached != null) Result.success(cached.toDto(gson))
                else Result.failure(Exception(response.body()?.message ?: "Anime topilmadi"))
            }
        } catch (e: Exception) {
            val cached = animeDao.getAnimeBySlug(slug)
            if (cached != null) Result.success(cached.toDto(gson))
            else Result.failure(e)
        }
    }

    // ==================== Search ====================
    suspend fun searchAnimes(
        query: String,
        genre: String? = null,
        year: Int? = null,
        status: String? = null,
        type: String? = null,
        sortBy: String? = null
    ): Result<List<AnimeDto>> = withContext(Dispatchers.IO) {
        val memKey = "search_${query}_${genre}_${year}_${status}_${type}_${sortBy}"
        CacheManager.getMemory<List<AnimeDto>>(memKey)?.let {
            return@withContext Result.success(it)
        }

        return@withContext try {
            val response = api.searchAnimes(query = query, genre = genre, year = year, status = status, type = type, sortBy = sortBy)
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data ?: emptyList()
                CacheManager.putMemory(memKey, data, 5 * 60 * 1000L) // 5 daqiqa
                Result.success(data)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Qidiruv muvaffaqiyatsiz"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== Genres ====================
    suspend fun getGenres(): Result<List<GenreDto>> = withContext(Dispatchers.IO) {
        val memKey = "genres"
        CacheManager.getMemory<List<GenreDto>>(memKey)?.let {
            return@withContext Result.success(it)
        }

        return@withContext try {
            val response = api.getGenres()
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data ?: emptyList()
                CacheManager.putMemory(memKey, data, CacheManager.TTL_GENRES)
                CacheManager.markGenresCached()
                Result.success(data)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Janrlarni yuklab bo'lmadi"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== Rate Anime ====================
    suspend fun rateAnime(slug: String, score: Int): Result<RateAnimeResponse> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = api.rateAnime(slug, RateAnimeRequest(score))
            if (response.isSuccessful && response.body()?.success == true) {
                // Anime detail cache ni invalidate qil (rating o'zgardi)
                CacheManager.invalidateAnimeDetail(slug)
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Baho saqlab bo'lmadi"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// ==================== Entity <-> DTO Converter Extensions ====================

private fun AnimeDto.toEntity(isTrending: Boolean): AnimeEntity {
    return AnimeEntity(
        slug = this.slug,
        id = this.id,
        title = this.title,
        originalTitle = this.originalTitle,
        englishTitle = this.englishTitle,
        synopsis = this.synopsis,
        posterUrl = this.posterUrl,
        bannerUrl = this.bannerUrl,
        type = this.type,
        status = this.status,
        year = this.year,
        rating = this.rating,
        ratingCount = this.ratingCount,
        genresJson = com.google.gson.Gson().toJson(this.genres),
        isTrending = isTrending,
        cachedAt = System.currentTimeMillis()
    )
}

private fun AnimeEntity.toDto(gson: Gson): AnimeDto {
    val genreType = object : TypeToken<List<GenreDto>>() {}.type
    val genres: List<GenreDto> = try {
        gson.fromJson(this.genresJson, genreType) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
    return AnimeDto(
        id = this.id,
        title = this.title,
        originalTitle = this.originalTitle,
        englishTitle = this.englishTitle,
        slug = this.slug,
        synopsis = this.synopsis,
        posterUrl = this.posterUrl,
        bannerUrl = this.bannerUrl,
        type = this.type,
        status = this.status,
        year = this.year,
        rating = this.rating,
        ratingCount = this.ratingCount,
        genres = genres
    )
}
