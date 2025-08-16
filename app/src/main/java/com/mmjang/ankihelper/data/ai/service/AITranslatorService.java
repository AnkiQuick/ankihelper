package com.mmjang.ankihelper.data.ai.service;

import android.util.Log;

import com.mmjang.ankihelper.data.ai.AITranslatorConfig;
import com.mmjang.ankihelper.data.ai.LLMConfig;
import com.mmjang.ankihelper.data.ai.cache.AITranslatorCache;
import com.mmjang.ankihelper.data.ai.cache.AICacheRepository;
import com.mmjang.ankihelper.data.ai.AIException;
import com.mmjang.ankihelper.data.ai.AIErrorType;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class AITranslatorService {
    private static final String TAG = "AITranslatorService";
    private AIService aiService;
    
    public AITranslatorService() {
        this.aiService = new AIService();
    }
    
    public String translateText(String text, String sourceLanguage, String targetLanguage, 
                               AITranslatorConfig config, LLMConfig llmConfig) 
            throws IOException, AIException {
        // Check cache first
        AITranslatorCache cachedResult = AICacheRepository.getTranslatorCache(text, sourceLanguage, 
                targetLanguage, llmConfig.getId());
        if (cachedResult != null) {
            Log.d(TAG, "Returning cached translation for text: " + text);
            return cachedResult.getTranslatedText();
        }
        
        // Prepare the system and user messages
        String systemMessage = "You are an experienced translator. Your task is to translate text from one language " +
            "to another accurately and fluently. You should be able to handle complex sentences " +
            "and idioms. Your responses should be clear, concise, and easy to understand.";
        String userMessage = "Please provide the translation of the text \"" + text + "\" from " + 
            sourceLanguage + " to " + targetLanguage + ".";
        
        // Call the LLM with system and user messages
        String response = aiService.callLLM(llmConfig, systemMessage, userMessage);
        
        // Parse the response
        String translatedText = parseTranslationResponse(response);
        
        // Cache the result
        AITranslatorCache cache = new AITranslatorCache();
        cache.setSourceText(text);
        cache.setSourceLanguage(sourceLanguage);
        cache.setTargetLanguage(targetLanguage);
        cache.setTranslatedText(translatedText);
        cache.setLlmConfigId(llmConfig.getId());
        cache.setTimestamp(System.currentTimeMillis());
        AICacheRepository.saveTranslatorCache(cache);
        
        return translatedText;
    }
    
    private String parseTranslationResponse(String response) throws IOException, AIException {
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
                    JSONObject translation = new JSONObject(content);
                    return translation.getString("translated_text");
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing translation response content", e);
                    throw new AIException(AIErrorType.INVALID_RESPONSE, 
                        "Invalid response format from AI service", e);
                }
            }
        } catch (AIException e) {
            throw e; // Re-throw AI exceptions
        } catch (Exception e) {
            Log.e(TAG, "Error parsing translation response", e);
            throw new AIException(AIErrorType.INVALID_RESPONSE, 
                "Error parsing translation response", e);
        }
        
        throw new AIException(AIErrorType.INVALID_RESPONSE, "Empty translation response");
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