package uz.meduza.anime.core.config

import android.content.Context
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import uz.meduza.anime.core.network.ApiClient

data class AppRuntimeConfig(
    val guestEnabled: Boolean = true,
    val maintenanceEnabled: Boolean = false,
    val maintenanceTitle: String = "Texnik ishlar",
    val maintenanceMessage: String = "Ilovani yaxshilayapmiz. Birozdan keyin qayta urinib ko‘ring.",
)

// The same policy is used at navigation and repository boundaries.
object AccessPolicy {
    fun canPlay(isLoggedIn: Boolean, config: AppRuntimeConfig) =
        isLoggedIn && !config.maintenanceEnabled

    fun canBrowseAsGuest(config: AppRuntimeConfig) =
        config.guestEnabled && !config.maintenanceEnabled
}

fun parseRuntimeConfig(json: com.google.gson.JsonObject): AppRuntimeConfig? = runCatching {
    fun flag(key: String): Boolean {
        val value = json.get(key)
        require(value != null && value.isJsonPrimitive && value.asJsonPrimitive.isBoolean)
        return value.asBoolean
    }
    fun copy(key: String, max: Int): String {
        val value = json.get(key)
        require(value != null && value.isJsonPrimitive && value.asJsonPrimitive.isString)
        return value.asString.trim().also { require(it.isNotEmpty() && it.length <= max) }
    }
    AppRuntimeConfig(
        flag("guestEnabled"),
        flag("maintenanceEnabled"),
        copy("maintenanceTitle", 80),
        copy("maintenanceMessage", 500),
    )
}
    .getOrNull()

class AppConfigStore private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("meduza_app_config", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val mutex = Mutex()
    private val state =
        MutableStateFlow(
            runCatching {
                com.google.gson.JsonParser.parseString(prefs.getString("config", null))
                    .asJsonObject
                    .let(::parseRuntimeConfig)
            }
                .getOrNull() ?: AppRuntimeConfig()
        )
    val config = state.asStateFlow()

    suspend fun refresh() = mutex.withLock {
        try {
            val response = withTimeout(4500) { ApiClient.getService(appContext).getAppConfig() }
            val value = response.body()?.takeIf { it.success }?.data?.let(::parseRuntimeConfig)
            if (response.isSuccessful && value != null) {
                val clean =
                    value.copy(
                        maintenanceTitle = value.maintenanceTitle.take(80),
                        maintenanceMessage = value.maintenanceMessage.take(500),
                    )
                state.value = clean
                withContext(Dispatchers.IO) {
                    prefs.edit().putString("config", gson.toJson(clean)).apply()
                }
            }
        } catch (e: CancellationException) {
            if (e !is kotlinx.coroutines.TimeoutCancellationException) throw e
        } catch (_: Exception) {
            /* Keep the last verified settings during outages; never guess maintenance off. */
        }
    }

    companion object {
        @Volatile private var instance: AppConfigStore? = null

        fun getInstance(context: Context) =
            instance
                ?: synchronized(this) { instance ?: AppConfigStore(context).also { instance = it } }
    }
}
