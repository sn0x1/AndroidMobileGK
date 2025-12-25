package com.example.photogallery.utils

import android.os.Handler
import android.os.Looper

object UIThreadHelper {

    private val handler = Handler(Looper.getMainLooper())

    fun post(action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action()
        } else {
            handler.post(action)
        }
    }

    fun postDelayed(delayMillis: Long, action: () -> Unit) {
        handler.postDelayed(action, delayMillis)
    }
}