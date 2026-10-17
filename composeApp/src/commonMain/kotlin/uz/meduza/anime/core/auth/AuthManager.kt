package uz.meduza.anime.core.auth

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import uz.meduza.anime.core.auth.browser.OAuthBrowser
import uz.meduza.anime.core.auth.config.OidcConfig
import uz.meduza.anime.core.auth.model.AuthState
import uz.meduza.anime.core.auth.model.AuthTokens
import uz.meduza.anime.core.auth.model.UserInfo
import uz.meduza.anime.core.auth.pkce.PkceGenerator
import uz.meduza.anime.core.auth.repository.AuthRepository
import uz.meduza.anime.core.auth.repository.AuthRepositoryImpl
import uz.meduza.anime.core.auth.storage.SecureStorage
import uz.meduza.anime.core.auth.util.currentEpochSeconds

class AuthManager(
    val config: OidcConfig = OidcConfig(),
    private val repository: AuthRepository = AuthRepositoryImpl(config),
    private val secureStorage: SecureStorage = SecureStorage(),
    private val oAuthBrowser: OAuthBrowser = OAuthBrowser()
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val refreshMutex = Mutex()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    companion object {
        private const val KEY_TOKENS = "oidc_auth_tokens"
        private const val KEY_USER = "oidc_user_info"
    }

    init {
        scope.launch {
            checkExistingSession()
        }
    }

    suspend fun checkExistingSession(): AuthState = refreshMutex.withLock {
        val storedJson = secureStorage.get(KEY_TOKENS)
        if (storedJson.isNullOrBlank()) {
            _authState.value = AuthState.Unauthenticated
            return AuthState.Unauthenticated
        }

        try {
            var tokens = json.decodeFromString<AuthTokens>(storedJson)
            val now = currentEpochSeconds()

            // If expired or about to expire in 60s, refresh
            if (tokens.isExpired(bufferSeconds = 60L, currentEpochSeconds = now)) {
                val refreshToken = tokens.refreshToken
                if (refreshToken != null) {
                    val refreshResult = repository.refreshTokens(refreshToken)
                    if (refreshResult.isSuccess) {
                        tokens = refreshResult.getOrThrow()
                        secureStorage.save(KEY_TOKENS, json.encodeToString(AuthTokens.serializer(), tokens))
                    } else {
                        // Refresh failed, clear session
                        secureStorage.clear()
                        _authState.value = AuthState.Unauthenticated
                        return AuthState.Unauthenticated
                    }
                } else {
                    secureStorage.clear()
                    _authState.value = AuthState.Unauthenticated
                    return AuthState.Unauthenticated
                }
            }

            // Restore user info if available
            val userJson = secureStorage.get(KEY_USER)
            val user = if (!userJson.isNullOrBlank()) {
                runCatching { json.decodeFromString<UserInfo>(userJson) }.getOrNull()
            } else null

            val authenticated = AuthState.Authenticated(tokens, user)
            _authState.value = authenticated
            return authenticated
        } catch (e: Exception) {
            secureStorage.clear()
            _authState.value = AuthState.Unauthenticated
            return AuthState.Unauthenticated
        }
    }

    suspend fun signIn(): Result<AuthTokens> {
        _authState.value = AuthState.Loading
        return runCatching {
            // 1. Generate RFC 7636 PKCE pair
            val pkce = PkceGenerator.generate()

            // 2. Generate random state for CSRF protection
            val state = PkceGenerator.generate().codeVerifier.take(16)

            // 3. Build authorization URL
            val authUrl = repository.buildAuthorizeUrl(pkce, state)

            // 4. Open native platform browser (Custom Tabs on Android, ASWebAuthenticationSession on iOS)
            val callbackUrl = oAuthBrowser.openAuthSession(authUrl, config.redirectUri)

            // 5. Parse callback URL parameters
            val callbackParams = parseQueryParams(callbackUrl)
            val code = callbackParams["code"]
                ?: error("Authorization code not found in callback response: $callbackUrl")
            val returnedState = callbackParams["state"]

            if (returnedState != state) {
                error("State mismatch (possible CSRF attack). Expected: $state, got: $returnedState")
            }

            // 6. Exchange authorization code for tokens
            val tokens = repository.exchangeCodeForTokens(code, pkce.codeVerifier).getOrThrow()

            // 7. Store tokens in hardware-encrypted secure storage
            secureStorage.save(KEY_TOKENS, json.encodeToString(AuthTokens.serializer(), tokens))

            // 8. Fetch UserInfo from Logto /oidc/me
            val userResult = repository.getUserInfo(tokens.accessToken)
            val user = userResult.getOrNull()
            if (user != null) {
                secureStorage.save(KEY_USER, json.encodeToString(UserInfo.serializer(), user))
            }

            _authState.value = AuthState.Authenticated(tokens, user)
            tokens
        }.onFailure { error ->
            _authState.value = AuthState.Error(error.message ?: "Sign-in failed")
        }
    }

    suspend fun signOut(launchBrowserEndSession: Boolean = false): Result<Unit> {
        return runCatching {
            val currentTokens = (authState.value as? AuthState.Authenticated)?.tokens
            secureStorage.clear()
            _authState.value = AuthState.Unauthenticated

            if (launchBrowserEndSession) {
                val endSessionUrl = repository.buildEndSessionUrl(currentTokens?.idToken)
                runCatching {
                    oAuthBrowser.openAuthSession(endSessionUrl, config.postLogoutRedirectUri)
                }
            }

            // Revoke refresh token if present
            currentTokens?.refreshToken?.let { rToken ->
                scope.launch {
                    repository.revokeToken(rToken)
                }
            }
            Unit
        }
    }

    suspend fun getValidAccessToken(): String? = refreshMutex.withLock {
        val current = _authState.value
        if (current !is AuthState.Authenticated) {
            val restored = checkExistingSession()
            if (restored is AuthState.Authenticated) {
                return restored.tokens.accessToken
            }
            return null
        }

        val tokens = current.tokens
        val now = currentEpochSeconds()
        if (!tokens.isExpired(bufferSeconds = 60L, currentEpochSeconds = now)) {
            return tokens.accessToken
        }

        val refreshToken = tokens.refreshToken ?: return null
        val refreshResult = repository.refreshTokens(refreshToken)
        if (refreshResult.isSuccess) {
            val newTokens = refreshResult.getOrThrow()
            secureStorage.save(KEY_TOKENS, json.encodeToString(AuthTokens.serializer(), newTokens))
            _authState.value = AuthState.Authenticated(newTokens, current.user)
            newTokens.accessToken
        } else {
            secureStorage.clear()
            _authState.value = AuthState.Unauthenticated
            null
        }
    }

    private fun parseQueryParams(url: String): Map<String, String> {
        val queryString = url.substringAfter("?", "")
        if (queryString.isEmpty()) return emptyMap()

        return queryString.split("&").mapNotNull { param ->
            val parts = param.split("=", limit = 2)
            if (parts.size == 2) parts[0] to parts[1] else null
        }.toMap()
    }
}
