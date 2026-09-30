package uz.meduza.anime.ui.detail

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.BookmarkRemove
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import uz.meduza.anime.core.ui.components.EpisodeItemCard
import uz.meduza.anime.core.ui.components.EpisodeStyle
import uz.meduza.anime.core.ui.components.formatRatingCount
import uz.meduza.anime.core.ui.theme.*
import uz.meduza.anime.data.models.AnimeDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeDetailScreen(
    slug: String,
    initialTab: Int = 0,
    onBackClick: () -> Unit,
    onEpisodeClick: (episodeId: String, slug: String) -> Unit,
    viewModel: AnimeDetailViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) } // 0: Qismlar, 1: Ma'lumot, 2: Fikrlar
    var isSynopsisExpanded by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }

    LaunchedEffect(slug) {
        viewModel.loadAnimeDetail(slug)
    }

    LaunchedEffect(uiState.anime) {
        val anime = uiState.anime
        if (anime != null && initialTab == 0 && (anime.status.equals("UPCOMING", ignoreCase = true) || anime.status.equals("SCHEDULED", ignoreCase = true) || (anime.totalReleasedEpisodes == 0 && uiState.episodes.isEmpty()))) {
            selectedTab = 1
        }
    }

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
        } else if (uiState.anime != null) {
            val anime = uiState.anime!!
            val isMovie = anime.type.equals("MOVIE", ignoreCase = true)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 40.dp)
            ) {
                // 1. HERO BANNER WITH GRADIENT & NAV BUTTONS
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(360.dp)
                    ) {
                        // Poster / Banner Backdrop
                        AsyncImage(
                            model = anime.bannerUrl ?: anime.posterUrl,
                            contentDescription = anime.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Vignette & Bottom fade
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.6f),
                                            Color.Transparent,
                                            BackgroundDark.copy(alpha = 0.7f),
                                            BackgroundDark
                                        )
                                    )
                                )
                        )

                        // Top Header Actions (Back & Bookmark & Share)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .align(Alignment.TopCenter),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onBackClick,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.65f))
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Orqaga",
                                    tint = TextWhite
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(
                                    onClick = { viewModel.toggleBookmark(slug) },
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.65f))
                                ) {
                                    Icon(
                                        imageVector = if (uiState.isBookmarked) Icons.Outlined.BookmarkRemove else Icons.Outlined.BookmarkAdd,
                                        contentDescription = "Saqlash",
                                        tint = if (uiState.isBookmarked) PrimaryCyan else TextWhite
                                    )
                                }
                            }
                        }

                        // Poster Overlay & Title preview at bottom of Hero
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomStart)
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Small floating poster card
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(145.dp)
                                    .border(1.dp, CardDarkElevated, RoundedCornerShape(12.dp))
                            ) {
                                AsyncImage(
                                    model = anime.posterUrl,
                                    contentDescription = anime.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            // Title & badges
                            Column(modifier = Modifier.weight(1f)) {
                                val isMovie = anime.type.equals("MOVIE", ignoreCase = true)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isMovie) BadgeMovieCyan else BadgeSeriesRed)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isMovie) "FILM" else "SERIAL",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(PrimaryCyan.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "O'zbekcha Dublyaj",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryCyan
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = anime.title,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { showRatingDialog = true }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Baho",
                                        tint = StarGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = String.format(java.util.Locale.US, "%.1f", anime.rating),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StarGold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "(${formatRatingCount(anime.ratingCount)} baho)",
                                        fontSize = 11.sp,
                                        color = TextGray
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. MAIN CTA BUTTONS (Hozir ko'rish & Treyler)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Play First Episode / Movie Button OR Premiere Pending Button
                        val firstEp = uiState.episodes.firstOrNull()
                        val isUpcoming = anime.status.equals("UPCOMING", ignoreCase = true) ||
                                anime.status.equals("SCHEDULED", ignoreCase = true) ||
                                (uiState.episodes.isEmpty() && anime.totalReleasedEpisodes == 0)

                        if (firstEp != null) {
                            Button(
                                onClick = {
                                    onEpisodeClick(firstEp.id, slug)
                                },
                                modifier = Modifier
                                    .weight(1.4f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryCyan,
                                    contentColor = Color.Black
                                )
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isMovie) "Filmni ko'rish" else "Ko'rish (${firstEp.episodeNumber}-qism)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            // Upcoming Premiere / No episodes yet Button
                            Button(
                                onClick = {
                                    Toast.makeText(
                                        context,
                                        "Ushbu anime premyerasi tez kunda chiqadi. Qismlar joylanishi bilan ko'rishingiz mumkin!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    if (!uiState.isBookmarked) {
                                        viewModel.toggleBookmark(slug)
                                        Toast.makeText(context, "Kutubxonangizga saqlandi!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1.4f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryCyan.copy(alpha = 0.2f),
                                    contentColor = PrimaryCyan
                                )
                            ) {
                                Icon(imageVector = Icons.Default.Schedule, contentDescription = "Premyera", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Premyera kutilmoqda",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        // Bookmark Button
                        OutlinedButton(
                            onClick = { viewModel.toggleBookmark(slug) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (uiState.isBookmarked) PrimaryCyan else TextWhite
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (uiState.isBookmarked) PrimaryCyan else InputBorder
                            )
                        ) {
                            Icon(
                                imageVector = if (uiState.isBookmarked) Icons.Default.Check else Icons.Outlined.BookmarkAdd,
                                contentDescription = "Saqlash",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (uiState.isBookmarked) "Saqlangan" else "Saqlash",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // 3. SEGMENTED TAB SWITCHER [ QISMLAR ] [ MA'LUMOT ] (Only for TV series)
                if (!isMovie) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 6.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(CardDark)
                                .padding(4.dp)
                        ) {
                            // Tab 0: Qismlar
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selectedTab == 0) PrimaryCyan else CardDark)
                                    .clickable { selectedTab = 0 }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Qismlar (${uiState.episodes.size})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTab == 0) Color.Black else TextGray
                                )
                            }

                            // Tab 1: Ma'lumot
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selectedTab == 1) PrimaryCyan else CardDark)
                                    .clickable { selectedTab = 1 }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Tafsilotlar",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTab == 1) Color.Black else TextGray
                                )
                            }
                        }
                    }
                }

                // 4. TAB CONTENT: QISMLAR (Episodes) - Only for TV series
                if (!isMovie && selectedTab == 0) {
                    // Season Selector (if multiple seasons)
                    if (anime.seasons.size > 1) {
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(anime.seasons) { season ->
                                    val isSelected = uiState.selectedSeason == season.seasonNumber
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(if (isSelected) PrimaryCyan else CardDark)
                                            .clickable { viewModel.selectSeason(slug, season.seasonNumber) }
                                            .padding(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "${season.seasonNumber}-Fasl",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.Black else TextGray
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (uiState.episodes.isEmpty()) {
                        item {
                            val isUpcoming = anime.status.equals("UPCOMING", ignoreCase = true) ||
                                    anime.status.equals("SCHEDULED", ignoreCase = true) ||
                                    (uiState.episodes.isEmpty() && anime.totalReleasedEpisodes == 0)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 36.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryCyan.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = "Premyera",
                                        tint = PrimaryCyan,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = if (isUpcoming) "Premyera tez kunda!" else "Qismlar hali joylanmagan",
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (isUpcoming) {
                                        "Ushbu anime tez kunda Meduza dublyajida premyera qilinadi. Chiqishi bilan tomosha qilish uchun saqlab qo'ying!"
                                    } else {
                                        "Ushbu fasl uchun qismlar yuklanmoqda yoki hali joylanmagan."
                                    },
                                    color = TextGray,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                                if (isUpcoming && !uiState.isBookmarked) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = {
                                            viewModel.toggleBookmark(slug)
                                            Toast.makeText(context, "Kutubxonangizga saqlandi!", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(20.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan, contentColor = Color.Black)
                                    ) {
                                        Icon(imageVector = Icons.Outlined.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "Sevimlilarga saqlash", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    } else {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            // Episode list section header — shows episode name context
                            Text(
                                text = "Barcha qismlar",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                            )
                        }

                        items(uiState.episodes, key = { it.id }) { episode ->
                            EpisodeItemCard(
                                episodeNumber = episode.episodeNumber,
                                title = episode.title,
                                durationSeconds = episode.duration,
                                thumbnailUrl = episode.thumbnailUrl ?: anime.posterUrl,
                                isPremium = episode.isPremium,
                                onClick = { onEpisodeClick(episode.id, slug) },
                                style = EpisodeStyle.COMPACT,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // 5. TAB CONTENT: TAFSILOTLAR (Details & Synopsis)
                if (isMovie || selectedTab == 1) {
                    item {
                        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                            // Synopsis Title
                            Text(
                                text = "Anime haqida",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Synopsis text
                            if (!anime.synopsis.isNullOrBlank()) {
                                val isLong = anime.synopsis.length > 140
                                Column(modifier = Modifier.animateContentSize()) {
                                    Text(
                                        text = anime.synopsis,
                                        fontSize = 13.sp,
                                        lineHeight = 20.sp,
                                        color = TextMuted,
                                        maxLines = if (isSynopsisExpanded || !isLong) Int.MAX_VALUE else 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (isLong) {
                                        Text(
                                            text = if (isSynopsisExpanded) "Kamroq ko'rsatish" else "Ko'proq o'qish...",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryCyan,
                                            modifier = Modifier
                                                .clickable { isSynopsisExpanded = !isSynopsisExpanded }
                                                .padding(vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Metadata Grid
                            Text(
                                text = "Qo'shimcha ma'lumotlar",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CardDark)
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                MetaRow(label = "Chiqarilgan yili:", value = anime.year?.toString() ?: "2024")
                                MetaRow(label = "Studiya:", value = anime.studio ?: "Meduza Studio")
                                MetaRow(label = "Holati:", value = if (anime.status.equals("COMPLETED", true)) "Tugallangan" else "Davom etmoqda")
                                MetaRow(label = "Tavsiya etilgan yosh:", value = anime.ageRating ?: "16+")
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Genres Chips
                            Text(
                                text = "Janrlar",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(anime.genres) { genre ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(CardDarkElevated)
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = genre.name,
                                            fontSize = 12.sp,
                                            color = PrimaryCyan,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (uiState.errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = uiState.errorMessage!!, color = TextGray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.loadAnimeDetail(slug) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan)
                    ) {
                        Text("Qayta yuklash", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Rating Dialog
    if (showRatingDialog) {
        var selectedScore by remember { mutableStateOf(10) }
        AlertDialog(
            onDismissRequest = { showRatingDialog = false },
            containerColor = CardDark,
            title = {
                Text(text = "Animeni baholash", color = TextWhite, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "★ $selectedScore / 10", color = StarGold, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Slider(
                        value = selectedScore.toFloat(),
                        onValueChange = { selectedScore = it.toInt() },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = PrimaryCyan,
                            activeTrackColor = PrimaryCyan,
                            inactiveTrackColor = CardDarkElevated
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.rateAnime(slug, selectedScore)
                        showRatingDialog = false
                    }
                ) {
                    Text("Baholash", color = PrimaryCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRatingDialog = false }) {
                    Text("Bekor qilish", color = TextGray)
                }
            }
        )
    }
}

@Composable
private fun MetaRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextGray, fontSize = 13.sp)
        Text(text = value, color = TextWhite, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}
