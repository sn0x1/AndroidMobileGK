package com.example.photogallery.data

import android.graphics.BitmapFactory
import android.util.Log
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max
import kotlin.math.min

class PhotoRepository(
    private val galleryId: String,
    private val storage: StorageManager
) {
    private val observers = CopyOnWriteArrayList<GalleryObserver>()

    // Доступ із різних потоків: читає UI/сервіси, пише sync-thread
    @Volatile
    private var cachedPhotos: List<Photo> = emptyList()

    // Для “cancel” синхронізації (знадобиться в Part 2)
    private val cancelRequested = AtomicBoolean(false)

    fun addObserver(observer: GalleryObserver) {
        observers.add(observer)
    }

    fun removeObserver(observer: GalleryObserver) {
        observers.remove(observer)
    }

    fun hasUpdates(): Boolean = true

    fun getCachedPhotos(): List<Photo> = cachedPhotos

    fun getPhotoById(id: String): Photo? = cachedPhotos.find { it.id == id }

    /**
     * Дозволяє сервісу “Cancel” скасувати sync.
     * (В Part 2 ми зробимо, щоб ACTION_CANCEL реально впливав на роботу)
     */
    fun requestCancel() {
        cancelRequested.set(true)
    }

    fun resetCancel() {
        cancelRequested.set(false)
    }

    /**
     * Синхронізація. Викликати ТІЛЬКИ з фонового потоку!
     * progressCallback очікує значення 0..100.
     */
    fun syncGallery(
        count: Int = 10,
        progressCallback: (Int) -> Unit
    ): Boolean {
        resetCancel()
        progressCallback(0)

        return try {
            Log.d("PhotoRepository", "Starting sync (galleryId=$galleryId) with $count photos...")

            val safeCount = max(1, count)
            val newPhotos = (1..safeCount).map { index ->
                Photo(
                    id = UUID.randomUUID().toString().take(8),
                    title = "Фото $index"
                )
            }

            newPhotos.forEachIndexed { index, photo ->
                if (cancelRequested.get()) {
                    Log.w("PhotoRepository", "Sync cancelled by user")
                    notifyError("Sync cancelled")
                    progressCallback(0)
                    return false
                }

                val ok = downloadAndSave(photo)
                if (!ok) {
                    Log.e("PhotoRepository", "Failed to download ${photo.id} after retries")
                    // не валимо всю sync — просто йдемо далі
                }

                val progress = ((index + 1) * 100) / newPhotos.size
                progressCallback(min(100, max(0, progress)))
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

    private fun downloadAndSave(photo: Photo): Boolean {
        var success = false
        var attempts = 0

        while (!success && attempts < 3 && !cancelRequested.get()) {
            try {
                val imageUrl = "https://picsum.photos/seed/${photo.id}/800/600"
                Log.d("PhotoRepository", "Downloading: $imageUrl")

                val connection = (URL(imageUrl).openConnection() as HttpURLConnection).apply {
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
                    try {
                        Thread.sleep(1000)
                    } catch (_: InterruptedException) {
                        // ignore
                    }
                }
            }
        }

        return success
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
