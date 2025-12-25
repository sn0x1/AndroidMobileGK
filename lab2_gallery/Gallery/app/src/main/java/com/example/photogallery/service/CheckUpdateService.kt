package com.example.photogallery.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.example.photogallery.core.GalleryApp
import com.example.photogallery.utils.UIThreadHelper

class CheckUpdateService : Service() {

    companion object {
        const val ACTION_CHECK_UPDATES = "com.example.photogallery.CHECK_UPDATES"
        const val ACTION_CANCEL = "com.example.photogallery.CANCEL"
        const val EXTRA_FROM_NOTIFICATION = "from_notification"
        private const val TAG = "CheckUpdateService"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CHECK_UPDATES -> {
                val fromNotification = intent.getBooleanExtra(EXTRA_FROM_NOTIFICATION, false)
                checkForUpdates(fromNotification)
            }
            ACTION_CANCEL -> {
                Toast.makeText(this, "Скасовано", Toast.LENGTH_SHORT).show()
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun checkForUpdates(fromNotification: Boolean) {
        Thread {
            try {
                val app = application as GalleryApp

                if (app.photoRepository.hasUpdates()) {
                    UIThreadHelper.post {
                        app.progressTracker.setHasUpdates(true)

                        if (fromNotification) {
                            Toast.makeText(
                                this@CheckUpdateService,
                                "Доступні нові фотографії!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                Log.d(TAG, "Update check completed")
            } catch (e: Exception) {
                Log.e(TAG, "Check failed", e)
            } finally {
                stopSelf()
            }
        }.start()
    }
}