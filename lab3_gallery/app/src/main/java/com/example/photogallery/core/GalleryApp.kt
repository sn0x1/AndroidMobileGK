package com.example.photogallery.core

import android.app.Application
import android.content.SharedPreferences
import com.example.photogallery.data.PhotoRepository
import com.example.photogallery.data.StorageManager
import com.example.photogallery.utils.ProgressTracker
import com.squareup.picasso.Picasso
import java.util.UUID

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

        val galleryId = ensureGalleryId(getSharedPreferences(PREFS_FILE, MODE_PRIVATE))

        val storage = StorageManager(this)
        _photoRepository = PhotoRepository(galleryId, storage)

        // RequestHandler for Picasso (loads images from internal storage)
        val imageLoader = ImageLoader(this)
        _photoRepository.addObserver(imageLoader)

        _picasso = Picasso.Builder(this)
            .addRequestHandler(imageLoader)
            .build()
    }

    private fun ensureGalleryId(prefs: SharedPreferences): String {
        val existing = prefs.getString(KEY_GALLERY_ID, null)
        if (!existing.isNullOrEmpty()) return existing

        val newId = UUID.randomUUID().toString()
        prefs.edit().putString(KEY_GALLERY_ID, newId).apply()
        return newId
    }
}
