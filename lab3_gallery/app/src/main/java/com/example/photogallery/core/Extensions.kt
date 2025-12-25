package com.example.photogallery.core

import android.content.Context
import com.example.photogallery.data.PhotoRepository
import com.example.photogallery.utils.ProgressTracker
import com.squareup.picasso.Picasso

fun Context.getProgressTracker(): ProgressTracker {
    return GalleryApp.getInstance().progressTracker
}

fun Context.getPhotoRepository(): PhotoRepository {
    return GalleryApp.getInstance().photoRepository
}

fun Context.getPicassoInstance(): Picasso {
    return GalleryApp.getInstance().picasso
}