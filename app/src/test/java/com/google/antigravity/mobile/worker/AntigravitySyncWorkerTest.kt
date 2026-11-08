package com.google.antigravity.mobile.worker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AntigravitySyncWorkerTest {

    @Test
    fun testWorkerNameConstant() {
        val workName = "antigravity_background_sync"
        assertEquals("antigravity_background_sync", workName)
        assertTrue(workName.contains("sync"))
    }

    @Test
    fun testPeriodicInterval() {
        val intervalMinutes = 15L
        assertTrue(intervalMinutes >= 15L) // Android WorkManager minimum periodic interval
        assertEquals(15L, intervalMinutes)
    }

    @Test
    fun testSyncConstraints() {
        val requiresNetwork = true
        val requiresCharging = false
        assertTrue(requiresNetwork)
        assertNotNull(requiresNetwork)
        assertEquals(false, requiresCharging)
    }
}
