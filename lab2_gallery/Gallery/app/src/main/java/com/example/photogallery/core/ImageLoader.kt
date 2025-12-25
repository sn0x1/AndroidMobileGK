package com.example.photogallery.core

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import com.squareup.picasso.Picasso
import com.squareup.picasso.Request
import com.squareup.picasso.RequestHandler
import com.example.photogallery.data.GalleryObserver
import com.example.photogallery.data.Photo
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

        val uriString = uri.toString()
        val photoId = when {
            uri?.path?.isNotEmpty() == true -> {
                uri.path!!.removePrefix("/").removeSuffix(".jpg")
            }
            uri?.schemeSpecificPart?.isNotEmpty() == true -> {
                uri.schemeSpecificPart!!.removePrefix("///").removeSuffix(".jpg")
            }
            uriString.contains("///") -> {
                uriString.substringAfter("///").removeSuffix(".jpg")
            }
            else -> {
                Log.e("ImageLoader", "❌ Cannot extract photo ID from URI: $uri")
                return null
            }
        }

        Log.d("ImageLoader", "   Extracted photo ID: '$photoId'")

        if (photoId.isEmpty()) {
            Log.e("ImageLoader", "❌ Photo ID is empty!")
            return null
        }

        val file = File(context.filesDir, "$photoId.jpg")

        Log.d("ImageLoader", "   Looking for file: ${file.absolutePath}")
        Log.d("ImageLoader", "   File exists: ${file.exists()}, size: ${if (file.exists()) file.length() else 0} bytes")

        return if (file.exists() && file.length() > 0) {
            try {
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
        } else {
            Log.w("ImageLoader", "❌ File not found or empty: ${file.absolutePath}")
            null
        }
    }

    override fun onPhotosUpdated(photos: List<Photo>) {
    }
}