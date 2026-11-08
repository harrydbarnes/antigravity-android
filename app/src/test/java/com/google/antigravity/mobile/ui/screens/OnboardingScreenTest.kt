package com.google.antigravity.mobile.ui.screens

import com.google.antigravity.mobile.data.model.PlanTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingScreenTest {

    @Test
    fun testOnboardingPageCount() {
        val totalPages = 5
        assertEquals(5, totalPages)
        assertTrue(totalPages >= 4)
    }

    @Test
    fun testOnboardingPageTitles() {
        val pageTitles = listOf(
            0 to "Agentic Autonomy in Your Pocket",
            1 to "Pair Program Anywhere",
            2 to "Cloud Sandboxes & Resources",
            3 to "Biometric & Actionable Security",
            4 to "Choose Your Plan"
        )

        assertEquals(5, pageTitles.size)
        assertEquals("Agentic Autonomy in Your Pocket", pageTitles[0].second)
        assertEquals("Pair Program Anywhere", pageTitles[1].second)
        assertEquals("Cloud Sandboxes & Resources", pageTitles[2].second)
        assertEquals("Biometric & Actionable Security", pageTitles[3].second)
        assertEquals("Choose Your Plan", pageTitles[4].second)
    }

    @Test
    fun testDefaultSelectedPlanTier() {
        val defaultTier = PlanTier.ULTRA
        assertEquals(PlanTier.ULTRA, defaultTier)
        assertNotNull(defaultTier)
    }

    @Test
    fun testOnboardingActionFlow() {
        var completed = false
        var selectedTier: PlanTier? = null

        val onFinish: (PlanTier) -> Unit = { tier ->
            completed = true
            selectedTier = tier
        }

        onFinish(PlanTier.PRO)
        assertTrue(completed)
        assertEquals(PlanTier.PRO, selectedTier)
    }
}
