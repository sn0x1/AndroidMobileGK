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
            val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

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
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d(TAG, "Job started")

        UIThreadHelper.post {
            getProgressTracker().setDownloading(true)
        }

        Thread {
            try {
                val success = getPhotoRepository().syncGallery(10) { progress ->
                    UIThreadHelper.post {
                        getProgressTracker().setProgress(progress)
                    }
                }

                UIThreadHelper.post {
                    if (success) {
                        getProgressTracker().updatePhotos(getPhotoRepository().getCachedPhotos())
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
        }.start()

        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d(TAG, "Job stopped by system")
        return false
    }
}