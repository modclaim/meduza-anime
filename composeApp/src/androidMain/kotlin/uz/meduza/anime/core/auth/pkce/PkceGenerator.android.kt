package uz.meduza.anime.core.auth.pkce

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

actual object PkceGenerator {
    private val secureRandom = SecureRandom()

    actual fun generate(): PkcePair {
        // RFC 7636: 32 random bytes encoded with URL-safe Base64 produces a 43-character string
        val randomBytes = ByteArray(32)
        secureRandom.nextBytes(randomBytes)
        val verifier = Base64.encodeToString(
            randomBytes,
            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
        )

        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(verifier.toByteArray(Charsets.US_ASCII))
        val challenge = Base64.encodeToString(
            hash,
            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
        )

        return PkcePair(
            codeVerifier = verifier,
            codeChallenge = challenge,
            codeChallengeMethod = "S256"
        )
    }
}
