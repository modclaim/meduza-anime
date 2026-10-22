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
    val errorMessage: String? = null,
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository(application)
    private val authManager = uz.meduza.anime.core.auth.AuthManager()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signInWithNexa() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val nexaResult = authManager.signIn()
            nexaResult.fold(
                onSuccess = { tokens ->
                    val userInfo =
                        (authManager.authState.value
                                as? uz.meduza.anime.core.auth.model.AuthState.Authenticated)
                            ?.user
                    val backendResult = repository.loginWithNexa(tokens, userInfo)
                    backendResult.fold(
                        onSuccess = { data ->
                            _uiState.value = AuthUiState(isSuccess = true, authData = data)
                        },
                        onFailure = { error ->
                            _uiState.value =
                                AuthUiState(
                                    errorMessage =
                                        "Kirishni yakunlab bo‘lmadi. Internet aloqasini tekshirib, qayta urinib ko‘ring."
                                )
                        },
                    )
                },
                onFailure = { error ->
                    _uiState.value =
                        AuthUiState(errorMessage = "Kirish yakunlanmadi. Qayta urinib ko‘ring.")
                },
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
