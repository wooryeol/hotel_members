package kr.co.parnashotel.rewards.model

import android.content.Context
import com.google.gson.Gson
import java.io.Serializable

data class MembershipUserInfoModel_V2 (
    var membershipYn: String? = null,
    var membershipId: String? = null,
    var membershipNo: String? = null,
    var memberName: String? = null,
    var memberGender: String? = null,
    var memberEmail: String? = null,
    var memberMobile: String? = null,
    var memberFirstName: String? = null,
    var memberLastName: String? = null,
    var employeeStatus: String? = null,
    var recommenderStatus: String? = null,
    var temporaryYn: String? = null
): Serializable {
    fun save(context: Context) {
        val sharedPreferences = context.getSharedPreferences("MembershipUserInfoPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(this) // 현재 UserInfoModel_V2 객체를 JSON으로 변환
        editor.putString("MembershipUserInfo", json)
        editor.apply()
    }

    // SharedPreferences에서 UserInfoModel_V2를 불러오는 함수
    fun LoadMembershipUserInfo(mContext: Context): MembershipUserInfoModel_V2? {
        val sharedPreferences = mContext.getSharedPreferences("MembershipUserInfoPrefs", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("MembershipUserInfo", null) ?: return null
        return Gson().fromJson(json, MembershipUserInfoModel_V2::class.java)
    }

    // SharedPreferences에서 UserInfoModel_V2를 삭제하는 함수
    fun clearMembershipUserInfo(mContext: Context) {
        val sharedPreferences = mContext.getSharedPreferences("MembershipUserInfoPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().remove("MembershipUserInfo").apply()
    }
}