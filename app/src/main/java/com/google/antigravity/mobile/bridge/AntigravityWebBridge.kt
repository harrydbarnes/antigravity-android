package com.google.antigravity.mobile.bridge

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import com.google.antigravity.mobile.data.model.AgentState
import com.google.antigravity.mobile.data.model.AgentStatus
import com.google.antigravity.mobile.data.model.ApprovalRiskLevel
import com.google.antigravity.mobile.data.model.ArtifactItem
import com.google.antigravity.mobile.data.model.ArtifactType
import com.google.antigravity.mobile.data.model.ToolApprovalRequest
import com.google.antigravity.mobile.notification.NotificationHelper

class AntigravityWebBridge(
    private val context: Context,
    private val onApprovalPrompt: (ToolApprovalRequest) -> Unit = {},
    private val onStatusUpdate: (AgentStatus) -> Unit = {},
    private val onArtifactCreated: (ArtifactItem) -> Unit = {},
    private val onCookieSync: (String) -> Unit = {}
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun postNotification(title: String, message: String, tag: String, targetUrl: String) {
        NotificationHelper.showTaskNotification(context, title, message, targetUrl)
    }

    @JavascriptInterface
    fun onApprovalRequested(toolName: String, command: String, promptId: String) {
        val request = ToolApprovalRequest(
            requestId = if (promptId.isNotBlank()) promptId else "req-${System.currentTimeMillis()}",
            toolName = toolName,
            command = command,
            explanation = "Agent requests approval to execute tool command.",
            riskLevel = if (command.contains("rm ") || command.contains("push ") || command.contains("drop ")) 
                ApprovalRiskLevel.HIGH_DANGER else ApprovalRiskLevel.MEDIUM
        )
        // Post native heads-up notification
        NotificationHelper.showApprovalNotification(context, request)

        // Update in-app state on main thread
        mainHandler.post {
            onApprovalPrompt(request)
        }
    }

    @JavascriptInterface
    fun onStatusChanged(stateStr: String, activity: String) {
        val state = when (stateStr.lowercase()) {
            "thinking", "reasoning" -> AgentState.THINKING
            "executing", "running" -> AgentState.EXECUTING_TOOL
            "waiting_approval", "ask" -> AgentState.WAITING_APPROVAL
            "error" -> AgentState.ERROR
            else -> AgentState.IDLE
        }
        val status = AgentStatus(
            state = state,
            currentActivity = if (activity.isNotBlank()) activity else "Processing task..."
        )
        mainHandler.post {
            onStatusUpdate(status)
        }
    }

    @JavascriptInterface
    fun onArtifactReady(title: String, url: String, typeStr: String) {
        val type = when (typeStr.lowercase()) {
            "diff", "patch" -> ArtifactType.CODE_DIFF
            "plan" -> ArtifactType.IMPLEMENTATION_PLAN
            "proof", "verification" -> ArtifactType.VERIFICATION_PROOF
            "ui", "html" -> ArtifactType.GENERATIVE_UI
            else -> ArtifactType.DOCUMENT
        }
        val artifact = ArtifactItem(
            id = "art-${System.currentTimeMillis()}",
            title = title,
            summary = "Created by agent during current session",
            type = type,
            rawContentUrl = url
        )
        mainHandler.post {
            onArtifactCreated(artifact)
        }
        NotificationHelper.showTaskNotification(
            context,
            "Artifact Ready: $title",
            "Agent delivered a new deliverable ready for inspection.",
            url
        )
    }

    @JavascriptInterface
    fun syncSessionCookies(cookieHeader: String) {
        mainHandler.post {
            onCookieSync(cookieHeader)
        }
    }
}
