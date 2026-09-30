package uz.meduza.anime.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import uz.meduza.anime.core.ui.theme.*

/** Episode card display style */
enum class EpisodeStyle {
    /** Full card with thumbnail image — used on Home "Continue Watching" style sections */
    CARD,

    /**
     * Compact pill-button row — used on the AnimeDetail episodes tab.
     * No image is loaded, keeping memory usage low and list scrolling fast.
     */
    COMPACT
}

@Composable
fun EpisodeItemCard(
    episodeNumber: Int,
    title: String,
    durationSeconds: Int,
    thumbnailUrl: String?,
    isPremium: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: EpisodeStyle = EpisodeStyle.COMPACT
) {
    when (style) {
        EpisodeStyle.COMPACT -> EpisodeCompactButton(
            episodeNumber = episodeNumber,
            title = title,
            durationSeconds = durationSeconds,
            isPremium = isPremium,
            onClick = onClick,
            modifier = modifier
        )
        EpisodeStyle.CARD -> EpisodeCardItem(
            episodeNumber = episodeNumber,
            title = title,
            durationSeconds = durationSeconds,
            thumbnailUrl = thumbnailUrl,
            isPremium = isPremium,
            onClick = onClick,
            modifier = modifier
        )
    }
}

// ---------------------------------------------------------------------------
// COMPACT style — simple numbered button row, no images
// ---------------------------------------------------------------------------
@Composable
private fun EpisodeCompactButton(
    episodeNumber: Int,
    title: String,
    durationSeconds: Int,
    isPremium: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardDark)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Episode number badge
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isPremium) PremiumGold.copy(alpha = 0.15f) else PrimaryCyan.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            if (isPremium) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Premium",
                    tint = PremiumGold,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text(
                    text = "$episodeNumber",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryCyan
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title & duration
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${episodeNumber}-qism",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryCyan
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (durationSeconds > 0) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${durationSeconds / 60} daqiqa",
                    fontSize = 11.sp,
                    color = TextGray
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Play / VIP pill
        if (isPremium) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(PremiumGold)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "VIP",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(PrimaryCyan.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = PrimaryCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// CARD style — thumbnail + info (used on Home / Continue Watching)
// ---------------------------------------------------------------------------
@Composable
private fun EpisodeCardItem(
    episodeNumber: Int,
    title: String,
    durationSeconds: Int,
    thumbnailUrl: String?,
    isPremium: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val durationMinutes = durationSeconds / 60

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardDark)
            .clickable { onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail with Play overlay
        Box(
            modifier = Modifier
                .width(110.dp)
                .height(68.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(CardDarkElevated)
        ) {
            AsyncImage(
                model = thumbnailUrl,
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Play / Lock overlay icon
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                if (isPremium) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Premium",
                        tint = PremiumGold,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = PrimaryCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Episode Info
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${episodeNumber}-qism",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryCyan
                )
                if (isPremium) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(PremiumGold)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "VIP",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (durationMinutes > 0) {
                Text(
                    text = "$durationMinutes daqiqa",
                    fontSize = 11.sp,
                    color = TextGray
                )
            }
        }
    }
}


