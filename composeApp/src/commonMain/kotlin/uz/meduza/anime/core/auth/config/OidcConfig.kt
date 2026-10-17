package uz.meduza.anime.core.auth.config

data class OidcConfig(
    val issuer: String = "https://auth.nexa.uz/",
    val clientId: String = "nexamobandroid01app",
    val redirectUri: String = "uz.nexa.app://callback",
    val postLogoutRedirectUri: String = "uz.nexa.app://callback",
    val scope: String = "openid profile email offline_access"
) {
    val authorizationEndpoint: String
        get() = "${issuer.trimEnd('/')}/oidc/auth"

    val tokenEndpoint: String
        get() = "${issuer.trimEnd('/')}/oidc/token"

    val userInfoEndpoint: String
        get() = "${issuer.trimEnd('/')}/oidc/me"

    val endSessionEndpoint: String
        get() = "${issuer.trimEnd('/')}/oidc/session/end"

    val revocationEndpoint: String
        get() = "${issuer.trimEnd('/')}/oidc/token/revocation"
}
