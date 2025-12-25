package com.example.photogallery.ui.details

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.photogallery.R
import com.example.photogallery.core.getPicassoInstance
import com.example.photogallery.data.Photo

class PhotoDetailFragment : Fragment(R.layout.fragment_photo_detail) {

    companion object {
        private const val ARG_PHOTO_ID = "photo_id"

        fun newInstance(photoId: String): PhotoDetailFragment {
            return PhotoDetailFragment().apply {
                arguments = Bundle().apply { putString(ARG_PHOTO_ID, photoId) }
            }
        }
    }

    private var photoId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        photoId = arguments?.getString(ARG_PHOTO_ID)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val img = view.findViewById<ImageView>(R.id.detailImage)
        val id = photoId ?: return

        val uri = Photo.generateUri(id)
        requireContext().getPicassoInstance()
            .load(uri)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_delete)
            .into(img)
    }
}
