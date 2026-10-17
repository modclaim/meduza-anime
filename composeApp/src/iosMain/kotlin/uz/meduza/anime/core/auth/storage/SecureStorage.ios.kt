package uz.meduza.anime.core.auth.storage

import kotlinx.cinterop.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.CoreFoundation.*
import platform.Foundation.*
import platform.Security.*

actual class SecureStorage() {
    private val serviceName = "uz.nexa.app.auth"

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun save(key: String, value: String): Unit = withContext(Dispatchers.IO) {
        remove(key)

        val valueData = (value as NSString).dataUsingEncoding(NSUTF8StringEncoding) ?: return@withContext

        val query = CFDictionaryCreateMutable(null, 5, null, null)
        CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
        CFDictionaryAddValue(query, kSecAttrService, (serviceName as NSString).bridge())
        CFDictionaryAddValue(query, kSecAttrAccount, (key as NSString).bridge())
        CFDictionaryAddValue(query, kSecValueData, valueData.bridge())
        CFDictionaryAddValue(query, kSecAttrAccessible, kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly)

        SecItemAdd(query, null)
        CFRelease(query)
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun get(key: String): String? = withContext(Dispatchers.IO) {
        val query = CFDictionaryCreateMutable(null, 6, null, null)
        CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
        CFDictionaryAddValue(query, kSecAttrService, (serviceName as NSString).bridge())
        CFDictionaryAddValue(query, kSecAttrAccount, (key as NSString).bridge())
        CFDictionaryAddValue(query, kSecReturnData, kCFBooleanTrue)
        CFDictionaryAddValue(query, kSecMatchLimit, kSecMatchLimitOne)

        memScoped {
            val result = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query, result.ptr)
            CFRelease(query)

            if (status == errSecSuccess && result.value != null) {
                val data = CFBridgingRelease(result.value) as? NSData
                if (data != null) {
                    NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
                } else null
            } else {
                null
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun remove(key: String): Unit = withContext(Dispatchers.IO) {
        val query = CFDictionaryCreateMutable(null, 3, null, null)
        CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
        CFDictionaryAddValue(query, kSecAttrService, (serviceName as NSString).bridge())
        CFDictionaryAddValue(query, kSecAttrAccount, (key as NSString).bridge())

        SecItemDelete(query)
        CFRelease(query)
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun clear(): Unit = withContext(Dispatchers.IO) {
        val query = CFDictionaryCreateMutable(null, 2, null, null)
        CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
        CFDictionaryAddValue(query, kSecAttrService, (serviceName as NSString).bridge())

        SecItemDelete(query)
        CFRelease(query)
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun Any.bridge(): CFTypeRef? = CFBridgingRetain(this)
}
