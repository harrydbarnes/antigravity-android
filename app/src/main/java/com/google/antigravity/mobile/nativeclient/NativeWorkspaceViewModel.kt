package com.google.antigravity.mobile.nativeclient

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

data class ChatMessage(val role: String, val text: String)
data class PermissionOption(val id: String, val name: String)
data class NativePermission(val id: String, val title: String, val details: String, val options: List<PermissionOption>)
data class WorkspaceState(
    val paired: Boolean = false, val connecting: Boolean = false, val connected: Boolean = false,
    val busy: Boolean = false, val messages: List<ChatMessage> = emptyList(),
    val permissions: List<NativePermission> = emptyList(), val error: String = "",
    val workspace: String = ""
)

class NativeWorkspaceViewModel(application: Application) : AndroidViewModel(application) {
    private val store = CompanionStore(application)
    private var client: CompanionClient? = null
    private var generation = 0
    private val _state = MutableStateFlow(WorkspaceState())
    val state = _state.asStateFlow()
    init {
        store.load()?.let { (endpoint, token) -> pair(endpoint, token) }
    }
    fun pair(endpoint: String, token: String) {
        if (_state.value.connecting) return
        val current = ++generation
        _state.value = _state.value.copy(connecting = true, error = "")
        viewModelScope.launch {
            try {
                val next = CompanionClient(endpoint, token.trim())
                val result = next.request("connect", JSONObject())
                if (generation != current) return@launch
                store.save(endpoint.trim(), token.trim())
                client = next
                update(result)
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                if (generation == current) _state.value = _state.value.copy(
                    connecting = false, error = e.message ?: "Could not connect to your computer.")
            }
        }
    }
    fun reconnect() {
        store.load()?.let { (endpoint, token) -> pair(endpoint, token) }
    }
    suspend fun refresh() {
        val active = client ?: return
        val current = generation
        try {
            val result = active.request("state")
            if (current == generation) update(result)
        } catch (e: CancellationException) { throw e
        } catch (e: Exception) {
            if (current == generation) _state.value = _state.value.copy(
                connected = false, permissions = emptyList(), error = e.message ?: "Computer disconnected.")
        }
    }
    private fun update(json: JSONObject) {
        val messages = json.optJSONArray("messages")
        val permissions = json.optJSONArray("permissions")
        _state.value = WorkspaceState(paired = true,
            connected = json.optString("sessionId").isNotBlank(), busy = json.optBoolean("busy"),
            workspace = json.optString("workspace"), error = json.optString("error"),
            messages = (0 until (messages?.length() ?: 0)).map { index ->
                val message = messages!!.getJSONObject(index)
                ChatMessage(message.getString("role"), message.getString("text"))
            }, permissions = (0 until (permissions?.length() ?: 0)).map { index ->
                val permission = permissions!!.getJSONObject(index)
                val options = permission.getJSONArray("options")
                NativePermission(permission.getString("id"), permission.getString("title"), permission.optString("details"),
                    (0 until options.length()).map { optionIndex ->
                        val option = options.getJSONObject(optionIndex)
                        PermissionOption(option.getString("optionId"), option.getString("name"))
                    })
            })
    }
    private fun action(path: String, body: JSONObject, onAccepted: () -> Unit = {}) {
        val active = client ?: return
        val current = generation
        viewModelScope.launch {
            try {
                val result = active.request(path, body)
                if (current == generation) { update(result); onAccepted() }
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                if (current == generation) _state.value = _state.value.copy(
                    error = e.message ?: "The action could not be confirmed. Check the workspace before retrying.")
            }
        }
    }
    fun send(text: String, onAccepted: () -> Unit) {
        if (!_state.value.connected || _state.value.busy || text.isBlank()) return
        _state.value = _state.value.copy(busy = true)
        action("prompt", JSONObject().put("text", text), onAccepted)
    }
    fun answer(permission: NativePermission, option: PermissionOption) {
        action("permission", JSONObject().put("id", permission.id).put("optionId", option.id))
    }
    fun cancel() { action("cancel", JSONObject()) }
    fun forget() {
        generation++
        client = null
        store.clear()
        _state.value = WorkspaceState()
    }
}
