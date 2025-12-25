// utils/SyncStateListener.kt
package com.example.photogallery.utils

interface SyncStateListener {
    fun onProgressChanged(percent: Int)
    fun onStateChanged(isDownloading: Boolean, hasUpdates: Boolean, scheduled: Boolean)
    fun onPhotosUpdated(photosCount: Int)
    fun onError(message: String) {}
}
