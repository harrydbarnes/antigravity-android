package com.google.antigravity.mobile.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class ArtifactType {
    IMPLEMENTATION_PLAN,
    CODE_DIFF,
    WALKTHROUGH,
    VERIFICATION_PROOF,
    GENERATIVE_UI,
    DOCUMENT
}

@Serializable
data class ArtifactItem(
    val id: String,
    val title: String,
    val summary: String,
    val type: ArtifactType,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val rawContentUrl: String? = null,
    val previewMarkdown: String? = null,
    val userFacing: Boolean = true
)
