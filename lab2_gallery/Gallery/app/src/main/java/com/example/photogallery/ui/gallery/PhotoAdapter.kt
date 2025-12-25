package com.example.photogallery.ui.gallery

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.example.photogallery.R
import com.example.photogallery.core.ImageLoader
import com.example.photogallery.data.Photo

class PhotoAdapter(
    private val picasso: Picasso,
    private val onClick: (View, String) -> Unit
) : ListAdapter<Photo, PhotoAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.photoImage)
        val titleView: TextView = view.findViewById(R.id.photoTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val photo = getItem(position)

        holder.titleView.text = photo.title
        holder.imageView.transitionName = "photo_${photo.id}"
        holder.imageView.setOnClickListener {
            onClick(it, photo.id)
        }

        val uri = ImageLoader.createUri(photo.id)
        android.util.Log.d("PhotoAdapter", "Loading image for ${photo.title}: $uri")

        picasso.load(uri)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_close_clear_cancel)
            .into(holder.imageView, object : com.squareup.picasso.Callback {
                override fun onSuccess() {
                    android.util.Log.d("PhotoAdapter", "✅ Image loaded successfully: ${photo.id}")
                }

                override fun onError(e: Exception?) {
                    android.util.Log.e("PhotoAdapter", "❌ Failed to load image: ${photo.id}", e)
                }
            })
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Photo>() {
        override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean {
            return oldItem == newItem
        }
    }
}