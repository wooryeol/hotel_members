package kr.co.parnashotel.rewards.common

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.user.model.User
import kr.co.parnashotel.R
import kr.co.parnashotel.rewards.model.MembershipUserInfoModel
import kr.co.parnashotel.rewards.model.UserInfoModel
import kr.co.parnashotel.rewards.model.UserInfoModel_V2
import kr.co.parnashotel.rewards.model.UserKakaoMembershipInfoModel

class GlobalApplication:Application() {

    companion object{
        var userInfo: UserInfoModel_V2? = null
        var membershipUserInfo: MembershipUserInfoModel? = null
        var userKakaoMembershipInfo: UserKakaoMembershipInfoModel? = null

        var sharedAccessToken = ""
        var sharedMembershipUserInfo = ""
        var sharedMembershipNo = ""

        var isLoggedIn = false
    }

    override fun onCreate() {
        super.onCreate()

        /*sharedAccessToken = SharedData.getSharedData(this, "accessToken", "")
        sharedMembershipUserInfo = SharedData.getSharedData(this, "membershipUserInfo", "")
        sharedMembershipNo = SharedData.getSharedData(this, "membershipNo", "")

        Log.d("wooryeol", "GlobalApplication accessToken >>> $sharedAccessToken")
        Log.d("wooryeol", "GlobalApplication membershipUserInfo >>> $sharedMembershipUserInfo")
        Log.d("wooryeol", "GlobalApplication membershipNo >>> $sharedMembershipNo")
        Log.d("wooryeol", "GlobalApplication isLoggedIn >>> $isLoggedIn")*/

        val userInfoModel = UserInfoModel_V2()
        Log.d("wooryeol", "userInfoModel.loadUserInfo(this) >>> ${userInfoModel.loadUserInfo(this)}")
        if(userInfoModel.loadUserInfo(this) != null) {
            isLoggedIn = true
        }

        //카카오 로그인
        KakaoSdk.init(this, getString(R.string.kakao_app_key))
    }

    fun save(mContext: Context, name: String) {
        val sharedPreferences = mContext.getSharedPreferences(name, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(this) // 현재 UserInfoModel_V2 객체를 JSON으로 변환
        editor.putString("UserInfo", json)
        editor.apply()
    }
}