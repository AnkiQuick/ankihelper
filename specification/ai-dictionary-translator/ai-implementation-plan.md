# AI Dictionary and Translator Implementation Plan

This document outlines the implementation plan for enhancing the AI dictionary and translator features to align with the functional specification.

## Current State Analysis

### Dictionary Registration
**Status**: ✓ COMPLETE
The `DictionaryRegister` class correctly includes all AI dictionaries:
- Static dictionaries are loaded from the `classList` array
- AI dictionaries are dynamically loaded from the database using `AIConfigRepository.getAllAIDictionaryConfigs()`
- Each AI dictionary configuration creates an instance of `AIDictionary`

### API Service Implementation
**Status**: ⚠ INCOMPLETE
The current `AIService` implementation has several issues:
1. Only supports user role messages, not system role messages
2. Doesn't use the structured JSON response format
3. No timeout handling
4. No retry mechanism

### Dictionary and Translator Services
**Status**: ⚠ PARTIALLY COMPLETE
The `AIDictionaryService` and `AITranslatorService` have basic functionality but need improvements:
1. Need to support the new system/user message format
2. Need to implement proper JSON schema response format
3. Need to add timeout and error handling

## Implementation Tasks

### 1. Update AIService for Enhanced API Support

#### Task 1.1: Implement System/User Message Format
Modify `AIService.java` to support both system and user messages:

```java
public String callLLM(LLMConfig config, String systemMessage, String userMessage) throws IOException {
    // Implementation details...
}
```

#### Task 1.2: Add JSON Schema Response Format
Enhance the request to specify JSON schema response format:

```java
JSONObject responseFormat = new JSONObject();
responseFormat.put("type", "json_schema");
// Add schema details...
```

#### Task 1.3: Implement Timeout Handling
Add timeout configuration to OkHttpClient:

```java
this.client = new OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build();
```

#### Task 1.4: Add Retry Mechanism
Implement exponential backoff retry logic:

```java
public String callLLMWithRetry(LLMConfig config, String systemMessage, String userMessage, 
                              int maxRetries) throws IOException {
    // Implementation with retry logic...
}
```

### 2. Update AIDictionaryService

#### Task 2.1: Update Message Format
Modify `getWordDefinition` to use the new system/user message format:

```java
// System message: "You are an experienced dictionary assistant..."
// User message: "Please provide the definitions of the word or phrase 'example'."
```

#### Task 2.2: Update Response Parsing
Modify `parseDictionaryResponse` to handle the new JSON schema format:

```java
// Parse response according to the defined JSON schema
// Handle both success and error responses
```

#### Task 2.3: Add Error Handling
Implement proper error handling for timeouts and API errors:

```java
try {
    // API call
} catch (SocketTimeoutException e) {
    // Handle timeout
} catch (IOException e) {
    // Handle other IO errors
}
```

### 3. Update AITranslatorService

#### Task 3.1: Update Message Format
Modify `translateText` to use the new system/user message format:

```java
// System message: "You are an experienced translator..."
// User message: "Please provide the translation of the text 'example text'."
```

#### Task 3.2: Update Response Parsing
Modify `parseTranslationResponse` to handle the new JSON schema format:

```java
// Parse response according to the defined JSON schema
// Handle both success and error responses
```

#### Task 3.3: Add Error Handling
Implement proper error handling for timeouts and API errors.

### 4. Enhance Error Handling Throughout

#### Task 4.1: Define Error Types
Create an enum for different error types:

```java
public enum AIErrorType {
    TIMEOUT,
    NETWORK_ERROR,
    API_ERROR,
    INVALID_RESPONSE
}
```

#### Task 4.2: Create Custom Exception Class
Create a custom exception class for AI-related errors:

```java
public class AIException extends Exception {
    private AIErrorType errorType;
    
    public AIException(AIErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }
    
    // Getters...
}
```

### 5. Implement Caching Improvements

#### Task 5.1: Add Cache Statistics
Enhance `AICacheRepository` to track cache statistics:

```java
public class CacheStatistics {
    private int hitCount;
    private int missCount;
    private long totalRequests;
    
    // Methods to update and retrieve statistics...
}
```

#### Task 5.2: Implement Cache TTL
Add TTL-based cache invalidation:

```java
private static final long CACHE_TTL = 7 * 24 * 60 * 60 * 1000; // 7 days

public boolean isCacheExpired(long timestamp) {
    return (System.currentTimeMillis() - timestamp) > CACHE_TTL;
}
```

## Testing Plan

### Unit Tests
1. Test AIService with mock HTTP responses
2. Test AIDictionaryService parsing with valid and invalid JSON
3. Test AITranslatorService parsing with valid and invalid JSON
4. Test timeout scenarios
5. Test retry mechanism

### Integration Tests
1. Test end-to-end dictionary lookup with real API
2. Test end-to-end translation with real API
3. Test cache behavior
4. Test error handling scenarios

## Timeline

### Phase 1: Core API Enhancements (Week 1)
- Update AIService with system/user messages
- Implement timeout and retry mechanisms
- Add error handling framework

### Phase 2: Service Improvements (Week 2)
- Update AIDictionaryService and AITranslatorService
- Implement JSON schema parsing
- Add comprehensive error handling

### Phase 3: Caching and Monitoring (Week 3)
- Enhance caching with TTL and statistics
- Add logging and monitoring capabilities

### Phase 4: Testing and Refinement (Week 4)
- Implement unit tests
- Conduct integration testing
- Refine based on test results

## Risk Mitigation

### API Compatibility
- Maintain backward compatibility with existing API calls
- Test with multiple LLM providers (OpenAI, Anthropic, etc.)

### Performance Impact
- Monitor memory usage during API calls
- Optimize JSON parsing for large responses
- Implement request queuing to prevent overwhelming the API

### Data Privacy
- Ensure no sensitive user data is sent to LLM APIs
- Encrypt all API tokens at rest
- Provide option to clear AI cache data