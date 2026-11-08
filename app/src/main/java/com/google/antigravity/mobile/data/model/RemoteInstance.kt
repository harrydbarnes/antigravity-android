package com.google.antigravity.mobile.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class MachineType {
    WORKSTATION,
    CLI_DAEMON,
    CLI_INTERACTIVE,
    CLOUD_SERVER
}

@Serializable
data class RemoteInstance(
    val id: String,
    val name: String,
    val hostname: String,
    val type: MachineType,
    val isOnline: Boolean,
    val isBusy: Boolean,
    val currentTask: String? = null,
    val lastSeenTimestamp: Long = System.currentTimeMillis(),
    val connectionUrl: String = "https://antigravity.google.com/r/$id",
    val activeConversationId: String? = null
)
