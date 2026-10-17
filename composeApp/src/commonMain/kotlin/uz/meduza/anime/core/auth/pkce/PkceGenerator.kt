package uz.meduza.anime.core.auth.pkce

data class PkcePair(
    val codeVerifier: String,
    val codeChallenge: String,
    val codeChallengeMethod: String = "S256"
)

expect object PkceGenerator {
    fun generate(): PkcePair
}
