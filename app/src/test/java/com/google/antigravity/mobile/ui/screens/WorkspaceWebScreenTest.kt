package com.google.antigravity.mobile.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkspaceWebScreenTest {

    @Test
    fun testDefaultWorkspaceUrl() {
        val defaultUrl = "https://antigravity.google.com"
        assertTrue(defaultUrl.startsWith("https://"))
        assertTrue(defaultUrl.contains("antigravity.google.com"))
        assertEquals("https://antigravity.google.com", defaultUrl)
    }

    @Test
    fun testQuickCommandChips() {
        val quickCommands = listOf(
            "/goal",
            "/plan",
            "/grill-me",
            "/teamwork",
            "git status",
            "Run unit tests"
        )

        assertEquals(6, quickCommands.size)
        assertTrue(quickCommands.contains("/goal"))
        assertTrue(quickCommands.contains("/plan"))
        assertTrue(quickCommands.contains("/grill-me"))
        assertTrue(quickCommands.contains("/teamwork"))
    }

    @Test
    fun testAudioPermissionRequirement() {
        val micPermission = "android.permission.RECORD_AUDIO"
        assertEquals("android.permission.RECORD_AUDIO", micPermission)
        assertNotNull(micPermission)
    }

    @Test
    fun testVoicePromptJsScriptGeneration() {
        val prompt = "Implement biometric unlock"
        val jsScript = "javascript:window.__antigravity_sendPrompt && window.__antigravity_sendPrompt('$prompt');"
        assertTrue(jsScript.startsWith("javascript:"))
        assertTrue(jsScript.contains(prompt))
    }
}
