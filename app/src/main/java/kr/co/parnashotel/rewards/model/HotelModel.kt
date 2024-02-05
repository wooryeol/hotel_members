package kr.co.parnashotel.rewards.model

data class HotelModel(
    var img: Int,
    var thumbUrl: String,
    var title: String,
    var url: String,
    val data: List<UserInfoModel>? = null
)