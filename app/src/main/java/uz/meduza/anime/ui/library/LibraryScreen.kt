package uz.meduza.anime.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import uz.meduza.anime.core.ui.components.AnimeCard
import uz.meduza.anime.core.ui.components.ContinueWatchingCard
import uz.meduza.anime.core.ui.theme.*

@Composable
fun LibraryScreen(
    onAnimeClick: (slug: String) -> Unit,
    onEpisodeClick: (episodeId: String, slug: String) -> Unit,
    viewModel: LibraryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(top = 16.dp)
    ) {
        Text(
            text = "Sevimlilar",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Main Tab Switcher [ SAQLANGANLAR ] [ TARIX ]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(CardDark)
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (uiState.selectedTab == 0) PrimaryCyan else CardDark)
                    .clickable { viewModel.selectTab(0) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Saqlanganlar",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (uiState.selectedTab == 0) OnPrimary else TextGray
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (uiState.selectedTab == 1) PrimaryCyan else CardDark)
                    .clickable { viewModel.selectTab(1) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Ko'rish Tarixi",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (uiState.selectedTab == 1) OnPrimary else TextGray
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.selectedTab == 0) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryCyan)
                }
            } else if (uiState.bookmarks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Saqlangan animelar yo'q", color = TextGray, fontSize = 14.sp)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.bookmarks) { item ->
                        AnimeCard(
                            title = item.anime.title,
                            posterUrl = item.anime.posterUrl,
                            type = item.anime.type,
                            rating = item.anime.rating,
                            onClick = { onAnimeClick(item.anime.slug) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        } else {
            // Watch History Tab
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryCyan)
                }
            } else if (uiState.history.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Ko'rish tarixi bo'sh", color = TextGray, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Barcha ko'rilganlar (${uiState.history.size})",
                                fontSize = 13.sp,
                                color = TextGray,
                                fontWeight = FontWeight.SemiBold
                            )
                            TextButton(onClick = { viewModel.clearHistory() }) {
                                Text(
                                    text = "Tarixni tozalash",
                                    color = BadgeSeriesRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    items(uiState.history) { item ->
                        val ep = item.episode
                        val remMins = ((item.duration - item.stoppedAt).coerceAtLeast(0)) / 60

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(CardDark)
                                .clickable { onEpisodeClick(ep.id, ep.anime.slug) }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Thumbnail with bottom progress
                            Box(
                                modifier = Modifier
                                    .width(110.dp)
                                    .height(68.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BackgroundDark)
                            ) {
                                AsyncImage(
                                    model = ep.thumbnailUrl ?: ep.anime.posterUrl,
                                    contentDescription = ep.anime.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                LinearProgressIndicator(
                                    progress = { (item.progressPercent.coerceIn(0, 100)) / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(3.dp)
                                        .align(Alignment.BottomCenter),
                                    color = PrimaryCyan,
                                    trackColor = CardDark.copy(alpha = 0.6f)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ep.anime.title,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "${ep.episodeNumber}-qism: ${ep.title}",
                                    color = PrimaryCyan,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "${item.progressPercent}% • Qolgan: $remMins daqiqa",
                                    color = TextGray,
                                    fontSize = 11.sp
                                )
                            }

                            IconButton(onClick = { viewModel.deleteHistoryItem(ep.id) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = BadgeSeriesRed
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
