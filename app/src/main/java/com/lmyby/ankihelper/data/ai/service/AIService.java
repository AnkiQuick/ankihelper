package com.lmyby.ankihelper.data.ai.service;

import android.util.Log;

import com.lmyby.ankihelper.data.ai.LLMConfig;
import com.lmyby.ankihelper.data.ai.AIException;
import com.lmyby.ankihelper.data.ai.AIErrorType;
import com.lmyby.ankihelper.data.ai.EncryptionUtil;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AIService {
    private static final String TAG = "AIService";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final int DEFAULT_TIMEOUT_SECONDS = 30;
    private static final int MAX_RETRIES = 3;
    
    private OkHttpClient client;
    
    public AIService() {
        this(DEFAULT_TIMEOUT_SECONDS);
    }
    
    public AIService(int timeoutSeconds) {
        this.client = new OkHttpClient.Builder()
            .connectTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .writeTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .build();
    }
    
    /**
     * Calls the LLM API with a single prompt (backward compatibility)
     * 
     * @param config LLM configuration
     * @param prompt User prompt
     * @return API response as string
     * @throws IOException if network error occurs
     * @throws AIException if timeout or API error occurs
     */
    public String callLLM(LLMConfig config, String prompt) throws IOException, AIException {
        return callLLM(config, "You are a helpful assistant.", prompt);
    }
    
    /**
     * Calls the LLM API with system and user messages
     * 
     * @param config LLM configuration
     * @param systemMessage System role message
     * @param userMessage User role message
     * @return API response as string
     * @throws IOException if network error occurs
     * @throws AIException if timeout or API error occurs
     */
    public String callLLM(LLMConfig config, String systemMessage, String userMessage) 
            throws IOException, AIException {
        return callLLMWithRetry(config, systemMessage, userMessage, MAX_RETRIES);
    }
    
    /**
     * Calls the LLM API with retry mechanism
     * 
     * @param config LLM configuration
     * @param systemMessage System role message
     * @param userMessage User role message
     * @param maxRetries Maximum number of retries
     * @return API response as string
     * @throws IOException if network error occurs
     * @throws AIException if timeout or API error occurs
     */
    public String callLLMWithRetry(LLMConfig config, String systemMessage, String userMessage, 
                                  int maxRetries) throws IOException, AIException {
        String apiToken = config.getApiToken();
        if (apiToken != null && !apiToken.isEmpty()) {
            apiToken = EncryptionUtil.decrypt(apiToken);
        }
        
        // Create messages array
        JSONArray messages = new JSONArray();
        
        try {
            // Add system message
            JSONObject systemMsg = new JSONObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemMessage);
            messages.put(systemMsg);
            
            // Add user message
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.put(userMsg);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON messages", e);
            throw new AIException(AIErrorType.INVALID_RESPONSE, "Error creating request messages", e);
        }
        
        // Create the request body
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model", config.getModelName());
            jsonBody.put("messages", messages);
            jsonBody.put("temperature", 0.3);
            jsonBody.put("max_tokens", 1000);
            
            // Add response format for structured output (now default for all providers)
            // This ensures consistent behavior across all LLM providers
            JSONObject responseFormat = new JSONObject();
            responseFormat.put("type", "json_object");
            jsonBody.put("response_format", responseFormat);
            Log.d(TAG, "Added response_format parameter for structured output");
            
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON body", e);
            throw new AIException(AIErrorType.INVALID_RESPONSE, "Error creating request body", e);
        }
        
        // Get the full API URL with chat completion endpoint
        String apiUrl = config.getChatCompletionUrl();
        if (apiUrl == null || apiUrl.isEmpty()) {
            throw new AIException(AIErrorType.API_ERROR, "Invalid base URL in LLM configuration");
        }
        
        Log.d(TAG, "Calling LLM API at URL: " + apiUrl);
        Log.d(TAG, "Request body: " + jsonBody.toString());
        Log.d(TAG, "Model name: " + config.getModelName());
        Log.d(TAG, "API token present: " + (apiToken != null && !apiToken.isEmpty()));
        
        // Mask the API token for security in logs (show first 4 chars if available)
        String maskedToken = "NOT_SET";
        if (apiToken != null && !apiToken.isEmpty()) {
            if (apiToken.length() > 4) {
                maskedToken = apiToken.substring(0, 4) + "...";
            } else {
                maskedToken = "***";
            }
        }
        Log.d(TAG, "API token (masked): " + maskedToken);
        
        // Create the request
        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer " + apiToken)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(jsonBody.toString(), JSON))
                .build();
        
        // Execute the request with retries
        IOException lastException = null;
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                long startTime = System.currentTimeMillis();
                try (Response response = client.newCall(request).execute()) {
                    long endTime = System.currentTimeMillis();
                    Log.d(TAG, "API call took " + (endTime - startTime) + "ms");
                    
                    if (!response.isSuccessful()) {
                        Log.e(TAG, "API call failed with code: " + response.code() + ", message: " + response.message());
                        handleErrorResponse(response);
                    }
                    
                    String responseBody = response.body().string();
                    Log.d(TAG, "LLM Response: " + responseBody);
                    return responseBody;
                }
            } catch (SocketTimeoutException e) {
                lastException = new IOException("Request timeout", e);
                Log.w(TAG, "Timeout on attempt " + (attempt + 1) + "/" + (maxRetries + 1), e);
                
                if (attempt < maxRetries) {
                    // Exponential backoff
                    long delay = (long) Math.pow(2, attempt) * 1000; // 1s, 2s, 4s, etc.
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Interrupted during retry delay", ie);
                    }
                }
            } catch (IOException e) {
                lastException = e;
                Log.w(TAG, "IO error on attempt " + (attempt + 1) + "/" + (maxRetries + 1), e);
                
                if (attempt < maxRetries) {
                    // Linear backoff for non-timeout errors
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Interrupted during retry delay", ie);
                    }
                }
            }
        }
        
        // If we get here, all retries failed
        throw new AIException(AIErrorType.TIMEOUT, "API call failed after " + (maxRetries + 1) + " attempts", lastException);
    }
    
    /**
     * Handles HTTP error responses
     * 
     * @param response HTTP response
     * @throws IOException with error details
     * @throws AIException for specific AI error types
     */
    private void handleErrorResponse(Response response) throws IOException, AIException {
        String errorBody = response.body() != null ? response.body().string() : "";
        String errorMessage = "API error " + response.code() + ": " + response.message();
        
        Log.e(TAG, "API Error - Code: " + response.code() + ", Message: " + response.message() + 
              ", Body: " + errorBody);
        
        AIErrorType errorType = AIErrorType.API_ERROR;
        if (response.code() == 400) {
            errorType = AIErrorType.API_ERROR;
            errorMessage = "Bad Request (400): " + response.message() + 
                          ". Please check your LLM configuration including model name, API key, and base URL. " +
                          "Response body: " + errorBody;
        } else if (response.code() == 401) {
            errorType = AIErrorType.API_ERROR;
            errorMessage = "Unauthorized (401): Invalid API key or authentication failed. " +
                          "Please check your API key in the LLM configuration.";
        } else if (response.code() == 404) {
            errorType = AIErrorType.API_ERROR;
            errorMessage = "Not Found (404): The requested endpoint was not found. " +
                          "Please check your base URL and endpoint configuration.";
        } else if (response.code() == 429) {
            errorType = AIErrorType.RATE_LIMIT;
            errorMessage = "Rate limit exceeded. Please try again later.";
        } else if (response.code() >= 500) {
            errorType = AIErrorType.SERVER_ERROR;
            errorMessage = "Server error. Please try again later.";
        }
        
        throw new AIException(errorType, errorMessage);
    }
}