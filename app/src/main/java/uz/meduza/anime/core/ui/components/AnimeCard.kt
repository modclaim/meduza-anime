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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import java.util.Locale
import uz.meduza.anime.core.ui.theme.*

fun formatRatingCount(count: Int): String =
    when {
        count >= 1_000_000 ->
            String.format(Locale.US, "%.1fM", count / 1_000_000.0).replace(".0M", "M")
        count >= 1_000 -> String.format(Locale.US, "%.1fK", count / 1_000.0).replace(".0K", "K")
        count > 0 -> count.toString()
        else -> ""
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
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.width(144.dp).clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Box(
            Modifier.fillMaxWidth()
                .aspectRatio(0.69f)
                .clip(RoundedCornerShape(12.dp))
                .background(CardDark)
        ) {
            AsyncImage(posterUrl, title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(
                Modifier.fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f)),
                            startY = 180f,
                        )
                    )
            )
            Row(
                Modifier.align(Alignment.BottomStart).padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (rating > 0) {
                    Icon(Icons.Default.Star, null, tint = StarGold, modifier = Modifier.size(13.dp))
                    Text(
                        String.format(Locale.US, "%.1f", rating),
                        color = TextWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
        Text(
            title,
            color = TextWhite,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            episodesInfo ?: if (type == "MOVIE") "Film" else "Serial",
            color = TextMuted,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
