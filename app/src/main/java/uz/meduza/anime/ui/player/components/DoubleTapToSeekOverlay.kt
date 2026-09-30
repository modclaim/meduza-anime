package uz.meduza.anime.ui.player.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import uz.meduza.anime.core.ui.theme.PrimaryCyan

@Composable
fun DoubleTapToSeekOverlay(
    onSeekBackward: (Long) -> Unit,
    onSeekForward: (Long) -> Unit,
    onSingleTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showRewindEffect by remember { mutableStateOf(false) }
    var showForwardEffect by remember { mutableStateOf(false) }

    LaunchedEffect(showRewindEffect) {
        if (showRewindEffect) {
            delay(650)
            showRewindEffect = false
        }
    }

    LaunchedEffect(showForwardEffect) {
        if (showForwardEffect) {
            delay(650)
            showForwardEffect = false
        }
    }

    Row(modifier = modifier.fillMaxSize()) {
        // Left Half for Rewind 10s
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            showRewindEffect = true
                            onSeekBackward(10000)
                        },
                        onTap = { onSingleTap() }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (showRewindEffect) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FastRewind,
                        contentDescription = "Rewind",
                        tint = PrimaryCyan,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "-10 soniya",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Right Half for Fast Forward 10s
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            showForwardEffect = true
                            onSeekForward(10000)
                        },
                        onTap = { onSingleTap() }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (showForwardEffect) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Forward",
                        tint = PrimaryCyan,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "+10 soniya",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
