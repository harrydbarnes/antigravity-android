package com.google.antigravity.mobile

import com.google.antigravity.mobile.data.model.ApprovalRiskLevel
import com.google.antigravity.mobile.data.model.ArtifactItem
import com.google.antigravity.mobile.data.model.ArtifactType
import com.google.antigravity.mobile.data.model.AttachedResource
import com.google.antigravity.mobile.data.model.CloudProject
import com.google.antigravity.mobile.data.model.MachineType
import com.google.antigravity.mobile.data.model.RemoteInstance
import com.google.antigravity.mobile.data.model.ToolApprovalRequest
import com.google.antigravity.mobile.data.model.UserAccount
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelSerializationTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testToolApprovalRequestSerialization() {
        val request = ToolApprovalRequest(
            requestId = "req-101",
            toolName = "run_command",
            command = "git checkout -b feature/mobile",
            explanation = "Creates a new feature branch",
            riskLevel = ApprovalRiskLevel.LOW
        )

        val serialized = json.encodeToString(request)
        assertTrue(serialized.contains("git checkout -b"))

        val deserialized = json.decodeFromString<ToolApprovalRequest>(serialized)
        assertEquals(request.requestId, deserialized.requestId)
        assertEquals(request.toolName, deserialized.toolName)
        assertEquals(request.command, deserialized.command)
        assertEquals(ApprovalRiskLevel.LOW, deserialized.riskLevel)
    }

    @Test
    fun testRemoteInstanceSerialization() {
        val instance = RemoteInstance(
            id = "inst-007",
            name = "macbook-air-m3",
            hostname = "macbook.roam.internal",
            type = MachineType.WORKSTATION,
            isOnline = true,
            isBusy = true,
            currentTask = "Running automated tests"
        )

        val serialized = json.encodeToString(instance)
        assertTrue(serialized.contains("macbook-air-m3"))

        val deserialized = json.decodeFromString<RemoteInstance>(serialized)
        assertEquals(instance.id, deserialized.id)
        assertEquals(instance.name, deserialized.name)
        assertTrue(deserialized.isOnline)
        assertTrue(deserialized.isBusy)
    }

    @Test
    fun testCloudProjectSerialization() {
        val project = CloudProject(
            id = "proj-42",
            name = "Antigravity Mobile",
            description = "Android app build",
            resources = listOf(
                AttachedResource("r1", "github-repo", "github", "https://github.com/test/repo")
            ),
            isTurboMode = true
        )

        val serialized = json.encodeToString(project)
        val deserialized = json.decodeFromString<CloudProject>(serialized)
        assertEquals(project.id, deserialized.id)
        assertEquals(1, deserialized.resources.size)
        assertTrue(deserialized.isTurboMode)
    }
}
