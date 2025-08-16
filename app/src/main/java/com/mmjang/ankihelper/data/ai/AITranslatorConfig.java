package com.mmjang.ankihelper.data.ai;

import org.litepal.crud.LitePalSupport;

public class AITranslatorConfig extends LitePalSupport {
    private long id;
    private String translatorName;
    private long llmId; // Foreign key to LLMConfig
    private String prompt;
    private boolean isDefault;
    private String sourceLanguage;
    private String targetLanguage;
    
    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTranslatorName() {
        return translatorName;
    }

    public void setTranslatorName(String translatorName) {
        this.translatorName = translatorName;
    }

    public long getLlmId() {
        return llmId;
    }

    public void setLlmId(long llmId) {
        this.llmId = llmId;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
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
}