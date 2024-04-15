package kr.co.parnashotel.rewards.net.model


import com.google.gson.annotations.SerializedName

data class GradeInfo(
    @SerializedName("coupon")
    val coupon: Int,
    @SerializedName("gradeName")
    val gradeName: String,
    @SerializedName("membershipNo")
    val membershipNo: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("point")
    val point: Int
)