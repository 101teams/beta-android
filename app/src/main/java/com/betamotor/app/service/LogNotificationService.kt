package com.betamotor.app.service
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.betamotor.app.LogActivity
import com.betamotor.app.R

class LogNotificationService : Service() {

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "log_channel"
            val channelName = "Log Notifications"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(channelId, channelName, importance)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createLogIntent(): PendingIntent {
        val intent = Intent(this, LogActivity::class.java)
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, "log_channel")
            .setContentTitle("Log Service")
            .setContentText("Click to see logs")
            .setSmallIcon(R.drawable.img_betamotor)
            .setContentIntent(createLogIntent())
            .setOngoing(true)
            .build()

        startForeground(1, notification)

        return START_STICKY
    }
}

