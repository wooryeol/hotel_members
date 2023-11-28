package kr.co.parnas.common

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences

object SharedData {
    const val SHARED_NAME = "SE"

    //deviceId
    const val DEVICE_ID = "device_id"
    //푸시 토큰
    const val PUSH_TOKEN = "push_token"
    //노티ID
    const val NOTI_ID = "noti_id"

    fun setSharedData(context: Context, strKey: String, objData: Any): Boolean {
        val prefs = context.getSharedPreferences(SHARED_NAME, Activity.MODE_PRIVATE)
        if (prefs == null || strKey == null || objData == null) {
            return false
        }
        val ed = prefs.edit()

        when {
            Boolean::class.javaObjectType == objData.javaClass -> {
                ed.putBoolean(strKey, objData as Boolean)
            }
            Int::class.javaObjectType == objData.javaClass -> {
                ed.putInt(strKey, objData as Int)
            }
            Long::class.javaObjectType == objData.javaClass -> {
                ed.putLong(strKey, objData as Long)
            }
            Float::class.javaObjectType == objData.javaClass -> {
                ed.putFloat(strKey, objData as Float)
            }
            String::class.javaObjectType == objData.javaClass -> {
                ed.putString(strKey, objData as String)
            }
            else -> {
                Utils.Log("저장 실패!")
                return false
            }
        }
        return ed.commit()
    }

    fun getSharedData(context: Context, strKey: String, objData: Boolean): Boolean {
        val prefs = context.getSharedPreferences(SHARED_NAME, Activity.MODE_PRIVATE)
        return prefs.getBoolean(strKey, objData)
    }

    fun getSharedData(context: Context, strKey: String, objData: String): String {
        val prefs = context.getSharedPreferences(SHARED_NAME, Activity.MODE_PRIVATE)
        return prefs.getString(strKey, objData) ?: ""
    }

    fun getSharedData(context: Context, strKey: String, objData: Int): Int {
        val prefs = context.getSharedPreferences(SHARED_NAME, Activity.MODE_PRIVATE)
        return prefs.getInt(strKey, objData)
    }

    fun getSharedData(context: Context, strKey: String, objData: Long): Long {
        val prefs = context.getSharedPreferences(SHARED_NAME, Activity.MODE_PRIVATE)
        return prefs.getLong(strKey, objData)
    }

    fun getSharedData(context: Context, strKey: String, objData: Float): Float {
        val prefs = context.getSharedPreferences(SHARED_NAME, Activity.MODE_PRIVATE)
        return prefs.getFloat(strKey, objData)
    }

    /**
     * Double은 따로 만들어 사용해야함
     * @param context
     * @param strKey
     * @param objData
     * @return
     */
    fun setSharedDataDouble(context: Context, strKey: String, objData: Double): Boolean {
        val prefs = context.getSharedPreferences(SHARED_NAME, Activity.MODE_PRIVATE)
        if (prefs == null || strKey == null || objData == null) {
            return false
        }
        val ed = prefs.edit()
        putDouble(ed, strKey, objData)
        return ed.commit()
    }

    /**
     * Double은 따로 만들어 사용해야함
     * @param context
     * @param strKey
     * @param objData
     * @return
     */
    fun getSharedDataDouble(context: Context, strKey: String, objData: Double): Double? {
        val prefs = context.getSharedPreferences(SHARED_NAME, Activity.MODE_PRIVATE)
        return if (prefs == null || strKey == null) {
            null
        } else getDouble(prefs, strKey, objData)
    }

    private fun putDouble(
        edit: SharedPreferences.Editor,
        key: String,
        value: Double
    ): SharedPreferences.Editor? {
        return edit.putLong(key, java.lang.Double.doubleToRawLongBits(value))
    }

    private fun getDouble(prefs: SharedPreferences, key: String, defaultValue: Double): Double {
        return java.lang.Double.longBitsToDouble(
            prefs.getLong(
                key,
                java.lang.Double.doubleToLongBits(defaultValue)
            )
        )
    }
}