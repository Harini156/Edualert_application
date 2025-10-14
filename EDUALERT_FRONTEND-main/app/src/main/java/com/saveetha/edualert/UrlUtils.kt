package com.saveetha.edualert

import android.net.Uri

object UrlUtils {
    fun resolveAttachmentUrl(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        val trimmed = raw.trim()
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            // If it's already absolute but points to a different host, normalize to our BASE_URL
            val uri = Uri.parse(trimmed)
            val path = uri.path ?: return trimmed
            // If it contains EDUALERT/api/uploads, rebuild against our BASE_URL
            if (path.contains("/EDUALERT/api/uploads/") || path.contains("/api/uploads/")) {
                val base = ApiClient::class.java.getDeclaredField("BASE_URL").let {
                    it.isAccessible = true
                    it.get(null) as String
                }
                val baseWithApi = if (base.endsWith("/")) base + "api/" else "$base/api/"
                val afterApi = path.substringAfter("/api/")
                return baseWithApi + afterApi.trimStart('/')
            }
            return trimmed
        }

        val base = ApiClient::class.java.getDeclaredField("BASE_URL").let {
            it.isAccessible = true
            it.get(null) as String
        }
        val baseWithApi = if (base.endsWith("/")) base + "api/" else "$base/api/"

        val encodedPath = Uri.encode(trimmed, "/:_-.")
        return baseWithApi + encodedPath
    }
}


