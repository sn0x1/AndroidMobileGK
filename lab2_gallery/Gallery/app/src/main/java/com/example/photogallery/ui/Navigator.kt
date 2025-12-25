package com.example.photogallery.ui

import android.view.View

interface Navigator {
    fun showPhotoDetail(sharedView: View, photoId: String)
}