package uz.meduza.anime.data.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uz.meduza.anime.core.network.ApiClient
import uz.meduza.anime.core.network.MeduzaApiService
import uz.meduza.anime.data.models.CreatePaymentRequest
import uz.meduza.anime.data.models.CreatePaymentResponse
import uz.meduza.anime.data.models.PaymentPlanDto
import uz.meduza.anime.data.models.PaymentStatusResponse

class PaymentRepository(context: Context) {
    private val api: MeduzaApiService = ApiClient.getService(context)

    suspend fun getPlans(): Result<List<PaymentPlanDto>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = api.getPaymentPlans()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.message ?: "Tariflarni yuklab bo'lmadi"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createPayment(planType: String): Result<CreatePaymentResponse> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = api.createPayment(CreatePaymentRequest(planType))
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "To'lov chekini yaratib bo'lmadi"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPaymentStatus(orderId: String): Result<PaymentStatusResponse> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = api.getPaymentStatus(orderId)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "To'lov holatini tekshirib bo'lmadi"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun simulateSuccess(orderId: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = api.simulatePaymentSuccess(orderId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Test to'lovini tasdiqlab bo'lmadi"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
