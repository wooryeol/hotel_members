package kr.co.parnashotel.rewards.common

import android.app.Application
import android.util.Log
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.user.model.User
import kr.co.parnashotel.R
import kr.co.parnashotel.rewards.model.MembershipUserInfoModel
import kr.co.parnashotel.rewards.model.UserInfoModel
import kr.co.parnashotel.rewards.model.UserKakaoMembershipInfoModel

class GlobalApplication:Application() {

    companion object{
        var userInfo: UserInfoModel? = null
        var membershipUserInfo: MembershipUserInfoModel? = null
        var userKakaoMembershipInfo: UserKakaoMembershipInfoModel? = null

        var sharedAccessToken = ""
        var sharedMembershipUserInfo = ""
        var sharedMembershipNo = ""

        var isLoggedIn = false
    }

    override fun onCreate() {
        super.onCreate()

        sharedAccessToken = SharedData.getSharedData(this, "accessToken", "")
        sharedMembershipUserInfo = SharedData.getSharedData(this, "membershipUserInfo", "")
        sharedMembershipNo = SharedData.getSharedData(this, "membershipNo", "")

        Log.d("wooryeol", "GlobalApplication accessToken >>> $sharedAccessToken")
        Log.d("wooryeol", "GlobalApplication membershipUserInfo >>> $sharedMembershipUserInfo")
        Log.d("wooryeol", "GlobalApplication membershipNo >>> $sharedMembershipNo")
        Log.d("wooryeol", "GlobalApplication isLoggedIn >>> $isLoggedIn")

        if (sharedAccessToken != "" && sharedMembershipUserInfo != "" && sharedMembershipNo != "") {
            isLoggedIn = true
            Log.d("wooryeol", "GlobalApplication isLoggedIn >>> $isLoggedIn")
        }

        //카카오 로그인
        KakaoSdk.init(this, getString(R.string.kakao_app_key))
    }

}