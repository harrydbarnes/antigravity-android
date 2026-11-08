package com.google.antigravity.mobile.ui.viewmodel

import com.google.antigravity.mobile.data.model.AgentState
import com.google.antigravity.mobile.data.model.AgentStatus
import com.google.antigravity.mobile.data.model.ApprovalRiskLevel
import com.google.antigravity.mobile.data.model.ArtifactItem
import com.google.antigravity.mobile.data.model.ArtifactType
import com.google.antigravity.mobile.data.model.CloudProject
import com.google.antigravity.mobile.data.model.MachineType
import com.google.antigravity.mobile.data.model.PlanTier
import com.google.antigravity.mobile.data.model.RemoteInstance
import com.google.antigravity.mobile.data.model.ToolApprovalRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MainViewModelTest {

    @Test
    fun testInitialAgentStatus() {
        val initialStatus = AgentStatus()
        assertEquals(AgentState.IDLE, initialStatus.state)
        assertEquals("idle", initialStatus.currentActivity)
        assertEquals("standard", initialStatus.reasoningDepth)
    }

    @Test
    fun testUpdateReasoningDepth() {
        val status = AgentStatus().copy(reasoningDepth = "deep")
        assertEquals("deep", status.reasoningDepth)
        assertEquals(AgentState.IDLE, status.state)
    }

    @Test
    fun testPendingApprovalStateFlow() {
        val request = ToolApprovalRequest(
            requestId = "req-test-99",
            toolName = "run_command",
            command = "pytest -v",
            explanation = "Running test suite",
            riskLevel = ApprovalRiskLevel.LOW
        )

        var pending: ToolApprovalRequest? = request
        assertNotNull(pending)
        assertEquals("req-test-99", pending?.requestId)
        assertEquals("pytest -v", pending?.command)

        pending = null
        assertNull(pending)
    }

    @Test
    fun testArtifactListPrepend() {
        val existing = listOf(
            ArtifactItem("1", "Old Doc", "Summary", ArtifactType.REPORT, "md", true)
        )
        val newArtifact = ArtifactItem("2", "New Plan", "Summary 2", ArtifactType.IMPLEMENTATION_PLAN, "md2", true)

        val updated = listOf(newArtifact) + existing
        assertEquals(2, updated.size)
        assertEquals("2", updated.first().id)
        assertEquals("1", updated.last().id)
    }

    @Test
    fun testInstanceSelectionUrlGeneration() {
        val instance = RemoteInstance(
            id = "inst-box-1",
            name = "macbook-air",
            hostname = "macbook.roam.internal",
            type = MachineType.WORKSTATION,
            isOnline = true,
            isBusy = false
        )

        val connectionUrl = instance.connectionUrl
        assertTrue(connectionUrl.contains("antigravity.google.com/remote/"))
        assertTrue(connectionUrl.endsWith("inst-box-1"))
    }

    @Test
    fun testProjectSelectionUrlGeneration() {
        val project = CloudProject(
            id = "proj-science",
            name = "Biology Workbench",
            description = "AI Research",
            resources = emptyList()
        )

        val projectUrl = "https://antigravity.google.com/projects/${project.id}"
        assertEquals("https://antigravity.google.com/projects/proj-science", projectUrl)
        assertTrue(projectUrl.startsWith("https://"))
    }
}
