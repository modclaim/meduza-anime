package uz.meduza.anime.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import uz.meduza.anime.core.ui.theme.*

@Composable
fun ContinueWatchingCard(
    title: String,
    episodeText: String,
    remainingMinutes: Int,
    thumbnailUrl: String?,
    progressPercent: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .width(252.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark)
            .clickable(onClick = onClick)
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f).background(CardDark)) {
            AsyncImage(
                thumbnailUrl,
                title,
                Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Icon(
                Icons.Default.PlayCircle,
                null,
                tint = TextWhite,
                modifier = Modifier.align(Alignment.Center).size(38.dp),
            )
            LinearProgressIndicator(
                progress = { progressPercent.coerceIn(0, 100) / 100f },
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(3.dp),
                color = PrimaryCyan,
                trackColor = CardDarkElevated,
            )
        }
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                title,
                color = TextWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "$episodeText · ${remainingMinutes.coerceAtLeast(0)} daq. qoldi",
                color = TextMuted,
                fontSize = 11.sp,
                maxLines = 1,
            )
        }
    }
}
