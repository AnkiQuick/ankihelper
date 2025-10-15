package com.lmyby.ankihelper.data.ai;

/**
 * Enum representing different types of AI-related errors
 */
public enum AIErrorType {
    TIMEOUT("Request timed out"),
    NETWORK_ERROR("Network connectivity issue"),
    API_ERROR("API returned an error"),
    RATE_LIMIT("Rate limit exceeded"),
    SERVER_ERROR("Server-side error"),
    INVALID_RESPONSE("Invalid response format"),
    UNKNOWN("Unknown error");
    
    private final String description;
    
    AIErrorType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}