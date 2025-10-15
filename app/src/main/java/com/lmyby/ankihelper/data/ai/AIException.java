package com.lmyby.ankihelper.data.ai;

import org.json.JSONObject;

/**
 * Custom exception class for AI-related errors
 */
public class AIException extends Exception {
    private AIErrorType errorType;
    private Throwable cause;
    
    public AIException(AIErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }
    
    public AIException(AIErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
        this.cause = cause;
    }
    
    public AIErrorType getErrorType() {
        return errorType;
    }
    
    @Override
    public Throwable getCause() {
        return cause;
    }
    
    /**
     * Converts the exception to a JSON error response
     * 
     * @return JSON string representation of the error
     */
    public String toJsonResponse() {
        try {
            JSONObject errorResponse = new JSONObject();
            JSONObject errorObj = new JSONObject();
            
            errorObj.put("type", errorType.name().toLowerCase());
            errorObj.put("message", getMessage());
            errorObj.put("details", cause != null ? cause.getMessage() : "");
            
            errorResponse.put("error", errorObj);
            
            return errorResponse.toString();
        } catch (Exception e) {
            return "{\"error\":{\"type\":\"unknown\",\"message\":\"Failed to serialize error\"}}";
        }
    }
}