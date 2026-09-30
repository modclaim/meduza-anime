package uz.meduza.anime.ui.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.http.*
import uz.meduza.anime.core.network.ApiClient
import uz.meduza.anime.data.models.ApiResponse
import uz.meduza.anime.data.models.UserDto

data class AdminStatsDto(
    @SerializedName("totalUsers") val totalUsers: Int = 0,
    @SerializedName("premiumUsers") val premiumUsers: Int = 0,
    @SerializedName("totalAnimes") val totalAnimes: Int = 0,
    @SerializedName("totalEpisodes") val totalEpisodes: Int = 0,
    @SerializedName("totalViews") val totalViews: Int = 0,
    @SerializedName("totalComments") val totalComments: Int = 0
)

data class MalSearchResultDto(
    @SerializedName("malId") val malId: Int,
    @SerializedName("title") val title: String,
    @SerializedName("posterUrl") val posterUrl: String?,
    @SerializedName("episodesCount") val episodes: Int?,
    @SerializedName("rating") val score: Double?
)

data class GrantPremiumRequest(
    @SerializedName("isPremium") val isPremium: Boolean = true,
    @SerializedName("days") val days: Int = 30
)

interface AdminApiService {
    @GET("admin/stats")
    suspend fun getStats(): Response<ApiResponse<AdminStatsDto>>

    @GET("admin/animes/fetch-mal")
    suspend fun searchMal(@Query("q") query: String): Response<ApiResponse<List<MalSearchResultDto>>>

    @POST("admin/users/{userId}/premium")
    suspend fun grantPremium(
        @Path("userId") userId: String,
        @Body request: GrantPremiumRequest
    ): Response<ApiResponse<UserDto>>
}

data class AdminUiState(
    val isLoading: Boolean = true,
    val stats: AdminStatsDto? = null,
    val malResults: List<MalSearchResultDto> = emptyList(),
    val isSearchingMal: Boolean = false,
    val message: String? = null
)

class AdminViewModel(application: Application) : AndroidViewModel(application) {
    private val adminApi = ApiClient.getService(application) as? AdminApiService
        ?: retrofit2.Retrofit.Builder()
            .baseUrl(uz.meduza.anime.core.constants.AppConstants.BASE_URL)
            .client(
                okhttp3.OkHttpClient.Builder()
                    .addInterceptor(uz.meduza.anime.core.network.AuthInterceptor(uz.meduza.anime.core.session.SessionManager.getInstance(application)))
                    .build()
            )
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(AdminApiService::class.java)

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val res = adminApi.getStats()
                if (res.isSuccessful && res.body()?.data != null) {
                    _uiState.value = _uiState.value.copy(isLoading = false, stats = res.body()!!.data)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun searchMal(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearchingMal = true)
            try {
                val res = adminApi.searchMal(query)
                _uiState.value = _uiState.value.copy(
                    isSearchingMal = false,
                    malResults = res.body()?.data ?: emptyList()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSearchingMal = false)
            }
        }
    }

    fun grantPremium(userId: String, days: Int = 30) {
        viewModelScope.launch {
            try {
                val res = adminApi.grantPremium(userId, GrantPremiumRequest(isPremium = true, days = days))
                if (res.isSuccessful) {
                    _uiState.value = _uiState.value.copy(message = "Foydalanuvchiga $days kunlik VIP Premium berildi!")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(message = e.message)
            }
        }
    }
}
