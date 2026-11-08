package com.google.antigravity.mobile.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class ApprovalRiskLevel {
    LOW,
    MEDIUM,
    HIGH_DANGER
}

@Serializable
data class ToolApprovalRequest(
    val requestId: String,
    val toolName: String,
    val command: String,
    val explanation: String,
    val riskLevel: ApprovalRiskLevel = ApprovalRiskLevel.MEDIUM,
    val requestedTimestamp: Long = System.currentTimeMillis()
)
