# AI Service Technical Specification

This document provides detailed technical specifications for implementing the enhanced AI service with proper system/user messages, JSON schema responses, timeout handling, and error management.

## AIService Class Enhancement

### Updated Constructor
```java
public class AIService {
    private static final String TAG = "AIService";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final int DEFAULT_TIMEOUT_SECONDS = 30;
    private static final int MAX_RETRIES = 3;
    
    private OkHttpClient client;
    
    public AIService() {
        this(DEFAULT_TIMEOUT_SECONDS);
    }
    
    public AIService(int timeoutSeconds) {
        this.client = new OkHttpClient.Builder()
            .connectTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .writeTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .build();
    }
}
```

### Enhanced Content Parsing with Markdown Cleanup
```java
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
    
    // Remove leading words if they precede markdown (common with some LLMs)
    if (cleaned.startsWith("Watch")) {
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

/**
 * Parses LLM response content with enhanced markdown handling
 * 
 * @param content Raw content from LLM response
 * @param results List to populate with parsed dictionary entries
 * @param word The word being defined
 * @param llmConfigId ID of the LLM configuration used
 * @throws Exception if parsing fails
 */
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

### Enhanced API Call Method
```java
/**
 * Calls the LLM API with system and user messages
 * 
 * @param config LLM configuration
 * @param systemMessage System role message
 * @param userMessage User role message
 * @return API response as string
 * @throws IOException if network error occurs
 * @throws AIException if timeout or API error occurs
 */
public String callLLM(LLMConfig config, String systemMessage, String userMessage) 
        throws IOException, AIException {
    return callLLMWithRetry(config, systemMessage, userMessage, MAX_RETRIES);
}

/**
 * Calls the LLM API with retry mechanism
 * 
 * @param config LLM configuration
 * @param systemMessage System role message
 * @param userMessage User role message
 * @param maxRetries Maximum number of retries
 * @return API response as string
 * @throws IOException if network error occurs
 * @throws AIException if timeout or API error occurs
 */
public String callLLMWithRetry(LLMConfig config, String systemMessage, String userMessage, 
                              int maxRetries) throws IOException, AIException {
    String apiToken = config.getApiToken();
    if (apiToken != null && !apiToken.isEmpty()) {
        apiToken = EncryptionUtil.decrypt(apiToken);
    }
    
    // Create messages array
    JSONArray messages = new JSONArray();
    
    // Add system message
    JSONObject systemMsg = new JSONObject();
    systemMsg.put("role", "system");
    systemMsg.put("content", systemMessage);
    messages.put(systemMsg);
    
    // Add user message
    JSONObject userMsg = new JSONObject();
    userMsg.put("role", "user");
    userMsg.put("content", userMessage);
    messages.put(userMsg);
    
    // Create the request body with JSON schema response format
    JSONObject jsonBody = new JSONObject();
    try {
        jsonBody.put("model", config.getModelName());
        jsonBody.put("messages", messages);
        jsonBody.put("temperature", 0.3);
        jsonBody.put("max_tokens", 1000);
        
        // Add response format for structured output
        JSONObject responseFormat = new JSONObject();
        responseFormat.put("type", "json_object"); // or json_schema with specific schema
        jsonBody.put("response_format", responseFormat);
        
    } catch (Exception e) {
        Log.e(TAG, "Error creating JSON body", e);
        throw new IOException("Error creating request body", e);
    }
    
    // Create the request
    Request request = new Request.Builder()
            .url(config.getBaseUrl())
            .addHeader("Authorization", "Bearer " + apiToken)
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create(jsonBody.toString(), JSON))
            .build();
    
    // Execute the request with retries
    IOException lastException = null;
    for (int attempt = 0; attempt <= maxRetries; attempt++) {
        try {
            long startTime = System.currentTimeMillis();
            try (Response response = client.newCall(request).execute()) {
                long endTime = System.currentTimeMillis();
                Log.d(TAG, "API call took " + (endTime - startTime) + "ms");
                
                if (!response.isSuccessful()) {
                    handleErrorResponse(response);
                }
                
                String responseBody = response.body().string();
                Log.d(TAG, "LLM Response: " + responseBody);
                return responseBody;
            }
        } catch (SocketTimeoutException e) {
            lastException = new IOException("Request timeout", e);
            Log.w(TAG, "Timeout on attempt " + (attempt + 1) + "/" + (maxRetries + 1), e);
            
            if (attempt < maxRetries) {
                // Exponential backoff
                long delay = (long) Math.pow(2, attempt) * 1000; // 1s, 2s, 4s, etc.
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted during retry delay", ie);
                }
            }
        } catch (IOException e) {
            lastException = e;
            Log.w(TAG, "IO error on attempt " + (attempt + 1) + "/" + (maxRetries + 1), e);
            
            if (attempt < maxRetries) {
                // Linear backoff for non-timeout errors
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted during retry delay", ie);
                }
            }
        }
    }
    
    // If we get here, all retries failed
    throw new AIException(AIErrorType.TIMEOUT, "API call failed after " + (maxRetries + 1) + " attempts", lastException);
}

/**
 * Handles HTTP error responses
 * 
 * @param response HTTP response
 * @throws IOException with error details
 */
private void handleErrorResponse(Response response) throws IOException {
    String errorBody = response.body() != null ? response.body().string() : "";
    String errorMessage = "API error " + response.code() + ": " + response.message();
    
    Log.e(TAG, "API Error - Code: " + response.code() + ", Message: " + response.message() + 
          ", Body: " + errorBody);
    
    AIErrorType errorType = AIErrorType.API_ERROR;
    if (response.code() == 429) {
        errorType = AIErrorType.RATE_LIMIT;
        errorMessage = "Rate limit exceeded. Please try again later.";
    } else if (response.code() >= 500) {
        errorType = AIErrorType.SERVER_ERROR;
        errorMessage = "Server error. Please try again later.";
    }
    
    throw new AIException(errorType, errorMessage);
}
```

## AI Error Handling

### Error Types Enum
```java
public enum AIErrorType {
    TIMEOUT("Request timed out"),
    NETWORK_ERROR("Network connectivity issue"),
    API_ERROR("API returned an error"),
    RATE_LIMIT("Rate limit exceeded"),
    SERVER_ERROR("Server-side error"),
    INVALID_RESPONSE("Invalid response format");
    
    private final String description;
    
    AIErrorType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
```

### Custom Exception Class
```java
public class AIException extends Exception {
    private AIErrorType errorType;
    private Throwable cause;
    
    public AIException(AIErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }
    
    public AIException(AIErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
        this.cause = cause;
    }
    
    public AIErrorType getErrorType() {
        return errorType;
    }
    
    @Override
    public Throwable getCause() {
        return cause;
    }
    
    /**
     * Converts the exception to a JSON error response
     * 
     * @return JSON string representation of the error
     */
    public String toJsonResponse() {
        try {
            JSONObject errorResponse = new JSONObject();
            JSONObject errorObj = new JSONObject();
            
            errorObj.put("type", errorType.name().toLowerCase());
            errorObj.put("message", getMessage());
            errorObj.put("details", cause != null ? cause.getMessage() : "");
            
            errorResponse.put("error", errorObj);
            
            return errorResponse.toString();
        } catch (Exception e) {
            return "{\"error\":{\"type\":\"unknown\",\"message\":\"Failed to serialize error\"}}";
        }
    }
}
```

## AIDictionaryService Enhancement

### Updated Method Signature
```java
public List<AIDictionaryCache> getWordDefinition(String word, AIDictionaryConfig config, 
                                                LLMConfig llmConfig) throws IOException, AIException {
    // Implementation...
}
```

### Enhanced Response Parsing
```java
private List<AIDictionaryCache> parseDictionaryResponse(String response, String word, 
                                                       long llmConfigId) throws IOException, AIException {
    List<AIDictionaryCache> results = new ArrayList<>();
    
    try {
        // Check if response is an error
        JSONObject jsonResponse = new JSONObject(response);
        if (jsonResponse.has("error")) {
            JSONObject errorObj = jsonResponse.getJSONObject("error");
            String errorType = errorObj.optString("type", "unknown");
            String errorMessage = errorObj.optString("message", "Unknown error");
            
            AIErrorType type = AIErrorType.INVALID_RESPONSE;
            try {
                type = AIErrorType.valueOf(errorType.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Use default INVALID_RESPONSE
            }
            
            throw new AIException(type, errorMessage);
        }
        
        // Parse successful response
        JSONArray choices = jsonResponse.getJSONArray("choices");
        if (choices.length() > 0) {
            JSONObject choice = choices.getJSONObject(0);
            JSONObject message = choice.getJSONObject("message");
            String content = message.getString("content");
            
            // Try to parse content as JSON
            try {
                JSONObject contentJson = new JSONObject(content);
                if (contentJson.has("definitions")) {
                    JSONArray definitions = contentJson.getJSONArray("definitions");
                    parseDefinitionsArray(definitions, results, word, llmConfigId);
                } else {
                    // Handle case where content is directly the definitions array
                    JSONArray definitions = new JSONArray(content);
                    parseDefinitionsArray(definitions, results, word, llmConfigId);
                }
            } catch (Exception e) {
                // If content is not JSON, try to parse as array directly
                try {
                    JSONArray definitions = new JSONArray(content);
                    parseDefinitionsArray(definitions, results, word, llmConfigId);
                } catch (Exception innerE) {
                    Log.e(TAG, "Error parsing dictionary response content", e);
                    throw new AIException(AIErrorType.INVALID_RESPONSE, 
                        "Invalid response format from AI service", e);
                }
            }
        }
    } catch (AIException e) {
        throw e; // Re-throw AI exceptions
    } catch (Exception e) {
        Log.e(TAG, "Error parsing dictionary response", e);
        throw new AIException(AIErrorType.INVALID_RESPONSE, 
            "Error parsing dictionary response", e);
    }
    
    return results;
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
```

## AITranslatorService Enhancement

### Updated Method Signature
```java
public String translateText(String text, String sourceLanguage, String targetLanguage,
                           AITranslatorConfig config, LLMConfig llmConfig) 
        throws IOException, AIException {
    // Implementation...
}
```

### Enhanced Response Parsing
```java
private String parseTranslationResponse(String response) throws IOException, AIException {
    try {
        // Check if response is an error
        JSONObject jsonResponse = new JSONObject(response);
        if (jsonResponse.has("error")) {
            JSONObject errorObj = jsonResponse.getJSONObject("error");
            String errorType = errorObj.optString("type", "unknown");
            String errorMessage = errorObj.optString("message", "Unknown error");
            
            AIErrorType type = AIErrorType.INVALID_RESPONSE;
            try {
                type = AIErrorType.valueOf(errorType.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Use default INVALID_RESPONSE
            }
            
            throw new AIException(type, errorMessage);
        }
        
        // Parse successful response
        JSONArray choices = jsonResponse.getJSONArray("choices");
        if (choices.length() > 0) {
            JSONObject choice = choices.getJSONObject(0);
            JSONObject message = choice.getJSONObject("message");
            String content = message.getString("content");
            
            // Try to parse content as JSON
            try {
                JSONObject translation = new JSONObject(content);
                return translation.getString("translated_text");
            } catch (Exception e) {
                Log.e(TAG, "Error parsing translation response content", e);
                throw new AIException(AIErrorType.INVALID_RESPONSE, 
                    "Invalid response format from AI service", e);
            }
        }
    } catch (AIException e) {
        throw e; // Re-throw AI exceptions
    } catch (Exception e) {
        Log.e(TAG, "Error parsing translation response", e);
        throw new AIException(AIErrorType.INVALID_RESPONSE, 
            "Error parsing translation response", e);
    }
    
    throw new AIException(AIErrorType.INVALID_RESPONSE, "Empty translation response");
}
```

## Integration with Existing Code

### AIDictionary Class Update
Update the `wordLookup` method to handle the new exceptions:

```java
@Override
public List<Definition> wordLookup(String key) {
    List<Definition> definitions = new ArrayList<>();
    
    try {
        // Get the LLM config
        LLMConfig llmConfig = AIConfigRepository.getLLMConfigById(config.getLlmId());
        if (llmConfig == null) {
            return definitions;
        }
        
        // Get the word definition from the AI service
        List<AIDictionaryCache> cacheResults = service.getWordDefinition(key, config, llmConfig);
        
        // Convert AIDictionaryCache results to Definition objects
        for (AIDictionaryCache cache : cacheResults) {
            // Create export elements map
            Map<String, String> exportElements = new HashMap<>();
            exportElements.put("Headword", cache.getHwd() != null ? cache.getHwd() : key);
            exportElements.put("Part of Speech", cache.getSense() != null ? cache.getSense() : "");
            exportElements.put("Phonetics", cache.getPhonetics() != null ? cache.getPhonetics() : "");
            exportElements.put("Definition (English)", cache.getDefEn() != null ? cache.getDefEn() : "");
            exportElements.put("Definition (Chinese)", cache.getDefCn() != null ? cache.getDefCn() : "");
            exportElements.put("Example Sentence", cache.getExample() != null ? cache.getExample() : "");
            
            // Create display HTML
            StringBuilder displayHtml = new StringBuilder();
            displayHtml.append("<b>").append(cache.getHwd() != null ? cache.getHwd() : key).append("</b>");
            if (cache.getPhonetics() != null && !cache.getPhonetics().isEmpty()) {
                displayHtml.append(" ").append(cache.getPhonetics());
            }
            if (cache.getSense() != null && !cache.getSense().isEmpty()) {
                displayHtml.append("<br/><i>").append(cache.getSense()).append("</i>");
            }
            if (cache.getDefEn() != null && !cache.getDefEn().isEmpty()) {
                displayHtml.append("<br/>").append(cache.getDefEn());
            }
            if (cache.getDefCn() != null && !cache.getDefCn().isEmpty()) {
                displayHtml.append("<br/>").append(cache.getDefCn());
            }
            if (cache.getExample() != null && !cache.getExample().isEmpty()) {
                displayHtml.append("<br/><br/><i>").append(cache.getExample()).append("</i>");
            }
            
            // Create Definition object
            Definition def = new Definition(exportElements, displayHtml.toString());
            definitions.add(def);
        }
    } catch (AIException e) {
        Log.e(TAG, "AI Error during dictionary lookup", e);
        // Handle AI-specific errors (e.g., show user-friendly message)
        // Could add a special Definition object to show the error
    } catch (Exception e) {
        Log.e(TAG, "Error during dictionary lookup", e);
        e.printStackTrace();
    }
    
    return definitions;
}
```

## Configuration and Constants

### Default Prompts
Update the default prompts in `AIConfigRepository`:

```java
// AI Dictionary System Prompt
public static final String DEFAULT_DICTIONARY_SYSTEM_PROMPT = 
    "You are an experienced dictionary assistant. Your task is to provide accurate and " +
    "comprehensive definitions for words and phrases. You should be able to handle complex " +
    "queries and provide detailed explanations. Your responses should be clear, concise, " +
    "and easy to understand.";

// AI Translator System Prompt
public static final String DEFAULT_TRANSLATOR_SYSTEM_PROMPT = 
    "You are an experienced translator. Your task is to translate text from one language " +
    "to another accurately and fluently. You should be able to handle complex sentences " +
    "and idioms. Your responses should be clear, concise, and easy to understand.";

// AI Dictionary User Message Template
public static final String DEFAULT_DICTIONARY_USER_MESSAGE = 
    "Please provide the definitions of the word or phrase \"{query}\".";

// AI Translator User Message Template
public static final String DEFAULT_TRANSLATOR_USER_MESSAGE = 
    "Please provide the translation of the text \"{text}\".";
```

## Testing Considerations

### Unit Tests for AIService
1. Test successful API calls with mock responses
2. Test timeout scenarios
3. Test retry mechanism
4. Test error response handling
5. Test JSON schema response parsing

### Integration Tests
1. Test end-to-end dictionary lookup with real API
2. Test end-to-end translation with real API
3. Test cache behavior
4. Test error handling scenarios

### Mock Response Examples
```java
// Successful dictionary response
{
  "id": "chatcmpl-123",
  "object": "chat.completion",
  "choices": [{
    "index": 0,
    "message": {
      "role": "assistant",
      "content": "{\n  \"definitions\": [\n    {\n      \"headword\": \"example\",\n      \"sense\": \"noun\",\n      \"phonetics\": \"UK: /ɪɡˈzɑːmpəl/ US: /ɪɡˈzæmpəl/\",\n      \"def_en\": \"A representative form or pattern\",\n      \"def_cn\": \"例子；样本\",\n      \"example\": \"This is an example of a good sentence.\"\n    }\n  ]\n}"
    },
    "finish_reason": "stop"
  }]
}

// Error response
{
  "error": {
    "type": "timeout",
    "message": "Request timed out",
    "details": "The request took longer than 30 seconds to complete"
  }
}
```