package com.lmyby.ankihelper.data.ai.cache;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import com.lmyby.ankihelper.data.ai.LLMConfig;

@Entity(
    tableName = "aitranslatorcache",
    foreignKeys = @ForeignKey(
        entity = LLMConfig.class,
        parentColumns = "id",
        childColumns = "llmConfigId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("llmConfigId"), @Index("sourceText")}
)
public class AITranslatorCache {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "sourceText")
    private String sourceText; // The text to be translated

    @ColumnInfo(name = "sourceLanguage")
    private String sourceLanguage;

    @ColumnInfo(name = "targetLanguage")
    private String targetLanguage;

    @ColumnInfo(name = "translatedText")
    private String translatedText; // The translated result

    @ColumnInfo(name = "llmConfigId")
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