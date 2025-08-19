package com.mmjang.ankihelper.data.ai.service;

import android.util.Log;

import com.mmjang.ankihelper.data.ai.AIDictionaryConfig;
import com.mmjang.ankihelper.data.ai.LLMConfig;
import com.mmjang.ankihelper.data.ai.cache.AIDictionaryCache;
import com.mmjang.ankihelper.data.ai.cache.AICacheRepository;
import com.mmjang.ankihelper.data.ai.AIException;
import com.mmjang.ankihelper.data.ai.AIErrorType;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AIDictionaryService {
    private static final String TAG = "AIDictionaryService";
    private AIService aiService;

    public AIDictionaryService() {
        this.aiService = new AIService();
    }

    public List<AIDictionaryCache> getWordDefinition(String word, AIDictionaryConfig config, LLMConfig llmConfig)
            throws IOException, AIException {
        Log.d(TAG, "Starting word definition lookup for: " + word);

        try {
            // Check cache first
            List<AIDictionaryCache> cachedResults = AICacheRepository.getDictionaryCache(word, llmConfig.getId());
            if (!cachedResults.isEmpty()) {
                Log.d(TAG, "Returning cached results for word: " + word + ", count: " + cachedResults.size());
                return cachedResults;
            }
        } catch (Exception e) {
            Log.w(TAG, "Error checking cache for word: " + word + ", continuing with LLM call", e);
        }

        Log.d(TAG, "No cached results found for word: " + word + ", calling LLM");

        // Prepare the system and user messages
        String systemMessage = "You are an experienced dictionary assistant. Your task is to provide accurate and " +
            "comprehensive definitions for words and phrases. You should be able to handle complex " +
            "queries and provide detailed explanations. Your responses should be clear, concise, " +
            "and easy to understand. IMPORTANT: You MUST respond with valid JSON format. " +
            "Your response should be a JSON object with a 'definitions' array containing definition objects. " +
            "Each definition object should have: 'hwd', 'phrase', 'sense', 'phonetics', 'def_en', 'def_cn', and 'example' fields."
            +
            "hwd: the key word to look up. " +
            "phrase: the phase that the word belong to. if not empty, the definition_cn and definition_en wiil be definition of the phase. "
            +
            "sense: is the Part of Speech, which refers to the grammatical category a word belongs to based on its function within a sentence, Common POS categories in English include nouns, verbs, adjectives, adverbs, pronouns, prepositions, conjunctions, and interjections"
            +
            "phonetics: contain both English and American English phonetics";
        String userMessage = "Please provide the definitions of the word or phrase \"" + word + "\" in JSON format " +
            "with a 'definitions' array containing definition objects. Each definition should have: " +
            "'headword', 'phrase', 'sense', 'phonetics', 'def_en', 'def_cn', and 'example' fields.";

        Log.d(TAG, "Calling LLM with system message: " + systemMessage);
        Log.d(TAG, "Calling LLM with user message: " + userMessage);

        // Call the LLM with system and user messages
        String response = aiService.callLLM(llmConfig, systemMessage, userMessage);

        Log.d(TAG, "Received response from LLM: " + response);

        // Parse the response
        List<AIDictionaryCache> results = parseDictionaryResponse(response, word, llmConfig.getId());

        Log.d(TAG, "Parsed " + results.size() + " results from response");

        try {
            // Cache the results
            for (AIDictionaryCache result : results) {
                result.setTimestamp(System.currentTimeMillis());
                AICacheRepository.saveDictionaryCache(result);
            }
            Log.d(TAG, "Saved " + results.size() + " results to cache");
        } catch (Exception e) {
            Log.w(TAG, "Error saving results to cache for word: " + word + ", continuing without caching", e);
        }

        return results;
    }

    private List<AIDictionaryCache> parseDictionaryResponse(String response, String word, long llmConfigId)
            throws IOException, AIException {
        List<AIDictionaryCache> results = new ArrayList<>();

        try {
            Log.d(TAG, "Attempting to parse LLM response: " + response);

            // Try to parse the JSON response
            JSONObject jsonResponse = new JSONObject(response);

            // Check if response is an error
            if (jsonResponse.has("error")) {
                JSONObject errorObj = jsonResponse.getJSONObject("error");
                handleErrorResponse(errorObj);
                return results; // Should not reach here as handleErrorResponse throws exception
            }

            // Handle different response formats
            if (jsonResponse.has("choices")) {
                // Standard OpenAI-style response
                JSONArray choices = jsonResponse.getJSONArray("choices");
                if (choices.length() > 0) {
                    JSONObject choice = choices.getJSONObject(0);
                    String content = "";

                    // Try different ways to get content
                    if (choice.has("message")) {
                        JSONObject message = choice.getJSONObject("message");
                        content = message.getString("content");
                    } else if (choice.has("text")) {
                        content = choice.getString("text");
                    } else {
                        // Try to get content directly from choice
                        content = choice.toString();
                    }

                    Log.d(TAG, "Extracted content from LLM response: " + content);
                    parseContent(content, results, word, llmConfigId);
                }
            } else if (jsonResponse.has("content")) {
                // Direct content response
                String content = jsonResponse.getString("content");
                parseContent(content, results, word, llmConfigId);
            } else if (jsonResponse.has("text")) {
                // Direct text response
                String content = jsonResponse.getString("text");
                parseContent(content, results, word, llmConfigId);
            } else {
                // Try to parse the entire response as content
                parseContent(response, results, word, llmConfigId);
            }

        } catch (AIException e) {
            throw e; // Re-throw AI exceptions
        } catch (Exception e) {
            Log.e(TAG, "Error parsing dictionary response. Raw response: " + response, e);
            // Try to create a basic result from the raw response
            try {
                AIDictionaryCache cache = new AIDictionaryCache();
                cache.setHwd(word);
                cache.setDefEn("Response from AI: " + response.substring(0, Math.min(200, response.length())));
                cache.setLlmConfigId(llmConfigId);
                cache.setTimestamp(System.currentTimeMillis());
                results.add(cache);
                Log.w(TAG, "Created fallback result from raw response");
            } catch (Exception fallbackE) {
                Log.e(TAG, "Error creating fallback result", fallbackE);
            }
            throw new AIException(AIErrorType.INVALID_RESPONSE,
                "Error parsing dictionary response. Raw response: " + response, e);
        }

        Log.d(TAG, "Successfully parsed " + results.size() + " results");
        return results;
    }

    private void parseContent(String content, List<AIDictionaryCache> results, String word, long llmConfigId)
            throws Exception {
        Log.d(TAG, "Parsing content: " + content);

        if (content == null || content.trim().isEmpty()) {
            Log.w(TAG, "Empty content received from LLM");
            return;
        }

        // Clean up markdown formatting if present
        String cleanedContent = cleanMarkdownFormatting(content);
        Log.d(TAG, "Cleaned content: " + cleanedContent);

        // Try to parse content as JSON
        try {
            // First try to parse as JSON object
            JSONObject contentJson = new JSONObject(cleanedContent);
            Log.d(TAG, "Successfully parsed content as JSON object. Keys: " + contentJson.keys().toString());

            if (contentJson.has("definitions")) {
                // Handle the case where content contains a definitions array
                JSONArray definitions = contentJson.getJSONArray("definitions");
                Log.d(TAG, "Found definitions array with " + definitions.length() + " items");
                parseDefinitionsArray(definitions, results, word, llmConfigId);
                Log.d(TAG, "Parsed " + definitions.length() + " definitions from definitions array");
            } else {
                // Check if it's a single definition object
                if (contentJson.has("headword") || contentJson.has("def_en") || contentJson.has("defEn")) {
                    JSONArray definitions = new JSONArray();
                    definitions.put(contentJson);
                    parseDefinitionsArray(definitions, results, word, llmConfigId);
                    Log.d(TAG, "Parsed single definition object");
                } else {
                    // Treat as a generic JSON object and convert to string definition
                    AIDictionaryCache cache = new AIDictionaryCache();
                    cache.setHwd(word);
                    cache.setDefEn(contentJson.toString());
                    cache.setLlmConfigId(llmConfigId);
                    cache.setTimestamp(System.currentTimeMillis());
                    results.add(cache);
                    Log.d(TAG, "Treated content as generic JSON object");
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "Content is not a JSON object, trying as JSON array", e);
            // If not JSON object, try as JSON array
            try {
                JSONArray contentArray = new JSONArray(cleanedContent);
                Log.d(TAG, "Successfully parsed content as JSON array with " + contentArray.length() + " items");
                parseDefinitionsArray(contentArray, results, word, llmConfigId);
                Log.d(TAG, "Parsed " + contentArray.length() + " definitions from JSON array");
            } catch (Exception arrayE) {
                Log.d(TAG, "Content is not a JSON array, treating as plain text", arrayE);
                // If not JSON at all, treat as plain text
                Log.d(TAG, "Treating content as plain text");
                AIDictionaryCache cache = new AIDictionaryCache();
                cache.setHwd(word);
                cache.setDefEn(cleanedContent);
                cache.setLlmConfigId(llmConfigId);
                cache.setTimestamp(System.currentTimeMillis());
                results.add(cache);
            }
        }
    }

    /**
     * Cleans up markdown formatting from LLM responses
     * Removes leading/trailing whitespace, markdown code block markers, and other formatting
     *
     * @param content The raw content from LLM
     * @return Cleaned content ready for JSON parsing
     */
    private String cleanMarkdownFormatting(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        String cleaned = content.trim();

        // Remove leading word if it's just the search term (common with some LLMs)
        if (cleaned.startsWith("Watch")) {
            // Check if the rest is JSON or markdown-wrapped JSON
            String rest = cleaned.substring(5).trim();
            if (rest.startsWith("```json") || rest.startsWith("```")) {
                cleaned = rest;
            }
        }

        // Remove markdown code block markers
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7); // Remove ```json
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3); // Remove ```
        }

        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3); // Remove trailing ```
        }

        // Remove any remaining leading/trailing whitespace
        cleaned = cleaned.trim();

        Log.d(TAG, "Cleaned markdown formatting. Original: " + content + " | Cleaned: " + cleaned);

        return cleaned;
    }

    private void parseDefinitionsArray(JSONArray definitions, List<AIDictionaryCache> results,
                                      String word, long llmConfigId) throws Exception {
        for (int i = 0; i < definitions.length(); i++) {
            JSONObject definition = definitions.getJSONObject(i);

            AIDictionaryCache cache = new AIDictionaryCache();
            cache.setHwd(definition.optString("headword", word));
            cache.setPhrase(definition.optString("phrase", ""));
            cache.setSense(definition.optString("sense", ""));
            cache.setPhonetics(definition.optString("phonetics", ""));
            cache.setDefEn(definition.optString("def_en", definition.optString("defEn", "")));
            cache.setDefCn(definition.optString("def_cn", definition.optString("defCn", "")));
            cache.setExample(definition.optString("example", ""));
            cache.setLlmConfigId(llmConfigId);

            results.add(cache);
        }
    }

    private void handleErrorResponse(JSONObject errorObj) throws AIException {
        String errorType = errorObj.optString("type", "unknown");
        String errorMessage = errorObj.optString("message", "Unknown error");

        AIErrorType type = AIErrorType.UNKNOWN;
        try {
            type = AIErrorType.valueOf(errorType.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Use default UNKNOWN
        }

        throw new AIException(type, errorMessage);
    }
}