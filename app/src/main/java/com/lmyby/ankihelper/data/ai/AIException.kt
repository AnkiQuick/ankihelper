package com.lmyby.ankihelper.data.ai

import org.json.JSONObject

/**
 * Custom exception class for AI-related errors
 * Converted to Kotlin as part of Phase 2 data model migration
 */
class AIException : Exception {
    val errorType: AIErrorType
    override val cause: Throwable?

    constructor(errorType: AIErrorType, message: String) : super(message) {
        this.errorType = errorType
        this.cause = null
    }

    constructor(errorType: AIErrorType, message: String, cause: Throwable) : super(message, cause) {
        this.errorType = errorType
        this.cause = cause
    }

    /**
     * Converts the exception to a JSON error response
     *
     * @return JSON string representation of the error
     */
    fun toJsonResponse(): String {
        return try {
            val errorResponse = JSONObject()
            val errorObj = JSONObject()

            errorObj.put("type", errorType.name.lowercase())
            errorObj.put("message", message)
            errorObj.put("details", cause?.message ?: "")

            errorResponse.put("error", errorObj)

            errorResponse.toString()
        } catch (e: Exception) {
            "{\"error\":{\"type\":\"unknown\",\"message\":\"Failed to serialize error\"}}"
        }
    }
}
