// utils/ProgressTracker.kt
package com.example.photogallery.utils

import android.content.Context
import com.example.photogallery.data.Photo
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.max
import kotlin.math.min

class ProgressTracker(context: Context) {

    private val listeners = CopyOnWriteArrayList<SyncStateListener>()

    @Volatile private var progress: Int = 0
    @Volatile private var downloading: Boolean = false
    @Volatile private var updates: Boolean = false
    @Volatile private var scheduled: Boolean = false
    @Volatile private var photos: List<Photo> = emptyList()

    fun addListener(listener: SyncStateListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: SyncStateListener) {
        listeners.remove(listener)
    }

    fun getProgress(): Int = progress
    fun isDownloading(): Boolean = downloading
    fun hasUpdates(): Boolean = updates
    fun isScheduled(): Boolean = scheduled
    fun getPhotos(): List<Photo> = photos

    fun setProgress(value: Int) {
        progress = min(100, max(0, value))
        notifyProgress(progress)
    }

    fun setDownloading(value: Boolean) {
        downloading = value
        notifyState()
    }

    fun setHasUpdates(value: Boolean) {
        updates = value
        notifyState()
    }

    fun setScheduled(value: Boolean) {
        scheduled = value
        notifyState()
    }

    fun updatePhotos(newPhotos: List<Photo>) {
        photos = newPhotos
        notifyPhotos()
    }

    fun onSyncSuccess() {
        downloading = false
        scheduled = false
        progress = 100
        notifyProgress(progress)
        notifyState()
    }

    fun onSyncError() {
        downloading = false
        scheduled = false
        progress = 0
        notifyProgress(progress)
        notifyState()
    }

    private fun notifyProgress(percent: Int) {
        UIThreadHelper.post {
            listeners.forEach { it.onProgressChanged(percent) }
        }
    }

    private fun notifyState() {
        val d = downloading
        val u = updates
        val s = scheduled
        UIThreadHelper.post {
            listeners.forEach { it.onStateChanged(d, u, s) }
        }
    }

    private fun notifyPhotos() {
        val count = photos.size
        UIThreadHelper.post {
            listeners.forEach { it.onPhotosUpdated(count) }
        }
    }
}
