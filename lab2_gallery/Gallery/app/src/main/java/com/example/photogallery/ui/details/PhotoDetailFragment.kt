package com.example.photogallery.ui.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.squareup.picasso.Callback
import com.example.photogallery.R
import com.example.photogallery.core.GalleryApp
import com.example.photogallery.core.ImageLoader
import com.example.photogallery.data.Photo

class PhotoDetailFragment : Fragment() {

    companion object {
        private const val ARG_PHOTO_ID = "photo_id"

        fun create(photo: Photo): PhotoDetailFragment {
            return PhotoDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PHOTO_ID, photo.id)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_photo_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val photoId = arguments?.getString(ARG_PHOTO_ID)
        val imageView = view.findViewById<ImageView>(R.id.detailImage)

        if (photoId == null) {
            imageView.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            return
        }

        val uri = ImageLoader.createUri(photoId)
        val picasso = GalleryApp.getInstance().picasso

        picasso.load(uri)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_close_clear_cancel)
            .into(imageView, object : Callback {
                override fun onSuccess() {
                    android.util.Log.d("PhotoDetailFragment", "Image loaded: $uri")
                }

                override fun onError(e: Exception?) {
                    android.util.Log.e("PhotoDetailFragment", "Failed to load: $uri", e)
                }
            })
    }
}