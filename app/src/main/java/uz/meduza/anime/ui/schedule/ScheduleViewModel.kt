package uz.meduza.anime.ui.schedule

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uz.meduza.anime.data.models.*
import uz.meduza.anime.data.repository.ScheduleRepository

fun getTodayDayOfWeek(): String {
    return try {
        java.time.LocalDate.now().dayOfWeek.name
    } catch (_: Exception) {
        "SATURDAY"
    }
}

data class ScheduleUiState(
    val isLoading: Boolean = true,
    val selectedDay: String = getTodayDayOfWeek(),
    val scheduleData: ScheduleResponse? = null,
    val dayAnimes: List<ScheduledDayAnimeDto> = emptyList(),
    val errorMessage: String? = null
)

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {
    private val scheduleRepo = ScheduleRepository(application)

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    init {
        loadSchedule()
    }

    fun selectDay(day: String) {
        _uiState.value = _uiState.value.copy(selectedDay = day)
        updateDayAnimes(day)
    }

    private fun updateDayAnimes(day: String) {
        val calendar = _uiState.value.scheduleData?.weeklyCalendar
        val animes = calendar?.get(day) ?: emptyList()
        _uiState.value = _uiState.value.copy(dayAnimes = animes)
    }

    fun loadSchedule() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = scheduleRepo.getWeeklySchedule()
            result.fold(
                onSuccess = { data ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        scheduleData = data
                    )
                    updateDayAnimes(_uiState.value.selectedDay)
                },
                onFailure = { err ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = err.message ?: "Jadvalni yuklab bo'lmadi"
                    )
                }
            )
        }
    }
}
