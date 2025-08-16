package com.mmjang.ankihelper.data.ai;

import android.content.ContentValues;

import org.litepal.LitePal;

import java.util.List;

public class AIConfigRepository {
    
    // Default prompts
    public static final String DEFAULT_DICTIONARY_PROMPT = "You are an experienced dictionary assistant. Your task is to provide accurate and comprehensive definitions for words and phrases. You should be able to handle complex queries and provide detailed explanations. Your responses should be clear, concise, and easy to understand.\n\nPlease provide the definitions of the word or phrase \"{query}\" in the following JSON format:\n\n{\n  \"definitions\": [\n    {\n      \"headword\": \"headword (the main word being defined)\",\n      \"phrase\": \"fixed phrase containing the word (only for phrases, empty for single words)\",\n      \"sense\": \"part of speech (noun, verb, adjective, adverb, etc.) - empty for phrases\",\n      \"phonetics\": \"British and American English phonetics (format: 'UK: /pronunciation/ US: /pronunciation/')\",\n      \"def_en\": \"English definition\",\n      \"def_cn\": \"Chinese definition\",\n      \"example\": \"example sentence using the word or phrase\"\n    }\n  ]\n}\n\nGuidelines:\n1. For single words:\n   - Provide all meanings/definitions of the word\n   - For each word meaning:\n     * headword: the word itself\n     * phrase: empty (since it's not a phrase)\n     * sense: part of speech (noun, verb, etc.)\n     * phonetics: both UK and US phonetics (e.g., \"UK: /ɡʊd/ US: /ɡʊd/\")\n     * def_en/def_cn: definitions for this meaning\n     * example: example sentence for this meaning\n\n2. For phrases containing the word:\n   - If the word is part of common phrases or phrasal verbs, include these as separate entries\n   - For each phrase:\n     * headword: the original query word\n     * phrase: the complete phrase (e.g., \"word up\", \"break word\")\n     * sense: empty (phrases don't have part of speech)\n     * phonetics: empty (phrases don't have phonetics)\n     * def_en/def_cn: definitions of the phrase\n     * example: example sentence using the phrase\n\n3. Return all definitions and phrases for the given word/phrase\n4. Each entry will be stored as a separate row in the cache table";
    
    public static final String DEFAULT_TRANSLATOR_PROMPT = "You are an experienced translator. Your task is to translate text from one language to another accurately and fluently. You should be able to handle complex sentences and idioms. Your responses should be clear, concise, and easy to understand.\n\nPlease provide the translation of the text \"{text}\" from {sourceLanguage} to {targetLanguage} in the following JSON format:\n\n{\n  \"translated_text\": \"translated text\",\n  \"source_language\": \"{sourceLanguage}\",\n  \"target_language\": \"{targetLanguage}\"\n}";
    
    // LLM Config methods
    public static List<LLMConfig> getAllLLMConfigs() {
        return LitePal.findAll(LLMConfig.class);
    }
    
    public static LLMConfig getLLMConfigById(long id) {
        return LitePal.find(LLMConfig.class, id);
    }
    
    public static void saveLLMConfig(LLMConfig config) {
        // Encrypt the API token before saving
        if (config.getApiToken() != null && !config.getApiToken().isEmpty()) {
            config.setApiToken(EncryptionUtil.encrypt(config.getApiToken()));
        }
        config.save();
    }
    
    public static void deleteLLMConfig(long id) {
        LitePal.delete(LLMConfig.class, id);
    }
    
    // TTS Config methods
    public static List<TTSConfig> getAllTTSConfigs() {
        return LitePal.findAll(TTSConfig.class);
    }
    
    public static TTSConfig getTTSConfigById(long id) {
        return LitePal.find(TTSConfig.class, id);
    }
    
    public static void saveTTSConfig(TTSConfig config) {
        // Encrypt the API token before saving
        if (config.getApiToken() != null && !config.getApiToken().isEmpty()) {
            config.setApiToken(EncryptionUtil.encrypt(config.getApiToken()));
        }
        config.save();
    }
    
    public static void deleteTTSConfig(long id) {
        LitePal.delete(TTSConfig.class, id);
    }
    
    // AI Dictionary Config methods
    public static List<AIDictionaryConfig> getAllAIDictionaryConfigs() {
        return LitePal.findAll(AIDictionaryConfig.class);
    }
    
    public static AIDictionaryConfig getAIDictionaryConfigById(long id) {
        return LitePal.find(AIDictionaryConfig.class, id);
    }
    
    public static void saveAIDictionaryConfig(AIDictionaryConfig config) {
        config.save();
    }
    
    public static void deleteAIDictionaryConfig(long id) {
        LitePal.delete(AIDictionaryConfig.class, id);
    }
    
    // AI Translator Config methods
    public static List<AITranslatorConfig> getAllAITranslatorConfigs() {
        return LitePal.findAll(AITranslatorConfig.class);
    }
    
    public static AITranslatorConfig getDefaultAITranslatorConfig() {
        List<AITranslatorConfig> configs = LitePal.where("isDefault = ?", "1").find(AITranslatorConfig.class);
        return configs.isEmpty() ? null : configs.get(0);
    }
    
    public static AITranslatorConfig getAITranslatorConfigById(long id) {
        return LitePal.find(AITranslatorConfig.class, id);
    }
    
    public static void saveAITranslatorConfig(AITranslatorConfig config) {
        // If this is set as default, unset any existing default
        if (config.isDefault()) {
            ContentValues values = new ContentValues();
            values.put("isDefault", false);
            LitePal.updateAll(AITranslatorConfig.class, values, "isDefault = ?", "1");
        }
        config.save();
    }
    
    public static void deleteAITranslatorConfig(long id) {
        LitePal.delete(AITranslatorConfig.class, id);
    }
}