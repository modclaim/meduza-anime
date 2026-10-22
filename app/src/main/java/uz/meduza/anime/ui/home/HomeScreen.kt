package uz.meduza.anime.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import uz.meduza.anime.R
import uz.meduza.anime.core.ui.components.*
import uz.meduza.anime.core.ui.theme.*
import uz.meduza.anime.data.models.AnimeDto

@Composable
fun HomeScreen(
    onAnimeClick: (String, Int) -> Unit,
    onEpisodeClick: (String, String) -> Unit,
    onSearchClick: () -> Unit = {},
    onScheduleClick: () -> Unit = {},
    onPlayClick: ((String) -> Unit)? = null,
    viewModel: HomeViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    LazyColumn(
        Modifier.fillMaxSize().background(BackgroundDark).statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item {
            Row(
                Modifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(painterResource(R.drawable.ic_meduza_logo), null, Modifier.size(32.dp))
                Text(
                    "MEDUZA",
                    color = TextWhite,
                    fontSize = 18.sp,
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f).padding(start = 10.dp),
                )
                IconButton(onClick = onScheduleClick) {
                    Icon(Icons.Outlined.CalendarToday, "Premyeralar jadvali", Modifier.size(21.dp))
                }
                IconButton(onClick = onSearchClick) {
                    Icon(Icons.Outlined.Search, "Anime qidirish")
                }
            }
        }
        if (state.isLoading && !state.hasData)
            item {
                Box(Modifier.fillMaxWidth().height(360.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        else if (!state.hasData)
            item {
                EmptyState(
                    "Hozircha yuklanmadi",
                    state.errorMessage ?: "Internet aloqasini tekshiring.",
                    actionLabel = "Qayta urinish",
                    onAction = { viewModel.refresh() },
                )
            }
        val hero = state.popularAnimes.firstOrNull() ?: state.latestAnimes.firstOrNull()
        if (hero != null)
            item {
                HeroBannerCard(
                    hero,
                    { onPlayClick?.invoke(hero.slug) ?: onAnimeClick(hero.slug, 0) },
                    { onAnimeClick(hero.slug, 1) },
                )
            }
        if (state.continueWatchingList.isNotEmpty())
            item {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    ShelfHeading("Davom ettiring", "To‘xtagan joyingizdan")
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.continueWatchingList, key = { it.episode.id }) { item ->
                            val ep = item.episode
                            ContinueWatchingCard(
                                ep.anime.title,
                                "${ep.episodeNumber}-qism",
                                ((item.duration - item.stoppedAt).coerceAtLeast(0) / 60),
                                ep.thumbnailUrl ?: ep.anime.posterUrl,
                                item.progressPercent,
                                { onEpisodeClick(ep.id, ep.anime.slug) },
                            )
                        }
                    }
                }
            }
        if (state.latestAnimes.isNotEmpty())
            item {
                AnimeShelf(
                    "Yangi qo‘shilgan",
                    "Yangi hikoyalarni kashf eting",
                    state.latestAnimes,
                    onAnimeClick,
                )
            }
        if (state.popularAnimes.isNotEmpty())
            item {
                AnimeShelf(
                    "Hozir ommabop",
                    "Tomoshabinlar tanlovi",
                    state.popularAnimes,
                    onAnimeClick,
                )
            }
        if (state.upcomingPremieres.isNotEmpty())
            item {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    ShelfHeading("Yaqin premyeralar", "Kutishga arziydigan hikoyalar")
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.upcomingPremieres) { premiere ->
                            PremiereCountdownCard(
                                title = premiere.title,
                                posterUrl = premiere.posterUrl,
                                secondsRemaining = premiere.secondsRemaining,
                                scheduledReleaseDate = premiere.scheduledReleaseDate,
                                onClick = { onAnimeClick(premiere.slug, 1) },
                            )
                        }
                    }
                }
            }
    }
}

@Composable
private fun AnimeShelf(
    title: String,
    subtitle: String,
    animes: List<AnimeDto>,
    onClick: (String, Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ShelfHeading(title, subtitle)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(animes, key = { it.id }) { anime ->
                AnimeCard(
                    anime.title,
                    anime.posterUrl,
                    anime.type,
                    if (anime.type == "MOVIE") "Film" else "${anime.totalReleasedEpisodes} qism",
                    anime.rating,
                    anime.ratingCount,
                    { onClick(anime.slug, 1) },
                )
            }
        }
    }
}

@Composable
private fun ShelfHeading(title: String, subtitle: String) {
    Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 21.sp, color = TextWhite)
        Text(subtitle, fontSize = 12.sp, color = TextMuted)
    }
}

@Composable
fun HeroBannerCard(
    anime: AnimeDto,
    onPlayClick: () -> Unit,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(390.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(CardDark)
    ) {
        AsyncImage(
            anime.bannerUrl ?: anime.posterUrl,
            anime.title,
            Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            Modifier.fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.1f),
                            Color.Black.copy(alpha = 0.15f),
                            Color(0xF20B0D11),
                        )
                    )
                )
        )
        Column(
            Modifier.align(Alignment.BottomStart).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "MEDUZA TANLOVI",
                color = PrimaryCyan,
                fontSize = 10.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                anime.title,
                color = TextWhite,
                fontSize = 27.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                listOfNotNull(
                        anime.year?.toString(),
                        if (anime.type == "MOVIE") "Film" else "Serial",
                        anime.genres.firstOrNull()?.name,
                    )
                    .joinToString("  ·  "),
                color = TextGray,
                fontSize = 12.sp,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onPlayClick,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(50.dp),
                ) {
                    Icon(Icons.Default.PlayArrow, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Tomosha qilish", fontWeight = FontWeight.SemiBold)
                }
                OutlinedIconButton(
                    onClick = onInfoClick,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(50.dp),
                ) {
                    Icon(Icons.Outlined.Info, "Batafsil")
                }
            }
        }
    }
}
