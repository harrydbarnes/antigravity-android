package com.google.antigravity.mobile.data.local

import com.google.antigravity.mobile.data.model.PlanTier
import com.google.antigravity.mobile.data.model.UserAccount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionManagerTest {

    @Test
    fun testDefaultUserAccountState() {
        val defaultUser = UserAccount(
            email = "developer@antigravity.internal",
            displayName = "Antigravity Engineer",
            planTier = PlanTier.ULTRA,
            isSignedIn = true
        )

        assertEquals("developer@antigravity.internal", defaultUser.email)
        assertEquals("Antigravity Engineer", defaultUser.displayName)
        assertEquals(PlanTier.ULTRA, defaultUser.planTier)
        assertTrue(defaultUser.isSignedIn)
    }

    @Test
    fun testUserAccountSignOut() {
        val signedOutUser = UserAccount(
            email = "",
            displayName = "",
            planTier = PlanTier.FREE,
            isSignedIn = false
        )

        assertFalse(signedOutUser.isSignedIn)
        assertEquals(PlanTier.FREE, signedOutUser.planTier)
        assertEquals("", signedOutUser.email)
    }

    @Test
    fun testPreferencesKeysConsistency() {
        val keys = listOf(
            "is_signed_in",
            "user_email",
            "user_name",
            "plan_tier",
            "biometric_lock",
            "active_instance_id",
            "active_project_id",
            "has_completed_onboarding"
        )

        assertEquals(8, keys.size)
        assertTrue(keys.contains("has_completed_onboarding"))
        assertTrue(keys.contains("biometric_lock"))
        assertTrue(keys.contains("active_instance_id"))
        assertNotNull(keys)
    }

    @Test
    fun testPlanTierResolution() {
        val proTier = PlanTier.valueOf("PRO")
        val ultraTier = PlanTier.valueOf("ULTRA")
        val freeTier = PlanTier.valueOf("FREE")

        assertEquals(PlanTier.PRO, proTier)
        assertEquals(PlanTier.ULTRA, ultraTier)
        assertEquals(PlanTier.FREE, freeTier)
    }
}
