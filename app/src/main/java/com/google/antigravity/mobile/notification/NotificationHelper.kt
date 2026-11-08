package com.google.antigravity.mobile.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.antigravity.mobile.R
import com.google.antigravity.mobile.data.model.ToolApprovalRequest
import com.google.antigravity.mobile.ui.MainActivity

object NotificationHelper {

    const val CHANNEL_APPROVALS = "channel_approvals"
    const val CHANNEL_TASKS = "channel_tasks"
    const val CHANNEL_INSTANCES = "channel_instances"

    const val ACTION_APPROVE = "com.google.antigravity.mobile.ACTION_APPROVE"
    const val ACTION_REJECT = "com.google.antigravity.mobile.ACTION_REJECT"
    const val EXTRA_REQUEST_ID = "extra_request_id"
    const val EXTRA_COMMAND = "extra_command"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // 1. Tool Approvals (High priority, vibration, heads-up)
            val approvalsChannel = NotificationChannel(
                CHANNEL_APPROVALS,
                context.getString(R.string.channel_approvals_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_approvals_desc)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250)
                setShowBadge(true)
            }

            // 2. Task Updates (Default priority)
            val tasksChannel = NotificationChannel(
                CHANNEL_TASKS,
                context.getString(R.string.channel_tasks_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.channel_tasks_desc)
                enableVibration(false)
            }

            // 3. Machine Connections (Low priority)
            val instancesChannel = NotificationChannel(
                CHANNEL_INSTANCES,
                context.getString(R.string.channel_instances_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.channel_instances_desc)
            }

            notificationManager.createNotificationChannels(listOf(approvalsChannel, tasksChannel, instancesChannel))
        }
    }

    fun showApprovalNotification(context: Context, request: ToolApprovalRequest) {
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_REQUEST_ID, request.requestId)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            request.requestId.hashCode(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Approve Intent
        val approveIntent = Intent(context, ApprovalActionReceiver::class.java).apply {
            action = ACTION_APPROVE
            putExtra(EXTRA_REQUEST_ID, request.requestId)
            putExtra(EXTRA_COMMAND, request.command)
        }
        val approvePendingIntent = PendingIntent.getBroadcast(
            context,
            (request.requestId + "_approve").hashCode(),
            approveIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Reject Intent
        val rejectIntent = Intent(context, ApprovalActionReceiver::class.java).apply {
            action = ACTION_REJECT
            putExtra(EXTRA_REQUEST_ID, request.requestId)
            putExtra(EXTRA_COMMAND, request.command)
        }
        val rejectPendingIntent = PendingIntent.getBroadcast(
            context,
            (request.requestId + "_reject").hashCode(),
            rejectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_APPROVALS)
            .setSmallIcon(R.drawable.ic_terminal)
            .setContentTitle("Counterweight Approval Needed")
            .setContentText("${request.toolName}: ${request.command}")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("${request.explanation}\n\nCommand:\n${request.command}"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(R.drawable.ic_check_circle, context.getString(R.string.action_approve), approvePendingIntent)
            .addAction(R.drawable.ic_cancel, context.getString(R.string.action_reject), rejectPendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(request.requestId.hashCode(), notification)
        } catch (e: SecurityException) {
            // Android 13+ permission not granted yet
        }
    }

    fun showTaskNotification(context: Context, title: String, message: String, targetUrl: String? = null) {
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            targetUrl?.let { putExtra("target_url", it) }
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            title.hashCode(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_TASKS)
            .setSmallIcon(R.drawable.ic_artifacts)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(title.hashCode(), notification)
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }
}
