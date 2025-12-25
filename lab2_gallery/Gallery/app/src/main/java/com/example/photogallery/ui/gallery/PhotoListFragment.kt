package com.example.photogallery.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.photogallery.R
import com.example.photogallery.core.GalleryApp
import com.example.photogallery.ui.Navigator
import com.example.photogallery.utils.ProgressTracker
import com.example.photogallery.utils.SyncStateListener
import kotlin.concurrent.thread

class PhotoListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var adapter: PhotoAdapter
    private lateinit var emptyText: android.widget.TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_photo_list, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        emptyText = view.findViewById(R.id.emptyText)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = PhotoAdapter(
            picasso = GalleryApp.getInstance().picasso,
            onClick = { sharedView, photoId ->
                (requireActivity() as? Navigator)?.showPhotoDetail(sharedView, photoId)
            }
        )

        recyclerView.layoutManager = GridLayoutManager(context, 2)
        recyclerView.adapter = adapter

        swipeRefresh.setColorSchemeResources(
            android.R.color.holo_orange_dark,
            android.R.color.holo_orange_light
        )

        val tracker = GalleryApp.getInstance().progressTracker
        if (!tracker.hasUpdates && tracker.photos.isNotEmpty()) {
            adapter.submitList(tracker.photos)
        }

        swipeRefresh.setOnRefreshListener {
            performRefresh()
        }

        if (tracker.photos.isEmpty()) {
            emptyText.visibility = android.view.View.VISIBLE
            recyclerView.visibility = android.view.View.GONE
        } else {
            emptyText.visibility = android.view.View.GONE
            recyclerView.visibility = android.view.View.VISIBLE
        }
    }

    override fun onStart() {
        super.onStart()
        GalleryApp.getInstance().progressTracker.addListener(syncListener)
    }

    override fun onStop() {
        super.onStop()
        GalleryApp.getInstance().progressTracker.removeListener(syncListener)
    }

    private fun performRefresh() {
        swipeRefresh.isRefreshing = true

        thread {
            val success = GalleryApp.getInstance().photoRepository.syncGallery(10) {}

            requireActivity().runOnUiThread {
                swipeRefresh.isRefreshing = false
                if (success) {
                    val photos = GalleryApp.getInstance().photoRepository.getCachedPhotos()
                    GalleryApp.getInstance().progressTracker.updatePhotos(photos)

                    adapter.submitList(photos)

                    if (photos.isNotEmpty()) {
                        emptyText.visibility = android.view.View.GONE
                        recyclerView.visibility = android.view.View.VISIBLE
                    }
                }
            }
        }
    }

    private val syncListener = object : SyncStateListener {
        override fun onStateChanged(state: ProgressTracker) {
            if (!state.hasUpdates && state.photos.isNotEmpty()) {
                adapter.submitList(state.photos)
                emptyText.visibility = android.view.View.GONE
                recyclerView.visibility = android.view.View.VISIBLE
            } else if (state.photos.isEmpty() && !state.isDownloading) {
                emptyText.visibility = android.view.View.VISIBLE
                recyclerView.visibility = android.view.View.GONE
            }
        }

        override fun onSyncCompleted() {
            swipeRefresh.isRefreshing = false
            if (GalleryApp.getInstance().progressTracker.photos.isNotEmpty()) {
                emptyText.visibility = android.view.View.GONE
                recyclerView.visibility = android.view.View.VISIBLE
            }
        }

        override fun onSyncFailed() {
            swipeRefresh.isRefreshing = false
        }
    }
}