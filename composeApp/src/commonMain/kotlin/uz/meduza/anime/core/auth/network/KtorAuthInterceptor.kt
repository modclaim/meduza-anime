package uz.meduza.anime.core.auth.network

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import uz.meduza.anime.core.auth.AuthManager
import uz.meduza.anime.core.auth.model.AuthState

fun HttpClientConfig<*>.installNexaAuth(authManager: AuthManager) {
    install(Auth) {
        bearer {
            loadTokens {
                val state = authManager.authState.value
                if (state is AuthState.Authenticated) {
                    BearerTokens(
                        accessToken = state.tokens.accessToken,
                        refreshToken = state.tokens.refreshToken ?: ""
                    )
                } else null
            }
            refreshTokens {
                val newAccessToken = authManager.getValidAccessToken()
                if (newAccessToken != null) {
                    val state = authManager.authState.value
                    if (state is AuthState.Authenticated) {
                        BearerTokens(
                            accessToken = state.tokens.accessToken,
                            refreshToken = state.tokens.refreshToken ?: ""
                        )
                    } else null
                } else null
            }
        }
    }
}
