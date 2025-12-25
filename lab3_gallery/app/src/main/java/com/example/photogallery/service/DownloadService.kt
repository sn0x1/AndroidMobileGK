package com.example.photogallery.service

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.util.Log
import com.example.photogallery.core.getPhotoRepository
import com.example.photogallery.core.getProgressTracker
import com.example.photogallery.utils.UIThreadHelper

class DownloadService : JobService() {

    companion object {
        private const val JOB_ID = 1001
        private const val TAG = "DownloadService"

        fun schedule(context: Context) {
            val scheduler =
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

            val jobInfo = JobInfo.Builder(
                JOB_ID,
                ComponentName(context, DownloadService::class.java)
            ).apply {
                setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                setPersisted(true)
                setMinimumLatency(0)
            }.build()

            val result = scheduler.schedule(jobInfo)
            Log.d(TAG, "Schedule result: ${if (result == JobScheduler.RESULT_SUCCESS) "SUCCESS" else "FAILURE"}")

            if (result == JobScheduler.RESULT_SUCCESS) {
                UIThreadHelper.post {
                    context.getProgressTracker().setScheduled(true)
                }
            }
        }

        /**
         * Скасування запланованої роботи + запит на скасування поточного завантаження.
         * ВАЖЛИВО: завдання ЛР3 вимагає виклик repository.requestCancel()
         */
        fun cancel(context: Context) {
            val scheduler =
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            scheduler.cancel(JOB_ID)

            // якщо вже качає — просимо репозиторій зупинитись
            try {
                context.getPhotoRepository().requestCancel()
            } catch (_: Throwable) {
            }

            UIThreadHelper.post {
                context.getProgressTracker().setScheduled(false)
                context.getProgressTracker().onSyncError() // зупиняємо "download" стан у UI
            }

            Log.d(TAG, "Job cancel requested")
        }
    }

    private var workerThread: Thread? = null

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d(TAG, "Job started")

        UIThreadHelper.post {
            getProgressTracker().setDownloading(true)
        }

        val repo = getPhotoRepository()

        workerThread = Thread {
            try {
                val success = repo.syncGallery(10) { progress ->
                    UIThreadHelper.post {
                        getProgressTracker().setProgress(progress)
                    }
                }

                UIThreadHelper.post {
                    if (success) {
                        getProgressTracker().updatePhotos(repo.getCachedPhotos())
                        getProgressTracker().onSyncSuccess()
                    } else {
                        getProgressTracker().onSyncError()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Download failed", e)
                UIThreadHelper.post {
                    getProgressTracker().onSyncError()
                }
            } finally {
                jobFinished(params, false)
            }
        }.also { it.start() }

        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d(TAG, "Job stopped by system -> requestCancel()")

        // Вимога ЛР3: підтримка скасування через repository.requestCancel()
        try {
            getPhotoRepository().requestCancel()
        } catch (_: Throwable) {
        }

        // якщо наш потік ще живий — можна його "підштовхнути" перериванням
        try {
            workerThread?.interrupt()
        } catch (_: Throwable) {
        }

        UIThreadHelper.post {
            getProgressTracker().onSyncError()
        }

        // false = не перезапускати автоматично
        return false
    }
}
