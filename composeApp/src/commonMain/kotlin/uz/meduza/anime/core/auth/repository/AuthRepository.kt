package uz.meduza.anime.core.auth.repository

import uz.meduza.anime.core.auth.model.AuthTokens
import uz.meduza.anime.core.auth.model.UserInfo
import uz.meduza.anime.core.auth.pkce.PkcePair

interface AuthRepository {
    fun buildAuthorizeUrl(pkce: PkcePair, state: String): String
    fun buildEndSessionUrl(idTokenHint: String? = null): String
    suspend fun exchangeCodeForTokens(code: String, codeVerifier: String): Result<AuthTokens>
    suspend fun refreshTokens(refreshToken: String): Result<AuthTokens>
    suspend fun getUserInfo(accessToken: String): Result<UserInfo>
    suspend fun revokeToken(token: String): Result<Unit>
}
