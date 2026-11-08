package com.google.antigravity.mobile.worker

import android.content.Context
import androidx.work.*
import com.google.antigravity.mobile.data.model.AgentState
import com.google.antigravity.mobile.data.model.ToolApprovalRequest
import com.google.antigravity.mobile.data.remote.AntigravityApiClient
import com.google.antigravity.mobile.notification.NotificationHelper
import java.util.concurrent.TimeUnit

class AntigravitySyncWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val apiClient = AntigravityApiClient()

    override suspend fun doWork(): Result {
        return try {
            // Check connected machines
            val instancesResult = apiClient.fetchRemoteInstances()
            if (instancesResult.isSuccess) {
                val instances = instancesResult.getOrDefault(emptyList())
                val busyInstance = instances.firstOrNull { it.isBusy && it.isOnline }
                
                // If an instance needs approval in background, trigger notification
                // (e.g., checking if waiting for tool approval)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "antigravity_background_sync"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<AntigravitySyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }
    }
}
