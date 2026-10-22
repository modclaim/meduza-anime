package uz.meduza.anime.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import uz.meduza.anime.R
import uz.meduza.anime.core.session.SessionManager
import uz.meduza.anime.core.ui.theme.*

@Composable
fun SplashScreen(onSplashFinished: (Boolean) -> Unit) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        delay(450)
        onSplashFinished(SessionManager.getInstance(context).isLoggedInSync())
    }
    Box(
        Modifier.fillMaxSize().background(BackgroundDark).safeDrawingPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Image(painterResource(R.drawable.ic_meduza_logo), "Meduza", Modifier.size(84.dp))
            Text(
                "MEDUZA",
                color = TextWhite,
                fontSize = 22.sp,
                letterSpacing = 5.sp,
                fontWeight = FontWeight.Bold,
            )
            Text("Hikoyalar o‘z tilingizda", color = TextMuted, fontSize = 13.sp)
        }
        CircularProgressIndicator(
            Modifier.align(Alignment.BottomCenter).padding(bottom = 48.dp).size(22.dp),
            color = PrimaryCyan,
            strokeWidth = 2.dp,
        )
    }
}
