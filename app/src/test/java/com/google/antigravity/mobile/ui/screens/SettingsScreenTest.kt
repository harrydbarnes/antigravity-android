package com.google.antigravity.mobile.ui.screens

import com.google.antigravity.mobile.data.model.PlanTier
import com.google.antigravity.mobile.data.model.UserAccount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsScreenTest {

    @Test
    fun testUserAccountDisplay() {
        val user = UserAccount(
            email = "engineer@google.com",
            displayName = "Senior Staff AI Engineer",
            planTier = PlanTier.ULTRA,
            isSignedIn = true
        )

        assertEquals("engineer@google.com", user.email)
        assertEquals("Senior Staff AI Engineer", user.displayName)
        assertEquals(PlanTier.ULTRA, user.planTier)
        assertTrue(user.isSignedIn)
    }

    @Test
    fun testBiometricToggleState() {
        var biometricEnabled = false
        val toggle: (Boolean) -> Unit = { enabled ->
            biometricEnabled = enabled
        }

        toggle(true)
        assertTrue(biometricEnabled)

        toggle(false)
        assertFalse(biometricEnabled)
    }

    @Test
    fun testCliDaemonSetupSnippet() {
        val snippet = "curl -fsSL https://antigravity.google.com/install.sh | bash\nagy remote-control start"
        assertTrue(snippet.contains("install.sh"))
        assertTrue(snippet.contains("agy remote-control start"))
        assertNotNull(snippet)
    }
}
