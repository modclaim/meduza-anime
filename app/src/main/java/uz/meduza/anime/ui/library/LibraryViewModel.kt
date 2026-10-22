package uz.meduza.anime.ui.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uz.meduza.anime.data.models.BookmarkItemDto
import uz.meduza.anime.data.models.WatchHistoryItemDto
import uz.meduza.anime.data.repository.LibraryRepository

data class LibraryUiState(
    val selectedTab: Int = 0, // 0 = Bookmarks, 1 = History
    val isLoading: Boolean = true,
    val bookmarks: List<BookmarkItemDto> = emptyList(),
    val history: List<WatchHistoryItemDto> = emptyList(),
    val errorMessage: String? = null,
)

class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val libraryRepo = LibraryRepository(application)
    private var loadJob: kotlinx.coroutines.Job? = null

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun selectTab(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tabIndex)
        loadData()
    }

    fun loadData() {
        if (_uiState.value.selectedTab == 0) {
            loadBookmarks()
        } else {
            loadHistory()
        }
    }

    private fun loadBookmarks() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = libraryRepo.getBookmarks(null)
            _uiState.value =
                _uiState.value.copy(
                    isLoading = false,
                    bookmarks = result.getOrDefault(_uiState.value.bookmarks),
                    errorMessage = result.exceptionOrNull()?.message,
                )
        }
    }

    private fun loadHistory() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = libraryRepo.getWatchHistory()
            _uiState.value =
                _uiState.value.copy(
                    isLoading = false,
                    history = result.getOrDefault(_uiState.value.history),
                    errorMessage = result.exceptionOrNull()?.message,
                )
        }
    }

    fun deleteHistoryItem(episodeId: String) {
        viewModelScope.launch {
            libraryRepo.deleteHistoryItem(episodeId)
            loadHistory()
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            libraryRepo.clearHistory()
            loadHistory()
        }
    }

    fun removeBookmark(slug: String) {
        viewModelScope.launch {
            libraryRepo.removeBookmark(slug)
            loadBookmarks()
        }
    }

    fun refreshCurrentTab() {
        loadData()
    }
}
