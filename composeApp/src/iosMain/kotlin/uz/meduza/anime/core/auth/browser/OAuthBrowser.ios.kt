package uz.meduza.anime.core.auth.browser

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AuthenticationServices.ASPresentationAnchor
import platform.AuthenticationServices.ASWebAuthenticationPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASWebAuthenticationSession
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

actual class OAuthBrowser() {

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun openAuthSession(authUrl: String, redirectUri: String): String =
        suspendCancellableCoroutine { continuation ->
            val nsUrl = NSURL.URLWithString(authUrl)
                ?: run {
                    continuation.resumeWithException(IllegalArgumentException("Invalid Auth URL: $authUrl"))
                    return@suspendCancellableCoroutine
                }

            // Extract callback scheme, e.g. "uz.nexa.app" from "uz.nexa.app://callback"
            val callbackScheme = redirectUri.substringBefore("://")

            val presentationProvider = object : NSObject(), ASWebAuthenticationPresentationContextProvidingProtocol {
                override fun presentationAnchorForWebAuthenticationSession(
                    session: ASWebAuthenticationSession
                ): ASPresentationAnchor {
                    return (UIApplication.sharedApplication.keyWindow
                        ?: UIApplication.sharedApplication.windows.firstOrNull() as? UIWindow)
                        ?: UIWindow()
                }
            }

            val session = ASWebAuthenticationSession(
                uRL = nsUrl,
                callbackURLScheme = callbackScheme,
                completionHandler = { callbackURL: NSURL?, error: NSError? ->
                    if (callbackURL != null) {
                        val urlString = callbackURL.absoluteString ?: ""
                        continuation.resume(urlString)
                    } else if (error != null) {
                        continuation.resumeWithException(
                            IllegalStateException("ASWebAuthenticationSession failed: ${error.localizedDescription}")
                        )
                    } else {
                        continuation.resumeWithException(
                            IllegalStateException("ASWebAuthenticationSession closed without callback")
                        )
                    }
                }
            )

            session.presentationContextProvider = presentationProvider
            session.prefersEphemeralWebBrowserSession = false

            if (!session.start()) {
                continuation.resumeWithException(IllegalStateException("Failed to start ASWebAuthenticationSession"))
            }

            continuation.invokeOnCancellation {
                session.cancel()
            }
        }
}
