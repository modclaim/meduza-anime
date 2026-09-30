package uz.meduza.anime.core.constants

object AppConstants {
    // AWS Cloud Enterprise live production API endpoint (HTTPS SSL)
    const val BASE_URL = "https://meduza.editor.voiplay.uz/api/v1/"

    // Local development fallback endpoint (Android Emulator -> Host machine)
    const val LOCAL_BASE_URL = "http://10.0.2.2:5000/api/v1/"

    const val DATASTORE_NAME = "meduza_preferences"
    const val KEY_ACCESS_TOKEN = "access_token"
    const val KEY_REFRESH_TOKEN = "refresh_token"
    const val KEY_USER_ID = "user_id"
    const val KEY_USERNAME = "username"
    const val KEY_EMAIL = "email"
    const val KEY_IS_PREMIUM = "is_premium"
    const val KEY_USER_ROLE = "user_role"
    const val KEY_AVATAR = "avatar"
}
