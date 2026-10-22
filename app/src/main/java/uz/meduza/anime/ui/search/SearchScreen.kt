package uz.meduza.anime.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import uz.meduza.anime.core.ui.components.*
import uz.meduza.anime.core.ui.theme.*

@Composable
fun SearchScreen(onAnimeClick: (String) -> Unit, viewModel: SearchViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    Column(Modifier.fillMaxSize().background(BackgroundDark).statusBarsPadding()) {
        ScreenHeading("Qidiruv", "Bir hikoya nomidan boshlang")
        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            placeholder = { Text("Anime nomini yozing", fontSize = 14.sp) },
            singleLine = true,
            leadingIcon = { Icon(Icons.Outlined.Search, null) },
            trailingIcon = {
                if (state.query.isNotEmpty())
                    IconButton(onClick = { viewModel.onQueryChange("") }) {
                        Icon(Icons.Outlined.Close, "Qidiruvni tozalash")
                    }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 12.dp, bottom = 6.dp),
        ) {
            items(
                listOf(
                    "latest" to "Eng yangi",
                    "rating" to "Reyting",
                    "views" to "Ommabop",
                    "title" to "A–Z",
                )
            ) { (key, label) ->
                FilterChip(
                    selected = state.sortBy == key,
                    onClick = { viewModel.setSort(key) },
                    label = { Text(label, fontSize = 12.sp) },
                )
            }
        }
        when {
            state.isLoading ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            state.errorMessage != null ->
                EmptyState(
                    "Qidiruv amalga oshmadi",
                    "Internet aloqasini tekshiring.",
                    actionLabel = "Qayta urinish",
                    onAction = viewModel::retry,
                )
            state.searchResults.isEmpty() ->
                EmptyState(
                    "Hech narsa topilmadi",
                    "Nomini boshqacharoq yozib ko‘ring.",
                    Icons.Outlined.Search,
                )
            else ->
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(144.dp),
                    contentPadding = PaddingValues(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    items(state.searchResults, key = { it.id }) { anime ->
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
}
