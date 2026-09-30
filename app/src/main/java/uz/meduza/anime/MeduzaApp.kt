package uz.meduza.anime

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import okhttp3.OkHttpClient
import timber.log.Timber
import uz.meduza.anime.core.crash.MeduzaCrashHandler
import uz.meduza.anime.core.notifications.NotificationHelper
import uz.meduza.anime.core.sync.SyncWorker
import uz.meduza.anime.data.local.CacheManager
import java.util.concurrent.TimeUnit

class MeduzaApp : Application(), SingletonImageLoader.Factory {

    override fun onCreate() {
        super.onCreate()

        // 1. Crash Interception & Logging (Must be first to catch any startup issues)
        MeduzaCrashHandler.install(this)
        Timber.plant(Timber.DebugTree())

        // 2. CacheManager ni ishga tushirish (SharedPreferences init)
        runCatching { CacheManager.init(this) }

        // 3. Bildirishnoma kanallari yaratish
        runCatching {
            NotificationHelper.createNotificationChannels(this)
            NotificationHelper.schedulePremiereWorker(this)
        }

        // 4. Background sync ni sozlash
        runCatching {
            SyncWorker.schedulePeriodic(this)
            SyncWorker.scheduleOnce(this)
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(25, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        return ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory(callFactory = { okHttpClient }))
            }
            .build()
    }
}
