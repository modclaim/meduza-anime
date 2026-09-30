package uz.meduza.anime.core.network

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Authenticator
import okhttp3.Route
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import uz.meduza.anime.core.constants.AppConstants
import uz.meduza.anime.core.session.SessionManager

/**
 * TokenAuthenticator — 401 javoblarda access token ni yangilaydi.
 *
 * Xavfsizlik:
 * - Mutex bilan bir vaqtda faqat bitta refresh so'rovi bajariladi
 * - Agar token refresh vaqtida allaqachon yangi token bo'lsa — qayta refresh qilinmaydi
 * - Refresh muvaffaqiyatsiz bo'lsa — sessiya tozalanadi (login ekraniga o'tish kerak bo'ladi)
 */
class TokenAuthenticator(
    private val sessionManager: SessionManager
) : Authenticator {

    companion object {
        // Global Mutex — bir vaqtda faqat bitta thread refresh qiladi
        private val refreshMutex = Mutex()

        // Bir request uchun maksimum retry sondi
        private const val MAX_RETRY = 2
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        // Agar ikki martadan ko'p retry qilgan bo'lsa — to'xtatish
        if (responseCount(response) >= MAX_RETRY) return null

        // Sync token olish
        val currentRefreshToken = sessionManager.getRefreshTokenSync()
        if (currentRefreshToken.isNullOrBlank()) return null

        // Mutex bilan thread-safe refresh
        return runBlocking {
            refreshMutex.withLock {
                // Mutex ichida token qayta tekshirish — boshqa thread allaqachon yangilagan bo'lishi mumkin
                val accessTokenAfterLock = sessionManager.getAccessTokenSync()
                val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")?.trim()

                // Agar request dagi token bilan joriy token farqlansa — allaqachon yangilangan
                if (accessTokenAfterLock != null && requestToken != null && accessTokenAfterLock != requestToken) {
                    // Yangi token bilan retry
                    return@withLock response.request.newBuilder()
                        .header("Authorization", "Bearer $accessTokenAfterLock")
                        .build()
                }

                // Refresh tokeni ham tekshirish
                val freshRefreshToken = sessionManager.getRefreshTokenSync()
                if (freshRefreshToken.isNullOrBlank()) {
                    sessionManager.clearSession()
                    return@withLock null
                }

                // Yangi OkHttpClient (interceptorsiz — cheksiz rekursiyadan himoya)
                val client = OkHttpClient.Builder()
                    .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .build()

                val json = JSONObject().apply {
                    put("refreshToken", freshRefreshToken)
                }
                val body = json.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("${AppConstants.BASE_URL}auth/refresh")
                    .post(body)
                    .header("Content-Type", "application/json")
                    .build()

                try {
                    val refreshResponse = client.newCall(request).execute()

                    if (refreshResponse.isSuccessful) {
                        val responseBody = refreshResponse.body?.string()
                        if (!responseBody.isNullOrBlank()) {
                            val jsonObj = JSONObject(responseBody)
                            val data = jsonObj.optJSONObject("data")
                            val newAccessToken = data?.optString("accessToken")
                            val newRefreshToken = data?.optString("refreshToken")

                            if (!newAccessToken.isNullOrBlank() && !newRefreshToken.isNullOrBlank()) {
                                // Tokenlarni saqlash
                                sessionManager.updateTokens(newAccessToken, newRefreshToken)

                                // Yangi token bilan asl request ni qaytarish
                                return@withLock response.request.newBuilder()
                                    .header("Authorization", "Bearer $newAccessToken")
                                    .build()
                            }
                        }
                    }

                    // Refresh muvaffaqiyatsiz — faqat token rad etilganda (401 yoki 403) sessiyani tozalash
                    if (refreshResponse.code == 401 || refreshResponse.code == 403) {
                        sessionManager.clearSession()
                    }
                    null
                } catch (e: Exception) {
                    // Tarmoq xatolik — sessiyani o'chirmaymiz
                    null
                }
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var priorResponse = response.priorResponse
        while (priorResponse != null) {
            count++
            priorResponse = priorResponse.priorResponse
        }
        return count
    }
}
