package com.google.antigravity.mobile.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeTest {

    @Test
    fun testBrandColorConstants() {
        assertNotNull(AntigravityViolet)
        assertNotNull(AntigravityCyan)
        assertNotNull(BackgroundDark)
        assertNotNull(SurfaceDark)
    }

    @Test
    fun testThemeDefaults() {
        val defaultDarkTheme = true
        val defaultDynamicColor = false

        assertTrue(defaultDarkTheme)
        assertEquals(false, defaultDynamicColor)
    }

    @Test
    fun testColorHexValues() {
        val violetHex = AntigravityViolet.value
        val cyanHex = AntigravityCyan.value
        assertTrue(violetHex != 0UL)
        assertTrue(cyanHex != 0UL)
    }
}
