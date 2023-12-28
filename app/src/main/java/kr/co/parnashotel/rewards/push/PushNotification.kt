package kr.co.parnashotel.rewards.push

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import kr.co.parnashotel.R
import kr.co.parnashotel.rewards.common.SharedData
import kr.co.parnashotel.rewards.common.Utils


class PushNotification {
    fun generateNotification(context: Context, data: Map<String?, String?>) {
        val push = SharedData.getSharedData(context, SharedData.PUSH_SERVICE, "")
        if (push == "N") return
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val title = data["title"]
        val message = data["message"]
        val index = data["Em_Index"]
        Utils.Log("data.get(index) ==> : $index")
        var notiId = SharedData.getSharedData(context, SharedData.NOTI_ID, 0) as Int
        notiId++
        if (notiId > 99) notiId = 0
        SharedData.setSharedData(context, SharedData.NOTI_ID, notiId)
        val intent = Intent(context,  kr.co.parnashotel.rewards.menu.home.MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.putExtra("index", index)
        val pendingIntent = PendingIntent.getActivity(
            context,
            notiId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val channel = context.getString(R.string.channel_id)

        val builder = NotificationCompat.Builder(context, channel)
        builder
            /*.setSmallIcon(R.drawable.ic_launcher)*/
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        //기본벨
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        builder.setSound(uri)
        //진동
        builder.setDefaults(Notification.DEFAULT_VIBRATE)
        val notification = builder.build()
        notificationManager.notify(notiId, notification)
    }
}