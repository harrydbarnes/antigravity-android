package com.google.antigravity.mobile

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import com.google.antigravity.mobile.data.local.SessionManager
import com.google.antigravity.mobile.notification.NotificationHelper

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

        // Retire the legacy sample-data worker on upgrade.
        try {
            androidx.work.WorkManager.getInstance(this)
                .cancelUniqueWork("antigravity_background_sync")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cancel legacy sync worker", e)
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

