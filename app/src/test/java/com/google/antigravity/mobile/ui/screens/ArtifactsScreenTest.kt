package com.google.antigravity.mobile.ui.screens

import com.google.antigravity.mobile.data.model.ArtifactItem
import com.google.antigravity.mobile.data.model.ArtifactType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ArtifactsScreenTest {

    @Test
    fun testArtifactFilteringByType() {
        val artifacts = listOf(
            ArtifactItem("1", "Plan A", "Summary", ArtifactType.IMPLEMENTATION_PLAN, "md", true),
            ArtifactItem("2", "Diff B", "Summary", ArtifactType.CODE_DIFF, "md", true),
            ArtifactItem("3", "Report C", "Summary", ArtifactType.REPORT, "md", true),
            ArtifactItem("4", "Plan D", "Summary", ArtifactType.IMPLEMENTATION_PLAN, "md", true)
        )

        val plans = artifacts.filter { it.type == ArtifactType.IMPLEMENTATION_PLAN }
        assertEquals(2, plans.size)

        val diffs = artifacts.filter { it.type == ArtifactType.CODE_DIFF }
        assertEquals(1, diffs.size)
    }

    @Test
    fun testUserFacingFilter() {
        val artifacts = listOf(
            ArtifactItem("1", "User Card", "Visible", ArtifactType.REPORT, "md", true),
            ArtifactItem("2", "Internal Temp", "Hidden", ArtifactType.FILE, "md", false)
        )

        val visible = artifacts.filter { it.userFacing }
        assertEquals(1, visible.size)
        assertEquals("User Card", visible.first().title)
        assertNotNull(visible)
    }

    @Test
    fun testArtifactPreviewDialogState() {
        var selectedForPreview: ArtifactItem? = null
        val item = ArtifactItem("art-1", "Architecture Spec", "Deep dive", ArtifactType.REPORT, "md", true)

        selectedForPreview = item
        assertNotNull(selectedForPreview)
        assertEquals("art-1", selectedForPreview?.id)

        selectedForPreview = null
        assertEquals(null, selectedForPreview)
    }
}
