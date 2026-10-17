package uz.meduza.anime.core.auth.browser

expect class OAuthBrowser() {
    suspend fun openAuthSession(authUrl: String, redirectUri: String): String
}
