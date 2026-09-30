package uz.meduza.anime.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
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

fun formatRatingCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format(java.util.Locale.US, "%.1fM", count / 1_000_000.0).replace(".0M", "M")
        count >= 1_000 -> String.format(java.util.Locale.US, "%.1fK", count / 1_000.0).replace(".0K", "K")
        count > 0 -> count.toString()
        else -> ""
    }
}

@Composable
fun AnimeCard(
    title: String,
    posterUrl: String,
    type: String = "TV",
    episodesInfo: String? = null,
    rating: Double = 0.0,
    ratingCount: Int = 0,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(140.dp)
            .clickable { onClick() }
    ) {
        // Poster Box with Top Badge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(CardDark)
        ) {
            AsyncImage(
                model = posterUrl,
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Series / Movie Pill Badge
            val isMovie = type.equals("MOVIE", ignoreCase = true)
            val badgeColor = if (isMovie) BadgeMovieCyan else BadgeSeriesRed
            val badgeText = if (isMovie) "FILM" else "SERIAL"

            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(badgeColor)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                    .align(Alignment.TopStart)
            ) {
                Text(
                    text = badgeText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isMovie) OnPrimary else Color.White
                )
            }
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

        // Episodes / Seasons text
        if (!episodesInfo.isNullOrEmpty()) {
            Text(
                text = episodesInfo,
                fontSize = 11.sp,
                color = TextGray,
                maxLines = 1
            )
        }

        // Rating Stars and Vote Count
        if (rating > 0.0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Rating",
                    tint = StarGold,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = String.format(java.util.Locale.US, "%.1f", rating),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = StarGold
                )
                if (ratingCount > 0) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(${formatRatingCount(ratingCount)})",
                        fontSize = 10.sp,
                        color = TextGray
                    )
                }
            }
        }
    }
}
