package com.google.antigravity.mobile.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class AgentState {
    IDLE,
    THINKING,
    EXECUTING_TOOL,
    WAITING_APPROVAL,
    ERROR,
    DISCONNECTED
}

@Serializable
data class AgentStatus(
    val state: AgentState = AgentState.IDLE,
    val currentActivity: String = "Ready for instructions",
    val reasoningDepth: String = "Balanced", // Fast, Balanced, Deep
    val activeToolName: String? = null,
    val pendingApprovalId: String? = null
)
