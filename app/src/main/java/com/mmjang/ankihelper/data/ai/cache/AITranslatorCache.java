package com.mmjang.ankihelper.data.ai.cache;

import org.litepal.crud.LitePalSupport;

public class AITranslatorCache extends LitePalSupport {
    private long id;
    private String sourceText; // The text to be translated
    private String sourceLanguage;
    private String targetLanguage;
    private String translatedText; // The translated result
    private long llmConfigId; // Foreign key to LLMConfig
    private long timestamp; // For expiration
    
    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSourceText() {
        return sourceText;
    }

    public void setSourceText(String sourceText) {
        this.sourceText = sourceText;
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }

    public long getLlmConfigId() {
        return llmConfigId;
    }

    public void setLlmConfigId(long llmConfigId) {
        this.llmConfigId = llmConfigId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}