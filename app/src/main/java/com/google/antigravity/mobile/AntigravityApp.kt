package com.google.antigravity.mobile

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import com.google.antigravity.mobile.data.local.SessionManager
import com.google.antigravity.mobile.notification.NotificationHelper
import com.google.antigravity.mobile.worker.AntigravitySyncWorker

class AntigravityApp : Application(), Configuration.Provider {

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        
        // 1. Initialize Notification Channels defensively
        try {
            NotificationHelper.createNotificationChannels(this)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create notification channels", e)
        }

        // 2. Schedule Background Sync Worker defensively
        try {
            AntigravitySyncWorker.schedule(this)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule background sync worker", e)
        }

        // 3. Initialize Session Manager defensively
        try {
            SessionManager.getInstance(this)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize session manager", e)
        }
    }

    companion object {
        private const val TAG = "CounterweightApp"
    }
}

