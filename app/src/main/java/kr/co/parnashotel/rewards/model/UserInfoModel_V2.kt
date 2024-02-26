package kr.co.parnashotel.rewards.model

import android.content.Context
import com.google.gson.Gson
import java.io.Serializable

data class UserInfoModel_V2(
    var name: String? = "",
    var gradeName: String? = "",
    var membershipNo: String? = "",
    var point: Int? = 0,
    var coupon: Int? = 0,
    var percent: Int? = 0,
    var accessToken: String? = ""
):Serializable {
    fun save(mContext: Context) {
        val sharedPreferences = mContext.getSharedPreferences("UserInfoPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(this) // 현재 UserInfoModel_V2 객체를 JSON으로 변환
        editor.putString("UserInfo", json)
        editor.apply()
    }

    // SharedPreferences에서 UserInfoModel_V2를 불러오는 함수
    fun loadUserInfo(mContext: Context): UserInfoModel_V2? {
        val sharedPreferences = mContext.getSharedPreferences("UserInfoPrefs", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("UserInfo", null) ?: return null
        return Gson().fromJson(json, UserInfoModel_V2::class.java)
    }

    // SharedPreferences에서 UserInfoModel_V2를 삭제하는 함수
    fun clearUserInfo(mContext: Context) {
        val sharedPreferences = mContext.getSharedPreferences("UserInfoPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().remove("UserInfo").apply()
    }
}
