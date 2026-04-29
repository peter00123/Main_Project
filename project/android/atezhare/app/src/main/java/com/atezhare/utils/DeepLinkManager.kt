package com.atezhare.utils

import android.content.Intent
import android.net.Uri

object DeepLinkManager {
    private val PAIR_CODE_REGEX = Regex("^/R(\\d{6})$")

    fun extractPairCode(intent: Intent?): String? {
        val data: Uri = intent?.data ?: return null
        
        // Support both legacy atezhare:// and new https://atezhare.com/
        val scheme = data.scheme
        val host = data.host
        val path = data.path ?: ""

        return when {
            scheme == "https" && host == "atezhare.com" -> {
                val matchResult = PAIR_CODE_REGEX.find(path)
                matchResult?.groupValues?.get(1)
            }
            scheme == "atezhare" -> {
                // Legacy support for atezhare://R123456 (where R123456 is host)
                val legacyRegex = Regex("^R(\\d{6})$")
                val legacyMatch = legacyRegex.find(host ?: "")
                legacyMatch?.groupValues?.get(1)
            }
            else -> null
        }
    }
    
    fun generateDeepLink(pairCode: String): String {
        return "https://atezhare.com/R$pairCode"
    }
}
