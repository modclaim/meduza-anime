package uz.meduza.anime.core.auth.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import io.ktor.http.encodeURLParameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import uz.meduza.anime.core.auth.config.OidcConfig
import uz.meduza.anime.core.auth.model.AuthTokens
import uz.meduza.anime.core.auth.model.UserInfo
import uz.meduza.anime.core.auth.pkce.PkcePair
import uz.meduza.anime.core.auth.util.currentEpochSeconds

class AuthRepositoryImpl(
    private val config: OidcConfig = OidcConfig(),
    private val httpClient: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = false
            })
        }
    }
) : AuthRepository {

    override fun buildAuthorizeUrl(pkce: PkcePair, state: String): String {
        val base = config.authorizationEndpoint
        val queryParams = listOf(
            "client_id" to config.clientId,
            "redirect_uri" to config.redirectUri,
            "response_type" to "code",
            "scope" to config.scope,
            "state" to state,
            "code_challenge" to pkce.codeChallenge,
            "code_challenge_method" to pkce.codeChallengeMethod
        ).joinToString("&") { (key, value) ->
            "${key}=${value.encodeURLParameter()}"
        }
        return "$base?$queryParams"
    }

    override fun buildEndSessionUrl(idTokenHint: String?): String {
        val base = config.endSessionEndpoint
        val params = mutableListOf(
            "post_logout_redirect_uri" to config.postLogoutRedirectUri,
            "client_id" to config.clientId
        )
        if (idTokenHint != null) {
            params.add("id_token_hint" to idTokenHint)
        }
        val query = params.joinToString("&") { (k, v) -> "${k}=${v.encodeURLParameter()}" }
        return "$base?$query"
    }

    override suspend fun exchangeCodeForTokens(
        code: String,
        codeVerifier: String
    ): Result<AuthTokens> = runCatching {
        val response = httpClient.submitForm(
            url = config.tokenEndpoint,
            formParameters = Parameters.build {
                append("grant_type", "authorization_code")
                append("client_id", config.clientId)
                append("redirect_uri", config.redirectUri)
                append("code", code)
                append("code_verifier", codeVerifier)
            }
        )
        val tokens: AuthTokens = response.body()
        tokens.copy(issuedAtEpochSeconds = currentEpochSeconds())
    }

    override suspend fun refreshTokens(refreshToken: String): Result<AuthTokens> = runCatching {
        val response = httpClient.submitForm(
            url = config.tokenEndpoint,
            formParameters = Parameters.build {
                append("grant_type", "refresh_token")
                append("client_id", config.clientId)
                append("refresh_token", refreshToken)
            }
        )
        val tokens: AuthTokens = response.body()
        tokens.copy(
            issuedAtEpochSeconds = currentEpochSeconds(),
            refreshToken = tokens.refreshToken ?: refreshToken
        )
    }

    override suspend fun getUserInfo(accessToken: String): Result<UserInfo> = runCatching {
        val response = httpClient.get(config.userInfoEndpoint) {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        response.body()
    }

    override suspend fun revokeToken(token: String): Result<Unit> = runCatching {
        httpClient.submitForm(
            url = config.revocationEndpoint,
            formParameters = Parameters.build {
                append("client_id", config.clientId)
                append("token", token)
            }
        )
        Unit
    }
}
