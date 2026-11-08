package com.google.antigravity.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.antigravity.mobile.R
import com.google.antigravity.mobile.data.model.AgentState
import com.google.antigravity.mobile.data.model.AgentStatus
import com.google.antigravity.mobile.data.model.RemoteInstance
import com.google.antigravity.mobile.ui.theme.AntigravityAmber
import com.google.antigravity.mobile.ui.theme.AntigravityCrimson
import com.google.antigravity.mobile.ui.theme.AntigravityCyan
import com.google.antigravity.mobile.ui.theme.AntigravityEmerald
import com.google.antigravity.mobile.ui.theme.AntigravityViolet
import com.google.antigravity.mobile.ui.theme.AntigravityVioletLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AntigravityTopBar(
    instances: List<RemoteInstance>,
    activeInstance: RemoteInstance?,
    agentStatus: AgentStatus,
    onSelectInstance: (RemoteInstance) -> Unit,
    onRefresh: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Logo & Instance Switcher
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { expanded = true }
                        .padding(vertical = 4.dp, horizontal = 6.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_counterweight_logo),
                        contentDescription = "Counterweight Logo",
                        tint = AntigravityViolet,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = activeInstance?.name ?: "Remote Agent (Cloud)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = if (activeInstance != null) activeInstance.hostname else "Isolated Sandbox Container",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Cloud Project (Google-managed)",
                                    fontWeight = if (activeInstance == null) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                expanded = false
                                // Cloud selection
                            }
                        )
                        HorizontalDivider()
                        instances.forEach { instance ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(if (instance.isOnline) AntigravityEmerald else AntigravityCrimson)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(instance.name, fontWeight = if (instance == activeInstance) FontWeight.Bold else FontWeight.Normal)
                                            Text(instance.hostname, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                },
                                onClick = {
                                    expanded = false
                                    onSelectInstance(instance)
                                }
                            )
                        }
                    }
                }

                // Status Badge & Refresh
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusPill(agentStatus)
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusPill(status: AgentStatus) {
    val (bgColor, textColor, label) = when (status.state) {
        AgentState.THINKING -> Triple(AntigravityViolet.copy(alpha = 0.2f), AntigravityVioletLight, "Thinking")
        AgentState.EXECUTING_TOOL -> Triple(AntigravityCyan.copy(alpha = 0.2f), AntigravityCyan, "Executing")
        AgentState.WAITING_APPROVAL -> Triple(AntigravityAmber.copy(alpha = 0.2f), AntigravityAmber, "Approval")
        AgentState.ERROR -> Triple(AntigravityCrimson.copy(alpha = 0.2f), AntigravityCrimson, "Error")
        AgentState.DISCONNECTED -> Triple(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), MaterialTheme.colorScheme.onSurfaceVariant, "Offline")
        AgentState.IDLE -> Triple(AntigravityEmerald.copy(alpha = 0.15f), AntigravityEmerald, "Idle")
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
