package kr.co.parnashotel.rewards.model

import java.io.Serializable

data class UserInfoModel(
    var name: String,
    var membershipNo: String,
    var point: Int,
    var gradeName: String,
    var accessToken: String
):Serializable