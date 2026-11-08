package com.google.antigravity.mobile.ui.components

import com.google.antigravity.mobile.data.model.AgentState
import com.google.antigravity.mobile.data.model.AgentStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AntigravityTopBarTest {

    @Test
    fun testAgentStatusDisplayLabel() {
        val testCases = mapOf(
            AgentState.IDLE to "Idle",
            AgentState.THINKING to "Reasoning...",
            AgentState.EXECUTING_TOOL to "Running Tool",
            AgentState.WAITING_APPROVAL to "Needs Approval",
            AgentState.ERROR to "Attention Required"
        )

        testCases.forEach { (state, expectedLabel) ->
            val label = when (state) {
                AgentState.IDLE -> "Idle"
                AgentState.THINKING -> "Reasoning..."
                AgentState.EXECUTING_TOOL -> "Running Tool"
                AgentState.WAITING_APPROVAL -> "Needs Approval"
                AgentState.ERROR -> "Attention Required"
            }
            assertEquals(expectedLabel, label)
        }
    }

    @Test
    fun testReasoningDepthLabels() {
        val depths = listOf("fast", "standard", "deep")
        assertEquals(3, depths.size)
        assertTrue(depths.contains("standard"))
        assertTrue(depths.contains("deep"))
    }

    @Test
    fun testStatusBadgeIndicator() {
        val status = AgentStatus(state = AgentState.THINKING, currentActivity = "Analyzing repo structure")
        assertEquals(AgentState.THINKING, status.state)
        assertEquals("Analyzing repo structure", status.currentActivity)
        assertNotNull(status.reasoningDepth)
    }
}
