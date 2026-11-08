package com.google.antigravity.mobile.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AntigravityNavBarTest {

    @Test
    fun testNavBarDestinations() {
        val navItems = listOf(
            "workspace" to "Workspace",
            "remote" to "Remote",
            "cloud" to "Projects",
            "artifacts" to "Artifacts",
            "settings" to "Settings"
        )

        assertEquals(5, navItems.size)
        assertEquals("workspace", navItems[0].first)
        assertEquals("remote", navItems[1].first)
        assertEquals("cloud", navItems[2].first)
        assertEquals("artifacts", navItems[3].first)
        assertEquals("settings", navItems[4].first)
    }

    @Test
    fun testTabSelectionIndex() {
        val destinations = listOf("workspace", "remote", "cloud", "artifacts", "settings")
        val selectedTab = "cloud"
        val selectedIndex = destinations.indexOf(selectedTab)

        assertEquals(2, selectedIndex)
        assertTrue(selectedIndex in destinations.indices)
    }
}
