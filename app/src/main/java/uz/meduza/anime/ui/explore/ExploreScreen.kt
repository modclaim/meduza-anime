package uz.meduza.anime.ui.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import uz.meduza.anime.core.ui.components.AnimeCard
import uz.meduza.anime.core.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExploreScreen(
    onAnimeClick: (slug: String) -> Unit,
    viewModel: ExploreViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showGenreSheet by remember { mutableStateOf(false) }
    var genreFilterQuery by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val filters = listOf(
        "ALL" to "Barchasi",
        "TV" to "Seriallar",
        "MOVIE" to "Filmlar",
        "ONGOING" to "Davom etayotgan",
        "COMPLETED" to "Tugallangan"
    )

    val sortOptions = listOf(
        "latest" to "Eng yangi",
        "rating" to "Reyting",
        "popular" to "Ommabop"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(top = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Katalog",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )

            // Janrlar Filter Trigger Button
            val hasGenreSelection = uiState.selectedGenres.isNotEmpty()
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (hasGenreSelection) PrimaryCyan else CardDark)
                    .border(
                        1.dp,
                        if (hasGenreSelection) PrimaryCyan else InputBorder,
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { showGenreSheet = true }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Janrlar",
                        tint = if (hasGenreSelection) Color.Black else PrimaryCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (hasGenreSelection) "Janrlar (${uiState.selectedGenres.size})" else "Janrlar",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasGenreSelection) Color.Black else TextWhite
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Selected Genres Row (if any selected, allow quick view & dismiss)
        if (uiState.selectedGenres.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.Red.copy(alpha = 0.15f))
                            .border(1.dp, Color.Red.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .clickable { viewModel.clearGenres() }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tozalash",
                                tint = Color(0xFFFF6B6B),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Tozalash",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFFF6B6B)
                            )
                        }
                    }
                }

                val selectedGenresList = uiState.genres.filter { uiState.selectedGenres.contains(it.slug) }
                items(selectedGenresList) { genre ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(PrimaryCyan)
                            .clickable { viewModel.toggleGenre(genre.slug) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = genre.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Olib tashlash",
                                tint = Color.Black.copy(alpha = 0.7f),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Horizontal Category Tabs
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filters) { (key, label) ->
                val isSelected = uiState.selectedFilter == key
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) PrimaryCyan else CardDark)
                        .clickable { viewModel.setFilter(key) }
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

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal Sort Options
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sortOptions) { (key, label) ->
                val isSelected = uiState.sortBy == key
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) CardDarkElevated else CardDark.copy(alpha = 0.6f))
                        .clickable { viewModel.setSort(key) }
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = (if (isSelected) "✓ " else "") + label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) PrimaryCyan else TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryCyan)
            }
        } else if (uiState.animes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = uiState.errorMessage ?: "Animelar topilmadi",
                        color = TextGray,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { viewModel.loadAnimes() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryCyan,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Qayta yuklash", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.animes) { anime ->
                    val epText = if (anime.type.equals("MOVIE", ignoreCase = true)) {
                        "To'liq metrajli film"
                    } else {
                        "${anime.totalReleasedEpisodes} ta qism"
                    }
                    AnimeCard(
                        title = anime.title,
                        posterUrl = anime.posterUrl,
                        type = anime.type,
                        episodesInfo = epText,
                        rating = anime.rating,
                        ratingCount = anime.ratingCount,
                        onClick = { onAnimeClick(anime.slug) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    // Genre Selection Modal Bottom Sheet in Catalog
    if (showGenreSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showGenreSheet = false
                genreFilterQuery = ""
            },
            sheetState = sheetState,
            containerColor = CardDark,
            dragHandle = { BottomSheetDefaults.DragHandle(color = TextGray) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Janrlarni tanlang",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    if (uiState.selectedGenres.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearGenres() }) {
                            Text("Barchasini tozalash", color = PrimaryCyan, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search inside genres
                OutlinedTextField(
                    value = genreFilterQuery,
                    onValueChange = { genreFilterQuery = it },
                    placeholder = { Text("Janr nomini yozing...", color = TextGray, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = PrimaryCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (genreFilterQuery.isNotEmpty()) {
                            IconButton(onClick = { genreFilterQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = TextGray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = InputBackground,
                        unfocusedContainerColor = InputBackground,
                        focusedBorderColor = PrimaryCyan,
                        unfocusedBorderColor = InputBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Multi-row Flow chips for all genres
                val filteredGenres = if (genreFilterQuery.isBlank()) {
                    uiState.genres
                } else {
                    uiState.genres.filter { it.name.contains(genreFilterQuery.trim(), ignoreCase = true) }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        filteredGenres.forEach { genre ->
                            val isSelected = uiState.selectedGenres.contains(genre.slug)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) PrimaryCyan else InputBackground)
                                    .border(
                                        1.dp,
                                        if (isSelected) PrimaryCyan else InputBorder,
                                        RoundedCornerShape(20.dp)
                                    )
                                    .clickable { viewModel.toggleGenre(genre.slug) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.Black,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Text(
                                        text = genre.name,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.Black else TextWhite
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Done button
                Button(
                    onClick = { showGenreSheet = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryCyan,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = if (uiState.selectedGenres.isNotEmpty()) {
                            "Ko'rsatish (${uiState.animes.size} ta anime)"
                        } else {
                            "Tayyor"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
