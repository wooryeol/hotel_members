package kr.co.parnas.network.model

data class HotelModel(
    var img: Int,
    var thumbUrl: String,
    var title: String,
    var url: String,
    val data: List<TierModel>? = null
)