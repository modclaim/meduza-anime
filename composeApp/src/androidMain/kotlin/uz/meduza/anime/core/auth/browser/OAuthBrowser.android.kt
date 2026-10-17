package uz.meduza.anime.core.auth.browser

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import uz.meduza.anime.core.auth.storage.AndroidAuthContext

object AndroidOAuthSessionBridge {
    private val mutex = Mutex()
    private var pendingDeferred: CompletableDeferred<String>? = null

    suspend fun registerPendingSession(): CompletableDeferred<String> = mutex.withLock {
        pendingDeferred?.cancel()
        val deferred = CompletableDeferred<String>()
        pendingDeferred = deferred
        deferred
    }

    fun notifyCallback(callbackUrl: String) {
        pendingDeferred?.complete(callbackUrl)
        pendingDeferred = null
    }

    fun cancelPending(reason: String) {
        pendingDeferred?.completeExceptionally(IllegalStateException(reason))
        pendingDeferred = null
    }
}

actual class OAuthBrowser {

    private fun getDefaultBrowserPackage(context: Context): String? {
        val testIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://auth.nexa.uz"))
        val resolveInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.resolveActivity(
                testIntent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.resolveActivity(testIntent, PackageManager.MATCH_DEFAULT_ONLY)
        }

        val pkg = resolveInfo?.activityInfo?.packageName
        if (!pkg.isNullOrBlank() && pkg != "android" && !pkg.contains("resolver")) {
            return pkg
        }

        return CustomTabsClient.getPackageName(context, null)
    }

    actual suspend fun openAuthSession(authUrl: String, redirectUri: String): String {
        val context = AndroidAuthContext.context
        val deferred = AndroidOAuthSessionBridge.registerPendingSession()
        val defaultBrowser = getDefaultBrowserPackage(context)
        val uri = Uri.parse(authUrl)

        try {
            var launched = false

            // 1. Try launching CustomTabs with the user's default browser package
            if (!defaultBrowser.isNullOrBlank()) {
                try {
                    val customTabsIntent = CustomTabsIntent.Builder()
                        .setShowTitle(true)
                        .setUrlBarHidingEnabled(false)
                        .setShareState(CustomTabsIntent.SHARE_STATE_ON)
                        .build()
                        .apply {
                            intent.setPackage(defaultBrowser)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }

                    customTabsIntent.launchUrl(context, uri)
                    launched = true
                } catch (e: Exception) {
                    launched = false
                }
            }

            // 2. If CustomTabs with default browser failed or wasn't supported,
            // launch standard ACTION_VIEW Intent targeting the default browser
            if (!launched) {
                val browserIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    if (!defaultBrowser.isNullOrBlank()) {
                        setPackage(defaultBrowser)
                    }
                }
                context.startActivity(browserIntent)
            }

            // 5-minute timeout for user to complete sign-in in browser
            return withTimeout(300_000L) {
                deferred.await()
            }
        } catch (e: Exception) {
            AndroidOAuthSessionBridge.cancelPending("Authentication session cancelled: ${e.message}")
            throw e
        }
    }
}

