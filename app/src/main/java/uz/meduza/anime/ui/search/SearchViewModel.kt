package uz.meduza.anime.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uz.meduza.anime.data.models.AnimeDto
import uz.meduza.anime.data.repository.AnimeRepository

data class SearchUiState(
    val query: String = "",
    val sortBy: String = "latest", // latest, rating, views, title
    val isLoading: Boolean = false,
    val searchResults: List<AnimeDto> = emptyList(),
    val errorMessage: String? = null
)

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    private val animeRepo = AnimeRepository(application)
    private var searchJob: Job? = null

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = animeRepo.getAnimes(sortBy = _uiState.value.sortBy)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                searchResults = result.getOrDefault(emptyList())
            )
        }
    }

    fun onQueryChange(newQuery: String) {
        _uiState.value = _uiState.value.copy(query = newQuery)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(350) // Debounce 350ms
            performSearch(newQuery, _uiState.value.sortBy)
        }
    }

    fun setSort(newSort: String) {
        if (_uiState.value.sortBy == newSort) return
        _uiState.value = _uiState.value.copy(sortBy = newSort)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            performSearch(_uiState.value.query, newSort)
        }
    }

    fun retry() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            performSearch(_uiState.value.query, _uiState.value.sortBy)
        }
    }

    private suspend fun performSearch(q: String, sort: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        val result = if (q.isBlank()) {
            animeRepo.getAnimes(sortBy = sort)
        } else {
            animeRepo.searchAnimes(query = q.trim(), sortBy = sort)
        }

        result.onSuccess { animes ->
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                searchResults = animes,
                errorMessage = null
            )
        }.onFailure { error ->
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = error.localizedMessage ?: "Qidiruvda xatolik yuz berdi"
            )
        }
    }
}
