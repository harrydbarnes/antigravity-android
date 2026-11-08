package com.google.antigravity.mobile.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.antigravity.mobile.R
import com.google.antigravity.mobile.data.model.CloudProject
import com.google.antigravity.mobile.ui.components.ProjectCard
import com.google.antigravity.mobile.ui.theme.AntigravityViolet

@Composable
fun CloudProjectsScreen(
    projects: List<CloudProject>,
    activeProject: CloudProject?,
    onSelectProject: (CloudProject) -> Unit
) {
    var showNewProjectDialog by remember { mutableStateOf(false) }
    var projectNameInput by remember { mutableStateOf("") }
    var projectDescInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cloud Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Cloud,
                            contentDescription = "Cloud",
                            tint = AntigravityViolet,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.cloud_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = stringResource(R.string.cloud_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "• Execution continues autonomously even if the app or phone sleeps.\n• Context attached via GitHub repositories, Drive files, and uploads.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Section Title & New Project Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Your Cloud Projects (${projects.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = { showNewProjectDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AntigravityViolet),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.cloud_new_project), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // List of Projects
        items(projects) { project ->
            ProjectCard(
                project = project,
                isSelected = project.id == activeProject?.id,
                onSelect = { onSelectProject(it) }
            )
        }
    }

    if (showNewProjectDialog) {
        AlertDialog(
            onDismissRequest = { showNewProjectDialog = false },
            title = { Text("New Remote Cloud Project") },
            text = {
                Column {
                    Text(
                        "Spins up an isolated Google-managed Linux sandbox container with persistent context.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = projectNameInput,
                        onValueChange = { projectNameInput = it },
                        label = { Text("Project Name") },
                        placeholder = { Text("e.g. Android Refactor Suite") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = projectDescInput,
                        onValueChange = { projectDescInput = it },
                        label = { Text("Description") },
                        placeholder = { Text("Purpose and goals...") },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (projectNameInput.isNotBlank()) {
                            val newProject = CloudProject(
                                id = "proj-${System.currentTimeMillis()}",
                                name = projectNameInput,
                                description = projectDescInput.ifBlank { "Cloud remote agent container" }
                            )
                            onSelectProject(newProject)
                            showNewProjectDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AntigravityViolet)
                ) {
                    Text("Create Sandbox")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewProjectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
