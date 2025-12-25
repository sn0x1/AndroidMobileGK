package com.example.photogallery.data

import android.graphics.BitmapFactory
import android.util.Log
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CopyOnWriteArrayList

class PhotoRepository(
    private val galleryId: String,
    private val storage: StorageManager
) {
    private val observers = CopyOnWriteArrayList<GalleryObserver>()
    private var cachedPhotos = emptyList<Photo>()

    fun addObserver(observer: GalleryObserver) {
        observers.add(observer)
    }

    fun removeObserver(observer: GalleryObserver) {
        observers.remove(observer)
    }

    fun hasUpdates(): Boolean = true

    fun getCachedPhotos(): List<Photo> = cachedPhotos

    fun syncGallery(count: Int = 10, progressCallback: (Int) -> Unit): Boolean {
        return try {
            Log.d("PhotoRepository", "Starting sync with $count photos...")

            val newPhotos = (1..count).map { index ->
                Photo(
                    id = java.util.UUID.randomUUID().toString().take(8),
                    title = "Фото $index"
                )
            }

            newPhotos.forEachIndexed { index, photo ->
                var success = false
                var attempts = 0

                while (!success && attempts < 3) {
                    try {
                        val imageUrl = "https://picsum.photos/seed/${photo.id}/800/600"
                        Log.d("PhotoRepository", "Downloading: $imageUrl")

                        val url = URL(imageUrl)
                        val connection = url.openConnection() as HttpURLConnection
                        connection.apply {
                            connectTimeout = 15000
                            readTimeout = 15000
                            requestMethod = "GET"
                            setRequestProperty("User-Agent", "PhotoGallery/1.0")
                            instanceFollowRedirects = true
                            doInput = true
                        }

                        connection.connect()
                        val responseCode = connection.responseCode

                        if (responseCode == 200) {
                            connection.inputStream.use { input ->
                                val bitmap = BitmapFactory.decodeStream(input)
                                if (bitmap != null) {
                                    storage.saveImage(photo, bitmap)
                                    success = true
                                    Log.d("PhotoRepository", "✅ Successfully saved: ${photo.id}")
                                } else {
                                    Log.w("PhotoRepository", "❌ Bitmap decode failed for ${photo.id}")
                                }
                            }
                        } else {
                            Log.w("PhotoRepository", "❌ Bad response code: $responseCode for ${photo.id}")
                        }
                    } catch (e: Exception) {
                        attempts++
                        Log.w("PhotoRepository", "Attempt $attempts failed for ${photo.id}", e)
                        if (attempts < 3) {
                            Thread.sleep(1000)
                        }
                    }
                }

                if (!success) {
                    Log.e("PhotoRepository", "Failed to download ${photo.id} after 3 attempts")
                }

                val progress = ((index + 1) * 100) / newPhotos.size
                progressCallback(progress)
                Thread.sleep(500)
            }

            cachedPhotos = newPhotos
            notifyObservers()
            Log.d("PhotoRepository", "Sync completed successfully")
            true

        } catch (e: Exception) {
            Log.e("PhotoRepository", "Sync failed", e)
            notifyError(e.message ?: "Unknown error")
            false
        }
    }

    fun getPhotoById(id: String): Photo? {
        return cachedPhotos.find { it.id == id }
    }

    private fun notifyObservers() {
        observers.forEach {
            try {
                it.onPhotosUpdated(cachedPhotos)
            } catch (e: Throwable) {
                Log.e("PhotoRepository", "Observer notification error", e)
            }
        }
    }

    private fun notifyError(message: String) {
        observers.forEach {
            try {
                it.onError(message)
            } catch (e: Throwable) {
                Log.e("PhotoRepository", "Observer error notification failed", e)
            }
        }
    }
}