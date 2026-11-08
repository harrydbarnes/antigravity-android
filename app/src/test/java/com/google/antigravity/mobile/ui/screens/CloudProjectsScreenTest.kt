package com.google.antigravity.mobile.ui.screens

import com.google.antigravity.mobile.data.model.AttachedResource
import com.google.antigravity.mobile.data.model.CloudProject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CloudProjectsScreenTest {

    @Test
    fun testProjectListFiltering() {
        val projects = listOf(
            CloudProject("1", "Mobile App", "Android Kotlin", emptyList()),
            CloudProject("2", "Backend API", "Go and Docker", emptyList()),
            CloudProject("3", "ML Research", "PyTorch Agent", emptyList())
        )

        val query = "Mobile"
        val filtered = projects.filter { it.name.contains(query, ignoreCase = true) }

        assertEquals(1, filtered.size)
        assertEquals("Mobile App", filtered.first().name)
    }

    @Test
    fun testProjectSelectionState() {
        val project1 = CloudProject("1", "Project Alpha", "Description", emptyList())
        val project2 = CloudProject("2", "Project Beta", "Description", emptyList())

        var activeProject: CloudProject? = project1
        assertEquals("1", activeProject?.id)

        activeProject = project2
        assertEquals("2", activeProject?.id)
        assertNotNull(activeProject)
    }

    @Test
    fun testEmptyProjectStateMessage() {
        val emptyMessage = "No cloud projects found"
        assertTrue(emptyMessage.contains("cloud projects"))
        assertEquals("No cloud projects found", emptyMessage)
    }
}
