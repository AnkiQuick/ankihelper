package com.mmjang.ankihelper.data.ai;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "aitranslatorconfig",
    foreignKeys = @ForeignKey(
        entity = LLMConfig.class,
        parentColumns = "id",
        childColumns = "llmId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = @Index("llmId")
)
public class AITranslatorConfig {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "translatorName")
    private String translatorName;

    @ColumnInfo(name = "llmId")
    private long llmId; // Foreign key to LLMConfig

    private String prompt;

    @ColumnInfo(name = "isDefault")
    private boolean isDefault;

    @ColumnInfo(name = "sourceLanguage")
    private String sourceLanguage;

    @ColumnInfo(name = "targetLanguage")
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