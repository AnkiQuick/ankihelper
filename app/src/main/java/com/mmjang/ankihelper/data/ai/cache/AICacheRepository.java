package com.mmjang.ankihelper.data.ai.cache;

import org.litepal.LitePal;
import org.litepal.LitePalDB;
import org.litepal.Operator;

import java.util.List;

public class AICacheRepository {
    
    // Initialize the AI cache database
    static {
        LitePalDB aiDB = LitePalDB.fromDefault("ai_cache");
        LitePal.use(aiDB);
    }
    
    // AI Dictionary Cache methods
    public static List<AIDictionaryCache> getDictionaryCache(String word, long llmConfigId) {
        return LitePal.where("hwd = ? and llmConfigId = ?", word, String.valueOf(llmConfigId))
                .find(AIDictionaryCache.class);
    }
    
    public static void saveDictionaryCache(AIDictionaryCache cache) {
        cache.save();
    }
    
    public static void clearExpiredDictionaryCache(long expirationTime) {
        LitePal.deleteAll(AIDictionaryCache.class, "timestamp < ?", String.valueOf(expirationTime));
    }
    
    // AI Translator Cache methods
    public static AITranslatorCache getTranslatorCache(String sourceText, String sourceLanguage, 
                                                       String targetLanguage, long llmConfigId) {
        List<AITranslatorCache> results = LitePal.where("sourceText = ? and sourceLanguage = ? and targetLanguage = ? and llmConfigId = ?", 
                sourceText, sourceLanguage, targetLanguage, String.valueOf(llmConfigId))
                .find(AITranslatorCache.class);
        return results.isEmpty() ? null : results.get(0);
    }
    
    public static void saveTranslatorCache(AITranslatorCache cache) {
        cache.save();
    }
    
    public static void clearExpiredTranslatorCache(long expirationTime) {
        LitePal.deleteAll(AITranslatorCache.class, "timestamp < ?", String.valueOf(expirationTime));
    }
}