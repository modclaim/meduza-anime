package uz.meduza.anime.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uz.meduza.anime.core.session.SessionManager
import uz.meduza.anime.data.models.PaymentPlanDto
import uz.meduza.anime.data.models.UserDto
import uz.meduza.anime.data.repository.AuthRepository
import uz.meduza.anime.data.repository.PaymentRepository
import uz.meduza.anime.data.repository.UserRepository

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: UserDto? = null,
    val isUpgradingPremium: Boolean = false,
    val errorMessage: String? = null,
    val plans: List<PaymentPlanDto> = emptyList(),
    val isLoadingPlans: Boolean = false,
    val isCreatingPayment: Boolean = false,
    val activeOrderId: String? = null,
    val isCheckingPayment: Boolean = false,
    val paymentSuccessMessage: String? = null,
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepo = UserRepository(application)
    private val authRepo = AuthRepository(application)
    private val paymentRepo = PaymentRepository(application)
    private val sessionManager = SessionManager.getInstance(application)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        if (sessionManager.isLoggedInSync()) {
            loadProfile()
            loadPaymentPlans()
        } else {
            _uiState.value = ProfileUiState(isLoading = false)
        }
    }

    fun loadProfile(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!forceRefresh) {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            }
            val result = userRepo.getMe(forceRefresh = forceRefresh)
            result.fold(
                onSuccess = { user ->
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            user = user,
                            errorMessage = null,
                        )
                },
                onFailure = { err ->
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            errorMessage = err.message,
                        )
                },
            )
        }
    }

    fun loadPaymentPlans() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingPlans = true)
            val result = paymentRepo.getPlans()
            result.fold(
                onSuccess = { plans ->
                    _uiState.value =
                        _uiState.value.copy(
                            plans = plans,
                            isLoadingPlans = false,
                        )
                },
                onFailure = {
                    _uiState.value =
                        _uiState.value.copy(
                            plans = emptyList(),
                            errorMessage = "Tariflarni yuklab bo‘lmadi. Qayta urinib ko‘ring.",
                            isLoadingPlans = false,
                        )
                },
            )
        }
    }

    fun createPayment(planType: String, onInvoiceReady: (payUrl: String, orderId: String) -> Unit) {
        if (
            _uiState.value.isCreatingPayment ||
                _uiState.value.plans.none { it.planType == planType }
        )
            return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingPayment = true, errorMessage = null)
            val result = paymentRepo.createPayment(planType)
            result.fold(
                onSuccess = { res ->
                    _uiState.value =
                        _uiState.value.copy(
                            isCreatingPayment = false,
                            activeOrderId = res.orderId,
                        )
                    onInvoiceReady(res.payUrl, res.orderId)
                },
                onFailure = { err ->
                    _uiState.value =
                        _uiState.value.copy(
                            isCreatingPayment = false,
                            errorMessage = err.message ?: "To'lovni boshlashda xatolik",
                        )
                },
            )
        }
    }

    fun checkPaymentStatus(orderId: String, onFinished: (Boolean) -> Unit) {
        if (_uiState.value.isCheckingPayment) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCheckingPayment = true)
            val result = paymentRepo.getPaymentStatus(orderId)
            result.fold(
                onSuccess = { statusRes ->
                    _uiState.value = _uiState.value.copy(isCheckingPayment = false)
                    if (
                        statusRes.status.equals("success", ignoreCase = true) ||
                            statusRes.paidAt != null
                    ) {
                        loadProfile(forceRefresh = true)
                        _uiState.value =
                            _uiState.value.copy(
                                paymentSuccessMessage =
                                    "To'lov muvaffaqiyatli amalga oshirildi! Premium obunangiz faollashtirildi! 🎉"
                            )
                        onFinished(true)
                    } else {
                        onFinished(false)
                    }
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isCheckingPayment = false)
                    onFinished(false)
                },
            )
        }
    }

    fun clearPaymentSuccessMessage() {
        _uiState.value = _uiState.value.copy(paymentSuccessMessage = null)
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            try {
                authRepo.logout()
            } finally {
                onLoggedOut()
            }
        }
    }

    fun deleteAccount(onDeleted: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = userRepo.deleteAccount()
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onDeleted()
                },
                onFailure = { err ->
                    _uiState.value =
                        _uiState.value.copy(isLoading = false, errorMessage = err.message)
                },
            )
        }
    }
}
