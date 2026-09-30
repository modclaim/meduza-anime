package uz.meduza.anime.core.crash

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * GlobalCoroutineExceptionHandler — Har qanday coroutine ichida ushlanmagan xatolikni tutib qoladi.
 * Bu coroutine xatosi tufayli butun ilova crash bo'lishining oldini oladi.
 */
val GlobalCoroutineExceptionHandler = CoroutineExceptionHandler { context, throwable ->
    Timber.e(throwable, "CoroutineExceptionHandler: kutilmagan coroutine xatoligi ushlandi [%s]", context)
}

/**
 * safeLaunch — CoroutineScope uchun xavfsiz launch kengaytmasi.
 * Istalgan ViewModel yoki Scope ichida crash xavfisiz ishlaydi.
 */
fun CoroutineScope.safeLaunch(
    context: CoroutineContext = EmptyCoroutineContext,
    onError: ((Throwable) -> Unit)? = null,
    block: suspend CoroutineScope.() -> Unit
): Job {
    val handler = CoroutineExceptionHandler { ctx, throwable ->
        Timber.e(throwable, "safeLaunch: xatolik ushlandi [%s]", ctx)
        onError?.invoke(throwable)
    }
    return launch(context + GlobalCoroutineExceptionHandler + handler) {
        try {
            block()
        } catch (t: Throwable) {
            Timber.e(t, "safeLaunch block ichida istisno")
            onError?.invoke(t)
        }
    }
}
