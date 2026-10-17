package uz.meduza.anime.core.auth.pkce

import kotlinx.cinterop.*
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.Foundation.NSData
import platform.Foundation.base64EncodedStringWithOptions
import platform.Foundation.create
import platform.Security.SecRandomCopyBytes
import platform.Security.kSecRandomDefault

actual object PkceGenerator {

    @OptIn(ExperimentalForeignApi::class)
    actual fun generate(): PkcePair {
        // RFC 7636: 32 cryptographically random bytes
        val randomBytes = ByteArray(32)
        randomBytes.usePinned { pinned ->
            SecRandomCopyBytes(kSecRandomDefault, 32.convert(), pinned.addressOf(0))
        }

        val verifier = toBase64Url(randomBytes)

        val verifierBytes = verifier.encodeToByteArray()
        val hash = ByteArray(CC_SHA256_DIGEST_LENGTH)
        verifierBytes.usePinned { vPinned ->
            hash.usePinned { hPinned ->
                CC_SHA256(vPinned.addressOf(0), verifierBytes.size.convert(), hPinned.addressOf(0).reinterpret())
            }
        }

        val challenge = toBase64Url(hash)

        return PkcePair(
            codeVerifier = verifier,
            codeChallenge = challenge,
            codeChallengeMethod = "S256"
        )
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun toBase64Url(bytes: ByteArray): String {
        val nsData = bytes.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = bytes.size.convert())
        }
        val base64 = nsData.base64EncodedStringWithOptions(0UL)
        return base64
            .replace("+", "-")
            .replace("/", "_")
            .trimEnd('=')
    }
}
