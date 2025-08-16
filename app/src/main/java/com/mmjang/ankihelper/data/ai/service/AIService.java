package com.mmjang.ankihelper.data.ai.service;

import android.util.Log;

import com.mmjang.ankihelper.data.ai.LLMConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AIService {
    private static final String TAG = "AIService";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private OkHttpClient client;
    
    public AIService() {
        this.client = new OkHttpClient();
    }
    
    public String callLLM(LLMConfig config, String prompt) throws IOException {
        // Decrypt the API token
        String apiToken = config.getApiToken();
        if (apiToken != null && !apiToken.isEmpty()) {
            apiToken = com.mmjang.ankihelper.data.ai.EncryptionUtil.decrypt(apiToken);
        }
        
        // Create the request body
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model", config.getModelName());
            jsonBody.put("messages", new JSONObject().put("role", "user").put("content", prompt));
            jsonBody.put("temperature", 0.7);
        } catch (Exception e) {
            Log.e(TAG, "Error creating JSON body", e);
            throw new IOException("Error creating request body", e);
        }
        
        // Create the request
        Request request = new Request.Builder()
                .url(config.getBaseUrl())
                .addHeader("Authorization", "Bearer " + apiToken)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(jsonBody.toString(), JSON))
                .build();
        
        // Execute the request
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }
            
            String responseBody = response.body().string();
            Log.d(TAG, "LLM Response: " + responseBody);
            return responseBody;
        }
    }
}