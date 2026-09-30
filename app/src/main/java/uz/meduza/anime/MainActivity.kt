package uz.meduza.anime

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import uz.meduza.anime.core.ui.theme.BackgroundDark
import uz.meduza.anime.core.ui.theme.MeduzaAnimeTheme
import uz.meduza.anime.navigation.AppNavigation

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Allow content to draw behind system bars (edge-to-edge)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Set native window background to match dark theme (eliminates white/black frame flicker)
        window.decorView.setBackgroundColor(Color.parseColor("#0F1015"))

        setContent {
            MeduzaAnimeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundDark
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
