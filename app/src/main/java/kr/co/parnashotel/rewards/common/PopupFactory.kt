package kr.co.parnashotel.rewards.common

import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import kr.co.parnashotel.R

object PopupFactory {
    /**
     * 버튼 하나짜리 팝업
     * @param context
     * @param msg : 메세지
     * @param ok : 확인버튼
     * @param handler : 버튼 클릭 이벤트를 받을 핸들러
     */
    fun drawSystemPopup(context: Context, msg: String?, ok: String?, handler: Handler) {
        try {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(context.getString(R.string.app_name))
            builder.setMessage(msg)
            builder.setCancelable(false)
            builder.setPositiveButton(
                ok
            ) { dialog, which -> // TODO Auto-generated method stub
                val msg = handler.obtainMessage()
                msg.what = kr.co.parnashotel.rewards.common.Define.EVENT_OK
                handler.sendMessage(msg)
            }
            builder.show()
        } catch (e: Exception) {
            kr.co.parnashotel.rewards.common.Utils.LogLine(e.message.toString())
        }
    }

    /**
     * 버튼 두개짜리 팝업
     * @param context
     * @param msg : 메세지
     * @param ok : 확인버튼
     * @param cancel : 취소버튼
     * @param handler : 버튼 클릭 이벤트를 받을 핸들러
     */
    fun drawSystemPopup(
        context: Context,
        msg: String?,
        ok: String?,
        cancel: String?,
        handler: Handler
    ) {
        try {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(context.getString(R.string.app_name))
            builder.setMessage(msg)
            builder.setCancelable(false)
            builder.setPositiveButton(
                ok
            ) { dialog, which ->
                // TODO Auto-generated method stub
                val msg = handler.obtainMessage()
                msg.what = kr.co.parnashotel.rewards.common.Define.EVENT_OK
                handler.sendMessage(msg)
            }.setNegativeButton(cancel) { dialog, which ->
                // TODO Auto-generated method stub
                val msg = handler.obtainMessage()
                msg.what = kr.co.parnashotel.rewards.common.Define.EVENT_CANCEL
                handler.sendMessage(msg)
            }
            builder.show()
        } catch (e: Exception) {
            kr.co.parnashotel.rewards.common.Utils.LogLine(e.message.toString())
        }
    }
}