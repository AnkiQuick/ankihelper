package com.mmjang.ankihelper.data.ai;

import android.content.Context;
import android.widget.Toast;

import com.mmjang.ankihelper.MyApplication;
import com.mmjang.ankihelper.data.ai.cache.AIDictionaryCache;
import com.mmjang.ankihelper.data.ai.service.AIDictionaryService;
import com.mmjang.ankihelper.data.ai.service.AITranslatorService;

import java.io.IOException;
import java.util.List;

public class AIManager {
    private static AIManager instance;
    private AIDictionaryService dictionaryService;
    private AITranslatorService translatorService;
    
    private AIManager() {
        this.dictionaryService = new AIDictionaryService();
        this.translatorService = new AITranslatorService();
    }
    
    public static synchronized AIManager getInstance() {
        if (instance == null) {
            instance = new AIManager();
        }
        return instance;
    }
    
    public List<AIDictionaryCache> getWordDefinition(String word, long dictionaryConfigId) throws IOException, AIException {
        // Check network connectivity
        if (!NetworkUtil.isNetworkAvailable(MyApplication.getContext())) {
            throw new IOException("No network connection. Please check your internet connection and try again.");
        }
        
        AIDictionaryConfig dictionaryConfig = AIConfigRepository.getAIDictionaryConfigById(dictionaryConfigId);
        if (dictionaryConfig == null) {
            throw new IOException("AI Dictionary configuration not found");
        }
        
        LLMConfig llmConfig = AIConfigRepository.getLLMConfigById(dictionaryConfig.getLlmId());
        if (llmConfig == null) {
            throw new IOException("LLM configuration not found");
        }
        
        return dictionaryService.getWordDefinition(word, dictionaryConfig, llmConfig);
    }
    
    public String translateText(String text, long translatorConfigId) throws IOException, AIException {
        // Check network connectivity
        if (!NetworkUtil.isNetworkAvailable(MyApplication.getContext())) {
            throw new IOException("No network connection. Please check your internet connection and try again.");
        }
        
        AITranslatorConfig translatorConfig = AIConfigRepository.getAITranslatorConfigById(translatorConfigId);
        if (translatorConfig == null) {
            throw new IOException("AI Translator configuration not found");
        }
        
        LLMConfig llmConfig = AIConfigRepository.getLLMConfigById(translatorConfig.getLlmId());
        if (llmConfig == null) {
            throw new IOException("LLM configuration not found");
        }
        
        return translatorService.translateText(text, translatorConfig.getSourceLanguage(), 
                translatorConfig.getTargetLanguage(), translatorConfig, llmConfig);
    }
    
    public String translateTextWithDefaultTranslator(String text) throws IOException, AIException {
        // Check network connectivity
        if (!NetworkUtil.isNetworkAvailable(MyApplication.getContext())) {
            throw new IOException("No network connection. Please check your internet connection and try again.");
        }
        
        AITranslatorConfig translatorConfig = AIConfigRepository.getDefaultAITranslatorConfig();
        if (translatorConfig == null) {
            throw new IOException("No default AI Translator configuration found");
        }
        
        LLMConfig llmConfig = AIConfigRepository.getLLMConfigById(translatorConfig.getLlmId());
        if (llmConfig == null) {
            throw new IOException("LLM configuration not found");
        }
        
        return translatorService.translateText(text, translatorConfig.getSourceLanguage(), 
                translatorConfig.getTargetLanguage(), translatorConfig, llmConfig);
    }
}