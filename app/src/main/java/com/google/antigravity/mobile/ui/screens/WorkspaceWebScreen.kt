package com.google.antigravity.mobile.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.webkit.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.antigravity.mobile.bridge.AntigravityWebBridge
import com.google.antigravity.mobile.data.model.ArtifactItem
import com.google.antigravity.mobile.data.model.ToolApprovalRequest
import com.google.antigravity.mobile.ui.theme.AntigravityViolet
import java.io.BufferedReader
import java.io.InputStreamReader

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WorkspaceWebScreen(
    url: String,
    onApprovalPrompt: (ToolApprovalRequest) -> Unit,
    onArtifactCreated: (ArtifactItem) -> Unit
) {
    val context = LocalContext.current
    var webProgress by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var quickPromptText by remember { mutableStateOf("") }

    // Read injected bridge script from assets
    val bridgeScript = remember {
        try {
            val stream = context.assets.open("js/antigravity_bridge.js")
            BufferedReader(InputStreamReader(stream)).use { it.readText() }
        } catch (e: Exception) {
            ""
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        allowFileAccess = true
                        allowContentAccess = true
                        setSupportZoom(true)
                        builtInZoomControls = true
                        displayZoomControls = false
                        cacheMode = WebSettings.LOAD_DEFAULT
                        userAgentString = "$userAgentString AntigravityAndroid/1.0"
                    }

                    // Attach Native Javascript Bridge
                    val bridge = AntigravityWebBridge(
                        context = ctx,
                        onApprovalPrompt = onApprovalPrompt,
                        onArtifactCreated = onArtifactCreated
                    )
                    addJavascriptInterface(bridge, "AntigravityNative")

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            isLoading = true
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading = false
                            // Inject native bridge observer script
                            if (bridgeScript.isNotBlank()) {
                                view?.evaluateJavascript(bridgeScript, null)
                            }
                        }
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            webProgress = newProgress
                        }
                    }

                    loadUrl(url)
                    webViewInstance = this
                }
            },
            update = { view ->
                if (view.url != url) {
                    view.loadUrl(url)
                }
            }
        )

        // Top loading progress bar
        if (isLoading && webProgress < 100) {
            LinearProgressIndicator(
                progress = { webProgress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .height(3.dp),
                color = AntigravityViolet,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }

        // Native Quick Prompt Floating Bar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = quickPromptText,
                    onValueChange = { quickPromptText = it },
                    placeholder = { Text("Prompt agent or type /goal, /plan...", fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AntigravityViolet,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (quickPromptText.isNotBlank()) {
                            val script = "window.__antigravity_send_prompt && window.__antigravity_send_prompt(${android.net.Uri.encode(quickPromptText).let { "'$it'" }});"
                            webViewInstance?.evaluateJavascript(script, null)
                            quickPromptText = ""
                        }
                    })
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (quickPromptText.isNotBlank()) {
                            val script = "window.__antigravity_send_prompt && window.__antigravity_send_prompt('${quickPromptText.replace("'", "\\'")}');"
                            webViewInstance?.evaluateJavascript(script, null)
                            quickPromptText = ""
                        }
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(AntigravityViolet)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
