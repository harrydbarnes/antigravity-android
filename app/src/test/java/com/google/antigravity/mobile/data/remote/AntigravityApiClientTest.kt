package com.google.antigravity.mobile.data.remote

import com.google.antigravity.mobile.data.model.ArtifactType
import com.google.antigravity.mobile.data.model.MachineType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AntigravityApiClientTest {

    private lateinit var apiClient: AntigravityApiClient

    @Before
    fun setUp() {
        apiClient = AntigravityApiClient()
    }

    @Test
    fun testFetchRemoteInstancesReturnsActiveMachines() = runBlocking {
        val result = apiClient.fetchRemoteInstances()
        assertTrue(result.isSuccess)

        val instances = result.getOrThrow()
        assertTrue(instances.isNotEmpty())
        assertEquals(4, instances.size)

        val primary = instances.first { it.id == "inst-mac-primary" }
        assertEquals("romin-macbook-pro", primary.name)
        assertEquals("romin-mac.roam.internal", primary.hostname)
        assertEquals(MachineType.WORKSTATION, primary.type)
        assertTrue(primary.isOnline)
        assertTrue(primary.isBusy)
        assertNotNull(primary.activeConversationId)

        val daemon = instances.first { it.id == "inst-cli-daemon-01" }
        assertEquals(MachineType.CLI_DAEMON, daemon.type)
        assertTrue(daemon.isOnline)
    }

    @Test
    fun testFetchCloudProjectsReturnsContainerSandboxes() = runBlocking {
        val result = apiClient.fetchCloudProjects()
        assertTrue(result.isSuccess)

        val projects = result.getOrThrow()
        assertEquals(3, projects.size)

        val mobileSuite = projects.first { it.id == "proj-cloud-antigravity" }
        assertEquals("Antigravity Mobile Suite", mobileSuite.name)
        assertTrue(mobileSuite.isTurboMode)
        assertEquals("gemini-2.5-pro", mobileSuite.defaultModel)
        assertEquals(3, mobileSuite.resources.size)

        val backendSandbox = projects.first { it.id == "proj-cloud-backend" }
        assertEquals(1, backendSandbox.resources.size)
        assertEquals("deepmind/agentic-proxy", backendSandbox.resources.first().name)
    }

    @Test
    fun testFetchArtifactsReturnsDeliverables() = runBlocking {
        val result = apiClient.fetchArtifacts()
        assertTrue(result.isSuccess)

        val artifacts = result.getOrThrow()
        assertEquals(3, artifacts.size)

        val plan = artifacts.first { it.type == ArtifactType.IMPLEMENTATION_PLAN }
        assertEquals("art-plan-01", plan.id)
        assertTrue(plan.title.contains("Android Jetpack Compose"))
        assertTrue(plan.userFacing)

        val diff = artifacts.first { it.type == ArtifactType.CODE_DIFF }
        assertEquals("art-diff-02", diff.id)
        assertTrue(diff.title.contains("ApprovalActionReceiver.kt"))
    }

    @Test
    fun testRespondToApprovalSucceeds() = runBlocking {
        val approveResult = apiClient.respondToApproval("req-42", true)
        assertTrue(approveResult.isSuccess)
        assertTrue(approveResult.getOrThrow())

        val rejectResult = apiClient.respondToApproval("req-42", false)
        assertTrue(rejectResult.isSuccess)
        assertTrue(rejectResult.getOrThrow())
    }

    @Test
    fun testSendRemotePromptSucceeds() = runBlocking {
        val result = apiClient.sendRemotePrompt("inst-mac-primary", "run build and check unit tests")
        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow())
    }
}
