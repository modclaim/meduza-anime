package uz.meduza.anime.core.network

import okhttp3.Interceptor
import okhttp3.Response
import uz.meduza.anime.core.session.SessionManager

class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()

        val token = sessionManager.getAccessTokenSync()

        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        requestBuilder.addHeader("Accept", "application/json")
        requestBuilder.addHeader("Content-Type", "application/json")

        val deviceName = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}".trim()
        requestBuilder.addHeader("X-Device-Name", deviceName)
        requestBuilder.addHeader("X-Device-Model", android.os.Build.MODEL)

        return chain.proceed(requestBuilder.build())
    }
}
