package com.example.photogallery.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class SystemBootReceiver : BroadcastReceiver() {

    companion object {
        private const val CHANNEL_ID = "gallery_updates_channel"
        private const val NOTIFICATION_ID = 100
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            createNotificationChannel(context)
            showUpdateNotification(context)
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Оновлення галереї",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Сповіщення про нові фотографії"
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun showUpdateNotification(context: Context) {
        val checkIntent = Intent(context, CheckUpdateService::class.java).apply {
            action = CheckUpdateService.ACTION_CHECK_UPDATES
            putExtra(CheckUpdateService.EXTRA_FROM_NOTIFICATION, true)
        }
        val checkPending = PendingIntent.getService(
            context, 0, checkIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val cancelIntent = Intent(context, CheckUpdateService::class.java).apply {
            action = CheckUpdateService.ACTION_CANCEL
        }
        val cancelPending = PendingIntent.getService(
            context, 1, cancelIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_gallery)
            .setContentTitle("Перевірити оновлення галереї?")
            .setContentText("Натисніть 'Так' для перевірки нових фотографій")
            .setTicker("Нові фото можуть бути доступні")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(android.R.drawable.ic_delete, "Ні", cancelPending)
            .addAction(android.R.drawable.ic_input_add, "Так", checkPending)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            android.util.Log.w("SystemBootReceiver", "Notification permission denied")
        }
    }
}