package uz.meduza.anime.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uz.meduza.anime.data.models.AuthData
import uz.meduza.anime.data.repository.AuthRepository

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val authData: AuthData? = null,
    val errorMessage: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository(application)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(loginText: String, passText: String) {
        if (loginText.isBlank() || passText.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Iltimos, barcha maydonlarni to'ldiring")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.login(loginText.trim(), passText.trim())
            result.fold(
                onSuccess = { data ->
                    _uiState.value = AuthUiState(isSuccess = true, authData = data)
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState(errorMessage = error.message ?: "Kirishda xatolik yuz berdi")
                }
            )
        }
    }

    fun register(emailText: String, userText: String, passText: String) {
        if (emailText.isBlank() || userText.isBlank() || passText.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Barcha maydonlar to'ldirilishi shart")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.register(emailText.trim(), userText.trim(), passText.trim())
            result.fold(
                onSuccess = { data ->
                    _uiState.value = AuthUiState(isSuccess = true, authData = data)
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState(errorMessage = error.message ?: "Ro'yxatdan o'tishda xatolik")
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun resetState() {
        _uiState.value = AuthUiState()
    }
}
