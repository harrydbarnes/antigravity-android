package com.google.antigravity.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.antigravity.mobile.R
import com.google.antigravity.mobile.data.model.RemoteInstance
import com.google.antigravity.mobile.ui.components.InstanceCard
import com.google.antigravity.mobile.ui.theme.AntigravityCyan
import com.google.antigravity.mobile.ui.theme.AntigravityViolet

@Composable
fun RemoteControlScreen(
    instances: List<RemoteInstance>,
    activeInstance: RemoteInstance?,
    onSelectInstance: (RemoteInstance) -> Unit
) {
    var showConnectDialog by remember { mutableStateOf(false) }
    var manualInstanceInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Explanatory Banner & Daemon CLI Helper
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
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = AntigravityViolet,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.rc_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = stringResource(R.string.rc_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "START DAEMON ON HOST MACHINE:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "agy remote-control start --name \"workstation\"",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = AntigravityCyan
                        )
                    }
                }
            }
        }

        // Section Title & Manual Connect CTA
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.rc_active_instances, instances.size),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                TextButton(onClick = { showConnectDialog = true }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Direct Tunnel", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // List of Machines
        if (instances.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.rc_no_instances),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(instances) { instance ->
                InstanceCard(
                    instance = instance,
                    isSelected = instance.id == activeInstance?.id,
                    onConnect = { onSelectInstance(it) }
                )
            }
        }
    }

    if (showConnectDialog) {
        AlertDialog(
            onDismissRequest = { showConnectDialog = false },
            title = { Text("Connect to Remote Instance") },
            text = {
                Column {
                    Text(
                        "Enter Remote Control Tunnel ID or URL (e.g. devbox-swift-falcon):",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = manualInstanceInput,
                        onValueChange = { manualInstanceInput = it },
                        placeholder = { Text("instance-id or URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (manualInstanceInput.isNotBlank()) {
                            val id = manualInstanceInput.substringAfterLast("/")
                            val newInst = RemoteInstance(
                                id = id,
                                name = id,
                                hostname = "$id.antigravity.internal",
                                type = com.google.antigravity.mobile.data.model.MachineType.CLI_INTERACTIVE,
                                isOnline = true,
                                isBusy = false
                            )
                            onSelectInstance(newInst)
                            showConnectDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AntigravityViolet)
                ) {
                    Text("Connect")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConnectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
