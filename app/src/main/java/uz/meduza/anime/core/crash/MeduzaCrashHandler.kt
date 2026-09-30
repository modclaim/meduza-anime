package uz.meduza.anime.core.crash

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import timber.log.Timber
import uz.meduza.anime.MainActivity
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * MeduzaCrashHandler — Global UncaughtExceptionHandler.
 * 
 * Vazifasi:
 * 1. Ilovada sodir bo'ladigan har qanday kutilmagan istisnolarni (crash) ushlab qolish.
 * 2. Crash sababini to'liq ma'lumotlar bilan xotiraga (crash_reports.log) yozish.
 * 3. Ilovaning o'z-o'zidan chiqib ketishi (force close) oldini olib, toza holatda qayta ishga tushirish.
 */
class MeduzaCrashHandler private constructor(
    private val context: Context,
    private val defaultHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    companion object {
        private const val CRASH_FILE = "crash_reports.log"
        private const val MAX_LOG_SIZE = 1024 * 512 // 512 KB

        @Volatile
        private var instance: MeduzaCrashHandler? = null

        fun install(context: Context) {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        val currentHandler = Thread.getDefaultUncaughtExceptionHandler()
                        val handler = MeduzaCrashHandler(context.applicationContext, currentHandler)
                        Thread.setDefaultUncaughtExceptionHandler(handler)
                        instance = handler
                        Timber.i("MeduzaCrashHandler muvaffaqiyatli o'rnatildi")
                    }
                }
            }
        }

        fun getCrashLogs(context: Context): String {
            return try {
                val file = File(context.filesDir, CRASH_FILE)
                if (file.exists()) file.readText() else "Hech qanday crash qayd etilmagan."
            } catch (e: Exception) {
                "Crash loglarini o'qishda xatolik: ${e.message}"
            }
        }
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            // 1. Logcat ga xabarni yozish
            Timber.e(throwable, "FATAL CRASH INTERCEPTED on thread [%s]", thread.name)

            // 2. Crash ma'lumotlarini faylga saqlash
            saveCrashReport(thread, throwable)

            // 3. Ilovani qayta ochish (Silent auto-recovery)
            restartApp()
        } catch (e: Exception) {
            Timber.e(e, "CrashHandler ichida xatolik yuz berdi")
            defaultHandler?.uncaughtException(thread, throwable)
        } finally {
            // Jarayonni toza tugatish
            Process.killProcess(Process.myPid())
            System.exit(10)
        }
    }

    private fun saveCrashReport(thread: Thread, throwable: Throwable) {
        try {
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            throwable.printStackTrace(pw)
            val stackTrace = sw.toString()

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val dateStr = dateFormat.format(Date())

            val report = buildString {
                appendLine("==================================================")
                appendLine("TIMESTAMP: $dateStr")
                appendLine("THREAD: ${thread.name} (id: ${thread.id})")
                appendLine("EXCEPTION: ${throwable.javaClass.name}: ${throwable.message}")
                appendLine("DEVICE: ${Build.MANUFACTURER} ${Build.MODEL} (SDK ${Build.VERSION.SDK_INT})")
                appendLine("APP VERSION: 1.0.1 (code: 2)")
                appendLine("STACKTRACE:")
                appendLine(stackTrace)
                appendLine("==================================================")
                appendLine()
            }

            val file = File(context.filesDir, CRASH_FILE)
            // Log fayli 512KB dan oshib ketsa tozalab yangidan yozamiz
            if (file.exists() && file.length() > MAX_LOG_SIZE) {
                file.delete()
            }
            file.appendText(report)
        } catch (e: Exception) {
            Timber.e(e, "Crash reportni faylga yozishda xatolik")
        }
    }

    private fun restartApp() {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e, "Ilovani qayta ishga tushirishda xatolik")
        }
    }
}
