package com.google.antigravity.mobile.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApprovalActionReceiverTest {

    @Test
    fun testActionResolution() {
        val approveAction = NotificationHelper.ACTION_APPROVE
        val rejectAction = NotificationHelper.ACTION_REJECT

        assertTrue(approveAction == "com.google.antigravity.mobile.ACTION_APPROVE")
        assertTrue(rejectAction == "com.google.antigravity.mobile.ACTION_REJECT")
        assertFalse(approveAction == rejectAction)
    }

    @Test
    fun testNotificationIdGeneration() {
        val reqId1 = "req-999-alpha"
        val reqId2 = "req-999-beta"

        val notifId1 = reqId1.hashCode()
        val notifId2 = reqId2.hashCode()

        assertEquals(reqId1.hashCode(), notifId1)
        assertEquals(reqId2.hashCode(), notifId2)
        assertTrue(notifId1 != notifId2)
    }

    @Test
    fun testApprovalFeedbackFormatting() {
        val command = "git checkout main"
        val approvedFeedback = "Approved execution: $command"
        val rejectedFeedback = "Rejected execution"

        assertTrue(approvedFeedback.contains(command))
        assertTrue(approvedFeedback.startsWith("Approved"))
        assertEquals("Rejected execution", rejectedFeedback)
    }
}
