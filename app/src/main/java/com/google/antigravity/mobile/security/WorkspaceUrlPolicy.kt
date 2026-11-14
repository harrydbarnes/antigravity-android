package com.google.antigravity.mobile.security

import java.net.URI

/** Only launch service URLs, never arbitrary destinations supplied by remote data. */
object WorkspaceUrlPolicy {
    fun isAllowed(url: String): Boolean = runCatching {
        val uri = URI(url)
        uri.scheme.equals("https", ignoreCase = true) &&
            uri.host.equals("antigravity.google.com", ignoreCase = true) &&
            uri.rawUserInfo == null && uri.port in listOf(-1, 443)
    }.getOrDefault(false)
}
