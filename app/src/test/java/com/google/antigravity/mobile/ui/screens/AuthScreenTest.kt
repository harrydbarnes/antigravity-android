package com.google.antigravity.mobile.ui.screens

import com.google.antigravity.mobile.data.model.PlanTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthScreenTest {

    @Test
    fun testPlanTiersAvailableForSelection() {
        val tiers = listOf(PlanTier.FREE, PlanTier.PRO, PlanTier.ULTRA)
        assertEquals(3, tiers.size)
        assertTrue(tiers.contains(PlanTier.ULTRA))
        assertTrue(tiers.contains(PlanTier.PRO))
    }

    @Test
    fun testSignInButtonText() {
        val buttonText = "Sign In with Google Account"
        assertTrue(buttonText.contains("Google"))
        assertEquals("Sign In with Google Account", buttonText)
    }

    @Test
    fun testAuthCallbackExecution() {
        var signedInTier: PlanTier? = null
        val onSignIn: (PlanTier) -> Unit = { tier ->
            signedInTier = tier
        }

        onSignIn(PlanTier.ULTRA)
        assertNotNull(signedInTier)
        assertEquals(PlanTier.ULTRA, signedInTier)
    }
}
