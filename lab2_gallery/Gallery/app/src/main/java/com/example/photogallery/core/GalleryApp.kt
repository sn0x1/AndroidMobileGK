package com.example.photogallery.core

import android.app.Application
import com.squareup.picasso.Picasso
import com.example.photogallery.data.PhotoRepository
import com.example.photogallery.data.StorageManager
import com.example.photogallery.utils.ProgressTracker

class GalleryApp : Application() {

    companion object {
        private const val PREFS_FILE = "gallery_prefs"
        private const val KEY_GALLERY_ID = "gallery_id"

        @Volatile
        private var instance: GalleryApp? = null

        fun getInstance(): GalleryApp {
            return instance ?: throw IllegalStateException("Application not initialized")
        }
    }

    private lateinit var _progressTracker: ProgressTracker
    private lateinit var _photoRepository: PhotoRepository
    private lateinit var _picasso: Picasso

    val progressTracker: ProgressTracker get() = _progressTracker
    val photoRepository: PhotoRepository get() = _photoRepository
    val picasso: Picasso get() = _picasso

    override fun onCreate() {
        super.onCreate()
        instance = this

        _progressTracker = ProgressTracker(this)

        val prefs = getSharedPreferences(PREFS_FILE, MODE_PRIVATE)
        var galleryId = prefs.getString(KEY_GALLERY_ID, null)

        if (galleryId.isNullOrEmpty()) {
            galleryId = java.util.UUID.randomUUID().toString()
            prefs.edit().putString(KEY_GALLERY_ID, galleryId).apply()
        }

        val storage = StorageManager(this)
        _photoRepository = PhotoRepository(galleryId, storage)

        val imageLoader = ImageLoader(this)
        _photoRepository.addObserver(imageLoader)

        _picasso = Picasso.Builder(this)
            .addRequestHandler(imageLoader)
            .build()
    }
}