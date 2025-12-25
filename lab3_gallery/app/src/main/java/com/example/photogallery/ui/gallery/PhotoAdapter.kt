package com.example.photogallery.ui.gallery

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.photogallery.R
import com.example.photogallery.core.getPicassoInstance
import com.example.photogallery.data.Photo

class PhotoAdapter(
    private val onClick: (Photo) -> Unit
) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    private val photos: MutableList<Photo> = mutableListOf()

    fun submitList(newList: List<Photo>) {
        photos.clear()
        photos.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(v, onClick)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(photos[position])
    }

    override fun getItemCount(): Int = photos.size

    class PhotoViewHolder(
        itemView: View,
        private val onClick: (Photo) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val image: ImageView = itemView.findViewById(R.id.photoImage)
        private val title: TextView = itemView.findViewById(R.id.photoTitle)

        fun bind(photo: Photo) {
            title.text = photo.title

            val uri = Photo.generateUri(photo.id)
            itemView.context.getPicassoInstance()
                .load(uri)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_delete)
                .into(image)

            itemView.setOnClickListener { onClick(photo) }
        }
    }
}
