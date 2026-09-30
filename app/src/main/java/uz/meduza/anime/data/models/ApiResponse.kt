package uz.meduza.anime.data.models

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: T? = null,
    @SerializedName("meta") val meta: MetaDto? = null,
    @SerializedName("error") val error: Any? = null
)

data class MetaDto(
    @SerializedName("page") val page: Int? = 1,
    @SerializedName("limit") val limit: Int? = 20,
    @SerializedName("total") val total: Int? = 0,
    @SerializedName("totalPages") val totalPages: Int? = 1,
    @SerializedName("hasNextPage") val hasNextPage: Boolean? = false,
    @SerializedName("hasPrevPage") val hasPrevPage: Boolean? = false
)
