package uz.meduza.anime.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import kotlinx.coroutines.delay
import uz.meduza.anime.core.ui.theme.*

@Composable
fun PremiereCountdownCard(
    title: String,
    posterUrl: String,
    secondsRemaining: Long,
    scheduledReleaseDate: String? = null,
    seasonBadge: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentSeconds by remember(secondsRemaining) { mutableLongStateOf(secondsRemaining) }

    LaunchedEffect(secondsRemaining) {
        var sec = secondsRemaining
        while (sec > 0) {
            kotlinx.coroutines.delay(1000L)
            sec--
            currentSeconds = sec
        }
    }

    val totalHours = currentSeconds / 3600
    val days = totalHours / 24
    val remHours = totalHours % 24
    val minutes = (currentSeconds % 3600) / 60

    val countdownText = when {
        currentSeconds <= 0 -> "Chiqdi / Tayyor"
        days > 0 -> "${days} kun %02d soat qoldi".format(remHours)
        else -> "%02d soat %02d daqiqa qoldi".format(totalHours, minutes)
    }

    val formattedDate = remember(scheduledReleaseDate) {
        scheduledReleaseDate?.let { formatReleaseDate(it) }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CardDark)
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Column: [Name] [Season Badge], [Date], [Countdown]
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (!seasonBadge.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PrimaryCyan.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = seasonBadge,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryCyan
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (!formattedDate.isNullOrBlank()) {
                    Text(
                        text = "Chiqish vaqti: $formattedDate",
                        fontSize = 11.sp,
                        color = TextGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // ⏳ Countdown line
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "⏳ ",
                        fontSize = 12.sp
                    )
                    Text(
                        text = countdownText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryCyan
                    )
                }
            }

            // Right: Poster Image
            AsyncImage(
                model = posterUrl,
                contentDescription = title,
                modifier = Modifier
                    .width(76.dp)
                    .height(100.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(BackgroundDark),
                contentScale = ContentScale.Crop
            )
        }
    }
}

private fun formatReleaseDate(raw: String): String {
    return try {
        if (raw.contains("T")) {
            val datePart = raw.substringBefore("T")
            val timePart = raw.substringAfter("T").take(5)
            "$datePart, $timePart"
        } else {
            raw
        }
    } catch (e: Exception) {
        raw
    }
}
