package com.mmjang.ankihelper.data.ai.cache;

import android.util.Log;

import org.litepal.LitePal;
import org.litepal.LitePalDB;
import org.litepal.Operator;

import java.util.ArrayList;
import java.util.List;

public class AICacheRepository {
    
    // AI cache database will be initialized in MyApplication
    // This avoids potential conflicts with multiple LitePal.use() calls
    
    // AI Dictionary Cache methods
    public static List<AIDictionaryCache> getDictionaryCache(String word, long llmConfigId) {
        Log.d("AICacheRepository", "Querying cache for word: " + word + ", llmConfigId: " + llmConfigId);
        try {
            // Check if we can access the database
            Log.d("AICacheRepository", "Current database: " + LitePal.getDatabase().getPath());
            
            List<AIDictionaryCache> results = LitePal.where("hwd = ? and llmConfigId = ?", word, String.valueOf(llmConfigId))
                    .find(AIDictionaryCache.class);
            Log.d("AICacheRepository", "Found " + results.size() + " cached results for word: " + word);
            return results;
        } catch (Exception e) {
            Log.e("AICacheRepository", "Error querying cache for word: " + word, e);
            // Return empty list on error
            return new ArrayList<>();
        }
    }
    
    public static void saveDictionaryCache(AIDictionaryCache cache) {
        try {
            Log.d("AICacheRepository", "Saving cache entry for word: " + cache.getHwd());
            cache.save();
            Log.d("AICacheRepository", "Successfully saved cache entry for word: " + cache.getHwd());
        } catch (Exception e) {
            Log.e("AICacheRepository", "Error saving cache entry for word: " + cache.getHwd(), e);
        }
    }
    
    public static void clearExpiredDictionaryCache(long expirationTime) {
        LitePal.deleteAll(AIDictionaryCache.class, "timestamp < ?", String.valueOf(expirationTime));
    }
    
    public static int deleteOldestDictionaryCache(int count) {
        List<AIDictionaryCache> oldestRecords = LitePal.order("timestamp asc").limit(count).find(AIDictionaryCache.class);
        int deletedCount = 0;
        for (AIDictionaryCache cache : oldestRecords) {
            int result = cache.delete();
            if (result > 0) {
                deletedCount++;
            }
        }
        return deletedCount;
    }
    
    public static int deleteAllDictionaryCache() {
        return LitePal.deleteAll(AIDictionaryCache.class);
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
    
    public static int deleteOldestTranslatorCache(int count) {
        List<AITranslatorCache> oldestRecords = LitePal.order("timestamp asc").limit(count).find(AITranslatorCache.class);
        int deletedCount = 0;
        for (AITranslatorCache cache : oldestRecords) {
            int result = cache.delete();
            if (result > 0) {
                deletedCount++;
            }
        }
        return deletedCount;
    }
    
    public static int deleteAllTranslatorCache() {
        return LitePal.deleteAll(AITranslatorCache.class);
    }
}