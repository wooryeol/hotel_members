package kr.co.parnashotel.rewards.common

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import kr.co.parnashotel.R

class GlobalApplication:Application() {
    override fun onCreate() {
        super.onCreate()

        //카카오 로그인
        KakaoSdk.init(this, getString(R.string.kakao_app_key))
    }
}