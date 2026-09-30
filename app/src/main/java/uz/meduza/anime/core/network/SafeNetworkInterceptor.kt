package uz.meduza.anime.core.network

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import timber.log.Timber
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * SafeNetworkInterceptor — Tarmoq uzilishlari, DNS xatoliklari va timeoutlarda
 * ilovaning kutilmagan crash bo'lishining oldini oladi.
 *
 * Xatolik yuz berganda, OkHttp xom Exception tashlash o'rniga, toza va xavfsiz
 * JSON HTTP 503 javobini qaytaradi.
 */
class SafeNetworkInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        return try {
            chain.proceed(request)
        } catch (e: Exception) {
            Timber.w(e, "SafeNetworkInterceptor: Tarmoq so'rovi muvaffaqiyatsiz bo'ldi [%s]", request.url)

            val errorMessage = when (e) {
                is UnknownHostException -> "Internet aloqasi mavjud emas. Tarmoqni tekshiring."
                is SocketTimeoutException -> "Server javob berish vaqti tugadi. Qayta urinib ko'ring."
                is ConnectException -> "Serverga ulanish imkoni bo'lmadi."
                is SSLException -> "Xavfsiz ulanishda (SSL) xatolik yuz berdi."
                is IOException -> "Tarmoq bilan aloqa uzildi."
                else -> e.message ?: "Kutilmagan tarmoq xatoligi"
            }

            val fallbackJson = """
                {
                    "success": false,
                    "message": "$errorMessage",
                    "data": null,
                    "error": "${e.javaClass.simpleName}"
                }
            """.trimIndent()

            Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(503)
                .message("Service Unavailable (Handled Network Error)")
                .body(fallbackJson.toResponseBody("application/json; charset=utf-8".toMediaType()))
                .build()
        }
    }
}
