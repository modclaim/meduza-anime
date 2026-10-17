package uz.meduza.anime.core.auth.storage

import android.content.Context

object AndroidAuthContext {
    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    val context: Context
        get() = appContext ?: error(
            "AndroidAuthContext is not initialized. Please call AndroidAuthContext.initialize(context) in your Application or MainActivity onCreate."
        )
}
