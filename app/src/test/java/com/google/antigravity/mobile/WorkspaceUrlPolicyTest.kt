package com.google.antigravity.mobile

import com.google.antigravity.mobile.security.WorkspaceUrlPolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkspaceUrlPolicyTest {
    @Test fun acceptsWorkspaceDestinations() {
        listOf(
            "https://antigravity.google.com",
            "https://antigravity.google.com/r/workstation?p=c%2Fconversation",
            "https://antigravity.google.com:443/projects/project-1",
            "https://antigravity.google.com/c/conversation-1"
        ).forEach { assertTrue(it, WorkspaceUrlPolicy.isAllowed(it)) }
    }

    @Test fun rejectsUntrustedDestinations() {
        listOf(
            "http://antigravity.google.com", "javascript:alert(1)",
            "https://antigravity.google.com.evil.example/r/1",
            "https://antigravity.google.com@evil.example",
            "https://user@antigravity.google.com", "https://antigravity.google.com:8443",
            "https://evil.example", "not a URL", "https://antigravity.google.com\\@evil.example"
        ).forEach { assertFalse(it, WorkspaceUrlPolicy.isAllowed(it)) }
    }
}
