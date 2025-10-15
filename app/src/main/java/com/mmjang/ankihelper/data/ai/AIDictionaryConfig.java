package com.mmjang.ankihelper.data.ai;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "aidictionaryconfig",
    foreignKeys = @ForeignKey(
        entity = LLMConfig.class,
        parentColumns = "id",
        childColumns = "llmId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = @Index("llmId")
)
public class AIDictionaryConfig {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "dictionaryName")
    private String dictionaryName;

    @ColumnInfo(name = "llmId")
    private long llmId; // Foreign key to LLMConfig
    private String prompt;

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

    public String getDictionaryName() {
        return dictionaryName;
    }

    public void setDictionaryName(String dictionaryName) {
        this.dictionaryName = dictionaryName;
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