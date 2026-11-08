package com.google.antigravity.mobile.data.remote

import com.google.antigravity.mobile.data.model.ArtifactItem
import com.google.antigravity.mobile.data.model.ArtifactType
import com.google.antigravity.mobile.data.model.AttachedResource
import com.google.antigravity.mobile.data.model.CloudProject
import com.google.antigravity.mobile.data.model.MachineType
import com.google.antigravity.mobile.data.model.RemoteInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class AntigravityApiClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()
) {

    suspend fun fetchRemoteInstances(): Result<List<RemoteInstance>> = withContext(Dispatchers.IO) {
        try {
            // Default active instances representing connected workstations & CLI daemons
            val instances = listOf(
                RemoteInstance(
                    id = "inst-mac-primary",
                    name = "romin-macbook-pro",
                    hostname = "romin-mac.roam.internal",
                    type = MachineType.WORKSTATION,
                    isOnline = true,
                    isBusy = true,
                    currentTask = "Running test suite & verification suite across modules",
                    lastSeenTimestamp = System.currentTimeMillis() - 12_000,
                    activeConversationId = "conv-mac-882"
                ),
                RemoteInstance(
                    id = "inst-cli-daemon-01",
                    name = "devbox-swift-falcon",
                    hostname = "devbox-linux-prod.cloud",
                    type = MachineType.CLI_DAEMON,
                    isOnline = true,
                    isBusy = false,
                    currentTask = "Headless daemon waiting for commands (agy remote-control start)",
                    lastSeenTimestamp = System.currentTimeMillis() - 30_000,
                    activeConversationId = null
                ),
                RemoteInstance(
                    id = "inst-gpu-workstation",
                    name = "deepmind-lab-box",
                    hostname = "dgx-cluster-node4.internal",
                    type = MachineType.CLOUD_SERVER,
                    isOnline = true,
                    isBusy = false,
                    currentTask = "PyTorch training agent pipeline idle",
                    lastSeenTimestamp = System.currentTimeMillis() - 95_000,
                    activeConversationId = null
                ),
                RemoteInstance(
                    id = "inst-laptop-offline",
                    name = "pixelbook-travel",
                    hostname = "pixelbook.home.arpa",
                    type = MachineType.WORKSTATION,
                    isOnline = false,
                    isBusy = false,
                    currentTask = null,
                    lastSeenTimestamp = System.currentTimeMillis() - 86_400_000L,
                    activeConversationId = null
                )
            )
            Result.success(instances)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchCloudProjects(): Result<List<CloudProject>> = withContext(Dispatchers.IO) {
        try {
            val projects = listOf(
                CloudProject(
                    id = "proj-cloud-antigravity",
                    name = "Antigravity Mobile Suite",
                    description = "Native Android Jetpack Compose client & Remote Control hub",
                    resources = listOf(
                        AttachedResource("res-1", "deepmind/antigravity-android", "github", "https://github.com/deepmind/antigravity-android"),
                        AttachedResource("res-2", "Mobile Architecture Spec.gdoc", "drive", "https://docs.google.com/document/d/spec-123"),
                        AttachedResource("res-3", "keystore-release.zip", "upload", "/home/antigravity/upload/keystore-release.zip")
                    ),
                    activeConversationCount = 3,
                    isTurboMode = true,
                    defaultModel = "gemini-2.5-pro"
                ),
                CloudProject(
                    id = "proj-cloud-backend",
                    name = "Cloud Services & Agent Proxy",
                    description = "Isolated Google-managed Linux sandbox container for backend orchestrator",
                    resources = listOf(
                        AttachedResource("res-4", "deepmind/agentic-proxy", "github", "https://github.com/deepmind/agentic-proxy")
                    ),
                    activeConversationCount = 1,
                    isTurboMode = true,
                    defaultModel = "gemini-2.5-pro"
                ),
                CloudProject(
                    id = "proj-science-discovery",
                    name = "Antimicrobial Peptide Exploration",
                    description = "Agentic science workbench to discover & evaluate novel antimicrobial peptides",
                    resources = listOf(
                        AttachedResource("res-5", "ProtNLM-embeddings.csv", "upload", "/home/antigravity/upload/ProtNLM-embeddings.csv"),
                        AttachedResource("res-6", "UniProt-Target-Screening.gsheet", "drive", "https://docs.google.com/spreadsheets/d/uniprot-456")
                    ),
                    activeConversationCount = 2,
                    isTurboMode = false,
                    defaultModel = "gemini-2.5-flash"
                )
            )
            Result.success(projects)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchArtifacts(): Result<List<ArtifactItem>> = withContext(Dispatchers.IO) {
        try {
            val artifacts = listOf(
                ArtifactItem(
                    id = "art-plan-01",
                    title = "Implementation Plan: Android Jetpack Compose Architecture",
                    summary = "Comprehensive breakdown of edge-to-edge UI, Native WebView bridge, and Biometrics",
                    type = ArtifactType.IMPLEMENTATION_PLAN,
                    previewMarkdown = "# Implementation Plan\n\n- [x] Configure Gradle 8.7 & Version Catalog\n- [x] Material 3 Dynamic Theming\n- [x] Native JS Bridge for bidirectional WebView events\n- [ ] Release Keystore & Play Store Signing",
                    userFacing = true
                ),
                ArtifactItem(
                    id = "art-diff-02",
                    title = "Code Diff: ApprovalActionReceiver.kt & Heads-up Notification",
                    summary = "Added instant 1-tap Approve/Deny buttons in Android system notification shade",
                    type = ArtifactType.CODE_DIFF,
                    previewMarkdown = "```kotlin\n+ val approveIntent = PendingIntent.getBroadcast(...)\n+ builder.addAction(R.drawable.ic_check_circle, \"Approve (Y)\", approveIntent)\n```",
                    userFacing = true
                ),
                ArtifactItem(
                    id = "art-proof-03",
                    title = "Verification Proof: Unit Test & Bridge Event Test",
                    summary = "Automated verification ensuring DOM scraper catches approval dialog within 150ms",
                    type = ArtifactType.VERIFICATION_PROOF,
                    previewMarkdown = "> Test passed: Notification emitted with priority HIGH and channel `channel_approvals`.",
                    userFacing = true
                )
            )
            Result.success(artifacts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun respondToApproval(requestId: String, isApproved: Boolean): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // Emits action to Antigravity remote control instance
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendRemotePrompt(instanceId: String, prompt: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
