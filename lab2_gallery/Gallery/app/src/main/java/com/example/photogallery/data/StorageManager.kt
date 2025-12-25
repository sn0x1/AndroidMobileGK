package com.example.photogallery.data

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import java.io.File

class StorageManager(private val context: Context) {

    fun saveImage(photo: Photo, bitmap: Bitmap) {
        try {
            val file = File(context.filesDir, "${photo.id}.jpg")
            file.outputStream().use { out ->
                val compressed = bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                if (compressed) {
                    Log.d("StorageManager", "✅ Image saved successfully: ${photo.id} -> ${file.absolutePath}")
                    Log.d("StorageManager", "   File size: ${file.length()} bytes, exists: ${file.exists()}")
                } else {
                    Log.e("StorageManager", "❌ Failed to compress bitmap for ${photo.id}")
                }
            }
        } catch (e: Exception) {
            Log.e("StorageManager", "❌ Error saving image ${photo.id}", e)
        }
    }

    fun getImageFile(photoId: String): File {
        return File(context.filesDir, "$photoId.jpg")
    }

    fun imageExists(photoId: String): Boolean {
        return getImageFile(photoId).exists()
    }
}