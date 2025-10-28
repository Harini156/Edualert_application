package com.saveetha.edualert

import android.net.Uri

object UrlUtils {
    fun resolveAttachmentUrl(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        val trimmed = raw.trim()
        
        // If it's already a complete URL, return as is
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed
        }

        // Get base URL from ApiClient
        val baseUrl = ApiClient.BASE_URL
        
        // Use the file access endpoint for better file serving
        val fileAccessUrl = if (baseUrl.endsWith("/")) {
            "${baseUrl}api/get_file.php?file=$trimmed"
        } else {
            "$baseUrl/api/get_file.php?file=$trimmed"
        }
        
        android.util.Log.d("UrlUtils", "Resolving: '$raw' -> '$fileAccessUrl'")
        return fileAccessUrl
    }
}


