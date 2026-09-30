package uz.meduza.anime.data.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uz.meduza.anime.core.network.ApiClient
import uz.meduza.anime.core.network.MeduzaApiService
import uz.meduza.anime.core.session.SessionManager
import uz.meduza.anime.data.local.CacheManager
import uz.meduza.anime.data.models.*

class AuthRepository(private val context: Context) {
    private val api: MeduzaApiService = ApiClient.getService(context)
    private val sessionManager = SessionManager.getInstance(context)

    suspend fun login(login: String, password: String): Result<AuthData> = withContext(Dispatchers.IO) {
        return@withContext try {
            val deviceName = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}".trim()
            val response = api.login(LoginRequest(login = login, password = password, deviceName = deviceName))
            if (response.isSuccessful && response.body()?.success == true) {
                val authData = response.body()!!.data!!
                // Session saqlash
                sessionManager.saveSession(
                    accessToken = authData.tokens.accessToken,
                    refreshToken = authData.tokens.refreshToken,
                    userId = authData.user.id,
                    username = authData.user.username,
                    email = authData.user.email,
                    isPremium = authData.user.isPremium,
                    role = authData.user.role,
                    avatar = authData.user.avatar
                )
                // Cache tozalash (yangi foydalanuvchi uchun)
                CacheManager.clearAllMemory()
                CacheManager.invalidateAll()
                // Sync
                uz.meduza.anime.core.sync.SyncWorker.scheduleOnce(context)
                Result.success(authData)
            } else {
                Result.failure(Exception(parseErrorMessage(response, "Login yoki parol noto'g'ri (${response.code()})")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(email: String, username: String, password: String): Result<AuthData> = withContext(Dispatchers.IO) {
        return@withContext try {
            val deviceName = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}".trim()
            val response = api.register(RegisterRequest(email = email, username = username, password = password, deviceName = deviceName))
            if (response.isSuccessful && response.body()?.success == true) {
                val authData = response.body()!!.data!!
                sessionManager.saveSession(
                    accessToken = authData.tokens.accessToken,
                    refreshToken = authData.tokens.refreshToken,
                    userId = authData.user.id,
                    username = authData.user.username,
                    email = authData.user.email,
                    isPremium = authData.user.isPremium,
                    role = authData.user.role,
                    avatar = authData.user.avatar
                )
                CacheManager.clearAllMemory()
                CacheManager.invalidateAll()
                uz.meduza.anime.core.sync.SyncWorker.scheduleOnce(context)
                Result.success(authData)
            } else {
                Result.failure(Exception(parseErrorMessage(response, "Ro'yxatdan o'tishda xatolik (${response.code()})")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        // Refresh token bilan API ga xabar berish
        val refreshToken = sessionManager.getRefreshTokenSync()
        if (!refreshToken.isNullOrBlank()) {
            runCatching { api.logout(RefreshTokenRequest(refreshToken)) }
        }
        // Session va cache tozalash
        sessionManager.clearSession()
        CacheManager.clearAllMemory()
        CacheManager.invalidateAll()
        // Sync bekor qilish
        uz.meduza.anime.core.sync.SyncWorker.cancelAll(context)
    }

    private fun <T> parseErrorMessage(response: retrofit2.Response<T>, defaultMessage: String): String {
        return try {
            val errorJson = response.errorBody()?.string()
            if (!errorJson.isNullOrBlank()) {
                val parsed = com.google.gson.JsonParser.parseString(errorJson).asJsonObject
                val errorElement = parsed.get("error") ?: parsed.get("errors")
                if (errorElement != null && errorElement.isJsonArray) {
                    val array = errorElement.asJsonArray
                    val messages = mutableListOf<String>()
                    for (item in array) {
                        if (item.isJsonObject) {
                            val msg = item.asJsonObject.get("message")?.asString
                            if (!msg.isNullOrBlank()) {
                                messages.add(msg)
                            }
                        }
                    }
                    if (messages.isNotEmpty()) {
                        return messages.first()
                    }
                }
                parsed.get("message")?.asString ?: defaultMessage
            } else {
                defaultMessage
            }
        } catch (e: Exception) {
            defaultMessage
        }
    }
}
