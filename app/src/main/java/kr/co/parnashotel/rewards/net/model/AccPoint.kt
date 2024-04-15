package kr.co.parnashotel.rewards.net.model


import com.google.gson.annotations.SerializedName

data class AccPoint(
    @SerializedName("month")
    val month: String,
    @SerializedName("point")
    val point: Int
)