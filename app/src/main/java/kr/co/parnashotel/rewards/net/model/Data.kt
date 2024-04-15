package kr.co.parnashotel.rewards.net.model


import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("accPoint")
    val accPoint: List<AccPoint>,
    @SerializedName("gradeInfo")
    val gradeInfo: GradeInfo,
    @SerializedName("usePoint")
    val usePoint: List<UsePoint>
)