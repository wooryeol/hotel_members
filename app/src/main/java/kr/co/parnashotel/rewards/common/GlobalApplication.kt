package kr.co.parnashotel.rewards.common

import android.annotation.SuppressLint
import android.app.Application
import com.google.gson.JsonObject
import com.kakao.sdk.common.KakaoSdk
import kr.co.parnashotel.R
import kr.co.parnashotel.rewards.menu.home.MainActivity
import kr.co.parnashotel.rewards.model.MembershipUserInfo
import kr.co.parnashotel.rewards.model.TierModel
import org.json.JSONObject

class GlobalApplication:Application() {

    companion object{
        var userInfo: TierModel? = null
        var membershipUserInfo: MembershipUserInfo? = null
    }

    override fun onCreate() {
        super.onCreate()

        //카카오 로그인
        KakaoSdk.init(this, getString(R.string.kakao_app_key))
    }
}