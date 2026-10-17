package uz.meduza.anime.core.auth.browser

import android.app.Activity
import android.content.Intent
import android.os.Bundle

class NexaAuthCallbackActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val uri = intent?.data
        if (uri != null) {
            AndroidOAuthSessionBridge.notifyCallback(uri.toString())
        }
        finish()
    }
}
