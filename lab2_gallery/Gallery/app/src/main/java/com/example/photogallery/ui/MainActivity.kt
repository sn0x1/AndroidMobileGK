package com.example.photogallery.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.photogallery.R
import com.example.photogallery.core.GalleryApp
import com.example.photogallery.service.DownloadService
import com.example.photogallery.ui.details.PhotoDetailFragment
import com.example.photogallery.ui.gallery.PhotoListFragment
import com.example.photogallery.utils.ProgressTracker
import com.example.photogallery.utils.SyncStateListener
import com.example.photogallery.utils.UIThreadHelper

class MainActivity : AppCompatActivity(), SyncStateListener, Navigator {

    private lateinit var statusContainer: View
    private lateinit var statusText: TextView
    private lateinit var progressText: TextView
    private lateinit var actionButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusContainer = findViewById(R.id.statusContainer)
        statusText = findViewById(R.id.statusText)
        progressText = findViewById(R.id.progressText)
        actionButton = findViewById(R.id.actionButton)

        actionButton.setOnClickListener {
            DownloadService.schedule(this)
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, PhotoListFragment())
                .commit()
        }

        GalleryApp.getInstance().progressTracker.setHasUpdates(true)
    }

    override fun onStart() {
        super.onStart()
        GalleryApp.getInstance().progressTracker.addListener(this)
        onStateChanged(GalleryApp.getInstance().progressTracker)
    }

    override fun onStop() {
        super.onStop()
        GalleryApp.getInstance().progressTracker.removeListener(this)
    }

    override fun onStateChanged(state: ProgressTracker) {
        UIThreadHelper.post {
            if (state.hasUpdates || state.isScheduled || state.isDownloading) {
                statusContainer.visibility = View.VISIBLE

                when {
                    state.isDownloading -> {
                        statusText.text = "Завантаження..."
                        progressText.visibility = View.VISIBLE
                        progressText.text = "${state.progressPercent}%"
                        actionButton.visibility = View.GONE
                    }
                    state.isScheduled -> {
                        statusText.text = "Завантаження заплановане"
                        progressText.visibility = View.GONE
                        actionButton.visibility = View.GONE
                    }
                    else -> {
                        statusText.text = "Доступне оновлення"
                        progressText.visibility = View.GONE
                        actionButton.visibility = View.VISIBLE
                    }
                }
            } else {
                statusContainer.visibility = View.GONE
            }
        }
    }

    override fun showPhotoDetail(sharedView: View, photoId: String) {
        val photo = GalleryApp.getInstance().photoRepository.getPhotoById(photoId) ?: return

        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .addSharedElement(sharedView, sharedView.transitionName)
            .replace(R.id.fragmentContainer, PhotoDetailFragment.create(photo))
            .addToBackStack(null)
            .commit()
    }
}