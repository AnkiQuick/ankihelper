package com.mmjang.ankihelper.data.ai.cache;

import org.litepal.crud.LitePalSupport;

public class AIDictionaryCache extends LitePalSupport {
    private long id;
    private String hwd; // Headword (the word being defined)
    private String phrase; // Fixed phrase with special meaning or usage (empty for single words)
    private String sense; // Part of speech (verb, noun, adjective, adverb, etc.) - empty for phrases
    private String phonetics; // British and American English phonetics (e.g., "UK: /ɡʊd/ US: /ɡʊd/")
    private String defEn; // English definition
    private String defCn; // Chinese definition
    private String example; // Example sentence
    private long llmConfigId; // Foreign key to LLMConfig
    private long timestamp; // For expiration
    
    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getHwd() {
        return hwd;
    }

    public void setHwd(String hwd) {
        this.hwd = hwd;
    }

    public String getPhrase() {
        return phrase;
    }

    public void setPhrase(String phrase) {
        this.phrase = phrase;
    }

    public String getSense() {
        return sense;
    }

    public void setSense(String sense) {
        this.sense = sense;
    }

    public String getPhonetics() {
        return phonetics;
    }

    public void setPhonetics(String phonetics) {
        this.phonetics = phonetics;
    }

    public String getDefEn() {
        return defEn;
    }

    public void setDefEn(String defEn) {
        this.defEn = defEn;
    }

    public String getDefCn() {
        return defCn;
    }

    public void setDefCn(String defCn) {
        this.defCn = defCn;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
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
    
    // Same structure as built-in dictionary tables
    // Each row represents one definition/meaning, so multiple rows for words with multiple meanings
}