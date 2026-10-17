package uz.meduza.anime.core.auth.browser

import android.content.Intent
import android.net.Uri
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
    actual suspend fun openAuthSession(authUrl: String, redirectUri: String): String {
        val context = AndroidAuthContext.context
        val deferred = AndroidOAuthSessionBridge.registerPendingSession()

        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
            .apply {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

        try {
            customTabsIntent.launchUrl(context, Uri.parse(authUrl))
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
