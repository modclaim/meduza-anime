package uz.meduza.anime.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import uz.meduza.anime.core.ui.components.*
import uz.meduza.anime.core.ui.theme.*
import uz.meduza.anime.data.models.AnimeDto

@Composable
fun HomeScreen(
    onAnimeClick: (slug: String, initialTab: Int) -> Unit,
    onEpisodeClick: (episodeId: String, slug: String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundDark
    ) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryCyan)
            }
        } else if (uiState.popularAnimes.isEmpty() && uiState.latestAnimes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = uiState.errorMessage ?: "Ma'lumotlar yuklanmadi",
                        color = TextGray,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { viewModel.loadHomeData() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryCyan,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = "Qayta yuklash", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // 1. HERO BANNER SECTION (At the very top)
                val heroAnime = uiState.popularAnimes.firstOrNull() ?: uiState.latestAnimes.firstOrNull()
                if (heroAnime != null) {
                    item {
                        HeroBannerCard(
                            anime = heroAnime,
                            onPlayClick = { onAnimeClick(heroAnime.slug, 0) },
                            onInfoClick = { onAnimeClick(heroAnime.slug, 1) }
                        )
                    }
                }

                // 2. CONTINUE WATCHING / KO'RISHDA DAVOM ETISH (If user has history)
                if (uiState.continueWatchingList.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(top = 20.dp)) {
                            SectionHeader(
                                title = "KO'RISHDA DAVOM ETISH",
                                subtitle = "Oxirgi ko'rilgan joyidan davom eting"
                            )

                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(uiState.continueWatchingList) { item ->
                                    val episode = item.episode
                                    val remainingMins = ((item.duration - item.stoppedAt).coerceAtLeast(0)) / 60
                                    val seasonTitle = episode.season?.title ?: "1-Fasl"
                                    val epText = "$seasonTitle • ${episode.episodeNumber}-qism"

                                    ContinueWatchingCard(
                                        title = episode.anime.title,
                                        episodeText = epText,
                                        remainingMinutes = if (remainingMins > 0) remainingMins else 1,
                                        thumbnailUrl = episode.thumbnailUrl ?: episode.anime.posterUrl,
                                        progressPercent = item.progressPercent,
                                        onClick = {
                                            onEpisodeClick(episode.id, episode.anime.slug)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. LATEST RELEASES / YANGI QO'SHILGAN VA YANGI QISMLAR
                if (uiState.latestAnimes.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(top = 24.dp)) {
                            SectionHeader(
                                title = "YANGI",
                                subtitle = "Yangi dublyaj qilingan seriyalar"
                            )

                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(uiState.latestAnimes) { anime ->
                                    val epsText = if (anime.type.equals("MOVIE", ignoreCase = true)) {
                                        "Film"
                                    } else {
                                        "${anime.totalReleasedEpisodes} ta qism"
                                    }

                                    AnimeCard(
                                        title = anime.title,
                                        posterUrl = anime.posterUrl,
                                        type = anime.type,
                                        episodesInfo = epsText,
                                        rating = anime.rating,
                                        ratingCount = anime.ratingCount,
                                        onClick = { onAnimeClick(anime.slug, 0) }
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. MOST POPULAR / ENG MASHHUR ANIMELAR
                if (uiState.popularAnimes.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(top = 24.dp)) {
                            SectionHeader(
                                title = "ENG MASHHUR ANIMELAR",
                                subtitle = "Foydalanuvchilar tomonidan eng ko'p ko'rilgan"
                            )

                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(uiState.popularAnimes) { anime ->
                                    val epsText = if (anime.type.equals("MOVIE", ignoreCase = true)) {
                                        "To'liq metrajli film"
                                    } else {
                                        "Serial • ${anime.totalReleasedEpisodes} qism"
                                    }

                                    AnimeCard(
                                        title = anime.title,
                                        posterUrl = anime.posterUrl,
                                        type = anime.type,
                                        episodesInfo = epsText,
                                        rating = anime.rating,
                                        ratingCount = anime.ratingCount,
                                        onClick = { onAnimeClick(anime.slug, 0) }
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. UPCOMING RELEASES / KUTILAYOTGAN PREMYERALAR (Always at the very bottom)
                if (uiState.upcomingPremieres.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(top = 28.dp)) {
                            SectionHeader(
                                title = "KUTILAYOTGAN PREMYERALAR",
                                subtitle = "Tez kunda Meduza dublyajida chiqadi"
                            )

                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(uiState.upcomingPremieres) { upcoming ->
                                    PremiereCountdownCard(
                                        title = upcoming.title,
                                        posterUrl = upcoming.posterUrl,
                                        secondsRemaining = upcoming.secondsRemaining,
                                        scheduledReleaseDate = upcoming.scheduledReleaseDate,
                                        seasonBadge = upcoming.badgeText ?: if (upcoming.type.equals("TV", ignoreCase = true)) "Yangi fasl" else "Tez kunda",
                                        onClick = { onAnimeClick(upcoming.slug, 1) },
                                        modifier = Modifier.width(290.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String? = null
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            letterSpacing = 1.sp
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextMuted,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun HeroBannerCard(
    anime: AnimeDto,
    onPlayClick: () -> Unit,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(340.dp)
            .clickable { onInfoClick() }
    ) {
        // Backdrop / Poster Image
        AsyncImage(
            model = anime.bannerUrl ?: anime.posterUrl,
            contentDescription = anime.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Gradient Vignette overlay (Top subtle dark, Bottom strong dark fade)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.4f),
                            Color.Transparent,
                            BackgroundDark.copy(alpha = 0.8f),
                            BackgroundDark
                        )
                    )
                )
        )

        // Hero Info & Actions at Bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Badges (Type + Dublyaj + Rating)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Type Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (anime.type.equals("MOVIE", true)) BadgeMovieCyan else BadgeSeriesRed)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (anime.type.equals("MOVIE", true)) "FILM" else "SERIAL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Uzbek Dubbing badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(PrimaryCyan.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "O'zbekcha Dublyaj",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryCyan
                    )
                }

                // Rating and Vote Count
                Text(
                    text = "★ ${String.format(java.util.Locale.US, "%.1f", anime.rating)}" +
                            if (anime.ratingCount > 0) " (${formatRatingCount(anime.ratingCount)})" else "",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = StarGold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Anime Title
            Text(
                text = anime.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Synopsis Preview
            if (!anime.synopsis.isNullOrBlank()) {
                Text(
                    text = anime.synopsis,
                    fontSize = 12.sp,
                    color = TextMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play Button
                Button(
                    onClick = onPlayClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryCyan,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Tomosha qilish", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // Info Button
                OutlinedButton(
                    onClick = onInfoClick,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextWhite
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = "Info", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Batafsil", fontSize = 13.sp)
                }
            }
        }
    }
}
