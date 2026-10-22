package uz.meduza.anime.ui.detail

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import java.util.Locale
import uz.meduza.anime.core.ui.components.*
import uz.meduza.anime.core.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AnimeDetailScreen(
    slug: String,
    initialTab: Int = 0,
    onBackClick: () -> Unit,
    onEpisodeClick: (String, String) -> Unit,
    isLoggedIn: Boolean = true,
    onSignIn: () -> Unit = {},
    viewModel: AnimeDetailViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var tab by remember(slug) { mutableIntStateOf(if (initialTab == 0 && isLoggedIn) 0 else 1) }
    var ratingOpen by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var selectedScore by remember { mutableIntStateOf(8) }
    LaunchedEffect(slug, isLoggedIn) { viewModel.loadAnimeDetail(slug, isLoggedIn) }
    LaunchedEffect(state.ratingSaved) { if (state.ratingSaved > 0) ratingOpen = false }
    val anime = state.anime
    Surface(Modifier.fillMaxSize(), color = BackgroundDark) {
        if (state.isLoading)
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        else if (anime == null)
            Box(Modifier.fillMaxSize().safeDrawingPadding(), contentAlignment = Alignment.Center) {
                EmptyState(
                    "Yuklab bo‘lmadi",
                    state.errorMessage ?: "Anime topilmadi",
                    actionLabel = "Qayta urinish",
                    onAction = { viewModel.loadAnimeDetail(slug, isLoggedIn) },
                )
            }
        else
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 32.dp)) {
                item {
                    Box(Modifier.fillMaxWidth().height(330.dp).background(CardDark)) {
                        AsyncImage(
                            anime.bannerUrl ?: anime.posterUrl,
                            null,
                            Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                        Box(
                            Modifier.fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Black.copy(alpha = 0.3f),
                                            Color.Transparent,
                                            BackgroundDark,
                                        )
                                    )
                                )
                        )
                        Row(
                            Modifier.fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            FilledTonalIconButton(
                                onClick = onBackClick,
                                colors =
                                    IconButtonDefaults.filledTonalIconButtonColors(
                                        containerColor = Color.Black.copy(alpha = 0.5f)
                                    ),
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Orqaga")
                            }
                            FilledTonalIconButton(
                                onClick = {
                                    val send =
                                        Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "${anime.title}\nhttps://play.google.com/store/apps/details?id=uz.meduza.anime",
                                            )
                                        }
                                    runCatching {
                                        context.startActivity(
                                            Intent.createChooser(send, "Ulashish")
                                        )
                                    }
                                },
                                colors =
                                    IconButtonDefaults.filledTonalIconButtonColors(
                                        containerColor = Color.Black.copy(alpha = 0.5f)
                                    ),
                            ) {
                                Icon(Icons.Outlined.Share, "Ulashish")
                            }
                        }
                        Row(
                            Modifier.align(Alignment.BottomStart)
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(18.dp),
                        ) {
                            AsyncImage(
                                anime.posterUrl,
                                anime.title,
                                Modifier.width(94.dp)
                                    .aspectRatio(0.69f)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop,
                            )
                            Column(
                                Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    if (anime.type == "MOVIE") "FILM" else "SERIAL",
                                    color = PrimaryCyan,
                                    fontSize = 11.sp,
                                    letterSpacing = 2.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    anime.title,
                                    color = TextWhite,
                                    fontSize = 22.sp,
                                    lineHeight = 27.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 5,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
                item {
                    Column(
                        Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                "${String.format(Locale.US, "%.1f", anime.rating)} / 10",
                                color = StarGold,
                                fontSize = 13.sp,
                            )
                            Text("${anime.ratingCount} baho", color = TextMuted, fontSize = 13.sp)
                            anime.year?.let {
                                Text(it.toString(), color = TextGray, fontSize = 13.sp)
                            }
                            anime.ageRating?.let { Text(it, color = TextGray, fontSize = 13.sp) }
                        }
                        val first = state.episodes.firstOrNull()
                        Button(
                            onClick = {
                                if (!isLoggedIn) onSignIn()
                                else if (first != null) onEpisodeClick(first.id, slug)
                            },
                            enabled = !isLoggedIn || first != null,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                        ) {
                            Icon(
                                if (isLoggedIn) Icons.Default.PlayArrow else Icons.Outlined.Lock,
                                null,
                                Modifier.size(22.dp),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (!isLoggedIn) "Tomosha qilish uchun kiring"
                                else if (first == null) "Qismlar tayyorlanmoqda"
                                else "${first.episodeNumber}-qismdan boshlash",
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = {
                                    if (isLoggedIn) viewModel.toggleBookmark(slug) else onSignIn()
                                },
                                enabled = !state.isBookmarking,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp),
                            ) {
                                Icon(
                                    if (state.isBookmarked) Icons.Outlined.BookmarkAdded
                                    else Icons.Outlined.BookmarkAdd,
                                    null,
                                    Modifier.size(19.dp),
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    if (state.isBookmarked) "Saqlangan" else "Saqlash",
                                    fontSize = 13.sp,
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    if (isLoggedIn) {
                                        selectedScore = state.userRating ?: 8
                                        viewModel.clearActionError()
                                        ratingOpen = true
                                    } else onSignIn()
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp),
                            ) {
                                Icon(Icons.Outlined.RateReview, null, Modifier.size(19.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    state.userRating?.let { "$it / 10 · Baho" } ?: "Baho berish",
                                    fontSize = 13.sp,
                                )
                            }
                        }
                        if (!ratingOpen) state.actionError?.let { InlineMessage(it) }
                        Text(
                            "Anime haqida",
                            color = TextWhite,
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            anime.synopsis ?: "Tavsif hozircha qo‘shilmagan.",
                            color = TextGray,
                            fontSize = 14.sp,
                            lineHeight = 23.sp,
                            maxLines = if (expanded) Int.MAX_VALUE else 4,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if ((anime.synopsis?.length ?: 0) > 180)
                            TextButton(
                                onClick = { expanded = !expanded },
                                contentPadding = PaddingValues(0.dp),
                            ) {
                                Text(if (expanded) "Qisqartirish" else "To‘liq o‘qish")
                            }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            anime.genres.forEach { genre ->
                                Surface(color = CardDark, shape = RoundedCornerShape(8.dp)) {
                                    Text(
                                        genre.name,
                                        color = TextGray,
                                        fontSize = 12.sp,
                                        modifier =
                                            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    TabRow(
                        selectedTabIndex = tab,
                        containerColor = BackgroundDark,
                        contentColor = PrimaryCyan,
                    ) {
                        Tab(
                            tab == 0,
                            onClick = { if (isLoggedIn) tab = 0 else onSignIn() },
                            text = { Text(if (isLoggedIn) "Qismlar" else "Qismlar · Kirish") },
                        )
                        Tab(tab == 1, onClick = { tab = 1 }, text = { Text("Ma’lumotlar") })
                    }
                    Spacer(Modifier.height(16.dp))
                }
                if (tab == 0 && isLoggedIn) {
                    if (anime.seasons.size > 1)
                        item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                items(anime.seasons) { season ->
                                    FilterChip(
                                        selected = state.selectedSeason == season.seasonNumber,
                                        onClick = {
                                            viewModel.selectSeason(slug, season.seasonNumber)
                                        },
                                        label = { Text("${season.seasonNumber}-mavsum") },
                                    )
                                }
                            }
                        }
                    if (state.isLoadingEpisodes)
                        item {
                            Box(
                                Modifier.fillMaxWidth().padding(24.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    else if (state.episodesError != null)
                        item {
                            Box(Modifier.padding(20.dp)) {
                                InlineMessage(state.episodesError!!) {
                                    viewModel.selectSeason(slug, state.selectedSeason)
                                }
                            }
                        }
                    else if (state.episodes.isEmpty())
                        item {
                            EmptyState(
                                "Qismlar tayyorlanmoqda",
                                "Yangi qismlar chiqqanda shu yerda paydo bo‘ladi.",
                            )
                        }
                    else
                        items(state.episodes, key = { it.id }) { episode ->
                            Surface(
                                onClick = { onEpisodeClick(episode.id, slug) },
                                color = SurfaceDark,
                                shape = RoundedCornerShape(12.dp),
                                modifier =
                                    Modifier.padding(horizontal = 20.dp, vertical = 5.dp)
                                        .fillMaxWidth(),
                            ) {
                                Row(
                                    Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                ) {
                                    Box(
                                        Modifier.width(104.dp)
                                            .aspectRatio(16f / 9f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(CardDark)
                                    ) {
                                        AsyncImage(
                                            episode.thumbnailUrl
                                                ?: anime.bannerUrl
                                                ?: anime.posterUrl,
                                            null,
                                            Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop,
                                        )
                                        Icon(
                                            if (episode.isPremium) Icons.Outlined.WorkspacePremium
                                            else Icons.Default.PlayArrow,
                                            null,
                                            Modifier.align(Alignment.Center).size(25.dp),
                                            tint =
                                                if (episode.isPremium) PremiumGold else TextWhite,
                                        )
                                    }
                                    Column(
                                        Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(5.dp),
                                    ) {
                                        Text(
                                            "${episode.episodeNumber}-qism",
                                            color = PrimaryCyan,
                                            fontSize = 11.sp,
                                        )
                                        Text(
                                            episode.title,
                                            color = TextWhite,
                                            fontSize = 13.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        if (episode.duration > 0)
                                            Text(
                                                "${episode.duration / 60} daqiqa",
                                                color = TextMuted,
                                                fontSize = 11.sp,
                                            )
                                    }
                                }
                            }
                        }
                } else
                    item {
                        Column(
                            Modifier.padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            DetailMetadata(
                                "Holati",
                                when (anime.status) {
                                    "COMPLETED" -> "Tugallangan"
                                    "UPCOMING",
                                    "SCHEDULED" -> "Kutilmoqda"
                                    else -> "Davom etmoqda"
                                },
                            )
                            anime.studio
                                ?.takeIf { it.isNotBlank() }
                                ?.let { DetailMetadata("Studiya", it) }
                            DetailMetadata(
                                "Format",
                                if (anime.type == "MOVIE") "Film" else anime.type,
                            )
                            if (!isLoggedIn)
                                InlineMessage(
                                    "Qismlarni ochish, baholash va tomosha qilish uchun hisobingizga kiring."
                                )
                        }
                    }
            }
    }
    if (ratingOpen)
        AlertDialog(
            onDismissRequest = { if (!state.isRating) ratingOpen = false },
            title = { Text("Sizning bahoyingiz") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Anime sizga qanchalik yoqdi?", color = TextGray)
                    Text(
                        "$selectedScore / 10",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryCyan,
                    )
                    (0..1).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            (1..5).forEach { col ->
                                val number = row * 5 + col
                                FilledTonalButton(
                                    onClick = { selectedScore = number },
                                    enabled = !state.isRating,
                                    contentPadding = PaddingValues(0.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors =
                                        ButtonDefaults.filledTonalButtonColors(
                                            containerColor =
                                                if (selectedScore == number) PrimaryCyan
                                                else CardDarkElevated,
                                            contentColor =
                                                if (selectedScore == number) OnPrimary
                                                else TextWhite,
                                        ),
                                    modifier = Modifier.weight(1f).heightIn(min = 44.dp),
                                ) {
                                    Text(number.toString())
                                }
                            }
                        }
                    }
                    state.actionError?.let { Text(it, color = BadgeSeriesRed, fontSize = 12.sp) }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.rateAnime(slug, selectedScore) },
                    enabled = !state.isRating,
                ) {
                    Text(if (state.isRating) "Saqlanmoqda…" else "Bahoni saqlash")
                }
            },
            dismissButton = {
                TextButton(onClick = { ratingOpen = false }, enabled = !state.isRating) {
                    Text("Bekor qilish")
                }
            },
        )
}

@Composable
private fun DetailMetadata(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(label, color = TextMuted, modifier = Modifier.weight(1f), fontSize = 13.sp)
        Text(value, color = TextWhite, modifier = Modifier.weight(1f), fontSize = 13.sp)
    }
}
