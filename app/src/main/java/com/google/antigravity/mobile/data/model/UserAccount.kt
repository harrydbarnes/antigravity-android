package com.google.antigravity.mobile.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class PlanTier {
    FREE,
    PRO,
    ULTRA,
    ENTERPRISE
}

@Serializable
data class UserAccount(
    val id: String = "user-default",
    val email: String = "Managed in your browser",
    val displayName: String = "Google account",
    val photoUrl: String? = null,
    val planTier: PlanTier = PlanTier.FREE,
    val isSignedIn: Boolean = false
)
