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
    val paymentSuccessMessage: String? = null
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepo = UserRepository(application)
    private val authRepo = AuthRepository(application)
    private val paymentRepo = PaymentRepository(application)
    private val sessionManager = SessionManager.getInstance(application)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
        loadPaymentPlans()
    }

    fun loadProfile(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!forceRefresh) {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            }
            val result = userRepo.getMe(forceRefresh = forceRefresh)
            result.fold(
                onSuccess = { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = user,
                        errorMessage = null
                    )
                },
                onFailure = { err ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = err.message
                    )
                }
            )
        }
    }

    fun loadPaymentPlans() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingPlans = true)
            val result = paymentRepo.getPlans()
            result.fold(
                onSuccess = { plans ->
                    _uiState.value = _uiState.value.copy(
                        plans = if (plans.isNotEmpty()) plans else defaultPlans(),
                        isLoadingPlans = false
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        plans = defaultPlans(),
                        isLoadingPlans = false
                    )
                }
            )
        }
    }

    private fun defaultPlans(): List<PaymentPlanDto> {
        return listOf(
            PaymentPlanDto(
                planType = "1_MONTH",
                title = "1 Oylik Premium",
                days = 30,
                price = 25000,
                currency = "UZS",
                discount = null,
                description = "1080p FHD sifat, barcha qismlar, reklamasiz"
            ),
            PaymentPlanDto(
                planType = "3_MONTHS",
                title = "3 Oylik Premium",
                days = 90,
                price = 70000,
                currency = "UZS",
                discount = "7% chegirma",
                description = "3 oy cheklovlarsiz tomosha va to'liq kirish"
            ),
            PaymentPlanDto(
                planType = "6_MONTHS",
                title = "6 Oylik Premium",
                days = 180,
                price = 130000,
                currency = "UZS",
                discount = "13% chegirma",
                description = "Yarim yillik qulay obuna va yangi premyeralar"
            ),
            PaymentPlanDto(
                planType = "12_MONTHS",
                title = "1 Yillik VIP Premium",
                days = 365,
                price = 230000,
                currency = "UZS",
                discount = "23% chegirma",
                description = "1 yil to'liq VIP obuna, erta premyeralar va eksklyuziv sifat"
            )
        )
    }

    fun createPayment(planType: String, onInvoiceReady: (payUrl: String, orderId: String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingPayment = true, errorMessage = null)
            val result = paymentRepo.createPayment(planType)
            result.fold(
                onSuccess = { res ->
                    _uiState.value = _uiState.value.copy(
                        isCreatingPayment = false,
                        activeOrderId = res.orderId
                    )
                    onInvoiceReady(res.payUrl, res.orderId)
                },
                onFailure = { err ->
                    _uiState.value = _uiState.value.copy(
                        isCreatingPayment = false,
                        errorMessage = err.message ?: "To'lovni boshlashda xatolik"
                    )
                }
            )
        }
    }

    fun checkPaymentStatus(orderId: String, onFinished: (Boolean) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCheckingPayment = true)
            val result = paymentRepo.getPaymentStatus(orderId)
            result.fold(
                onSuccess = { statusRes ->
                    _uiState.value = _uiState.value.copy(isCheckingPayment = false)
                    if (statusRes.status.equals("success", ignoreCase = true) || statusRes.paidAt != null) {
                        loadProfile(forceRefresh = true)
                        _uiState.value = _uiState.value.copy(
                            paymentSuccessMessage = "To'lov muvaffaqiyatli amalga oshirildi! Premium obunangiz faollashtirildi! 🎉"
                        )
                        onFinished(true)
                    } else {
                        onFinished(false)
                    }
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isCheckingPayment = false)
                    onFinished(false)
                }
            )
        }
    }

    fun simulateTestPayment(orderId: String, onFinished: (Boolean) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCheckingPayment = true)
            val result = paymentRepo.simulateSuccess(orderId)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isCheckingPayment = false)
                    loadProfile(forceRefresh = true)
                    _uiState.value = _uiState.value.copy(
                        paymentSuccessMessage = "Demo to'lov muvaffaqiyatli tasdiqlandi! Premium faol! 🎉"
                    )
                    onFinished(true)
                },
                onFailure = { err ->
                    _uiState.value = _uiState.value.copy(
                        isCheckingPayment = false,
                        errorMessage = err.message
                    )
                    onFinished(false)
                }
            )
        }
    }

    fun clearPaymentSuccessMessage() {
        _uiState.value = _uiState.value.copy(paymentSuccessMessage = null)
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            authRepo.logout()
            onLoggedOut()
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
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = err.message)
                }
            )
        }
    }
}
