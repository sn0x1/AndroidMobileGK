package com.example.photogallery.data

interface GalleryObserver {
    fun onPhotosUpdated(photos: List<Photo>)
    fun onError(message: String) {}
}