package kr.co.parnashotel.rewards.net.model


import com.google.gson.annotations.SerializedName

data class DashboardInfoModel(
    @SerializedName("code")
    val code: String,
    @SerializedName("data")
    val data: Data,
    @SerializedName("message")
    val message: String
)