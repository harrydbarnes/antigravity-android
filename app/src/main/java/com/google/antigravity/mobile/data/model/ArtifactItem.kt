package com.google.antigravity.mobile.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class ArtifactType {
    IMPLEMENTATION_PLAN,
    CODE_DIFF,
    WALKTHROUGH,
    VERIFICATION_PROOF,
    GENERATIVE_UI,
    DOCUMENT,
    REPORT,
    FILE
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
) {
    constructor(
        id: String,
        title: String,
        summary: String,
        type: ArtifactType,
        previewMarkdown: String?,
        userFacing: Boolean = true
    ) : this(
        id = id,
        title = title,
        summary = summary,
        type = type,
        createdTimestamp = System.currentTimeMillis(),
        rawContentUrl = null,
        previewMarkdown = previewMarkdown,
        userFacing = userFacing
    )
}
