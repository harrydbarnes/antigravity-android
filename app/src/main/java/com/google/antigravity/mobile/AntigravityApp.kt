package com.google.antigravity.mobile

import android.app.Application
import com.google.antigravity.mobile.data.local.SessionManager
import com.google.antigravity.mobile.notification.NotificationHelper
import com.google.antigravity.mobile.worker.AntigravitySyncWorker

class AntigravityApp : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // 1. Initialize Notification Channels for Approvals, Tasks, and Instances
        NotificationHelper.createNotificationChannels(this)

        // 2. Schedule Background Sync Worker
        AntigravitySyncWorker.schedule(this)

        // 3. Initialize Session Manager
        SessionManager.getInstance(this)
    }
}
