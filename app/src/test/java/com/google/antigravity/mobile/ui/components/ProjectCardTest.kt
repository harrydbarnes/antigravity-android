package com.google.antigravity.mobile.ui.components

import com.google.antigravity.mobile.data.model.AttachedResource
import com.google.antigravity.mobile.data.model.CloudProject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProjectCardTest {

    @Test
    fun testProjectCardDetails() {
        val project = CloudProject(
            id = "proj-card-1",
            name = "Antigravity Android App",
            description = "Kotlin Material 3 application",
            resources = listOf(
                AttachedResource("r1", "github.com/harrydbarnes/antigravity-android", "github", "https://github.com"),
                AttachedResource("r2", "Architecture.gdoc", "drive", "https://drive.google.com")
            ),
            activeConversationCount = 2,
            isTurboMode = true,
            defaultModel = "gemini-2.5-pro"
        )

        assertEquals("proj-card-1", project.id)
        assertEquals("Antigravity Android App", project.name)
        assertTrue(project.isTurboMode)
        assertEquals("gemini-2.5-pro", project.defaultModel)
        assertEquals(2, project.resources.size)
        assertEquals(2, project.activeConversationCount)
    }

    @Test
    fun testResourceBadgeIcons() {
        val githubRes = AttachedResource("1", "repo", "github", "url")
        val driveRes = AttachedResource("2", "doc", "drive", "url")
        val uploadRes = AttachedResource("3", "file", "upload", "url")

        assertEquals("github", githubRes.type)
        assertEquals("drive", driveRes.type)
        assertEquals("upload", uploadRes.type)
        assertNotNull(githubRes.name)
    }
}
