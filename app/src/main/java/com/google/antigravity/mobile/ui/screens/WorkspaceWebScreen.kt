package com.google.antigravity.mobile.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.antigravity.mobile.security.WorkspaceUrlPolicy
import com.google.antigravity.mobile.data.model.ArtifactItem
import com.google.antigravity.mobile.data.model.ToolApprovalRequest

/** Keep login and the workspace in the same browser cookie store. */
@Composable
fun WorkspaceWebScreen(
    url: String,
    onApprovalPrompt: (ToolApprovalRequest) -> Unit,
    onArtifactCreated: (ArtifactItem) -> Unit
) {
    val context = LocalContext.current
    var error by remember { mutableStateOf<String?>(null) }
    var openedUrl by rememberSaveable { mutableStateOf<String?>(null) }

    fun openWorkspace() {
        val uri = Uri.parse(url)
        if (!WorkspaceUrlPolicy.isAllowed(url)) {
            error = "This workspace link is not a supported Antigravity address."
            return
        }
        try {
            // Pin to a browser so Android App Links cannot route back into this app.
            val browserPackage = CustomTabsClient.getPackageName(context, null)
                ?: context.packageManager.resolveActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
                        .addCategory(Intent.CATEGORY_BROWSABLE), 0
                )?.activityInfo?.packageName
            if (browserPackage == null || browserPackage == context.packageName) {
                error = "Install or enable a web browser, then try again."
                return
            }
            val tab = CustomTabsIntent.Builder().setShowTitle(true).build()
            tab.intent.setPackage(browserPackage)
            tab.launchUrl(context, uri)
            error = null
            openedUrl = url
        } catch (_: ActivityNotFoundException) {
            error = "No browser is available. Enable a browser and try again."
        } catch (_: SecurityException) {
            error = "The browser could not open this workspace. Try another browser."
        }
    }

    // Open once per destination, not every time the browser returns to the app.
    LaunchedEffect(url) {
        if (openedUrl != url) openWorkspace()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Your Antigravity workspace", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Text("Sign in with Google in the browser tab. Your browser remembers your account for future visits. Use its account menu to switch accounts or sign out.")
        Spacer(Modifier.height(12.dp))
        Text("Web tools, uploads and approvals are available in the browser. Native prompt controls and page-driven notifications are not connected in this mode.", style = MaterialTheme.typography.bodySmall)
        error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(20.dp))
        Button(onClick = { openWorkspace() }) { Text("Open workspace") }
    }
}
