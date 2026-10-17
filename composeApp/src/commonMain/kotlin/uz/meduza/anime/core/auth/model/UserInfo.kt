package uz.meduza.anime.core.auth.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserInfo(
    @SerialName("sub") val sub: String,
    @SerialName("name") val name: String? = null,
    @SerialName("username") val username: String? = null,
    @SerialName("email") val email: String? = null,
    @SerialName("email_verified") val emailVerified: Boolean? = null,
    @SerialName("phone_number") val phoneNumber: String? = null,
    @SerialName("picture") val picture: String? = null,
    @SerialName("avatar") val avatar: String? = null
) {
    val displayName: String
        get() = name ?: username ?: email ?: sub
}
