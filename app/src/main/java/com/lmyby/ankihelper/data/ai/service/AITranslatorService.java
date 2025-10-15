package com.lmyby.ankihelper.data.ai.service;

import android.util.Log;

import com.lmyby.ankihelper.data.ai.AITranslatorConfig;
import com.lmyby.ankihelper.data.ai.LLMConfig;
import com.lmyby.ankihelper.data.ai.cache.AITranslatorCache;
import com.lmyby.ankihelper.data.ai.cache.AICacheRepository;
import com.lmyby.ankihelper.data.ai.AIException;
import com.lmyby.ankihelper.data.ai.AIErrorType;

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
        "and idioms. Your responses should be clear, concise, and easy to understand. Key Points: Accuracy is Paramount; Fluent and Natural Writing; Standardized Terminology. IMPORTANT: You MUST respond with valid JSON format. Your response should be a JSON object with a 'translation' map containing translation object, which contains translatedText, sourceLanguage, targetLanguage";
    String userMessage = "Please provide the translation of the text \"" + text + "\" from " +
        sourceLanguage + " to " + targetLanguage + ".";

    // Call the LLM with system and user messages
    String response = aiService.callLLM(llmConfig, systemMessage, userMessage);
    Log.e(TAG, "Raw LLM response for translation: " + response);

    // Parse the response
    JSONObject parsedResponse = parseTranslationResponse(response);
    String translatedText = parsedResponse.optString("translatedText",text);
    String parsedSourceLanguage = parsedResponse.optString("sourceLanguage",sourceLanguage);
    String parsedTargetLanguage = parsedResponse.optString("targetLanguage",targetLanguage);

    // Cache the result
    AITranslatorCache cache = new AITranslatorCache();
    cache.setSourceText(text);
    cache.setSourceLanguage(parsedSourceLanguage);
    cache.setTargetLanguage(parsedTargetLanguage);
    cache.setTranslatedText(translatedText);
    cache.setLlmConfigId(llmConfig.getId());
    cache.setTimestamp(System.currentTimeMillis());
    AICacheRepository.saveTranslatorCache(cache);

    return translatedText;
  }

  private JSONObject parseTranslationResponse(String response) throws IOException, AIException {
    JSONObject translationObject = new JSONObject();
    try {
      // Clean up markdown formatting if present

      Log.d(TAG, "Attempting to parse LLM response: " + response);

      // Try to parse the JSON response
      JSONObject jsonResponse = new JSONObject(response);

      // Check if response is an error
      if (jsonResponse.has("error")) {
          JSONObject errorObj = jsonResponse.getJSONObject("error");
          handleErrorResponse(errorObj);
          return translationObject; // Should not reach here as handleErrorResponse throws exception
      }

      if (jsonResponse.has("choices")){
        JSONArray choices = jsonResponse.getJSONArray("choices");
       if (choices.length() > 0) {
          JSONObject choice = choices.getJSONObject(0);
          if (choice.has("message")) {
            JSONObject message = choice.getJSONObject("message");
            if (message.has("content")) {
              String content = message.getString("content");
              String cleanedContent = cleanMarkdownFormatting(content);
              JSONObject cleanedContentObject = new JSONObject(cleanedContent);
              if (cleanedContentObject.has("translation") ){
                 translationObject = cleanedContentObject.getJSONObject("translation");
              }
            }
          }
        }
      }else if (jsonResponse.has("content")) {
          // Direct content response
          String content = jsonResponse.getString("content");
          String cleanedContent = cleanMarkdownFormatting(content);
          JSONObject cleanedContentObject = new JSONObject(cleanedContent);
          if (cleanedContentObject.has("translation") ){
             translationObject = cleanedContentObject.getJSONObject("translation");
          }
      } else if (jsonResponse.has("text")) {
          // Direct text response
          String content = jsonResponse.getString("text");
          String cleanedContent = cleanMarkdownFormatting(content);
          JSONObject cleanedContentObject = new JSONObject(cleanedContent);
          if (cleanedContentObject.has("translation") ){
             translationObject = cleanedContentObject.getJSONObject("translation");
          }
      } else {
          // Try to parse the entire response as content
          translationObject = translationObject.getJSONObject("translation");
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

    // Handle case where content is wrapped in triple quotes ("""json""" or just """)
    if (cleaned.startsWith("\"\"\"") && cleaned.endsWith("\"\"\"")) {
      cleaned = cleaned.substring(3, cleaned.length() - 3); // Remove surrounding triple quotes
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

