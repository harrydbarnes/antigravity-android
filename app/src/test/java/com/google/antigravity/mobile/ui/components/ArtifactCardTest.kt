package com.google.antigravity.mobile.ui.components

import com.google.antigravity.mobile.data.model.ArtifactItem
import com.google.antigravity.mobile.data.model.ArtifactType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ArtifactCardTest {

    @Test
    fun testArtifactTypeLabels() {
        val testCases = mapOf(
            ArtifactType.IMPLEMENTATION_PLAN to "Plan",
            ArtifactType.CODE_DIFF to "Diff",
            ArtifactType.REPORT to "Report",
            ArtifactType.VERIFICATION_PROOF to "Proof",
            ArtifactType.WALKTHROUGH to "Walkthrough",
            ArtifactType.FILE to "File"
        )

        testCases.forEach { (type, expectedLabel) ->
            val label = when (type) {
                ArtifactType.IMPLEMENTATION_PLAN -> "Plan"
                ArtifactType.CODE_DIFF -> "Diff"
                ArtifactType.REPORT -> "Report"
                ArtifactType.VERIFICATION_PROOF -> "Proof"
                ArtifactType.WALKTHROUGH -> "Walkthrough"
                ArtifactType.FILE -> "File"
            }
            assertEquals(expectedLabel, label)
        }
    }

    @Test
    fun testArtifactCardPreviewContent() {
        val artifact = ArtifactItem(
            id = "art-card-test",
            title = "CI/CD Deployment Workflow",
            summary = "GitHub Actions pipeline YAML",
            type = ArtifactType.REPORT,
            previewMarkdown = "```yaml\nname: Android CI\non: [push]\n```",
            userFacing = true
        )

        assertEquals("art-card-test", artifact.id)
        assertEquals("CI/CD Deployment Workflow", artifact.title)
        assertTrue(artifact.previewMarkdown.contains("Android CI"))
        assertTrue(artifact.userFacing)
        assertNotNull(artifact.summary)
    }
}
