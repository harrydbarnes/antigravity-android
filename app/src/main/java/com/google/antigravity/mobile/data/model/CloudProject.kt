package com.google.antigravity.mobile.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AttachedResource(
    val id: String,
    val name: String,
    val type: String, // "github", "drive", "upload"
    val uri: String
)

@Serializable
data class CloudProject(
    val id: String,
    val name: String,
    val description: String,
    val resources: List<AttachedResource> = emptyList(),
    val activeConversationCount: Int = 1,
    val lastModifiedTimestamp: Long = System.currentTimeMillis(),
    val isTurboMode: Boolean = true,
    val defaultModel: String = "gemini-2.5-pro"
)
