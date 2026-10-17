package uz.meduza.anime.core.auth.storage

expect class SecureStorage() {
    suspend fun save(key: String, value: String)
    suspend fun get(key: String): String?
    suspend fun remove(key: String)
    suspend fun clear()
}
