package com.google.antigravity.mobile.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.antigravity.mobile.data.local.SessionManager
import com.google.antigravity.mobile.data.model.AgentState
import com.google.antigravity.mobile.data.model.AgentStatus
import com.google.antigravity.mobile.data.model.ArtifactItem
import com.google.antigravity.mobile.data.model.CloudProject
import com.google.antigravity.mobile.data.model.PlanTier
import com.google.antigravity.mobile.data.model.RemoteInstance
import com.google.antigravity.mobile.data.model.ToolApprovalRequest
import com.google.antigravity.mobile.data.model.UserAccount
import com.google.antigravity.mobile.data.remote.AntigravityApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager.getInstance(application)
    private val apiClient = AntigravityApiClient()

    // Authentication & Account
    val currentUser: StateFlow<UserAccount> = sessionManager.currentUser
    val isBiometricEnabled: StateFlow<Boolean> = sessionManager.isBiometricLockEnabled
    val hasCompletedOnboarding: StateFlow<Boolean> = sessionManager.hasCompletedOnboarding

    // Remote Control instances
    private val _instances = MutableStateFlow<List<RemoteInstance>>(emptyList())
    val instances: StateFlow<List<RemoteInstance>> = _instances.asStateFlow()

    private val _activeInstance = MutableStateFlow<RemoteInstance?>(null)
    val activeInstance: StateFlow<RemoteInstance?> = _activeInstance.asStateFlow()

    // Cloud Projects
    private val _cloudProjects = MutableStateFlow<List<CloudProject>>(emptyList())
    val cloudProjects: StateFlow<List<CloudProject>> = _cloudProjects.asStateFlow()

    private val _activeProject = MutableStateFlow<CloudProject?>(null)
    val activeProject: StateFlow<CloudProject?> = _activeProject.asStateFlow()

    // Artifacts & Deliverables
    private val _artifacts = MutableStateFlow<List<ArtifactItem>>(emptyList())
    val artifacts: StateFlow<List<ArtifactItem>> = _artifacts.asStateFlow()

    // Agent Live Status & Activity
    private val _agentStatus = MutableStateFlow(AgentStatus())
    val agentStatus: StateFlow<AgentStatus> = _agentStatus.asStateFlow()

    // Pending Tool Approval
    private val _pendingApproval = MutableStateFlow<ToolApprovalRequest?>(null)
    val pendingApproval: StateFlow<ToolApprovalRequest?> = _pendingApproval.asStateFlow()

    // Active WebView URL
    private val _webUrl = MutableStateFlow("https://antigravity.google.com")
    val webUrl: StateFlow<String> = _webUrl.asStateFlow()

    // Loading / Refresh state
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isRefreshing.value = true

            // Fetch remote control machines
            apiClient.fetchRemoteInstances().onSuccess { list ->
                _instances.value = list
                val savedId = sessionManager.activeInstanceId.value
                val selected = list.firstOrNull { it.id == savedId } ?: list.firstOrNull { it.isOnline }
                _activeInstance.value = selected
            }

            // Fetch cloud projects
            apiClient.fetchCloudProjects().onSuccess { list ->
                _cloudProjects.value = list
                val savedProjectId = sessionManager.activeProjectId.value
                val selectedProj = list.firstOrNull { it.id == savedProjectId } ?: list.firstOrNull()
                _activeProject.value = selectedProj
            }

            // Fetch artifacts
            apiClient.fetchArtifacts().onSuccess { list ->
                _artifacts.value = list
            }

            _isRefreshing.value = false
        }
    }

    fun selectInstance(instance: RemoteInstance) {
        _activeInstance.value = instance
        sessionManager.setActiveInstanceId(instance.id)
        _webUrl.value = instance.connectionUrl
    }

    fun selectProject(project: CloudProject) {
        _activeProject.value = project
        sessionManager.setActiveProjectId(project.id)
        _webUrl.value = "https://antigravity.google.com/projects/${project.id}"
    }

    fun setReasoningDepth(depth: String) {
        _agentStatus.value = _agentStatus.value.copy(reasoningDepth = depth)
    }

    fun setAgentStatus(status: AgentStatus) {
        _agentStatus.value = status
    }

    fun setPendingApproval(request: ToolApprovalRequest?) {
        _pendingApproval.value = request
    }

    fun approvePendingTool() {
        val request = _pendingApproval.value ?: return
        viewModelScope.launch {
            apiClient.respondToApproval(request.requestId, true)
            _pendingApproval.value = null
            _agentStatus.value = _agentStatus.value.copy(
                state = AgentState.EXECUTING_TOOL,
                currentActivity = "Executing approved tool..."
            )
        }
    }

    fun rejectPendingTool() {
        val request = _pendingApproval.value ?: return
        viewModelScope.launch {
            apiClient.respondToApproval(request.requestId, false)
            _pendingApproval.value = null
            _agentStatus.value = _agentStatus.value.copy(
                state = AgentState.IDLE,
                currentActivity = "Tool execution cancelled"
            )
        }
    }

    fun addArtifact(artifact: ArtifactItem) {
        _artifacts.value = listOf(artifact) + _artifacts.value
    }

    fun toggleBiometrics(enabled: Boolean) {
        sessionManager.setBiometricLockEnabled(enabled)
    }

    fun completeOnboarding() {
        sessionManager.setHasCompletedOnboarding(true)
    }

    fun replayOnboarding() {
        sessionManager.resetOnboarding()
    }

    fun signInWithGoogle(planTier: PlanTier) {
        sessionManager.saveUser(
            UserAccount(
                email = "developer@google.com",
                displayName = "Google Engineer",
                planTier = planTier,
                isSignedIn = true
            )
        )
        sessionManager.setHasCompletedOnboarding(true)
    }

    fun signOut() {
        sessionManager.signOut()
    }
}
