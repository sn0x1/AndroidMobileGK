package com.example.photogallery.utils

import android.content.Context
import com.example.photogallery.data.Photo
import java.util.concurrent.CopyOnWriteArrayList

class ProgressTracker(private val context: Context) {

    private val listeners = CopyOnWriteArrayList<SyncStateListener>()

    var hasUpdates: Boolean = false
        private set

    var isScheduled: Boolean = false
        private set

    var isDownloading: Boolean = false
        private set

    var progressPercent: Int = 0
        private set

    private val _photos = mutableListOf<Photo>()
    val photos: List<Photo> get() = _photos.toList()

    fun addListener(listener: SyncStateListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: SyncStateListener) {
        listeners.remove(listener)
    }

    fun setHasUpdates(value: Boolean) {
        hasUpdates = value
        notifyListeners()
    }

    fun setScheduled(value: Boolean) {
        isScheduled = value
        notifyListeners()
    }

    fun setDownloading(value: Boolean) {
        isDownloading = value
        notifyListeners()
    }

    fun setProgress(percent: Int) {
        progressPercent = percent.coerceIn(0, 100)
        notifyListeners()
    }

    fun updatePhotos(newPhotos: List<Photo>) {
        _photos.clear()
        _photos.addAll(newPhotos)
        notifyListeners()
    }

    fun onSyncSuccess() {
        isDownloading = false
        hasUpdates = false
        isScheduled = false
        progressPercent = 0
        notifyListeners()
        listeners.forEach { it.onSyncCompleted() }
    }

    fun onSyncError() {
        isDownloading = false
        isScheduled = false
        notifyListeners()
        listeners.forEach { it.onSyncFailed() }
    }

    private fun notifyListeners() {
        listeners.forEach {
            try {
                it.onStateChanged(this)
            } catch (e: Throwable) {
            }
        }
    }
}