package uz.meduza.anime.core.session

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uz.meduza.anime.core.constants.AppConstants

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = AppConstants.DATASTORE_NAME)

class SessionManager(private val context: Context) {

    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences("meduza_session_prefs", Context.MODE_PRIVATE)

    companion object {
        private val KEY_ACCESS_TOKEN = stringPreferencesKey(AppConstants.KEY_ACCESS_TOKEN)
        private val KEY_REFRESH_TOKEN = stringPreferencesKey(AppConstants.KEY_REFRESH_TOKEN)
        private val KEY_USER_ID = stringPreferencesKey(AppConstants.KEY_USER_ID)
        private val KEY_USERNAME = stringPreferencesKey(AppConstants.KEY_USERNAME)
        private val KEY_EMAIL = stringPreferencesKey(AppConstants.KEY_EMAIL)
        private val KEY_IS_PREMIUM = booleanPreferencesKey(AppConstants.KEY_IS_PREMIUM)
        private val KEY_ROLE = stringPreferencesKey(AppConstants.KEY_USER_ROLE)
        private val KEY_AVATAR = stringPreferencesKey(AppConstants.KEY_AVATAR)

        @Volatile
        private var INSTANCE: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SessionManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    /**
     * Synchronous check for instant cold start without async DataStore delay
     */
    fun isLoggedInSync(): Boolean {
        val token = sharedPrefs.getString(AppConstants.KEY_ACCESS_TOKEN, null)
        return !token.isNullOrBlank()
    }

    fun getAccessTokenSync(): String? {
        return sharedPrefs.getString(AppConstants.KEY_ACCESS_TOKEN, null)
    }

    fun getRefreshTokenSync(): String? {
        return sharedPrefs.getString(AppConstants.KEY_REFRESH_TOKEN, null)
    }

    val accessToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_ACCESS_TOKEN] ?: sharedPrefs.getString(AppConstants.KEY_ACCESS_TOKEN, null)
    }

    val refreshToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_REFRESH_TOKEN] ?: sharedPrefs.getString(AppConstants.KEY_REFRESH_TOKEN, null)
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        val token = preferences[KEY_ACCESS_TOKEN] ?: sharedPrefs.getString(AppConstants.KEY_ACCESS_TOKEN, null)
        !token.isNullOrBlank()
    }

    val username: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_USERNAME] ?: sharedPrefs.getString(AppConstants.KEY_USERNAME, "Otaku") ?: "Otaku"
    }

    val isPremium: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_IS_PREMIUM] ?: sharedPrefs.getBoolean(AppConstants.KEY_IS_PREMIUM, false)
    }

    val userRole: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_ROLE] ?: sharedPrefs.getString(AppConstants.KEY_USER_ROLE, "USER") ?: "USER"
    }

    suspend fun saveSession(
        accessToken: String,
        refreshToken: String,
        userId: String,
        username: String,
        email: String,
        isPremium: Boolean,
        role: String = "USER",
        avatar: String? = null
    ) {
        // 1. Instant SharedPreferences write (Never lost on app restart)
        sharedPrefs.edit()
            .putString(AppConstants.KEY_ACCESS_TOKEN, accessToken)
            .putString(AppConstants.KEY_REFRESH_TOKEN, refreshToken)
            .putString(AppConstants.KEY_USER_ID, userId)
            .putString(AppConstants.KEY_USERNAME, username)
            .putString(AppConstants.KEY_EMAIL, email)
            .putBoolean(AppConstants.KEY_IS_PREMIUM, isPremium)
            .putString(AppConstants.KEY_USER_ROLE, role)
            .apply()

        // 2. DataStore write for reactive Compose state
        context.dataStore.edit { preferences ->
            preferences[KEY_ACCESS_TOKEN] = accessToken
            preferences[KEY_REFRESH_TOKEN] = refreshToken
            preferences[KEY_USER_ID] = userId
            preferences[KEY_USERNAME] = username
            preferences[KEY_EMAIL] = email
            preferences[KEY_IS_PREMIUM] = isPremium
            preferences[KEY_ROLE] = role
            if (avatar != null) preferences[KEY_AVATAR] = avatar
        }
    }

    suspend fun updateTokens(accessToken: String, refreshToken: String) {
        sharedPrefs.edit()
            .putString(AppConstants.KEY_ACCESS_TOKEN, accessToken)
            .putString(AppConstants.KEY_REFRESH_TOKEN, refreshToken)
            .apply()

        context.dataStore.edit { preferences ->
            preferences[KEY_ACCESS_TOKEN] = accessToken
            preferences[KEY_REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun updatePremiumStatus(isPremium: Boolean) {
        sharedPrefs.edit()
            .putBoolean(AppConstants.KEY_IS_PREMIUM, isPremium)
            .apply()

        context.dataStore.edit { preferences ->
            preferences[KEY_IS_PREMIUM] = isPremium
        }
    }

    suspend fun clearSession() {
        sharedPrefs.edit().clear().apply()
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    fun areNotificationsEnabled(): Boolean {
        return sharedPrefs.getBoolean("pref_notifications_enabled", false)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("pref_notifications_enabled", enabled).apply()
    }
}
