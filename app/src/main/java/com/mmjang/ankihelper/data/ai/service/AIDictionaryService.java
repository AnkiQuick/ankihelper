package com.mmjang.ankihelper.data.ai.service;

import android.util.Log;

import com.mmjang.ankihelper.data.ai.AIDictionaryConfig;
import com.mmjang.ankihelper.data.ai.LLMConfig;
import com.mmjang.ankihelper.data.ai.cache.AIDictionaryCache;
import com.mmjang.ankihelper.data.ai.cache.AICacheRepository;
import com.mmjang.ankihelper.data.ai.AIException;
import com.mmjang.ankihelper.data.ai.AIErrorType;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AIDictionaryService {
    private static final String TAG = "AIDictionaryService";
    private AIService aiService;
    
    public AIDictionaryService() {
        this.aiService = new AIService();
    }
    
    public List<AIDictionaryCache> getWordDefinition(String word, AIDictionaryConfig config, LLMConfig llmConfig) 
            throws IOException, AIException {
        Log.d(TAG, "Starting word definition lookup for: " + word);
        
        try {
            // Check cache first
            List<AIDictionaryCache> cachedResults = AICacheRepository.getDictionaryCache(word, llmConfig.getId());
            if (!cachedResults.isEmpty()) {
                Log.d(TAG, "Returning cached results for word: " + word + ", count: " + cachedResults.size());
                return cachedResults;
            }
        } catch (Exception e) {
            Log.w(TAG, "Error checking cache for word: " + word + ", continuing with LLM call", e);
        }
        
        Log.d(TAG, "No cached results found for word: " + word + ", calling LLM");
        
        // Prepare the system and user messages
        String systemMessage = "You are an experienced dictionary assistant. Your task is to provide accurate and " +
            "comprehensive definitions for words and phrases. You should be able to handle complex " +
            "queries and provide detailed explanations. Your responses should be clear, concise, " +
            "and easy to understand.";
        String userMessage = "Please provide the definitions of the word or phrase \"" + word + "\".";
        
        Log.d(TAG, "Calling LLM with system message: " + systemMessage);
        Log.d(TAG, "Calling LLM with user message: " + userMessage);
        
        // Call the LLM with system and user messages
        String response = aiService.callLLM(llmConfig, systemMessage, userMessage);
        
        Log.d(TAG, "Received response from LLM: " + response);
        
        // Parse the response
        List<AIDictionaryCache> results = parseDictionaryResponse(response, word, llmConfig.getId());
        
        Log.d(TAG, "Parsed " + results.size() + " results from response");
        
        try {
            // Cache the results
            for (AIDictionaryCache result : results) {
                result.setTimestamp(System.currentTimeMillis());
                AICacheRepository.saveDictionaryCache(result);
            }
            Log.d(TAG, "Saved " + results.size() + " results to cache");
        } catch (Exception e) {
            Log.w(TAG, "Error saving results to cache for word: " + word + ", continuing without caching", e);
        }
        
        return results;
    }
    
    private List<AIDictionaryCache> parseDictionaryResponse(String response, String word, long llmConfigId) 
            throws IOException, AIException {
        List<AIDictionaryCache> results = new ArrayList<>();
        
        try {
            // Parse the JSON response
            JSONObject jsonResponse = new JSONObject(response);
            
            // Check if response is an error
            if (jsonResponse.has("error")) {
                handleErrorResponse(jsonResponse.getJSONObject("error"));
            }
            
            JSONArray choices = jsonResponse.getJSONArray("choices");
            if (choices.length() > 0) {
                JSONObject choice = choices.getJSONObject(0);
                JSONObject message = choice.getJSONObject("message");
                String content = message.getString("content");
                
                // Try to parse content as JSON
                try {
                    JSONObject contentJson = new JSONObject(content);
                    if (contentJson.has("definitions")) {
                        JSONArray definitions = contentJson.getJSONArray("definitions");
                        parseDefinitionsArray(definitions, results, word, llmConfigId);
                    } else {
                        // Handle case where content is directly the definitions array
                        JSONArray definitions = new JSONArray(content);
                        parseDefinitionsArray(definitions, results, word, llmConfigId);
                    }
                } catch (Exception e) {
                    // If content is not JSON, try to parse as array directly
                    try {
                        JSONArray definitions = new JSONArray(content);
                        parseDefinitionsArray(definitions, results, word, llmConfigId);
                    } catch (Exception innerE) {
                        Log.e(TAG, "Error parsing dictionary response content", e);
                        throw new AIException(AIErrorType.INVALID_RESPONSE, 
                            "Invalid response format from AI service", e);
                    }
                }
            }
        } catch (AIException e) {
            throw e; // Re-throw AI exceptions
        } catch (Exception e) {
            Log.e(TAG, "Error parsing dictionary response", e);
            throw new AIException(AIErrorType.INVALID_RESPONSE, 
                "Error parsing dictionary response", e);
        }
        
        return results;
    }
    
    private void parseDefinitionsArray(JSONArray definitions, List<AIDictionaryCache> results, 
                                      String word, long llmConfigId) throws Exception {
        for (int i = 0; i < definitions.length(); i++) {
            JSONObject definition = definitions.getJSONObject(i);
            
            AIDictionaryCache cache = new AIDictionaryCache();
            cache.setHwd(definition.optString("headword", word));
            cache.setPhrase(definition.optString("phrase", ""));
            cache.setSense(definition.optString("sense", ""));
            cache.setPhonetics(definition.optString("phonetics", ""));
            cache.setDefEn(definition.optString("def_en", definition.optString("defEn", "")));
            cache.setDefCn(definition.optString("def_cn", definition.optString("defCn", "")));
            cache.setExample(definition.optString("example", ""));
            cache.setLlmConfigId(llmConfigId);
            
            results.add(cache);
        }
    }
    
    private void handleErrorResponse(JSONObject errorObj) throws AIException {
        String errorType = errorObj.optString("type", "unknown");
        String errorMessage = errorObj.optString("message", "Unknown error");
        
        AIErrorType type = AIErrorType.UNKNOWN;
        try {
            type = AIErrorType.valueOf(errorType.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Use default UNKNOWN
        }
        
        throw new AIException(type, errorMessage);
    }
}