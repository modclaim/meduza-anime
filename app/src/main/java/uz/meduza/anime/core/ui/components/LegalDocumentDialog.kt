package uz.meduza.anime.core.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LegalDocumentDialog(title: String, url: String, onDismiss: () -> Unit) {
    var loading by remember(url) { mutableStateOf(true) }
    var failed by remember(url) { mutableStateOf(false) }
    var attempt by remember(url) { mutableIntStateOf(0) }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.9f),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
        ) {
            Column {
                Row(
                    Modifier.fillMaxWidth()
                        .padding(start = 20.dp, top = 8.dp, bottom = 8.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        title,
                        Modifier.weight(1f),
                        color = Color(0xFF161B22),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Hujjatni yopish", tint = Color(0xFF161B22))
                    }
                }
                HorizontalDivider(color = Color(0xFFE8EBEF))
                if (loading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                if (failed) {
                    Column(
                        Modifier.fillMaxSize().padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            "Hujjatni yuklab bo‘lmadi. Internet aloqasini tekshiring.",
                            color = Color.DarkGray,
                        )
                        TextButton(
                            onClick = {
                                failed = false
                                loading = true
                                attempt++
                            }
                        ) {
                            Text("Qayta urinish")
                        }
                    }
                } else
                    key(url, attempt) {
                        AndroidView(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            factory = { context ->
                                WebView(context).apply {
                                    setBackgroundColor(android.graphics.Color.WHITE)
                                    settings.javaScriptEnabled = false
                                    settings.allowFileAccess = false
                                    settings.allowContentAccess = false
                                    settings.builtInZoomControls = true
                                    settings.displayZoomControls = false
                                    settings.textZoom = 110
                                    webViewClient =
                                        object : WebViewClient() {
                                            override fun shouldOverrideUrlLoading(
                                                view: WebView?,
                                                request: WebResourceRequest?,
                                            ): Boolean {
                                                val target = request?.url ?: return true
                                                // Documents remain inside the reader; no arbitrary
                                                // external-app intents.
                                                return target.scheme != "https" ||
                                                    target.host != android.net.Uri.parse(url).host
                                            }

                                            override fun onPageStarted(
                                                view: WebView?,
                                                url: String?,
                                                favicon: Bitmap?,
                                            ) {
                                                loading = true
                                            }

                                            override fun onPageFinished(
                                                view: WebView?,
                                                url: String?,
                                            ) {
                                                loading = false
                                            }

                                            override fun onReceivedError(
                                                view: WebView?,
                                                request: WebResourceRequest?,
                                                error: WebResourceError?,
                                            ) {
                                                if (request?.isForMainFrame == true) {
                                                    failed = true
                                                    loading = false
                                                }
                                            }

                                            override fun onReceivedHttpError(
                                                view: WebView?,
                                                request: WebResourceRequest?,
                                                response: android.webkit.WebResourceResponse?,
                                            ) {
                                                if (
                                                    request?.isForMainFrame == true &&
                                                        (response?.statusCode ?: 200) >= 400
                                                ) {
                                                    failed = true
                                                    loading = false
                                                }
                                            }
                                        }
                                    loadUrl(url)
                                }
                            },
                            onRelease = {
                                it.stopLoading()
                                it.destroy()
                            },
                        )
                    }
            }
        }
    }
}
