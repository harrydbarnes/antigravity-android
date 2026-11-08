package com.google.antigravity.mobile.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import android.content.pm.PackageManager
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.antigravity.mobile.data.model.RemoteInstance
import com.google.antigravity.mobile.security.BiometricHelper
import com.google.antigravity.mobile.ui.components.AntigravityNavBar
import com.google.antigravity.mobile.ui.components.AntigravityTopBar
import com.google.antigravity.mobile.ui.components.ApprovalDialog
import com.google.antigravity.mobile.ui.screens.ArtifactsScreen
import com.google.antigravity.mobile.ui.screens.CloudProjectsScreen
import com.google.antigravity.mobile.ui.screens.OnboardingScreen
import com.google.antigravity.mobile.ui.screens.RemoteControlScreen
import com.google.antigravity.mobile.ui.screens.SettingsScreen
import com.google.antigravity.mobile.ui.screens.WorkspaceWebScreen
import com.google.antigravity.mobile.ui.theme.AntigravityTheme
import com.google.antigravity.mobile.ui.viewmodel.MainViewModel

class MainActivity : FragmentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var biometricHelper: BiometricHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        biometricHelper = BiometricHelper(this)

        handleIncomingIntent(intent)

        setContent {
            AntigravityTheme {
                MainAppContent(
                    viewModel = viewModel,
                    biometricHelper = biometricHelper
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        try {
            val data = intent?.data ?: return
            val path = data.path ?: return

            when {
                path.startsWith("/r/") -> {
                    val instanceId = path.removePrefix("/r/")
                    viewModel.selectInstance(
                        RemoteInstance(
                            id = instanceId,
                            name = instanceId,
                            hostname = "$instanceId.antigravity.internal",
                            type = com.google.antigravity.mobile.data.model.MachineType.CLI_INTERACTIVE,
                            isOnline = true,
                            isBusy = false
                        )
                    )
                }
                path.startsWith("/projects/") -> {
                    val projectId = path.removePrefix("/projects/")
                    // Switch project
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("MainActivity", "Failed to parse incoming intent", e)
        }
    }
}

@Composable
fun MainAppContent(
    viewModel: MainViewModel,
    biometricHelper: BiometricHelper
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val instances by viewModel.instances.collectAsStateWithLifecycle()
    val activeInstance by viewModel.activeInstance.collectAsStateWithLifecycle()
    val cloudProjects by viewModel.cloudProjects.collectAsStateWithLifecycle()
    val activeProject by viewModel.activeProject.collectAsStateWithLifecycle()
    val artifacts by viewModel.artifacts.collectAsStateWithLifecycle()
    val agentStatus by viewModel.agentStatus.collectAsStateWithLifecycle()
    val pendingApproval by viewModel.pendingApproval.collectAsStateWithLifecycle()
    val webUrl by viewModel.webUrl.collectAsStateWithLifecycle()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()
    val hasCompletedOnboarding by viewModel.hasCompletedOnboarding.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }

    // Safely request notification permissions on Android 13+
    val context = LocalContext.current
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { /* Perm status handled */ }
        )
        LaunchedEffect(Unit) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                runCatching {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    if (!hasCompletedOnboarding || !currentUser.isSignedIn) {
        OnboardingScreen(
            onFinish = { tier ->
                viewModel.signInWithGoogle(tier)
            },
            onSkip = {
                viewModel.completeOnboarding()
            }
        )
    } else {
        Scaffold(
            topBar = {
                AntigravityTopBar(
                    instances = instances,
                    activeInstance = activeInstance,
                    agentStatus = agentStatus,
                    onSelectInstance = { inst ->
                        viewModel.selectInstance(inst)
                        selectedTab = 0 // Switch to workspace web view
                    },
                    onRefresh = { viewModel.loadData() }
                )
            },
            bottomBar = {
                AntigravityNavBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (selectedTab) {
                    0 -> WorkspaceWebScreen(
                        url = webUrl,
                        onApprovalPrompt = { req -> viewModel.setPendingApproval(req) },
                        onArtifactCreated = { art -> viewModel.addArtifact(art) }
                    )
                    1 -> RemoteControlScreen(
                        instances = instances,
                        activeInstance = activeInstance,
                        onSelectInstance = { inst ->
                            viewModel.selectInstance(inst)
                            selectedTab = 0
                        }
                    )
                    2 -> CloudProjectsScreen(
                        projects = cloudProjects,
                        activeProject = activeProject,
                        onSelectProject = { proj ->
                            viewModel.selectProject(proj)
                            selectedTab = 0
                        }
                    )
                    3 -> ArtifactsScreen(
                        artifacts = artifacts
                    )
                    4 -> SettingsScreen(
                        user = currentUser,
                        reasoningDepth = agentStatus.reasoningDepth,
                        isBiometricEnabled = isBiometricEnabled,
                        onSetReasoningDepth = { viewModel.setReasoningDepth(it) },
                        onToggleBiometrics = { viewModel.toggleBiometrics(it) },
                        onReplayTour = { viewModel.replayOnboarding() },
                        onSignOut = { viewModel.signOut() }
                    )
                }

                // Interactive Tool Approval Modal
                pendingApproval?.let { request ->
                    ApprovalDialog(
                        request = request,
                        isBiometricEnabled = isBiometricEnabled,
                        onApprove = { viewModel.approvePendingTool() },
                        onReject = { viewModel.rejectPendingTool() },
                        onRequestBiometrics = { callback ->
                            if (biometricHelper.isBiometricAvailable()) {
                                biometricHelper.promptBiometricAuth(
                                    title = "Confirm Tool Approval",
                                    subtitle = "Authenticate to approve ${request.toolName}",
                                    onSuccess = callback,
                                    onError = { /* Error feedback */ }
                                )
                            } else {
                                callback()
                            }
                        }
                    )
                }
            }
        }
    }
}
