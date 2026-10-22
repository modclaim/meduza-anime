package uz.meduza.anime.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.History
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
import uz.meduza.anime.core.ui.components.*
import uz.meduza.anime.core.ui.theme.*

@Composable
fun LibraryScreen(
    onAnimeClick: (String) -> Unit,
    onEpisodeClick: (String, String) -> Unit,
    viewModel: LibraryViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var clear by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(BackgroundDark).statusBarsPadding()) {
        ScreenHeading(
            "Kutubxona",
            "Siz uchun saqlangan hikoyalar",
            action = {
                if (state.selectedTab == 1 && state.history.isNotEmpty())
                    IconButton(onClick = { clear = true }) {
                        Icon(Icons.Outlined.DeleteOutline, "Tarixni tozalash", tint = TextMuted)
                    }
            },
        )
        TabRow(state.selectedTab, containerColor = BackgroundDark, contentColor = PrimaryCyan) {
            Tab(
                selected = state.selectedTab == 0,
                onClick = { viewModel.selectTab(0) },
                text = { Text("Saqlanganlar") },
            )
            Tab(
                selected = state.selectedTab == 1,
                onClick = { viewModel.selectTab(1) },
                text = { Text("Ko‘rish tarixi") },
            )
        }
        if (state.errorMessage != null)
            Box(Modifier.padding(20.dp)) {
                InlineMessage("Kutubxonani yangilab bo‘lmadi.") { viewModel.refreshCurrentTab() }
            }
        if (state.isLoading)
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        else if (state.selectedTab == 0) {
            if (state.bookmarks.isEmpty())
                EmptyState(
                    "Hozircha hech narsa yo‘q",
                    "Yoqtirgan animengizni saqlang. U shu yerda paydo bo‘ladi.",
                    Icons.Outlined.BookmarkBorder,
                )
            else
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(144.dp),
                    contentPadding = PaddingValues(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    items(state.bookmarks, key = { it.id }) { saved ->
                        AnimeCard(
                            saved.anime.title,
                            saved.anime.posterUrl,
                            saved.anime.type,
                            rating = saved.anime.rating,
                            onClick = { onAnimeClick(saved.anime.slug) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
        } else if (state.history.isEmpty())
            EmptyState(
                "Tarix hali bo‘sh",
                "Tomosha qilishni boshlang. Keyin shu yerdan davom ettiring.",
                Icons.Outlined.History,
            )
        else
            LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.history, key = { it.id }) { history ->
                    val ep = history.episode
                    Surface(
                        onClick = { onEpisodeClick(ep.id, ep.anime.slug) },
                        color = SurfaceDark,
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                Modifier.width(104.dp)
                                    .aspectRatio(16f / 9f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CardDark)
                            ) {
                                AsyncImage(
                                    ep.thumbnailUrl ?: ep.anime.posterUrl,
                                    null,
                                    Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                )
                                LinearProgressIndicator(
                                    progress = { history.progressPercent.coerceIn(0, 100) / 100f },
                                    modifier =
                                        Modifier.align(Alignment.BottomCenter)
                                            .fillMaxWidth()
                                            .height(3.dp),
                                    color = PrimaryCyan,
                                )
                            }
                            Column(
                                Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(5.dp),
                            ) {
                                Text(
                                    ep.anime.title,
                                    color = TextWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    "${ep.episodeNumber}-qism · ${history.progressPercent.coerceIn(0, 100)}%",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                )
                            }
                            IconButton(
                                onClick = { viewModel.deleteHistoryItem(ep.id) },
                                modifier = Modifier.size(40.dp),
                            ) {
                                Icon(
                                    Icons.Outlined.DeleteOutline,
                                    "Tarixdan olib tashlash",
                                    tint = TextMuted,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                }
            }
    }
    if (clear)
        AlertDialog(
            onDismissRequest = { clear = false },
            title = { Text("Tarixni tozalaysizmi?") },
            text = {
                Text("Ko‘rish tarixi o‘chiriladi. Saqlangan animelaringiz kutubxonada qoladi.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        clear = false
                        viewModel.clearHistory()
                    }
                ) {
                    Text("Tozalash")
                }
            },
            dismissButton = { TextButton(onClick = { clear = false }) { Text("Bekor qilish") } },
        )
}
