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
    val email: String = "developer@example.com",
    val displayName: String = "Antigravity Engineer",
    val photoUrl: String? = null,
    val planTier: PlanTier = PlanTier.ULTRA,
    val isSignedIn: Boolean = true
)
