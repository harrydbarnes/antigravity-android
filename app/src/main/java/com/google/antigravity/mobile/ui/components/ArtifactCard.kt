package com.google.antigravity.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.antigravity.mobile.R
import com.google.antigravity.mobile.data.model.ArtifactItem
import com.google.antigravity.mobile.data.model.ArtifactType
import com.google.antigravity.mobile.ui.theme.AntigravityCyan
import com.google.antigravity.mobile.ui.theme.AntigravityViolet
import com.google.antigravity.mobile.ui.theme.AntigravityVioletLight

@Composable
fun ArtifactCard(
    artifact: ArtifactItem,
    onInspect: (ArtifactItem) -> Unit,
    onShare: (ArtifactItem) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Type badge
                val (typeBg, typeColor, typeLabel) = when (artifact.type) {
                    ArtifactType.IMPLEMENTATION_PLAN -> Triple(AntigravityViolet.copy(alpha = 0.2f), AntigravityVioletLight, "PLAN")
                    ArtifactType.CODE_DIFF -> Triple(AntigravityCyan.copy(alpha = 0.2f), AntigravityCyan, "DIFF")
                    ArtifactType.VERIFICATION_PROOF -> Triple(AntigravityViolet.copy(alpha = 0.15f), AntigravityVioletLight, "PROOF")
                    ArtifactType.GENERATIVE_UI -> Triple(AntigravityCyan.copy(alpha = 0.2f), AntigravityCyan, "UI PREVIEW")
                    ArtifactType.DOCUMENT -> Triple(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurfaceVariant, "DOC")
                }

                Surface(
                    color = typeBg,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = typeLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = typeColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                IconButton(
                    onClick = { onShare(artifact) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = artifact.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = artifact.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!artifact.previewMarkdown.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(10.dp)
                ) {
                    Text(
                        text = artifact.previewMarkdown,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 4
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = { onInspect(artifact) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Text(
                    text = "Inspect Full Deliverable",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AntigravityVioletLight
                )
            }
        }
    }
}
