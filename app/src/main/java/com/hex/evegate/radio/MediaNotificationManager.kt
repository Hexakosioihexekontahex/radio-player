package com.hex.evegate.radio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.hex.evegate.R
import com.hex.evegate.ui.activity.MainActivity


class MediaNotificationManager(private val service: RadioService) {
    private val PRIMARY_CHANNEL = "PRIMARY_CHANNEL_ID"
    private val PRIMARY_CHANNEL_NAME = "PRIMARY"
    private var strAppName: String
    private var strLiveBroadcast: String
    private val resources: Resources
    private val notificationManager: NotificationManagerCompat
    private var largeIc: Bitmap? = null

    init {
        this.resources = service.resources
        strAppName = resources.getString(R.string.app_name)
        strLiveBroadcast = resources.getString(R.string.live_broadcast)
        notificationManager = NotificationManagerCompat.from(service)
    }

    fun startNotify(playbackStatus: String) {
        val largeIcon = largeIc ?: BitmapFactory.decodeResource(resources, R.drawable.evegate_large)
        var icon = R.drawable.ic_pause_white
        val playbackAction = Intent(service, RadioService::class.java)
        playbackAction.action = RadioService.ACTION_PAUSE
        var action = PendingIntent.getService(service, 1, playbackAction, 0)
        if (playbackStatus == PlaybackStatus.PAUSED) {
            icon = R.drawable.ic_play_white
            playbackAction.action = RadioService.ACTION_PLAY
            action = PendingIntent.getService(service, 2, playbackAction, 0)

        }

        val stopIntent = Intent(service, RadioService::class.java)
        stopIntent.action = RadioService.ACTION_STOP
        val stopAction = PendingIntent.getService(service, 3, stopIntent, 0)

        val intent = Intent(service, MainActivity::class.java)
        intent.action = Intent.ACTION_MAIN
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val pendingIntent = PendingIntent.getActivity(service, 0, intent, 0)

        notificationManager.cancel(NOTIFICATION_ID)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(PRIMARY_CHANNEL, PRIMARY_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            manager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(service, PRIMARY_CHANNEL)
                .setAutoCancel(false)
                .setLargeIcon(largeIcon)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.mipmap.ic_evegate_foreground)
                .addAction(icon, "pause", action)
                .addAction(R.drawable.ic_stop_white, "stop", stopAction)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setWhen(System.currentTimeMillis())
                .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1)
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(stopAction)
                )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setContentTitle("$strAppName - $strLiveBroadcast")
        } else {
            builder.setContentTitle(strLiveBroadcast)
                    .setContentText(strAppName)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(NotificationCompat.CATEGORY_STATUS)
                    .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                    .priority = NotificationCompat.PRIORITY_MIN
        }

        service.startForeground(NOTIFICATION_ID, builder.build())
    }

    fun cancelNotify() {
        service.stopForeground(true)
    }

    fun onTrackUpdated(song: String, artist: String, playbackStatus: String) {
        strLiveBroadcast = song
        strAppName = artist
        startNotify(playbackStatus)
    }

    fun onIconsLoaded(icon: Bitmap, playbackStatus: String) {
        largeIc = icon
        startNotify(playbackStatus)
    }

    companion object {
        val NOTIFICATION_ID = 555
    }

}
