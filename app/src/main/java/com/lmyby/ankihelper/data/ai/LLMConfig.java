package com.lmyby.ankihelper.data.ai;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "llmconfig")
public class LLMConfig {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String name;
    private String baseUrl;
    private String apiToken; // Encrypted
    private String modelName;
    private String endpointPath; // Optional endpoint path, defaults to "/v1/chat/completions"
    
    // Getters and setters
    public long getId() {
        return id;
        }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
    
    public String getEndpointPath() {
        return endpointPath;
    }
    
    public void setEndpointPath(String endpointPath) {
        this.endpointPath = endpointPath;
    }
    
    /**
     * Gets the full chat completion API URL by appending the endpoint to the base URL
     * @return Full API URL for chat completions
     */
    public String getChatCompletionUrl() {
        String baseUrl = this.baseUrl;
        if (baseUrl == null || baseUrl.isEmpty()) {
            return null;
        }
        
        // Remove trailing slash if present
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        
        // Use custom endpoint path if provided, otherwise use default
        String endpoint = this.endpointPath;
        if (endpoint == null || endpoint.isEmpty()) {
            endpoint = "/v1/chat/completions"; // Default OpenAI-style endpoint
        }
        
        // Ensure endpoint starts with "/"
        if (!endpoint.startsWith("/")) {
            endpoint = "/" + endpoint;
        }
        
        return baseUrl + endpoint;
    }
}