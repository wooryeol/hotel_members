package kr.co.parnashotel.rewards.common

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
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

        // 유저 정보 저장
        /*if(userInfo != null || membershipUserInfo != null) {
            val savedUserInfo = SharedData.getSharedData(this, "userInfo", "")

            val userInfoJson = JSONObject(savedUserInfo)
            val name = userInfoJson.get("name").toString()
            val gradeName = userInfoJson.get("gradeName").toString()
            val membershipNo = userInfoJson.get("membershipNo").toString()
            val point = userInfoJson.get("point").toString().toInt()
            val accessToken = userInfoJson.get("accessToken").toString()

            userInfo = TierModel(name, membershipNo, point, gradeName, accessToken)

            // 멤버십 정보 저장
            val savedMembershipUserInfo = SharedData.getSharedData(this, "membershipUserInfo", "")

            val membershipUserInfoJson = JSONObject(savedMembershipUserInfo)
            val membershipYn = membershipUserInfoJson.get("membershipYn").toString()
            val membershipId = membershipUserInfoJson.get("membershipId").toString()
            val memberName = membershipUserInfoJson.get("memberName").toString()
            val memberGender = membershipUserInfoJson.get("memberGender").toString()
            val memberEmail = membershipUserInfoJson.get("memberEmail").toString()
            val memberMobile = membershipUserInfoJson.get("memberMobile").toString()
            val memberFirstName = membershipUserInfoJson.get("memberFirstName").toString()
            val memberLastName = membershipUserInfoJson.get("memberLastName").toString()
            val employeeStatus = membershipUserInfoJson.get("employeeStatus").toString()
            val recommenderStatus = membershipUserInfoJson.get("recommenderStatus").toString()
            val temporaryYn = membershipUserInfoJson.get("temporaryYn").toString()

            membershipUserInfo = MembershipUserInfo(
                membershipYn,
                membershipId,
                membershipNo,
                memberName,
                memberGender,
                memberEmail,
                memberMobile,
                memberFirstName,
                memberLastName,
                employeeStatus,
                recommenderStatus,
                temporaryYn
            )
        }*/

        if(userInfo != null) {
            val savedUserInfo = SharedData.getSharedData(this, "userInfo", "")

            val userInfoJson = JSONObject(savedUserInfo)
            val name = userInfoJson.get("name").toString()
            val gradeName = userInfoJson.get("gradeName").toString()
            val membershipNo = userInfoJson.get("membershipNo").toString()
            val point = userInfoJson.get("point").toString().toInt()
            val accessToken = userInfoJson.get("accessToken").toString()

            userInfo = TierModel(name, membershipNo, point, gradeName, accessToken)
        }

        //카카오 로그인
        KakaoSdk.init(this, getString(R.string.kakao_app_key))
    }

}