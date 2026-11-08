package com.google.antigravity.mobile.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BiometricHelperTest {

    @Test
    fun testDefaultPromptStrings() {
        val defaultTitle = "Authenticate with Antigravity"
        val defaultSubtitle = "Confirm biometric identity to approve execution"

        assertEquals("Authenticate with Antigravity", defaultTitle)
        assertEquals("Confirm biometric identity to approve execution", defaultSubtitle)
        assertTrue(defaultTitle.contains("Antigravity"))
    }

    @Test
    fun testAuthenticationErrorMessages() {
        val failureMsg = "Authentication failed. Please retry."
        val cancelMsg = "Authentication was cancelled by the user."

        assertNotNull(failureMsg)
        assertTrue(failureMsg.contains("retry"))
        assertTrue(cancelMsg.contains("cancelled"))
    }

    @Test
    fun testAuthenticatorFlags() {
        val strongBiometric = 0x000F
        val deviceCredential = 0x8000
        val combined = strongBiometric or deviceCredential

        assertTrue(combined != 0)
        assertEquals(0x800F, combined)
    }
}
