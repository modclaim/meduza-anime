package uz.meduza.anime.core.auth.model

sealed interface AuthState {
    data object Unauthenticated : AuthState
    data object Loading : AuthState
    data class Authenticated(
        val tokens: AuthTokens,
        val user: UserInfo?
    ) : AuthState
    data class Error(val message: String) : AuthState
}
