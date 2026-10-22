package uz.meduza.anime.ui.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import uz.meduza.anime.core.ui.components.*
import uz.meduza.anime.core.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExploreScreen(onAnimeClick: (String) -> Unit, viewModel: ExploreViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    var filtersOpen by remember { mutableStateOf(false) }
    var selectedGenres by remember { mutableStateOf(emptySet<String>()) }
    Column(Modifier.fillMaxSize().background(BackgroundDark).statusBarsPadding()) {
        ScreenHeading(
            "Katalog",
            "Keyingi sevimli hikoyangizni toping",
            action = {
                IconButton(
                    onClick = {
                        selectedGenres = state.selectedGenres
                        filtersOpen = true
                    }
                ) {
                    Icon(
                        Icons.Outlined.Tune,
                        "Janrlarni tanlash",
                        tint = if (state.selectedGenres.isEmpty()) TextGray else PrimaryCyan,
                    )
                }
            },
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                listOf(
                    "ALL" to "Barchasi",
                    "TV" to "Seriallar",
                    "MOVIE" to "Filmlar",
                    "ONGOING" to "Davom etmoqda",
                    "COMPLETED" to "Tugallangan",
                )
            ) { (key, label) ->
                FilterChip(
                    selected = state.selectedFilter == key,
                    onClick = { viewModel.setFilter(key) },
                    label = { Text(label, fontSize = 12.sp) },
                )
            }
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(listOf("latest" to "Eng yangi", "rating" to "Reyting", "views" to "Ommabop")) {
                (key, label) ->
                TextButton(onClick = { viewModel.setSort(key) }) {
                    Text(
                        label,
                        color = if (state.sortBy == key) PrimaryCyan else TextMuted,
                        fontSize = 12.sp,
                    )
                }
            }
        }
        if (state.selectedGenres.isNotEmpty())
            TextButton(
                onClick = viewModel::clearGenres,
                modifier = Modifier.padding(horizontal = 12.dp),
            ) {
                Text("${state.selectedGenres.size} janr tanlangan · Tozalash", fontSize = 12.sp)
            }
        when {
            state.isLoading ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            state.errorMessage != null ->
                EmptyState(
                    "Katalog yuklanmadi",
                    "Internet aloqasini tekshiring.",
                    actionLabel = "Qayta urinish",
                    onAction = viewModel::loadAnimes,
                )
            state.animes.isEmpty() ->
                EmptyState("Anime topilmadi", "Boshqa janr yoki saralash turini tanlab ko‘ring.")
            else ->
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(144.dp),
                    contentPadding = PaddingValues(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    items(state.animes, key = { it.id }) { anime ->
                        AnimeCard(
                            anime.title,
                            anime.posterUrl,
                            anime.type,
                            if (anime.type == "MOVIE") "Film"
                            else "${anime.totalReleasedEpisodes} qism",
                            anime.rating,
                            anime.ratingCount,
                            { onAnimeClick(anime.slug) },
                            Modifier.fillMaxWidth(),
                        )
                    }
                }
        }
    }
    if (filtersOpen)
        ModalBottomSheet(onDismissRequest = { filtersOpen = false }, containerColor = SurfaceDark) {
            Column(
                Modifier.fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("Janrlarni tanlang", style = MaterialTheme.typography.headlineMedium)
                Text("Bir nechta janr tanlashingiz mumkin", color = TextMuted, fontSize = 13.sp)
                if (state.genres.isEmpty()) Text("Janrlar hozircha yuklanmagan", color = TextGray)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    state.genres.forEach { genre ->
                        FilterChip(
                            selected = genre.slug in selectedGenres,
                            onClick = {
                                selectedGenres =
                                    if (genre.slug in selectedGenres) selectedGenres - genre.slug
                                    else selectedGenres + genre.slug
                            },
                            label = { Text(genre.name) },
                        )
                    }
                }
                Button(
                    onClick = {
                        viewModel.setGenres(selectedGenres)
                        filtersOpen = false
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                ) {
                    Text("Natijalarni ko‘rish")
                }
                TextButton(
                    onClick = { selectedGenres = emptySet() },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    Text("Tanlovni tozalash")
                }
                Spacer(Modifier.height(12.dp))
            }
        }
}
