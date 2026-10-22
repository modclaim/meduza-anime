package uz.meduza.anime.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import uz.meduza.anime.core.ui.components.*
import uz.meduza.anime.core.ui.theme.*

@Composable
fun ScheduleScreen(onAnimeClick: (String) -> Unit, viewModel: ScheduleViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    val days =
        listOf(
            "MONDAY" to "Du",
            "TUESDAY" to "Se",
            "WEDNESDAY" to "Ch",
            "THURSDAY" to "Pa",
            "FRIDAY" to "Ju",
            "SATURDAY" to "Sh",
            "SUNDAY" to "Ya",
        )
    Column(Modifier.fillMaxSize().background(BackgroundDark).statusBarsPadding()) {
        ScreenHeading(
            "Premyeralar",
            "Jadval · Toshkent vaqti",
            action = {
                IconButton(onClick = viewModel::loadSchedule) {
                    Icon(Icons.Outlined.Refresh, "Jadvalni yangilash")
                }
            },
        )
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            days.forEach { (key, label) ->
                FilterChip(
                    selected = state.selectedDay == key,
                    onClick = { viewModel.selectDay(key) },
                    label = { Text(label, fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        if (state.isLoading)
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        else if (state.errorMessage != null)
            EmptyState(
                "Jadval yuklanmadi",
                "Internet aloqasini tekshirib, qayta urinib ko‘ring.",
                actionLabel = "Qayta urinish",
                onAction = viewModel::loadSchedule,
            )
        else
            LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (state.dayAnimes.isEmpty())
                    item {
                        EmptyState(
                            "Bugun premyera yo‘q",
                            "Boshqa kunni tanlang yoki yaqin premyeralarni ko‘ring.",
                            Icons.Outlined.CalendarToday,
                        )
                    }
                items(state.dayAnimes, key = { it.id }) { anime ->
                    Surface(
                        onClick = { onAnimeClick(anime.slug) },
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceDark,
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            AsyncImage(
                                anime.posterUrl,
                                null,
                                Modifier.width(64.dp).height(90.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop,
                            )
                            Column(
                                Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    anime.title,
                                    color = TextWhite,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                )
                                Text(
                                    anime.releaseTime?.let { "$it · Toshkent" }
                                        ?: "Vaqti aniqlanmoqda",
                                    color = PrimaryCyan,
                                    fontSize = 12.sp,
                                )
                            }
                        }
                    }
                }
                val upcoming = state.scheduleData?.upcomingPremieres?.animes.orEmpty()
                if (upcoming.isNotEmpty())
                    item {
                        Text(
                            "Yaqin premyeralar",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(top = 16.dp, bottom = 12.dp),
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(upcoming, key = { it.id }) { anime ->
                                PremiereCountdownCard(
                                    anime.title,
                                    anime.posterUrl,
                                    anime.secondsRemaining,
                                    anime.scheduledReleaseDate,
                                    anime.badgeText,
                                    { onAnimeClick(anime.slug) },
                                )
                            }
                        }
                    }
            }
    }
}
