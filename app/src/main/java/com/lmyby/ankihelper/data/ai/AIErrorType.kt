package com.lmyby.ankihelper.data.ai

/**
 * Enum representing different types of AI-related errors
 * Converted to Kotlin as part of Phase 2 data model migration
 */
enum class AIErrorType(val description: String) {
    TIMEOUT("Request timed out"),
    NETWORK_ERROR("Network connectivity issue"),
    API_ERROR("API returned an error"),
    RATE_LIMIT("Rate limit exceeded"),
    SERVER_ERROR("Server-side error"),
    INVALID_RESPONSE("Invalid response format"),
    UNKNOWN("Unknown error")
}
