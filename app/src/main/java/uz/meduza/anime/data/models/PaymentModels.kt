package uz.meduza.anime.data.models

import com.google.gson.annotations.SerializedName

data class PaymentPlanDto(
    @SerializedName("planType") val planType: String,
    @SerializedName("title") val title: String,
    @SerializedName("days") val days: Int,
    @SerializedName("price") val price: Long,
    @SerializedName("currency") val currency: String = "UZS",
    @SerializedName("discount") val discount: String? = null,
    @SerializedName("description") val description: String = ""
)

data class CreatePaymentRequest(
    @SerializedName("planType") val planType: String
)

data class CreatePaymentResponse(
    @SerializedName("id") val id: String,
    @SerializedName("orderId") val orderId: String,
    @SerializedName("amount") val amount: Long,
    @SerializedName("currency") val currency: String,
    @SerializedName("planType") val planType: String,
    @SerializedName("planTitle") val planTitle: String,
    @SerializedName("days") val days: Int,
    @SerializedName("description") val description: String,
    @SerializedName("payUrl") val payUrl: String,
    @SerializedName("isInpayLive") val isInpayLive: Boolean
)

data class PaymentStatusResponse(
    @SerializedName("orderId") val orderId: String,
    @SerializedName("amount") val amount: Long,
    @SerializedName("currency") val currency: String,
    @SerializedName("status") val status: String,
    @SerializedName("planType") val planType: String,
    @SerializedName("days") val days: Int,
    @SerializedName("description") val description: String,
    @SerializedName("payUrl") val payUrl: String?,
    @SerializedName("username") val username: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("paidAt") val paidAt: String?,
    @SerializedName("createdAt") val createdAt: String?
)
