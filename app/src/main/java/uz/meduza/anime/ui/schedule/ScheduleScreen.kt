package uz.meduza.anime.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import uz.meduza.anime.core.ui.components.AnimeCard
import uz.meduza.anime.core.ui.components.PremiereCountdownCard
import uz.meduza.anime.core.ui.theme.*

@Composable
fun ScheduleScreen(
    onAnimeClick: (slug: String) -> Unit,
    viewModel: ScheduleViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val days = listOf(
        "MONDAY" to "Dushanba",
        "TUESDAY" to "Seshanba",
        "WEDNESDAY" to "Chorshanba",
        "THURSDAY" to "Payshanba",
        "FRIDAY" to "Juma",
        "SATURDAY" to "Shanba",
        "SUNDAY" to "Yakshanba"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(top = 16.dp)
    ) {
        Text(
            text = "Premyeralar jadvali",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Weekday Selector Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(days) { (key, label) ->
                val isSelected = uiState.selectedDay == key

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) PrimaryCyan else CardDark)
                        .clickable { viewModel.selectDay(key) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) OnPrimary else TextGray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryCyan)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Section: Upcoming Premieres Countdown
                val upcoming = uiState.scheduleData?.upcomingPremieres?.animes ?: emptyList()
                if (upcoming.isNotEmpty()) {
                    item {
                        Column {
                            Text(
                                text = "KUTILAYOTGAN PREMYERALAR (COUNTDOWN)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(upcoming) { item ->
                                    PremiereCountdownCard(
                                        title = item.title,
                                        posterUrl = item.posterUrl,
                                        secondsRemaining = item.secondsRemaining,
                                        scheduledReleaseDate = item.scheduledReleaseDate,
                                        seasonBadge = item.badgeText ?: if (item.type.equals("TV", ignoreCase = true)) "Yangi fasl" else "Tez kunda",
                                        onClick = { onAnimeClick(item.slug) },
                                        modifier = Modifier.width(290.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Section: Day's scheduled anime releases
                item {
                    Text(
                        text = "KUNLIK ANIMELAR RO'YXATI",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                if (uiState.dayAnimes.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Ushbu kunda premyeralar mavjud emas",
                                color = TextGray,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    items(uiState.dayAnimes) { anime ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(CardDark)
                                .clickable { onAnimeClick(anime.slug) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AnimeCard(
                                title = anime.title,
                                posterUrl = anime.posterUrl,
                                type = "TV",
                                rating = anime.rating,
                                onClick = { onAnimeClick(anime.slug) },
                                modifier = Modifier.width(90.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = anime.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Chiqish vaqti: ${anime.releaseTime ?: "18:00"}",
                                    fontSize = 12.sp,
                                    color = PrimaryCyan
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
