package uz.meduza.anime.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(160.dp)
            .clickable { onClick() }
    ) {
        // Thumbnail with bottom progress indicator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CardDark)
        ) {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Progress bar overlay at bottom of thumbnail
            LinearProgressIndicator(
                progress = { (progressPercent.coerceIn(0, 100)) / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .align(Alignment.BottomCenter),
                color = PrimaryCyan,
                trackColor = CardDark.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Title
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextWhite,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Episode & Remaining Time
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = episodeText,
                fontSize = 11.sp,
                color = TextGray,
                maxLines = 1
            )
            Text(
                text = "$remainingMinutes mins",
                fontSize = 11.sp,
                color = TextGray
            )
        }
    }
}
