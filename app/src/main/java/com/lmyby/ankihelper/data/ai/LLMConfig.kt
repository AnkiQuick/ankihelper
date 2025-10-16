package com.lmyby.ankihelper.data.ai

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Converted to Kotlin as part of Phase 2 data model migration
 */
@Entity(tableName = "llmconfig")
data class LLMConfig(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var name: String? = null,
    var baseUrl: String? = null,
    var apiToken: String? = null, // Encrypted
    var modelName: String? = null,
    var endpointPath: String? = null // Optional endpoint path, defaults to "/v1/chat/completions"
) {
    /**
     * Gets the full chat completion API URL by appending the endpoint to the base URL
     * @return Full API URL for chat completions
     */
    fun getChatCompletionUrl(): String? {
        var baseUrl = this.baseUrl
        if (baseUrl.isNullOrEmpty()) {
            return null
        }

        // Remove trailing slash if present
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length - 1)
        }

        // Use custom endpoint path if provided, otherwise use default
        var endpoint = this.endpointPath
        if (endpoint.isNullOrEmpty()) {
            endpoint = "/v1/chat/completions" // Default OpenAI-style endpoint
        }

        // Ensure endpoint starts with "/"
        if (!endpoint.startsWith("/")) {
            endpoint = "/$endpoint"
        }

        return baseUrl + endpoint
    }
}
