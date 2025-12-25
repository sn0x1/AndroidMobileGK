package com.example.photogallery.data

data class Photo(
    val id: String,
    val title: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        const val SCHEME = "photo"

        fun generateUri(photoId: String): String {
            return "$SCHEME:///$photoId.jpg"
        }
    }
}
