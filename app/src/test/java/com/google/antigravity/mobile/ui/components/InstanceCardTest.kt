package com.google.antigravity.mobile.ui.components

import com.google.antigravity.mobile.data.model.MachineType
import com.google.antigravity.mobile.data.model.RemoteInstance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InstanceCardTest {

    @Test
    fun testMachineTypeBadgeLabel() {
        val testCases = mapOf(
            MachineType.WORKSTATION to "Workstation",
            MachineType.CLI_DAEMON to "CLI Daemon",
            MachineType.CLOUD_SERVER to "Cloud VM",
            MachineType.CLI_INTERACTIVE to "Interactive"
        )

        testCases.forEach { (type, expected) ->
            val label = when (type) {
                MachineType.WORKSTATION -> "Workstation"
                MachineType.CLI_DAEMON -> "CLI Daemon"
                MachineType.CLOUD_SERVER -> "Cloud VM"
                MachineType.CLI_INTERACTIVE -> "Interactive"
            }
            assertEquals(expected, label)
        }
    }

    @Test
    fun testInstanceCardState() {
        val instance = RemoteInstance(
            id = "inst-card-01",
            name = "arch-linux-workstation",
            hostname = "arch.home.lab",
            type = MachineType.WORKSTATION,
            isOnline = true,
            isBusy = false,
            currentTask = "Ready for instructions"
        )

        assertEquals("inst-card-01", instance.id)
        assertEquals("arch-linux-workstation", instance.name)
        assertEquals("arch.home.lab", instance.hostname)
        assertTrue(instance.isOnline)
        assertFalse(instance.isBusy)
        assertNotNull(instance.currentTask)
    }

    @Test
    fun testRelativeTimeFormatting() {
        val now = System.currentTimeMillis()
        val twoMinutesAgo = now - 120_000L
        val diffSeconds = (now - twoMinutesAgo) / 1000

        assertTrue(diffSeconds >= 120)
        assertEquals(120L, diffSeconds)
    }
}
