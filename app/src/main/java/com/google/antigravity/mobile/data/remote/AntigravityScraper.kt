package com.google.antigravity.mobile.data.remote

import com.google.antigravity.mobile.data.model.AgentState
import com.google.antigravity.mobile.data.model.AgentStatus
import com.google.antigravity.mobile.data.model.RemoteInstance
import com.google.antigravity.mobile.data.model.MachineType

class AntigravityScraper {

    /**
     * Extracts instances listed inside the Antigravity Remote Control dashboard HTML.
     */
    fun scrapeInstancesFromHtml(html: String): List<RemoteInstance> {
        val instances = mutableListOf<RemoteInstance>()
        val instancePattern = Regex("""data-instance-id=["']([^"']+)["'][^>]*data-hostname=["']([^"']+)["'][^>]*data-name=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
        val matches = instancePattern.findAll(html)

        for (match in matches) {
            val (id, hostname, name) = match.destructured
            val isOnline = !html.contains("data-instance-offline=\"$id\"")
            val isBusy = html.contains("data-instance-busy=\"$id\"")
            instances.add(
                RemoteInstance(
                    id = id,
                    name = name,
                    hostname = hostname,
                    type = if (name.contains("daemon", ignoreCase = true)) MachineType.CLI_DAEMON else MachineType.WORKSTATION,
                    isOnline = isOnline,
                    isBusy = isBusy
                )
            )
        }
        return instances
    }

    /**
     * Parses the agent state from the HTML status indicator.
     */
    fun scrapeAgentStatus(html: String): AgentStatus {
        return when {
            html.contains("Waiting for approval", ignoreCase = true) || html.contains("data-agent-state=\"waiting_approval\"") -> {
                AgentStatus(
                    state = AgentState.WAITING_APPROVAL,
                    currentActivity = "Agent paused waiting for tool execution approval"
                )
            }
            html.contains("Thinking", ignoreCase = true) || html.contains("data-agent-state=\"thinking\"") -> {
                AgentStatus(
                    state = AgentState.THINKING,
                    currentActivity = "Reasoning over files and generating action steps..."
                )
            }
            html.contains("Executing", ignoreCase = true) || html.contains("data-agent-state=\"executing\"") -> {
                AgentStatus(
                    state = AgentState.EXECUTING_TOOL,
                    currentActivity = "Running tool command in sandbox..."
                )
            }
            else -> {
                AgentStatus(
                    state = AgentState.IDLE,
                    currentActivity = "Ready for instructions"
                )
            }
        }
    }
}
