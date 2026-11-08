package com.google.antigravity.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.antigravity.mobile.R
import com.google.antigravity.mobile.data.model.ApprovalRiskLevel
import com.google.antigravity.mobile.data.model.ToolApprovalRequest
import com.google.antigravity.mobile.ui.theme.*

@Composable
fun ApprovalDialog(
    request: ToolApprovalRequest,
    isBiometricEnabled: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onRequestBiometrics: (() -> Unit) -> Unit
) {
    Dialog(
        onDismissRequest = onReject,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with Risk Level
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_terminal),
                            contentDescription = "Tool",
                            tint = AntigravityViolet,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = request.toolName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Risk Badge
                    val (riskBg, riskText, riskLabel) = when (request.riskLevel) {
                        ApprovalRiskLevel.HIGH_DANGER -> Triple(AntigravityCrimson.copy(alpha = 0.2f), AntigravityCrimson, "HIGH RISK")
                        ApprovalRiskLevel.MEDIUM -> Triple(AntigravityAmber.copy(alpha = 0.2f), AntigravityAmber, "REVIEW")
                        ApprovalRiskLevel.LOW -> Triple(AntigravityEmerald.copy(alpha = 0.2f), AntigravityEmerald, "SAFE")
                    }

                    Surface(
                        color = riskBg,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = riskLabel,
                            color = riskText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = request.explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Monospace Command Block
                Text(
                    text = "COMMAND TO EXECUTE:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = request.command,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = AntigravityCyan,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.action_reject), color = AntigravityCrimson)
                    }

                    Button(
                        onClick = {
                            if (isBiometricEnabled) {
                                onRequestBiometrics { onApprove() }
                            } else {
                                onApprove()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AntigravityViolet),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.action_approve), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
