package kr.co.parnashotel.rewards.model

import java.io.Serializable

data class TierModel(
    var name: String? = null,
    var membershipNo: String? = null,
    var point: Int? = null,
    var gradeName: String? = null,
    var accessToken: String? = null
):Serializable