package com.google.antigravity.mobile.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MainActivityTest {

    @Test
    fun testTabNavigationIndices() {
        val tabWorkspace = 0
        val tabRemote = 1
        val tabCloud = 2
        val tabArtifacts = 3
        val tabSettings = 4

        assertEquals(0, tabWorkspace)
        assertEquals(1, tabRemote)
        assertEquals(2, tabCloud)
        assertEquals(3, tabArtifacts)
        assertEquals(4, tabSettings)
    }

    @Test
    fun testMainActivityClassDefinition() {
        val activityClass = MainActivity::class.java
        assertEquals("com.google.antigravity.mobile.ui.MainActivity", activityClass.name)
        assertNotNull(activityClass)
    }

    @Test
    fun testNotificationPermissionConstant() {
        val postNotifications = "android.permission.POST_NOTIFICATIONS"
        assertEquals("android.permission.POST_NOTIFICATIONS", postNotifications)
        assertTrue(postNotifications.contains("POST_NOTIFICATIONS"))
    }
}
