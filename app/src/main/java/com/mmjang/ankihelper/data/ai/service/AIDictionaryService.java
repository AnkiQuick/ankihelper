package com.mmjang.ankihelper.data.ai.service;

import android.util.Log;

import com.mmjang.ankihelper.data.ai.AIDictionaryConfig;
import com.mmjang.ankihelper.data.ai.LLMConfig;
import com.mmjang.ankihelper.data.ai.cache.AIDictionaryCache;
import com.mmjang.ankihelper.data.ai.cache.AICacheRepository;

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
            throws IOException {
        // Check cache first
        List<AIDictionaryCache> cachedResults = AICacheRepository.getDictionaryCache(word, llmConfig.getId());
        if (!cachedResults.isEmpty()) {
            Log.d(TAG, "Returning cached results for word: " + word);
            return cachedResults;
        }
        
        // Prepare the prompt
        String prompt = config.getPrompt().replace("{query}", word);
        
        // Call the LLM
        String response = aiService.callLLM(llmConfig, prompt);
        
        // Parse the response
        List<AIDictionaryCache> results = parseDictionaryResponse(response, word, llmConfig.getId());
        
        // Cache the results
        for (AIDictionaryCache result : results) {
            result.setTimestamp(System.currentTimeMillis());
            AICacheRepository.saveDictionaryCache(result);
        }
        
        return results;
    }
    
    private List<AIDictionaryCache> parseDictionaryResponse(String response, String word, long llmConfigId) 
            throws IOException {
        List<AIDictionaryCache> results = new ArrayList<>();
        
        try {
            // Parse the JSON response
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray choices = jsonResponse.getJSONArray("choices");
            if (choices.length() > 0) {
                JSONObject choice = choices.getJSONObject(0);
                JSONObject message = choice.getJSONObject("message");
                String content = message.getString("content");
                
                // Parse the content as JSON array
                JSONArray definitions = new JSONArray(content);
                
                for (int i = 0; i < definitions.length(); i++) {
                    JSONObject definition = definitions.getJSONObject(i);
                    
                    AIDictionaryCache cache = new AIDictionaryCache();
                    cache.setHwd(definition.optString("hwd", word));
                    cache.setPhrase(definition.optString("phrase", ""));
                    cache.setSense(definition.optString("sense", ""));
                    cache.setPhonetics(definition.optString("phonetics", ""));
                    cache.setDefEn(definition.optString("defEn", ""));
                    cache.setDefCn(definition.optString("defCn", ""));
                    cache.setExample(definition.optString("example", ""));
                    cache.setLlmConfigId(llmConfigId);
                    
                    results.add(cache);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing dictionary response", e);
            throw new IOException("Error parsing dictionary response", e);
        }
        
        return results;
    }
}