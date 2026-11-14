package com.google.antigravity.mobile.nativeclient

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

@Composable
fun NativeWorkspaceScreen(model: NativeWorkspaceViewModel = viewModel()) {
    val state by model.state.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var endpoint by rememberSaveable { mutableStateOf("") }
    // A pairing secret must not enter saved-instance-state or keyboard suggestions.
    var token by remember { mutableStateOf("") }
    var prompt by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()
    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) { model.refresh(); delay(1000) }
        }
    }
    LaunchedEffect(state.messages.size, state.messages.lastOrNull()?.text?.length) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.lastIndex)
    }
    Column(Modifier.fillMaxSize().safeDrawingPadding().imePadding().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Counterweight", style = MaterialTheme.typography.headlineMedium)
        if (!state.paired) {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Connect your Antigravity computer")
                Text("Sign in to Antigravity once on your computer, start the companion, then paste its HTTPS address and pairing token. Google login stays on your computer; chat stays native on your phone.")
                OutlinedTextField(endpoint, { endpoint = it }, label = { Text("Companion HTTPS address") },
                    singleLine = true, enabled = !state.connecting, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(token, { token = it }, label = { Text("Pairing token") },
                    visualTransformation = PasswordVisualTransformation(), singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(autoCorrect = false),
                    enabled = !state.connecting, modifier = Modifier.fillMaxWidth())
                Button(onClick = { model.pair(endpoint, token) }, enabled = !state.connecting) {
                    Text(if (state.connecting) "Connecting…" else "Pair computer")
                }
                TextButton(onClick = model::reconnect, enabled = !state.connecting) { Text("Retry saved connection") }
            }
        } else {
            LaunchedEffect(Unit) { token = "" }
            Text(if (state.connected) "Connected to ${state.workspace}" else "Computer disconnected",
                style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!state.connected) Button(onClick = model::reconnect, enabled = !state.connecting) { Text("Reconnect") }
                TextButton(onClick = model::forget, enabled = !state.busy) { Text("Forget computer") }
                if (state.busy) TextButton(onClick = model::cancel) { Text("Stop") }
            }
            SelectionContainer(Modifier.weight(1f)) {
                LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.messages) { message ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Text(if (message.role == "user") "You" else "Antigravity",
                                    style = MaterialTheme.typography.labelMedium)
                                Text(message.text)
                            }
                        }
                    }
                }
            }
            state.permissions.firstOrNull()?.let { permission ->
                Text("Approval required: ${permission.title}")
                SelectionContainer {
                    androidx.compose.foundation.lazy.LazyColumn(Modifier.heightIn(max = 160.dp)) {
                        item { Text(permission.details, style = MaterialTheme.typography.bodySmall) }
                    }
                }
                permission.options.forEach { option ->
                    OutlinedButton(onClick = { model.answer(permission, option) }, enabled = state.connected) {
                        Text(option.name)
                    }
                }
                Text("Unanswered requests expire after two minutes.", style = MaterialTheme.typography.bodySmall)
            }
            OutlinedTextField(prompt, { prompt = it }, label = { Text("Message Antigravity") },
                modifier = Modifier.fillMaxWidth(), maxLines = 4, enabled = state.connected)
            Button(onClick = { val sent = prompt; model.send(sent) { if (prompt == sent) prompt = "" } },
                enabled = state.connected && !state.busy && prompt.isNotBlank()) {
                Text(if (state.busy) "Working…" else "Send")
            }
        }
        if (state.error.isNotEmpty()) Text(state.error, color = MaterialTheme.colorScheme.error)
    }
}
