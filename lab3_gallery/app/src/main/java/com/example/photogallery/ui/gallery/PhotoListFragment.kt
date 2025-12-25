package com.example.photogallery.ui.gallery

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.photogallery.R
import com.example.photogallery.core.getProgressTracker
import com.example.photogallery.service.DownloadService
import com.example.photogallery.ui.Navigator
import com.example.photogallery.utils.SyncStateListener

class PhotoListFragment : Fragment(R.layout.fragment_photo_list), SyncStateListener {

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyText: TextView

    private lateinit var adapter: PhotoAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        recyclerView = view.findViewById(R.id.recyclerView)
        emptyText = view.findViewById(R.id.emptyText)

        adapter = PhotoAdapter { photo ->
            Navigator.openDetails(parentFragmentManager, photo.id)
        }

        recyclerView.layoutManager = androidx.recyclerview.widget.GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = adapter

        // Swipe-to-refresh запускає завантаження
        swipeRefresh.setOnRefreshListener {
            DownloadService.schedule(requireContext().applicationContext)
        }

        // Підписка на зміни (щоб оновлювати список/empty/swipeRefresh)
        requireContext().applicationContext.getProgressTracker().addListener(this)

        // Первинне заповнення
        val tracker = requireContext().applicationContext.getProgressTracker()
        adapter.submitList(tracker.getPhotos())
        updateEmptyState(tracker.getPhotos().isEmpty())
        swipeRefresh.isRefreshing = tracker.isDownloading()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireContext().applicationContext.getProgressTracker().removeListener(this)
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        emptyText.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    override fun onProgressChanged(percent: Int) {
        // Прогрес показує Activity (нижній статус-блок),
        // тут тільки підтримуємо “крутилку” swipeRefresh.
        // (залишаємо без дій)
    }

    override fun onStateChanged(isDownloading: Boolean, hasUpdates: Boolean, scheduled: Boolean) {
        // Зупиняємо / запускаємо swipeRefresh індикатор
        swipeRefresh.isRefreshing = isDownloading
        if (!isDownloading) swipeRefresh.isRefreshing = false
    }

    override fun onPhotosUpdated(photosCount: Int) {
        val photos = requireContext().applicationContext.getProgressTracker().getPhotos()
        adapter.submitList(photos)
        updateEmptyState(photos.isEmpty())
    }

    override fun onError(message: String) {
        swipeRefresh.isRefreshing = false
        // Empty state не чіпаємо, бо може лишатись список зі старих фото
    }
}
