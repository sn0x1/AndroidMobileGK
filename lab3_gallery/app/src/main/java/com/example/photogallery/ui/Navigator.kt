// ui/Navigator.kt
package com.example.photogallery.ui

import androidx.fragment.app.FragmentManager
import com.example.photogallery.R
import com.example.photogallery.ui.details.PhotoDetailFragment
import com.example.photogallery.ui.gallery.PhotoListFragment

object Navigator {

    fun openGallery(fm: FragmentManager) {
        fm.beginTransaction()
            .replace(R.id.fragmentContainer, PhotoListFragment())
            .commit()
    }

    fun openDetails(fm: FragmentManager, photoId: String) {
        fm.beginTransaction()
            .replace(R.id.fragmentContainer, PhotoDetailFragment.newInstance(photoId))
            .addToBackStack(null)
            .commit()
    }
}
