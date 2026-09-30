package uz.meduza.anime.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import uz.meduza.anime.MainActivity
import uz.meduza.anime.R
import java.util.concurrent.TimeUnit

object NotificationHelper {
    const val CHANNEL_ID_PREMIERES = "meduza_premieres_channel"
    const val CHANNEL_NAME_PREMIERES = "Anime Premyeralari"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_PREMIERES,
                CHANNEL_NAME_PREMIERES,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Yangi anime va qismlar premyerasi haqida eslatmalar"
                enableVibration(true)
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showPremiereNotification(context: Context, title: String, message: String, notificationId: Int = 1001) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val largeIcon = android.graphics.BitmapFactory.decodeResource(context.resources, R.drawable.ic_meduza_logo)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_PREMIERES)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(largeIcon)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, notification)
    }

    fun schedulePremiereWorker(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicWork = PeriodicWorkRequestBuilder<PremiereNotificationWorker>(6, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "meduza_premieres_checker",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWork
        )
    }
}

class PremiereNotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val scheduleRepo = uz.meduza.anime.data.repository.ScheduleRepository(context)
            val result = scheduleRepo.getWeeklySchedule()

            if (result.isSuccess) {
                val schedule = result.getOrNull()
                val upcoming = schedule?.upcomingPremieres?.animes?.firstOrNull()

                if (upcoming != null && upcoming.secondsRemaining in 1..3600) {
                    NotificationHelper.showPremiereNotification(
                        context = context,
                        title = "🔥 Bugun Premyera: ${upcoming.title}",
                        message = "${upcoming.title} tez orada chiqadi! O'tkazib yubormang."
                    )
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
