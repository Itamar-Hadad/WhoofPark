package com.example.whoofpark.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.whoofpark.MainActivity

class CheckOutWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val dogName = inputData.getString("dogName") ?: "Punch"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "whoof_park_notifications"
        //define vibration pattern (half second of silence, half second of vibration)
        val pattern = longArrayOf(0, 500, 500, 500)


        //notification in android 8.0 and more
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "WhoofPark", NotificationManager.IMPORTANCE_HIGH).apply {
                enableVibration(true) //vibrate for
                vibrationPattern = pattern
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("TIMER_EXPIRED", true) // Flag to indicate timer has expired
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        //notification in other versions
        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Time's Up! 🐾")
            .setContentText("$dogName's time at the park is up. Are you staying?")
            .setSmallIcon(android.R.drawable.ic_dialog_info) //I need to change the pic
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent) // הופך את ההתראה ללחיצה!
            .setAutoCancel(true) // ההתראה תיעלם מהרשימה אחרי הלחיצה
        // for android less than 8.0 we need to add vibration manually
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setVibrate(pattern)
        }
        notificationManager.notify(1, builder.build())
        return Result.success()



    }
}