package uz.meduza.anime

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import uz.meduza.anime.core.ui.theme.MeduzaAnimeTheme
import uz.meduza.anime.ui.player.PlayerSurface
import uz.meduza.anime.ui.profile.PremiumScreen
import uz.meduza.anime.ui.profile.ProfileViewModel

/** Debug-only component verification; never included in APK/AAB release builds. */
class DesignPreviewActivity : ComponentActivity() {
    @androidx.annotation.OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (intent.getStringExtra("screen") == "player") {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
        setContent {
            MeduzaAnimeTheme {
                if (intent.getStringExtra("screen") == "player") {
                    val view = LocalView.current
                    SideEffect {
                        WindowInsetsControllerCompat(window, view)
                            .hide(WindowInsetsCompat.Type.systemBars())
                    }
                    val player = remember {
                        ExoPlayer.Builder(this).build().apply {
                            repeatMode = Player.REPEAT_MODE_ONE
                            setMediaItem(MediaItem.fromUri("file:///android_asset/player-test.mp4"))
                            prepare()
                            playWhenReady = true
                        }
                    }
                    var fill by remember { mutableStateOf(true) }
                    DisposableEffect(player) { onDispose { player.release() } }
                    Box(Modifier.fillMaxSize().background(Color.Black)) {
                        PlayerSurface(player, Modifier.fillMaxSize(), fill)
                        TextButton(
                            onClick = { fill = !fill },
                            modifier = Modifier.align(Alignment.TopEnd).padding(20.dp),
                        ) {
                            Text(if (fill) "To‘ldirish" else "Moslash", color = Color.White)
                        }
                    }
                } else {
                    val model: ProfileViewModel = viewModel()
                    LaunchedEffect(Unit) { model.loadPaymentPlans() }
                    PremiumScreen(onBack = { finish() }, viewModel = model)
                }
            }
        }
    }
}
