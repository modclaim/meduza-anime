package uz.meduza.anime.core.auth.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthTokens(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("id_token") val idToken: String? = null,
    @SerialName("token_type") val tokenType: String = "Bearer",
    @SerialName("expires_in") val expiresIn: Long = 3600L,
    @SerialName("scope") val scope: String? = null,
    val issuedAtEpochSeconds: Long = 0L
) {
    fun isExpired(
        bufferSeconds: Long = 60L,
        currentEpochSeconds: Long = uz.meduza.anime.core.auth.util.currentEpochSeconds()
    ): Boolean {
        if (issuedAtEpochSeconds == 0L) return false
        val expiresAt = issuedAtEpochSeconds + expiresIn
        return currentEpochSeconds >= (expiresAt - bufferSeconds)
    }
}
