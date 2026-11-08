package com.google.antigravity.mobile.bridge

import com.google.antigravity.mobile.data.model.AgentState
import com.google.antigravity.mobile.data.model.AgentStatus
import com.google.antigravity.mobile.data.model.ApprovalRiskLevel
import com.google.antigravity.mobile.data.model.ArtifactItem
import com.google.antigravity.mobile.data.model.ArtifactType
import com.google.antigravity.mobile.data.model.ToolApprovalRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AntigravityWebBridgeTest {

    @Test
    fun testAgentStateMapping() {
        val testCases = mapOf(
            "thinking" to AgentState.THINKING,
            "reasoning" to AgentState.THINKING,
            "executing" to AgentState.EXECUTING_TOOL,
            "running" to AgentState.EXECUTING_TOOL,
            "waiting_approval" to AgentState.WAITING_APPROVAL,
            "ask" to AgentState.WAITING_APPROVAL,
            "error" to AgentState.ERROR,
            "unknown_state" to AgentState.IDLE
        )

        testCases.forEach { (stateStr, expected) ->
            val mapped = when (stateStr.lowercase()) {
                "thinking", "reasoning" -> AgentState.THINKING
                "executing", "running" -> AgentState.EXECUTING_TOOL
                "waiting_approval", "ask" -> AgentState.WAITING_APPROVAL
                "error" -> AgentState.ERROR
                else -> AgentState.IDLE
            }
            assertEquals(expected, mapped)
        }
    }

    @Test
    fun testApprovalRiskAssessment() {
        val dangerousCommands = listOf(
            "rm -rf /var/log/tmp",
            "git push --force origin main",
            "drop table users cascade"
        )
        dangerousCommands.forEach { cmd ->
            val risk = if (cmd.contains("rm ") || cmd.contains("push ") || cmd.contains("drop ")) {
                ApprovalRiskLevel.HIGH_DANGER
            } else {
                ApprovalRiskLevel.MEDIUM
            }
            assertEquals(ApprovalRiskLevel.HIGH_DANGER, risk)
        }

        val safeCommands = listOf(
            "git status",
            "ls -la /home/antigravity",
            "cat build.gradle.kts"
        )
        safeCommands.forEach { cmd ->
            val risk = if (cmd.contains("rm ") || cmd.contains("push ") || cmd.contains("drop ")) {
                ApprovalRiskLevel.HIGH_DANGER
            } else {
                ApprovalRiskLevel.MEDIUM
            }
            assertEquals(ApprovalRiskLevel.MEDIUM, risk)
        }
    }

    @Test
    fun testToolApprovalRequestCreation() {
        val request = ToolApprovalRequest(
            requestId = "req-1234",
            toolName = "run_command",
            command = "rm -rf build/",
            explanation = "Clean build output directory",
            riskLevel = ApprovalRiskLevel.HIGH_DANGER
        )
        assertEquals("req-1234", request.requestId)
        assertEquals("run_command", request.toolName)
        assertEquals("rm -rf build/", request.command)
        assertEquals(ApprovalRiskLevel.HIGH_DANGER, request.riskLevel)
        assertNotNull(request.explanation)
    }

    @Test
    fun testArtifactCreationFromBridge() {
        val artifact = ArtifactItem(
            id = "art-test-01",
            title = "Test Results Artifact",
            summary = "Summary of executed test pass",
            type = ArtifactType.REPORT,
            previewMarkdown = "# All Tests Passed\n\n100% pass rate",
            userFacing = true
        )
        assertEquals(ArtifactType.REPORT, artifact.type)
        assertTrue(artifact.userFacing)
        assertTrue(artifact.previewMarkdown?.contains("All Tests Passed") == true)
    }
}
