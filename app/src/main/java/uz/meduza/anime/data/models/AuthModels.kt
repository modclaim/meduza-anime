package uz.meduza.anime.data.models

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("login") val login: String,
    @SerializedName("password") val password: String,
    @SerializedName("deviceName") val deviceName: String? = null
)

data class RegisterRequest(
    @SerializedName("email") val email: String,
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("avatar") val avatar: String? = null,
    @SerializedName("deviceName") val deviceName: String? = null
)

data class DeviceNotificationRequest(
    @SerializedName("deviceName") val deviceName: String,
    @SerializedName("enabled") val enabled: Boolean,
    @SerializedName("fcmToken") val fcmToken: String? = null
)

data class RefreshTokenRequest(
    @SerializedName("refreshToken") val refreshToken: String
)

data class AuthData(
    @SerializedName("user") val user: UserDto,
    @SerializedName("tokens") val tokens: TokenDto
)

data class UserDto(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String,
    @SerializedName("username") val username: String,
    @SerializedName("avatar") val avatar: String? = null,
    @SerializedName("role") val role: String = "USER",
    @SerializedName("isPremium") val isPremium: Boolean = false,
    @SerializedName("premiumExpiresAt") val premiumExpiresAt: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("_count") val count: UserCountDto? = null
)

data class UserCountDto(
    @SerializedName("bookmarks") val bookmarks: Int = 0,
    @SerializedName("watchHistory") val watchHistory: Int = 0,
    @SerializedName("comments") val comments: Int = 0,
    @SerializedName("ratings") val ratings: Int = 0
)

data class TokenDto(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("tokenType") val tokenType: String = "Bearer"
)

data class UpdateProfileRequest(
    @SerializedName("username") val username: String? = null,
    @SerializedName("avatar") val avatar: String? = null,
    @SerializedName("currentPassword") val currentPassword: String? = null,
    @SerializedName("newPassword") val newPassword: String? = null
)

data class UpgradePremiumRequest(
    @SerializedName("days") val days: Int = 30
)

data class ChangePasswordRequest(
    @SerializedName("currentPassword") val currentPassword: String,
    @SerializedName("newPassword") val newPassword: String
)

data class SetBookmarkRequest(
    @SerializedName("animeSlug") val animeSlug: String,
    @SerializedName("status") val status: String
)
