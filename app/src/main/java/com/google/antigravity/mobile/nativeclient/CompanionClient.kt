package com.google.antigravity.mobile.nativeclient

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class CompanionClient(endpoint: String, private val token: String) {
    private val base = endpoint.trim().trimEnd('/').toHttpUrl().also {
        require(it.isHttps && it.username.isEmpty() && it.password.isEmpty() &&
            it.encodedPath == "/" && it.query == null && it.fragment == null) {
            "Enter the companion's HTTPS address without a path."
        }
        require(token.length >= 32 && !token.contains('\n') && !token.contains('\r')) {
            "Paste the complete pairing token from your computer."
        }
    }
    // Never send the pairing token to a redirect destination or weaken TLS validation.
    private val http = OkHttpClient.Builder().followRedirects(false).followSslRedirects(false)
        .connectTimeout(10, TimeUnit.SECONDS).readTimeout(75, TimeUnit.SECONDS).build()
    suspend fun request(path: String, body: JSONObject? = null): JSONObject = withContext(Dispatchers.IO) {
        require(path in setOf("state", "connect", "prompt", "permission", "cancel"))
        val request = Request.Builder().url(base.newBuilder().addPathSegment(path).build())
            .header("Authorization", "Bearer $token")
        if (body != null) request.post(body.toString().toRequestBody("application/json".toMediaType()))
        http.newCall(request.build()).execute().use { response ->
            val json = runCatching { JSONObject(response.body?.string().orEmpty()) }.getOrElse {
                throw IllegalStateException("The companion returned an invalid response.")
            }
            check(response.isSuccessful) {
                json.optString("error", "Connection failed (${response.code}).")
            }
            json
        }
    }
}
