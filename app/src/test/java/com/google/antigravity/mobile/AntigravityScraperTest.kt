package com.google.antigravity.mobile

import com.google.antigravity.mobile.data.model.AgentState
import com.google.antigravity.mobile.data.model.MachineType
import com.google.antigravity.mobile.data.remote.AntigravityScraper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AntigravityScraperTest {

    private lateinit var scraper: AntigravityScraper

    @Before
    fun setUp() {
        scraper = AntigravityScraper()
    }

    @Test
    fun testScrapeInstancesFromHtml() {
        val sampleHtml = """
            <div class="instances-container">
                <div data-instance-id="inst-1" data-hostname="macbook-dev.local" data-name="macbook-primary" data-instance-busy="inst-1">
                    <span>Active Session</span>
                </div>
                <div data-instance-id="inst-2" data-hostname="daemon-box.cloud" data-name="daemon-worker" data-instance-offline="inst-2">
                    <span>Offline</span>
                </div>
            </div>
        """.trimIndent()

        val instances = scraper.scrapeInstancesFromHtml(sampleHtml)
        assertEquals(2, instances.size)

        val first = instances[0]
        assertEquals("inst-1", first.id)
        assertEquals("macbook-dev.local", first.hostname)
        assertEquals("macbook-primary", first.name)
        assertTrue(first.isOnline)
        assertTrue(first.isBusy)
        assertEquals(MachineType.WORKSTATION, first.type)

        val second = instances[1]
        assertEquals("inst-2", second.id)
        assertFalse(second.isOnline)
        assertEquals(MachineType.CLI_DAEMON, second.type)
    }

    @Test
    fun testScrapeAgentStatus_WaitingApproval() {
        val sampleHtml = """
            <div data-agent-state="waiting_approval">
                <h3>Waiting for approval to run bash script</h3>
            </div>
        """.trimIndent()

        val status = scraper.scrapeAgentStatus(sampleHtml)
        assertEquals(AgentState.WAITING_APPROVAL, status.state)
        assertTrue(status.currentActivity.contains("approval"))
    }

    @Test
    fun testScrapeAgentStatus_Thinking() {
        val sampleHtml = """
            <div data-agent-state="thinking">
                <span>Thinking over project requirements</span>
            </div>
        """.trimIndent()

        val status = scraper.scrapeAgentStatus(sampleHtml)
        assertEquals(AgentState.THINKING, status.state)
    }

    @Test
    fun testScrapeAgentStatus_Idle() {
        val sampleHtml = """
            <div data-agent-state="idle">
                <span>Ready for input</span>
            </div>
        """.trimIndent()

        val status = scraper.scrapeAgentStatus(sampleHtml)
        assertEquals(AgentState.IDLE, status.state)
    }
}
