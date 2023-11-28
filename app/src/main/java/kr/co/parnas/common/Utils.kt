package kr.co.parnas.common

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.TypedValue
import kr.co.parnas.BuildConfig
import java.io.UnsupportedEncodingException
import java.util.*

object Utils {
    fun Log(msg: String) {
        if (BuildConfig.DEBUG) android.util.Log.d("SE", msg)
    }

    fun LogLine(msg: String) {
        if (BuildConfig.DEBUG) android.util.Log.d(tag(), msg)
    }

    /**
     * 현재 페이지명을 테그로 가져와 로그를 클릭했을때 해당 소스로 이동한다.
     */
    private fun tag(): String {
        val level = 4
        val trace = Thread.currentThread().stackTrace[level]
        val fileName = trace.fileName
        val classPath = trace.className
        val className = classPath.substring(classPath.lastIndexOf(".") + 1)
        val methodName = trace.methodName
        val lineNumber = trace.lineNumber
        return "CRM# $className.$methodName($fileName:$lineNumber)"
    }

    /**
     * 토스트 함수
     */
    fun Toast(context: Context, msg: String) {
        android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
    }

    /**
     * 버전에 따른 getColor를 버전에 맞게 return
     * @param context : context
     * @param id : color
     * @return : 버전에 맞는 color
     */
    fun getColor(context: Context, id: Int): Int {
        val version = Build.VERSION.SDK_INT
        return if (version >= Build.VERSION_CODES.M) {
            context.getColor(id)
        } else {
            context.resources.getColor(id)
        }
    }

    /**
     * dp를 pixel로 변환한다.
     * @param context: context
     * @param pd : 변환할 dp
     * @return 변환된 pixel
     */
    fun getDpToPixel(context: Context, dp: Float): Int{
        var px = 0.0f
        try {
            px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
        } catch (e: Exception) {
            Log("e ==> : " + e.message)
            LogLine(e.message.toString())
        }

        return px.toInt()
    }

    /**
     * android id(device id) 가져오기
     * @param context
     * @return andorid id
     */
    fun getDeviceId(context: Context): String {
        //저장된 deviceId가 있는 경우 저장된 값 리턴
        val device_id = SharedData.getSharedData(context, SharedData.DEVICE_ID, "")
        if (!device_id.isEmpty()) return device_id
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        //일부 기종에서 9774d56d682e549c만 반환하는 경우가 있어서 예외처리
        return if (androidId.isEmpty() || androidId == "9774d56d682e549c") {
            val random = UUID.randomUUID().toString()
            SharedData.setSharedData(context, SharedData.DEVICE_ID, random)
            random
        } else {
            try {
                //uuid 형식으로 변경(ab84df38-949b-305d-a6d7-65def76e0c4f)
                val uuid = UUID.nameUUIDFromBytes(androidId.toByteArray(charset("UTF8")))
                SharedData.setSharedData(context, SharedData.DEVICE_ID, uuid.toString())
                uuid.toString()
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
                val random = UUID.randomUUID().toString()
                SharedData.setSharedData(context, SharedData.DEVICE_ID, random)
                random
            }
        }
    }

    /**
     * 앱 버전 가져오기
     * @param context
     * @return 앱버전
     */
    fun appVersionName(context: Context): String? {
        var version = "0"
        try {
            val i = context.packageManager.getPackageInfo(context.packageName, 0)
            version = i.versionName
        } catch (e: PackageManager.NameNotFoundException) {
        }
        return version
    }

    /**
     * 앱 버전 코드 가져오기
     * @param context
     * @return 앱버전 코드
     */
    fun appVersionCode(context: Context): Long {
        var version = 0L
        try {
            val i = context.packageManager.getPackageInfo(context.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                version = i.longVersionCode
            }else{
                version = i.versionCode.toLong()
            }
        } catch (e: PackageManager.NameNotFoundException) {
        }
        return version
    }

    /**
     * 단말기 모델 가져오기
     * @param context
     * @return 단말기 모델
     */
    fun appDeviceName(): String? {
        return Build.MODEL
    }

    /**
     * 디자인 시 참고할 화면의 크기를 알아낸다.
     * dp의 계산법은 px/density
     * 대상 단말기의 dpi에 따라 디자인 작업을 진행한다.
     * ldpi : 120dpi
     * mdpi : 160dpi (기본)
     * hdpi : 240dpi
     * xhdpi : 320dpi
     * xxhdpi : 480dpi
     * xxxhdpi : 640dpi
     */
    fun getDisplayInfo(context: Context){
        val metrics = context.resources.displayMetrics
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels

        val dpi = metrics.densityDpi
        val density = metrics.density

        var device = ""
        when(dpi) {
            120 -> device = "ldpi"
            160 -> device = "mdpi"
            240 -> device = "hdpi"
            320 -> device = "xhdpi"
            480 -> device = "xxhdpi"
            640 -> device = "xxxhdpi"
        }

        Log("width px ==> : $screenWidth")
        Log("height px ==> : $screenHeight")
        Log("dpi ==> : $dpi")
        Log("density ==> : $density")
        Log("target ==> : $device")
    }
}