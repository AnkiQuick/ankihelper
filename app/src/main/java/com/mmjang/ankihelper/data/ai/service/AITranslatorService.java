package com.mmjang.ankihelper.data.ai.service;

import android.util.Log;

import com.mmjang.ankihelper.data.ai.AITranslatorConfig;
import com.mmjang.ankihelper.data.ai.LLMConfig;
import com.mmjang.ankihelper.data.ai.cache.AITranslatorCache;
import com.mmjang.ankihelper.data.ai.cache.AICacheRepository;
import com.mmjang.ankihelper.data.ai.AIException;
import com.mmjang.ankihelper.data.ai.AIErrorType;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class AITranslatorService {
  private static final String TAG = "AITranslatorService";
  private AIService aiService;

  public AITranslatorService() {
    this.aiService = new AIService();
  }

  public String translateText(String text, String sourceLanguage, String targetLanguage,
      AITranslatorConfig config, LLMConfig llmConfig)
      throws IOException, AIException {
    // Check cache first
    AITranslatorCache cachedResult = AICacheRepository.getTranslatorCache(text, sourceLanguage,
        targetLanguage, llmConfig.getId());
    if (cachedResult != null) {
      Log.d(TAG, "Returning cached translation for text: " + text);
      return cachedResult.getTranslatedText();
    }

    // Prepare the system and user messages
    String systemMessage = "You are an experienced translator. Your task is to translate text from one language " +
        "to another accurately and fluently. You should be able to handle complex sentences " +
        "and idioms. Your responses should be clear, concise, and easy to understand.IMPORTANT: You MUST respond with valid JSON format. Your response should be a JSON object with a 'translation' map containing translation object, which contains translatedText, sourceLanguage, targetLanguage";
    String userMessage = "Please provide the translation of the text \"" + text + "\" from " +
        sourceLanguage + " to " + targetLanguage + ".";

    // Call the LLM with system and user messages
    String response = aiService.callLLM(llmConfig, systemMessage, userMessage);
    Log.e(TAG, "Raw LLM response for translation: " + response);

    // Parse the response
    JSONObject parsedResponse = parseTranslationResponse(response);
    String translatedText = parsedResponse.optString("translatedText", parsedResponse.optString("translatedText", ""));
    String parsedSourceLanguage = parsedResponse.optString("sourceLanguage",
        parsedResponse.optString("sourceLanguage", sourceLanguage));
    String parsedTargetLanguage = parsedResponse.optString("targetLanguage",
        parsedResponse.optString("targetLanguage", targetLanguage));

    // Cache the result
    AITranslatorCache cache = new AITranslatorCache();
    cache.setSourceText(text);
    cache.setSourceLanguage(parsedSourceLanguage);
    cache.setTargetLanguage(parsedTargetLanguage);
    cache.setTranslatedText(translatedText);
    cache.setLlmConfigId(llmConfig.getId());
    cache.setTimestamp(System.currentTimeMillis());
    AICacheRepository.saveTranslatorCache(cache);

    return response;
  }

  private JSONObject parseTranslationResponse(String response) throws IOException, AIException {
    try {
      // Clean up markdown formatting if present
      String cleanedContent = cleanMarkdownFormatting(response);

      // Parse the cleaned content as JSON
      JSONObject jsonResponse = new JSONObject(cleanedContent);

      // Check if response is an error (if the LLM returns an error in the content
      // itself)
      if (jsonResponse.has("error")) {
        handleErrorResponse(jsonResponse.getJSONObject("error"));
      }

      // Get the nested 'translation' object
      JSONObject translationObject = jsonResponse.getJSONObject("translation");

      // Validate required fields within the nested object
      if (!translationObject.has("translatedText") && !translationObject.has("translated_text")) {
        throw new AIException(AIErrorType.INVALID_RESPONSE,
            "Missing 'translatedText' or 'translated_text' in nested translation object");
      }
      if (!translationObject.has("sourceLanguage") && !translationObject.has("source_language")) {
        throw new AIException(AIErrorType.INVALID_RESPONSE,
            "Missing 'sourceLanguage' or 'source_language' in nested translation object");
      }
      if (!translationObject.has("targetLanguage") && !translationObject.has("target_language")) {
        throw new AIException(AIErrorType.INVALID_RESPONSE,
            "Missing 'targetLanguage' or 'target_language' in nested translation object");
      }

      return translationObject;
    } catch (AIException e) {
      throw e; // Re-throw AI exceptions
    } catch (Exception e) {
      Log.e(TAG, "Error parsing translation response", e);
      throw new AIException(AIErrorType.INVALID_RESPONSE,
          "Error parsing translation response", e);
    }
  }

  private String cleanMarkdownFormatting(String content) {
    if (content == null || content.isEmpty()) {
      return content;
    }

    String cleaned = content.trim();

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

    return cleaned;
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