package com.google.antigravity.mobile.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationHelperTest {

    @Test
    fun testChannelIdConstants() {
        assertEquals("channel_approvals", NotificationHelper.CHANNEL_APPROVALS)
        assertEquals("channel_tasks", NotificationHelper.CHANNEL_TASKS)
        assertEquals("channel_instances", NotificationHelper.CHANNEL_INSTANCES)
    }

    @Test
    fun testNotificationActionConstants() {
        assertEquals("com.google.antigravity.mobile.ACTION_APPROVE", NotificationHelper.ACTION_APPROVE)
        assertEquals("com.google.antigravity.mobile.ACTION_REJECT", NotificationHelper.ACTION_REJECT)
        assertTrue(NotificationHelper.ACTION_APPROVE.startsWith("com.google.antigravity.mobile"))
        assertTrue(NotificationHelper.ACTION_REJECT.startsWith("com.google.antigravity.mobile"))
    }

    @Test
    fun testIntentExtraKeys() {
        assertEquals("extra_request_id", NotificationHelper.EXTRA_REQUEST_ID)
        assertEquals("extra_command", NotificationHelper.EXTRA_COMMAND)
        assertNotNull(NotificationHelper.EXTRA_REQUEST_ID)
        assertNotNull(NotificationHelper.EXTRA_COMMAND)
    }

    @Test
    fun testNotificationChannelsStructure() {
        val channels = listOf(
            NotificationHelper.CHANNEL_APPROVALS,
            NotificationHelper.CHANNEL_TASKS,
            NotificationHelper.CHANNEL_INSTANCES
        )
        assertEquals(3, channels.size)
        assertTrue(channels.contains(NotificationHelper.CHANNEL_APPROVALS))
        assertTrue(channels.contains(NotificationHelper.CHANNEL_TASKS))
        assertTrue(channels.contains(NotificationHelper.CHANNEL_INSTANCES))
    }
}
