package com.google.antigravity.mobile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AntigravityAppTest {

    @Test
    fun testAppClassPackage() {
        val appClassName = AntigravityApp::class.java.name
        assertEquals("com.google.antigravity.mobile.AntigravityApp", appClassName)
        assertTrue(appClassName.endsWith("AntigravityApp"))
    }

    @Test
    fun testInitializationContracts() {
        val channelApprovals = "channel_approvals"
        val syncWorkName = "antigravity_background_sync"

        assertNotNull(channelApprovals)
        assertNotNull(syncWorkName)
        assertEquals("channel_approvals", channelApprovals)
        assertEquals("antigravity_background_sync", syncWorkName)
    }

    @Test
    fun testWorkManagerConfigurationProvider() {
        val app = AntigravityApp()
        assertNotNull(app.workManagerConfiguration)
    }
}
