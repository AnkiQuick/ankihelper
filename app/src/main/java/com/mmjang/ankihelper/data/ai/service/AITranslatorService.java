package com.mmjang.ankihelper.data.ai.service;

import android.util.Log;

import com.mmjang.ankihelper.data.ai.AITranslatorConfig;
import com.mmjang.ankihelper.data.ai.LLMConfig;
import com.mmjang.ankihelper.data.ai.cache.AITranslatorCache;
import com.mmjang.ankihelper.data.ai.cache.AICacheRepository;

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
                               AITranslatorConfig config, LLMConfig llmConfig) throws IOException {
        // Check cache first
        AITranslatorCache cachedResult = AICacheRepository.getTranslatorCache(text, sourceLanguage, 
                targetLanguage, llmConfig.getId());
        if (cachedResult != null) {
            Log.d(TAG, "Returning cached translation for text: " + text);
            return cachedResult.getTranslatedText();
        }
        
        // Prepare the prompt
        String prompt = config.getPrompt()
                .replace("{sourceLanguage}", sourceLanguage)
                .replace("{targetLanguage}", targetLanguage)
                .replace("{text}", text);
        
        // Call the LLM
        String response = aiService.callLLM(llmConfig, prompt);
        
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
    
    private String parseTranslationResponse(String response) throws IOException {
        try {
            // Parse the JSON response
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray choices = jsonResponse.getJSONArray("choices");
            if (choices.length() > 0) {
                JSONObject choice = choices.getJSONObject(0);
                JSONObject message = choice.getJSONObject("message");
                String content = message.getString("content");
                
                // Parse the content as JSON object
                JSONObject translation = new JSONObject(content);
                return translation.getString("translatedText");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing translation response", e);
            throw new IOException("Error parsing translation response", e);
        }
        
        throw new IOException("Empty translation response");
    }
}