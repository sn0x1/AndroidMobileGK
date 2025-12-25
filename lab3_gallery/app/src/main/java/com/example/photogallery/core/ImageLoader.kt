package com.example.photogallery.core

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import com.example.photogallery.data.GalleryObserver
import com.example.photogallery.data.Photo
import com.squareup.picasso.Picasso
import com.squareup.picasso.Request
import com.squareup.picasso.RequestHandler
import java.io.File

class ImageLoader(private val context: Context) : RequestHandler(), GalleryObserver {

    companion object {
        fun createUri(photoId: String): String = Photo.generateUri(photoId)
    }

    override fun canHandleRequest(data: Request): Boolean {
        return data.uri?.scheme == Photo.SCHEME
    }

    override fun load(request: Request, networkPolicy: Int): Result? {
        val uri = request.uri
        Log.d("ImageLoader", "📸 Loading request: $uri")

        val photoId = extractPhotoId(uri?.toString(), uri?.path, uri?.schemeSpecificPart)
        if (photoId.isNullOrEmpty()) {
            Log.e("ImageLoader", "❌ Cannot extract photo ID from URI: $uri")
            return null
        }

        val file = File(context.filesDir, "$photoId.jpg")
        Log.d("ImageLoader", "   Looking for file: ${file.absolutePath}")
        Log.d("ImageLoader", "   File exists: ${file.exists()}, size: ${if (file.exists()) file.length() else 0} bytes")

        if (!file.exists() || file.length() <= 0) {
            Log.w("ImageLoader", "❌ File not found or empty: ${file.absolutePath}")
            return null
        }

        return try {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            if (bitmap != null) {
                Log.d("ImageLoader", "✅ Successfully loaded: $photoId (${bitmap.width}x${bitmap.height})")
                Result(bitmap, Picasso.LoadedFrom.DISK)
            } else {
                Log.e("ImageLoader", "❌ Bitmap decode returned null for ${file.absolutePath}")
                null
            }
        } catch (e: Exception) {
            Log.e("ImageLoader", "❌ Error decoding bitmap", e)
            null
        }
    }

    /**
     * Observer callback. Тут нічого критичного робити не потрібно:
     * Picasso при наступному bind сам попросить RequestHandler і прочитає файл.
     * Але цей callback корисний як “гачок” для логування/дебагу.
     */
    override fun onPhotosUpdated(photos: List<Photo>) {
        Log.d("ImageLoader", "Photos updated: ${photos.size}")
    }

    private fun extractPhotoId(
        uriString: String?,
        path: String?,
        schemeSpecificPart: String?
    ): String? {
        if (uriString.isNullOrEmpty()) return null

        return when {
            !path.isNullOrEmpty() -> path.removePrefix("/").removeSuffix(".jpg")
            !schemeSpecificPart.isNullOrEmpty() -> schemeSpecificPart.removePrefix("///").removeSuffix(".jpg")
            uriString.contains("///") -> uriString.substringAfter("///").removeSuffix(".jpg")
            else -> null
        }
    }
}
