package com.mmjang.ankihelper.data.dict;

import android.content.Context;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ArrayAdapter;

import com.mmjang.ankihelper.data.ai.AIDictionaryConfig;
import com.mmjang.ankihelper.data.ai.LLMConfig;
import com.mmjang.ankihelper.data.ai.AIConfigRepository;
import com.mmjang.ankihelper.data.ai.service.AIDictionaryService;
import com.mmjang.ankihelper.data.ai.AIException;
import com.mmjang.ankihelper.data.ai.cache.AIDictionaryCache;
import com.mmjang.ankihelper.data.dict.Definition;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AIDictionary implements IDictionary {
    private AIDictionaryConfig config;
    private AIDictionaryService service;
    
    public AIDictionary(AIDictionaryConfig config) {
        this.config = config;
        this.service = new AIDictionaryService();
    }
    
    @Override
    public String getDictionaryName() {
        return config.getDictionaryName();
    }
    
    public String getDictionaryKey() {
        return "AI_" + config.getId();
    }
    
    @Override
    public String getIntroduction() {
        LLMConfig llmConfig = AIConfigRepository.getLLMConfigById(config.getLlmId());
        if (llmConfig != null) {
            return "AI Dictionary using " + llmConfig.getName() + " model";
        }
        return "AI Dictionary";
    }
    
    @Override
    public String[] getExportElementsList() {
        return new String[]{"Headword", "Part of Speech", "Phonetics", "Definition (English)", 
                           "Definition (Chinese)", "Example Sentence"};
    }
    
    @Override
    public List<Definition> wordLookup(String key) {
        List<Definition> definitions = new ArrayList<>();
        
        try {
            Log.d("AIDictionary", "Starting word lookup for: " + key);
            
            // Get the LLM config
            LLMConfig llmConfig = AIConfigRepository.getLLMConfigById(config.getLlmId());
            if (llmConfig == null) {
                Log.w("AIDictionary", "LLM config not found for config ID: " + config.getLlmId());
                return definitions;
            }
            
            Log.d("AIDictionary", "LLM config found: " + llmConfig.getName());
            
            // Get the word definition from the AI service
            List<AIDictionaryCache> cacheResults = service.getWordDefinition(key, config, llmConfig);
            
            Log.d("AIDictionary", "Received " + cacheResults.size() + " cache results");
            
            // Convert AIDictionaryCache results to Definition objects
            for (AIDictionaryCache cache : cacheResults) {
                // Create export elements map
                Map<String, String> exportElements = new HashMap<>();
                exportElements.put("Headword", cache.getHwd() != null ? cache.getHwd() : key);
                exportElements.put("Part of Speech", cache.getSense() != null ? cache.getSense() : "");
                exportElements.put("Phonetics", cache.getPhonetics() != null ? cache.getPhonetics() : "");
                exportElements.put("Definition (English)", cache.getDefEn() != null ? cache.getDefEn() : "");
                exportElements.put("Definition (Chinese)", cache.getDefCn() != null ? cache.getDefCn() : "");
                exportElements.put("Example Sentence", cache.getExample() != null ? cache.getExample() : "");
                
                // Create display HTML
                StringBuilder displayHtml = new StringBuilder();
                displayHtml.append("<b>").append(cache.getHwd() != null ? cache.getHwd() : key).append("</b>");
                if (cache.getPhonetics() != null && !cache.getPhonetics().isEmpty()) {
                    displayHtml.append(" ").append(cache.getPhonetics());
                }
                if (cache.getSense() != null && !cache.getSense().isEmpty()) {
                    displayHtml.append("<br/><i>").append(cache.getSense()).append("</i>");
                }
                if (cache.getDefEn() != null && !cache.getDefEn().isEmpty()) {
                    displayHtml.append("<br/>").append(cache.getDefEn());
                }
                if (cache.getDefCn() != null && !cache.getDefCn().isEmpty()) {
                    displayHtml.append("<br/>").append(cache.getDefCn());
                }
                if (cache.getExample() != null && !cache.getExample().isEmpty()) {
                    displayHtml.append("<br/><br/><i>").append(cache.getExample()).append("</i>");
                }
                
                // Create Definition object
                Definition def = new Definition(exportElements, displayHtml.toString());
                definitions.add(def);
            }
            
            Log.d("AIDictionary", "Returning " + definitions.size() + " definitions");
        } catch (AIException e) {
            Log.e("AIDictionary", "AI Error during dictionary lookup for word: " + key, e);
            // Create a Definition object to show the error to the user
            Map<String, String> exportElements = new HashMap<>();
            exportElements.put("Error", "AI Dictionary Error: " + e.getMessage());
            Definition errorDef = new Definition(exportElements, 
                "<b>AI Dictionary Error</b><br/>" + e.getMessage() + 
                "<br/><br/>Please check your LLM configuration and ensure the API is accessible.");
            definitions.add(errorDef);
        } catch (Exception e) {
            Log.e("AIDictionary", "Error during dictionary lookup for word: " + key, e);
            e.printStackTrace();
            // Create a Definition object to show the error to the user
            Map<String, String> exportElements = new HashMap<>();
            exportElements.put("Error", "Error: " + e.getMessage());
            Definition errorDef = new Definition(exportElements, 
                "<b>Error</b><br/>" + e.getMessage() + 
                "<br/><br/>Please check the logs for more details.");
            definitions.add(errorDef);
        }
        
        return definitions;
    }
    
    @Override
    public ListAdapter getAutoCompleteAdapter(Context context, int layout) {
        // Return a simple adapter with no autocomplete for AI dictionaries
        return new ArrayAdapter<>(context, layout, new String[]{});
    }
    
    public long getConfigId() {
        return config.getId();
    }
}