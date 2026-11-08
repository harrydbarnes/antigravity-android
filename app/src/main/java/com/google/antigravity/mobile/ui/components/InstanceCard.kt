package com.google.antigravity.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.antigravity.mobile.R
import com.google.antigravity.mobile.data.model.MachineType
import com.google.antigravity.mobile.data.model.RemoteInstance
import com.google.antigravity.mobile.ui.theme.*

@Composable
fun InstanceCard(
    instance: RemoteInstance,
    isSelected: Boolean,
    onConnect: (RemoteInstance) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) AntigravityViolet else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(16.dp)
            ),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (instance.isOnline) AntigravityEmerald else AntigravityCrimson)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = instance.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Type Badge
                val typeLabel = when (instance.type) {
                    MachineType.CLI_DAEMON -> "CLI DAEMON"
                    MachineType.WORKSTATION -> "WORKSTATION"
                    MachineType.CLI_INTERACTIVE -> "INTERACTIVE"
                    MachineType.CLOUD_SERVER -> "CLOUD VM"
                }

                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = typeLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = instance.hostname,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!instance.currentTask.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (instance.isBusy) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 2.dp,
                                color = AntigravityCyan
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = instance.currentTask,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (instance.isOnline) "Ready to tunnel" else "Offline",
                    fontSize = 12.sp,
                    color = if (instance.isOnline) AntigravityEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = { onConnect(instance) },
                    enabled = instance.isOnline,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) AntigravityViolet else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else AntigravityVioletLight
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(if (isSelected) "Active Session" else "Drive Session", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Connect",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
