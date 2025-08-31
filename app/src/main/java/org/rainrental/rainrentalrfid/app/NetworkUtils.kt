package org.rainrental.rainrentalrfid.app

import java.net.NetworkInterface
import java.util.*

object NetworkUtils {
    
    /**
     * Constructs a proper URL by ensuring there's exactly one slash between the base URL and the path.
     * This handles cases where the base URL may or may not have a trailing slash.
     * 
     * @param baseUrl The base URL (e.g., "https://example.com" or "https://example.com/")
     * @param path The path to append (e.g., "api/v1/endpoint" or "/api/v1/endpoint")
     * @return A properly constructed URL with exactly one slash between base and path
     */
    fun constructUrl(baseUrl: String, path: String): String {
        val cleanBaseUrl = baseUrl.trimEnd('/')
        val cleanPath = path.trimStart('/')
        
        return "$cleanBaseUrl/$cleanPath"
    }
    
    /**
     * Validates that a URL is properly formatted.
     * 
     * @param url The URL to validate
     * @return true if the URL appears to be valid, false otherwise
     */
    fun validateUrl(url: String): Boolean {
        return try {
            url.startsWith("http://") || url.startsWith("https://")
        } catch (e: Exception) {
            false
        }
    }
} 