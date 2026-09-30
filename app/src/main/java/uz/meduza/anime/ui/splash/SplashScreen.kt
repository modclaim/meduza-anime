package uz.meduza.anime.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import uz.meduza.anime.R
import uz.meduza.anime.core.session.SessionManager
import uz.meduza.anime.core.ui.theme.BackgroundDark
import uz.meduza.anime.core.ui.theme.PrimaryCyan
import uz.meduza.anime.core.ui.theme.TextMuted
import uz.meduza.anime.core.ui.theme.TextWhite

@Composable
fun SplashScreen(
    onSplashFinished: (isLoggedIn: Boolean) -> Unit
) {
    val context = LocalContext.current

    // 3.2s cinematic delay before navigating directly with fresh session check
    LaunchedEffect(Unit) {
        delay(3200)
        val sessionManager = SessionManager.getInstance(context)
        onSplashFinished(sessionManager.isLoggedInSync())
    }

    // Infinite pulsing & breathing animation
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // Fade-in for text
    val textAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(400)
        textAlpha.animateTo(1f, animationSpec = tween(1000))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        // Glowing background halo
        Box(
            modifier = Modifier
                .size(260.dp)
                .scale(scale * 1.2f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PrimaryCyan.copy(alpha = 0.35f * glowAlpha),
                            PrimaryCyan.copy(alpha = 0.08f * glowAlpha),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Animated Mascot Icon
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(scale),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_meduza_logo),
                    contentDescription = "Meduza Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text(
                text = "MEDUZA ANIME",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = TextWhite,
                letterSpacing = 3.sp,
                modifier = Modifier.alpha(textAlpha.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Uzbek Subtitle
            Text(
                text = "O'zbekcha professional dublyaj platformasi",
                fontSize = 13.sp,
                color = PrimaryCyan,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(textAlpha.value)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Pulsing loading caption
            Text(
                text = "Yuklanmoqda...",
                fontSize = 11.sp,
                color = TextMuted,
                letterSpacing = 1.sp,
                modifier = Modifier.alpha(glowAlpha)
            )
        }
    }
}
