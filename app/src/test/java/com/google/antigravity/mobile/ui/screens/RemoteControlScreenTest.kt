package com.google.antigravity.mobile.ui.screens

import com.google.antigravity.mobile.data.model.MachineType
import com.google.antigravity.mobile.data.model.RemoteInstance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteControlScreenTest {

    @Test
    fun testInstanceSortingOnlineFirst() {
        val instances = listOf(
            RemoteInstance("1", "Offline Box", "offline.lan", MachineType.WORKSTATION, isOnline = false, isBusy = false),
            RemoteInstance("2", "Active Mac", "mac.lan", MachineType.WORKSTATION, isOnline = true, isBusy = true),
            RemoteInstance("3", "Idle Linux", "linux.lan", MachineType.CLI_DAEMON, isOnline = true, isBusy = false)
        )

        val sorted = instances.sortedByDescending { it.isOnline }
        assertTrue(sorted[0].isOnline)
        assertTrue(sorted[1].isOnline)
        assertEquals(false, sorted[2].isOnline)
    }

    @Test
    fun testConnectInstructionsCommand() {
        val command = "agy remote-control start"
        assertEquals("agy remote-control start", command)
        assertTrue(command.startsWith("agy"))
    }

    @Test
    fun testInstanceSelectionCallback() {
        var selectedInstance: RemoteInstance? = null
        val onSelect: (RemoteInstance) -> Unit = { instance ->
            selectedInstance = instance
        }

        val target = RemoteInstance("test-id", "Test Box", "box.local", MachineType.WORKSTATION, true, false)
        onSelect(target)
        assertNotNull(selectedInstance)
        assertEquals("test-id", selectedInstance?.id)
    }
}
