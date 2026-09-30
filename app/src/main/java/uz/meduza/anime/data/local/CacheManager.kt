package uz.meduza.anime.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * CacheManager — Markaziy cache nazorat tizimi.
 *
 * Strategiya:
 *  1. Memory cache (fast, vaqtinchalik)
 *  2. Room DB cache (persistent, offline)
 *  3. TTL (Time-To-Live) asosida cache muddatini boshqarish
 *  4. Cache invalidation (manual va automatic)
 */
object CacheManager {

    // ==================== Cache TTL konstantalari ====================
    const val TTL_TRENDING    = 10 * 60 * 1000L   // 10 daqiqa
    const val TTL_LATEST      = 15 * 60 * 1000L   // 15 daqiqa
    const val TTL_ANIMES_LIST = 30 * 60 * 1000L   // 30 daqiqa
    const val TTL_ANIME_DETAIL = 60 * 60 * 1000L  // 1 soat
    const val TTL_EPISODES    = 60 * 60 * 1000L   // 1 soat
    const val TTL_SCHEDULE    = 30 * 60 * 1000L   // 30 daqiqa
    const val TTL_GENRES      = 24 * 60 * 60 * 1000L // 24 soat

    // ==================== Cache kalitlari ====================
    private const val PREFS_NAME = "meduza_cache_timestamps"
    private const val KEY_TRENDING_CACHED_AT = "trending_cached_at"
    private const val KEY_LATEST_CACHED_AT = "latest_cached_at"
    private const val KEY_GENRES_CACHED_AT = "genres_cached_at"
    private const val KEY_SCHEDULE_CACHED_AT = "schedule_cached_at"
    private const val PREFIX_ANIME_DETAIL = "anime_detail_"
    private const val PREFIX_EPISODES = "episodes_"

    // ==================== In-Memory cache (tezlik uchun) ====================
    private val memoryCache = mutableMapOf<String, Pair<Long, Any>>()

    @Synchronized
    fun <T : Any> putMemory(key: String, data: T, ttlMs: Long) {
        memoryCache[key] = Pair(System.currentTimeMillis() + ttlMs, data)
    }

    @Synchronized
    @Suppress("UNCHECKED_CAST")
    fun <T> getMemory(key: String): T? {
        val entry = memoryCache[key] ?: return null
        if (System.currentTimeMillis() > entry.first) {
            memoryCache.remove(key)
            return null
        }
        return entry.second as? T
    }

    @Synchronized
    fun invalidateMemory(key: String) {
        memoryCache.remove(key)
    }

    @Synchronized
    fun invalidateMemoryByPrefix(prefix: String) {
        memoryCache.keys.filter { it.startsWith(prefix) }.forEach { memoryCache.remove(it) }
    }

    @Synchronized
    fun clearAllMemory() {
        memoryCache.clear()
    }

    // ==================== SharedPreferences cache timestamps ====================
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    private fun getPrefs(): SharedPreferences {
        return prefs ?: throw IllegalStateException("CacheManager not initialized. Call CacheManager.init(context) first.")
    }

    fun isFresh(key: String, ttlMs: Long): Boolean {
        val cachedAt = getPrefs().getLong(key, 0L)
        return cachedAt > 0L && (System.currentTimeMillis() - cachedAt) < ttlMs
    }

    fun markCached(key: String) {
        getPrefs().edit().putLong(key, System.currentTimeMillis()).apply()
    }

    fun invalidate(key: String) {
        getPrefs().edit().putLong(key, 0L).apply()
    }

    fun invalidateAll() {
        getPrefs().edit().clear().apply()
        clearAllMemory()
    }

    // ==================== Qulay helper metodlar ====================
    fun isTrendingFresh() = isFresh(KEY_TRENDING_CACHED_AT, TTL_TRENDING)
    fun isLatestFresh() = isFresh(KEY_LATEST_CACHED_AT, TTL_LATEST)
    fun isGenresFresh() = isFresh(KEY_GENRES_CACHED_AT, TTL_GENRES)
    fun isScheduleFresh() = isFresh(KEY_SCHEDULE_CACHED_AT, TTL_SCHEDULE)
    fun isAnimeDetailFresh(slug: String) = isFresh("$PREFIX_ANIME_DETAIL$slug", TTL_ANIME_DETAIL)
    fun isEpisodesFresh(slug: String) = isFresh("$PREFIX_EPISODES$slug", TTL_EPISODES)

    fun markTrendingCached() = markCached(KEY_TRENDING_CACHED_AT)
    fun markLatestCached() = markCached(KEY_LATEST_CACHED_AT)
    fun markGenresCached() = markCached(KEY_GENRES_CACHED_AT)
    fun markScheduleCached() = markCached(KEY_SCHEDULE_CACHED_AT)
    fun markAnimeDetailCached(slug: String) = markCached("$PREFIX_ANIME_DETAIL$slug")
    fun markEpisodesCached(slug: String) = markCached("$PREFIX_EPISODES$slug")

    fun invalidateAnimeDetail(slug: String) {
        invalidate("$PREFIX_ANIME_DETAIL$slug")
        invalidateMemory("anime_$slug")
    }

    fun invalidateEpisodes(slug: String) {
        invalidate("$PREFIX_EPISODES$slug")
        invalidateMemory("episodes_$slug")
    }

    fun invalidateTrending() {
        invalidate(KEY_TRENDING_CACHED_AT)
        invalidateMemory("trending")
    }

    fun invalidateLatest() {
        invalidate(KEY_LATEST_CACHED_AT)
        invalidateMemory("latest")
    }

    // ==================== Eski cache tozalash ====================
    suspend fun cleanExpiredCache(db: MeduzaDatabase) = withContext(Dispatchers.IO) {
        val expireTs = System.currentTimeMillis() - TTL_ANIME_DETAIL
        db.animeDao().clearOldCache(expireTs)
    }
}
