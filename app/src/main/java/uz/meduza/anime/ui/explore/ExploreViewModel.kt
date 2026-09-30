package uz.meduza.anime.ui.explore

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uz.meduza.anime.data.models.AnimeDto
import uz.meduza.anime.data.models.GenreDto
import uz.meduza.anime.data.repository.AnimeRepository

data class ExploreUiState(
    val isLoading: Boolean = true,
    val selectedFilter: String = "ALL", // ALL, TV, MOVIE, ONGOING, COMPLETED
    val sortBy: String = "latest", // latest, views, rating
    val genres: List<GenreDto> = emptyList(),
    val selectedGenres: Set<String> = emptySet(),
    val animes: List<AnimeDto> = emptyList(),
    val errorMessage: String? = null
)

class ExploreViewModel(application: Application) : AndroidViewModel(application) {
    private val animeRepo = AnimeRepository(application)

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val genresResult = animeRepo.getGenres()
            val sortedGenres = genresResult.getOrDefault(emptyList()).sortedBy { it.name }
            _uiState.value = _uiState.value.copy(genres = sortedGenres)
            loadAnimes()
        }
    }

    fun setFilter(filter: String) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
        loadAnimes()
    }

    fun setSort(sort: String) {
        _uiState.value = _uiState.value.copy(sortBy = sort)
        loadAnimes()
    }

    fun toggleGenre(genreSlug: String) {
        val current = _uiState.value.selectedGenres
        val next = if (current.contains(genreSlug)) {
            current - genreSlug
        } else {
            current + genreSlug
        }
        _uiState.value = _uiState.value.copy(selectedGenres = next)
        loadAnimes()
    }

    fun clearGenres() {
        if (_uiState.value.selectedGenres.isEmpty()) return
        _uiState.value = _uiState.value.copy(selectedGenres = emptySet())
        loadAnimes()
    }

    fun loadAnimes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val state = _uiState.value
            val typeParam = if (state.selectedFilter == "TV" || state.selectedFilter == "MOVIE") state.selectedFilter else null
            val statusParam = if (state.selectedFilter == "ONGOING" || state.selectedFilter == "COMPLETED") state.selectedFilter else null
            val genreParam = if (state.selectedGenres.isEmpty()) null else state.selectedGenres.sorted().joinToString(",")

            val result = animeRepo.getAnimes(
                type = typeParam,
                status = statusParam,
                sortBy = state.sortBy,
                genre = genreParam
            )

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                animes = result.getOrDefault(emptyList()),
                errorMessage = if (result.isFailure) result.exceptionOrNull()?.localizedMessage else null
            )
        }
    }
}
