package com.example.photogallery.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.photogallery.R
import com.example.photogallery.core.getProgressTracker
import com.example.photogallery.service.DownloadService
import com.example.photogallery.utils.SyncStateListener

class MainActivity : AppCompatActivity(R.layout.activity_main), SyncStateListener {

    private lateinit var statusContainer: View
    private lateinit var statusText: TextView
    private lateinit var progressText: TextView
    private lateinit var actionButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        statusContainer = findViewById(R.id.statusContainer)
        statusText = findViewById(R.id.statusText)
        progressText = findViewById(R.id.progressText)
        actionButton = findViewById(R.id.actionButton)

        if (savedInstanceState == null) {
            Navigator.openGallery(supportFragmentManager)
        }

        // Підписка на стани синхронізації
        applicationContext.getProgressTracker().addListener(this)

        // Обробник кнопки "ОНОВИТИ"/"СКАСУВАТИ"
        actionButton.setOnClickListener {
            val tracker = applicationContext.getProgressTracker()
            val isBusy = tracker.isDownloading() || tracker.isScheduled()
            if (isBusy) {
                DownloadService.cancel(applicationContext)
            } else {
                DownloadService.schedule(applicationContext)
            }
        }

        // Ініціалізація UI поточним станом
        val tracker = applicationContext.getProgressTracker()
        onProgressChanged(tracker.getProgress())
        onStateChanged(
            isDownloading = tracker.isDownloading(),
            hasUpdates = tracker.hasUpdates(),
            scheduled = tracker.isScheduled()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        applicationContext.getProgressTracker().removeListener(this)
    }

    override fun onProgressChanged(percent: Int) {
        // Вимога ЛР3: показ прогресу у відсотках
        progressText.text = "$percent%"
        progressText.visibility = if (percent in 1..100) View.VISIBLE else View.GONE
    }

    override fun onStateChanged(isDownloading: Boolean, hasUpdates: Boolean, scheduled: Boolean) {
        // Показ/приховування нижнього статус-блоку
        val showStatus = isDownloading || scheduled || hasUpdates
        statusContainer.visibility = if (showStatus) View.VISIBLE else View.GONE

        when {
            isDownloading -> {
                statusText.text = "Завантаження фото..."
                actionButton.text = "СКАСУВАТИ"
                progressText.visibility = View.VISIBLE
            }
            scheduled -> {
                statusText.text = "Заплановано завантаження..."
                actionButton.text = "СКАСУВАТИ"
                progressText.visibility = View.GONE
                progressText.text = "0%"
            }
            hasUpdates -> {
                statusText.text = "Доступні оновлення"
                actionButton.text = "ОНОВИТИ"
                progressText.visibility = View.GONE
                progressText.text = "0%"
            }
            else -> {
                statusText.text = "Статус"
                actionButton.text = "ОНОВИТИ"
                progressText.visibility = View.GONE
                progressText.text = "0%"
            }
        }
    }

    override fun onPhotosUpdated(photosCount: Int) {
        // Тут можна було б змінювати статус, але список і empty state обробляє фрагмент.
        // Лишаємо без дій.
    }

    override fun onError(message: String) {
        statusContainer.visibility = View.VISIBLE
        statusText.text = message
        actionButton.text = "ОНОВИТИ"
        progressText.visibility = View.GONE
        progressText.text = "0%"
    }
}
