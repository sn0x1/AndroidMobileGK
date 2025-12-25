package com.example.photogallery.utils

interface SyncStateListener {
    fun onStateChanged(state: ProgressTracker)
    fun onSyncCompleted() {}
    fun onSyncFailed() {}
}