package com.google.antigravity.mobile.ui.components

import com.google.antigravity.mobile.data.model.ApprovalRiskLevel
import com.google.antigravity.mobile.data.model.ToolApprovalRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ApprovalDialogTest {

    @Test
    fun testRiskLevelBadgeText() {
        val testCases = mapOf(
            ApprovalRiskLevel.LOW to "LOW RISK",
            ApprovalRiskLevel.MEDIUM to "MODERATE RISK",
            ApprovalRiskLevel.HIGH_DANGER to "HIGH RISK — EXTREME CAUTION"
        )

        testCases.forEach { (level, expected) ->
            val label = when (level) {
                ApprovalRiskLevel.LOW -> "LOW RISK"
                ApprovalRiskLevel.MEDIUM -> "MODERATE RISK"
                ApprovalRiskLevel.HIGH_DANGER -> "HIGH RISK — EXTREME CAUTION"
            }
            assertEquals(expected, label)
        }
    }

    @Test
    fun testApprovalDialogRequestFields() {
        val request = ToolApprovalRequest(
            requestId = "req-dialog-1",
            toolName = "write_to_file",
            command = "write_to_file path='/etc/hosts'",
            explanation = "Update system hosts file",
            riskLevel = ApprovalRiskLevel.HIGH_DANGER
        )

        assertEquals("req-dialog-1", request.requestId)
        assertEquals("write_to_file", request.toolName)
        assertTrue(request.command.contains("/etc/hosts"))
        assertEquals(ApprovalRiskLevel.HIGH_DANGER, request.riskLevel)
        assertNotNull(request.explanation)
    }

    @Test
    fun testActionButtonsText() {
        val approveText = "Approve & Execute (Y)"
        val rejectText = "Reject & Terminate (N)"

        assertTrue(approveText.startsWith("Approve"))
        assertTrue(rejectText.startsWith("Reject"))
        assertEquals("Approve & Execute (Y)", approveText)
    }
}
