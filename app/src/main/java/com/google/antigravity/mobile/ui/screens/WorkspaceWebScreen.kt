package com.google.antigravity.mobile.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.webkit.*
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.antigravity.mobile.bridge.AntigravityWebBridge
import com.google.antigravity.mobile.data.model.ArtifactItem
import com.google.antigravity.mobile.data.model.ToolApprovalRequest
import com.google.antigravity.mobile.ui.theme.AntigravityCyan
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
    var fileUploadCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }

    // Android Back button navigates back in WebView history
    BackHandler(enabled = webViewInstance?.canGoBack() == true) {
        webViewInstance?.goBack()
    }

    // Modern file chooser contract for resource uploads (GitHub patches, Drive files, images)
    val fileChooserLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uris = WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
        fileUploadCallback?.onReceiveValue(uris)
        fileUploadCallback = null
    }

    // Top Antigravity Slash Commands
    val slashCommands = remember {
        listOf("/goal", "/plan", "/remote-control", "/teamwork", "/browser", "/diff", "/voice", "/agents")
    }

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

                    // Enable third-party cookies for Google OAuth session sync
                    val cookieManager = CookieManager.getInstance()
                    cookieManager.setAcceptCookie(true)
                    cookieManager.setAcceptThirdPartyCookies(this, true)

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
                        mediaPlaybackRequiresUserGesture = false
                        cacheMode = WebSettings.LOAD_DEFAULT
                        // Mobile-optimized browser agent identity
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

                        // Handle file attachments (Upload Files to /home/antigravity/upload)
                        override fun onShowFileChooser(
                            webView: WebView?,
                            filePathCallback: ValueCallback<Array<Uri>>?,
                            fileChooserParams: FileChooserParams?
                        ): Boolean {
                            fileUploadCallback?.onReceiveValue(null)
                            fileUploadCallback = filePathCallback
                            val intent = fileChooserParams?.createIntent()
                            try {
                                if (intent != null) {
                                    fileChooserLauncher.launch(intent)
                                    return true
                                }
                            } catch (e: Exception) {
                                fileUploadCallback = null
                            }
                            return false
                        }

                        // Grant Audio capture for Gemini Live Voice Mode in Antigravity
                        override fun onPermissionRequest(request: PermissionRequest?) {
                            request?.resources?.forEach { resource ->
                                if (resource == PermissionRequest.RESOURCE_AUDIO_CAPTURE) {
                                    request.grant(arrayOf(PermissionRequest.RESOURCE_AUDIO_CAPTURE))
                                    return
                                }
                            }
                            request?.grant(request.resources)
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

        // Native Quick Prompt Floating Bar & Slash Command Shortcuts
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Horizontal Slash Command Pills
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(slashCommands) { cmd ->
                    Surface(
                        onClick = {
                            quickPromptText = if (quickPromptText.isBlank()) "$cmd " else "$quickPromptText $cmd "
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                        tonalElevation = 2.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text(
                            text = cmd,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = AntigravityCyan,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Input Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = quickPromptText,
                        onValueChange = { quickPromptText = it },
                        placeholder = { Text("Prompt agent or type slash command...", fontSize = 13.sp) },
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
                                val script = "window.__antigravity_send_prompt && window.__antigravity_send_prompt('${quickPromptText.replace("'", "\\'")}');"
                                webViewInstance?.evaluateJavascript(script, null)
                                quickPromptText = ""
                            }
                        })
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Voice Mode button (Gemini Live)
                    IconButton(
                        onClick = {
                            val script = "window.__antigravity_toggle_voice && window.__antigravity_toggle_voice();"
                            webViewInstance?.evaluateJavascript(script, null)
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Mode",
                            tint = AntigravityCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Send Button
                    IconButton(
                        onClick = {
                            if (quickPromptText.isNotBlank()) {
                                val script = "window.__antigravity_send_prompt && window.__antigravity_send_prompt('${quickPromptText.replace("'", "\\'")}');"
                                webViewInstance?.evaluateJavascript(script, null)
                                quickPromptText = ""
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(AntigravityViolet)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
