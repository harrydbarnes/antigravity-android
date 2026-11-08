package com.google.antigravity.mobile.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.google.antigravity.mobile.data.remote.AntigravityApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ApprovalActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val requestId = intent.getStringExtra(NotificationHelper.EXTRA_REQUEST_ID) ?: return
        val command = intent.getStringExtra(NotificationHelper.EXTRA_COMMAND) ?: ""
        val isApprove = intent.action == NotificationHelper.ACTION_APPROVE

        // Dismiss the notification
        NotificationManagerCompat.from(context).cancel(requestId.hashCode())

        val feedback = if (isApprove) "Approved execution: $command" else "Rejected execution"
        Toast.makeText(context, feedback, Toast.LENGTH_SHORT).show()

        // Asynchronously notify Antigravity API
        CoroutineScope(Dispatchers.IO).launch {
            try {
                AntigravityApiClient().respondToApproval(requestId, isApprove)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
