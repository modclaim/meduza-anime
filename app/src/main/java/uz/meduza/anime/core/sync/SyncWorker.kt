package uz.meduza.anime.core.sync

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import uz.meduza.anime.core.session.SessionManager
import uz.meduza.anime.data.local.CacheManager
import uz.meduza.anime.data.repository.AnimeRepository
import uz.meduza.anime.data.repository.LibraryRepository
import java.util.concurrent.TimeUnit

/**
 * SyncWorker — Background sinxronizatsiya ishchisi.
 *
 * Vazifalar:
 * 1. Trending va latest animelarni yangilash
 * 2. Login bo'lgan foydalanuvchi uchun bookmark va history sinxronizatsiya
 * 3. Eski cache yozuvlarini tozalash
 *
 * Qachon ishlaydi:
 * - Har 30 daqiqada (periodic background sync)
 * - Tarmoq mavjud bo'lganda
 */
class SyncWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "meduza_sync_worker"
        const val WORK_NAME_ONCE = "meduza_sync_once"

        /**
         * Periodic sync ni sozlash — har 30 daqiqada
         */
        fun schedulePeriodic(context: Context) {
            runCatching {
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val request = PeriodicWorkRequestBuilder<SyncWorker>(
                    30, TimeUnit.MINUTES,
                    15, TimeUnit.MINUTES // flex interval
                )
                    .setConstraints(constraints)
                    .setBackoffCriteria(
                        BackoffPolicy.EXPONENTIAL,
                        15, TimeUnit.MINUTES
                    )
                    .build()

                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
            }
        }

        /**
         * Bir martalik sync — ilovani ochganda yoki tarmoq qayta ulananda
         */
        fun scheduleOnce(context: Context) {
            runCatching {
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val request = OneTimeWorkRequestBuilder<SyncWorker>()
                    .setConstraints(constraints)
                    .setBackoffCriteria(BackoffPolicy.LINEAR, 5, TimeUnit.MINUTES)
                    .build()

                WorkManager.getInstance(context).enqueueUniqueWork(
                    WORK_NAME_ONCE,
                    ExistingWorkPolicy.REPLACE,
                    request
                )
            }
        }

        /**
         * Barcha sync ishlarini bekor qilish
         */
        fun cancelAll(context: Context) {
            runCatching {
                WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
                WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME_ONCE)
            }
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val animeRepo = AnimeRepository(context)
            val sessionManager = SessionManager.getInstance(context)
            val isLoggedIn = sessionManager.isLoggedInSync()

            coroutineScope {
                // Parallel sync
                val trendingSync = async {
                    runCatching {
                        CacheManager.invalidateTrending()
                        animeRepo.getTrendingAnimes(10)
                    }
                }

                val latestSync = async {
                    runCatching {
                        CacheManager.invalidateLatest()
                        animeRepo.getLatestAnimes(10)
                    }
                }

                val librarySync = async {
                    if (isLoggedIn) {
                        runCatching {
                            val libraryRepo = LibraryRepository(context)
                            libraryRepo.getWatchHistory()
                            libraryRepo.getBookmarks()
                        }
                    }
                }

                // Barcha sync lar tugashini kutish
                trendingSync.await()
                latestSync.await()
                librarySync.await()
            }

            // Eski cache tozalash
            runCatching {
                val db = uz.meduza.anime.data.local.MeduzaDatabase.getInstance(context)
                CacheManager.cleanExpiredCache(db)
            }

            Result.success()
        } catch (e: Exception) {
            // Retry
            if (runAttemptCount < 3) Result.retry()
            else Result.failure()
        }
    }
}
