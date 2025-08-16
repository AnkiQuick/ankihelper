# JSON Schema Validation Specification

This document outlines the JSON schema validation requirements for AI dictionary and translator responses.

## Overview

To ensure data consistency and reliability, we will implement JSON schema validation for AI service responses. This will help catch malformed responses early and provide better error handling.

## JSON Schema Validation Library

We'll use a lightweight JSON schema validation library. For Android, we can use:

1. **json-schema-validator** - A Java implementation of JSON Schema validation
2. **custom validation** - Implement our own lightweight validation

For this implementation, we'll create a custom validator to keep dependencies minimal.

## Dictionary Response Schema

### Schema Definition
```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "Dictionary Definitions",
  "description": "Schema for dictionary definitions response",
  "type": "object",
  "properties": {
    "definitions": {
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "headword": {
            "type": "string",
            "description": "The main word or phrase being defined"
          },
          "phrase": {
            "type": "string",
            "description": "Fixed phrase containing the word (only for phrases, empty for single words)"
          },
          "sense": {
            "type": "string",
            "description": "Part of speech (noun, verb, adjective, adverb, etc.) - empty for phrases"
          },
          "phonetics": {
            "type": "string",
            "description": "British and American English phonetics (format: 'UK: /pronunciation/ US: /pronunciation/')"
          },
          "def_en": {
            "type": "string",
            "description": "English definition"
          },
          "def_cn": {
            "type": "string",
            "description": "Chinese definition"
          },
          "example": {
            "type": "string",
            "description": "Example sentence using the word or phrase"
          }
        },
        "required": ["headword", "phrase", "sense", "phonetics", "def_en", "def_cn", "example"],
        "additionalProperties": false
      },
      "minItems": 1
    }
  },
  "required": ["definitions"],
  "additionalProperties": false
}
```

### Enhanced Validation with Markdown Handling
The validation now includes preprocessing to handle markdown-wrapped JSON:

```java
/**
 * Validates a dictionary response with markdown preprocessing
 * 
 * @param response The raw response from LLM
 * @return ValidationResult containing validation status and errors
 */
public static ValidationResult validateDictionaryResponseWithMarkdown(String response) {
    ValidationResult result = new ValidationResult();
    
    try {
        // First clean up markdown formatting
        String cleanedResponse = cleanMarkdownFormatting(response);
        
        // Then validate as JSON
        JSONObject jsonResponse = new JSONObject(cleanedResponse);
        
        // Continue with standard validation
        return validateDictionaryResponse(jsonResponse);
    } catch (Exception e) {
        Log.e(TAG, "Error validating dictionary response with markdown cleanup", e);
        result.addError("Exception during validation: " + e.getMessage());
        return result;
    }
}

/**
 * Cleans up markdown formatting from LLM responses
 * 
 * @param content The raw content from LLM
 * @return Cleaned content ready for JSON parsing
 */
private static String cleanMarkdownFormatting(String content) {
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
    
    return cleaned;
}

### Validation Implementation

#### Schema Validator Class
```java
public class DictionaryResponseValidator {
    private static final String TAG = "DictionaryResponseValidator";
    
    /**
     * Validates a dictionary response against the expected schema
     * 
     * @param response The JSON response to validate
     * @return ValidationResult containing validation status and errors
     */
    public static ValidationResult validateDictionaryResponse(JSONObject response) {
        ValidationResult result = new ValidationResult();
        
        try {
            // Check if response has definitions array
            if (!response.has("definitions")) {
                result.addError("Missing required field: definitions");
                return result;
            }
            
            JSONArray definitions = response.getJSONArray("definitions");
            if (definitions.length() == 0) {
                result.addError("Definitions array cannot be empty");
                return result;
            }
            
            // Validate each definition
            for (int i = 0; i < definitions.length(); i++) {
                JSONObject definition = definitions.getJSONObject(i);
                validateDefinition(definition, result, i);
            }
            
            result.setValid(true);
        } catch (Exception e) {
            Log.e(TAG, "Error validating dictionary response", e);
            result.addError("Exception during validation: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Validates a single definition object
     * 
     * @param definition The definition object to validate
     * @param result The validation result to update
     * @param index The index of the definition in the array
     */
    private static void validateDefinition(JSONObject definition, ValidationResult result, int index) {
        // Required fields
        String[] requiredFields = {"headword", "phrase", "sense", "phonetics", "def_en", "def_cn", "example"};
        
        for (String field : requiredFields) {
            if (!definition.has(field)) {
                result.addError("Definition[" + index + "]: Missing required field '" + field + "'");
                continue;
            }
            
            try {
                Object value = definition.get(field);
                if (!(value instanceof String)) {
                    result.addError("Definition[" + index + "]: Field '" + field + "' must be a string");
                }
            } catch (Exception e) {
                result.addError("Definition[" + index + "]: Error accessing field '" + field + "'");
            }
        }
        
        // Check for additional properties
        JSONArray names = definition.names();
        if (names != null) {
            Set<String> allowedFields = new HashSet<>(Arrays.asList(requiredFields));
            for (int i = 0; i < names.length(); i++) {
                try {
                    String fieldName = names.getString(i);
                    if (!allowedFields.contains(fieldName)) {
                        result.addError("Definition[" + index + "]: Unexpected field '" + fieldName + "'");
                    }
                } catch (Exception e) {
                    result.addError("Definition[" + index + "]: Error checking field names");
                }
            }
        }
    }
}
```

#### Validation Result Class
```java
public class ValidationResult {
    private boolean valid = false;
    private List<String> errors = new ArrayList<>();
    
    public boolean isValid() {
        return valid;
    }
    
    public void setValid(boolean valid) {
        this.valid = valid;
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
    public void addError(String error) {
        this.errors.add(error);
    }
    
    public String getErrorMessage() {
        if (errors.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < errors.size(); i++) {
            if (i > 0) {
                sb.append("; ");
            }
            sb.append(errors.get(i));
        }
        
        return sb.toString();
    }
}
```

## Translator Response Schema

### Schema Definition
```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "Translation Response",
  "description": "Schema for translation response",
  "type": "object",
  "properties": {
    "translated_text": {
      "type": "string",
      "description": "The translated text"
    },
    "source_language": {
      "type": "string",
      "description": "The source language code"
    },
    "target_language": {
      "type": "string",
      "description": "The target language code"
    }
  },
  "required": ["translated_text", "source_language", "target_language"],
  "additionalProperties": false
}
```

### Validation Implementation

#### Schema Validator Class
```java
public class TranslationResponseValidator {
    private static final String TAG = "TranslationResponseValidator";
    
    /**
     * Validates a translation response against the expected schema
     * 
     * @param response The JSON response to validate
     * @return ValidationResult containing validation status and errors
     */
    public static ValidationResult validateTranslationResponse(JSONObject response) {
        ValidationResult result = new ValidationResult();
        
        try {
            // Required fields
            String[] requiredFields = {"translated_text", "source_language", "target_language"};
            
            for (String field : requiredFields) {
                if (!response.has(field)) {
                    result.addError("Missing required field: " + field);
                    continue;
                }
                
                try {
                    Object value = response.get(field);
                    if (!(value instanceof String)) {
                        result.addError("Field '" + field + "' must be a string");
                    }
                } catch (Exception e) {
                    result.addError("Error accessing field '" + field + "'");
                }
            }
            
            // Check for additional properties
            JSONArray names = response.names();
            if (names != null) {
                Set<String> allowedFields = new HashSet<>(Arrays.asList(requiredFields));
                for (int i = 0; i < names.length(); i++) {
                    try {
                        String fieldName = names.getString(i);
                        if (!allowedFields.contains(fieldName)) {
                            result.addError("Unexpected field '" + fieldName + "'");
                        }
                    } catch (Exception e) {
                        result.addError("Error checking field names");
                    }
                }
            }
            
            result.setValid(true);
        } catch (Exception e) {
            Log.e(TAG, "Error validating translation response", e);
            result.addError("Exception during validation: " + e.getMessage());
        }
        
        return result;
    }
}
```

## Integration with AI Services

### AIDictionaryService Integration
```java
private List<AIDictionaryCache> parseDictionaryResponse(String response, String word, 
                                                       long llmConfigId) throws IOException, AIException {
    List<AIDictionaryCache> results = new ArrayList<>();
    
    try {
        // Parse the JSON response
        JSONObject jsonResponse = new JSONObject(response);
        
        // Check if response is an error
        if (jsonResponse.has("error")) {
            handleErrorResponse(jsonResponse.getJSONObject("error"));
        }
        
        JSONArray choices = jsonResponse.getJSONArray("choices");
        if (choices.length() > 0) {
            JSONObject choice = choices.getJSONObject(0);
            JSONObject message = choice.getJSONObject("message");
            String content = message.getString("content");
            
            // Parse the content as JSON
            JSONObject contentJson = new JSONObject(content);
            
            // Validate the response against schema
            ValidationResult validationResult = DictionaryResponseValidator.validateDictionaryResponse(contentJson);
            if (!validationResult.isValid()) {
                throw new AIException(AIErrorType.INVALID_RESPONSE, 
                    "Invalid dictionary response format: " + validationResult.getErrorMessage());
            }
            
            // Process valid response
            JSONArray definitions = contentJson.getJSONArray("definitions");
            for (int i = 0; i < definitions.length(); i++) {
                JSONObject definition = definitions.getJSONObject(i);
                
                AIDictionaryCache cache = new AIDictionaryCache();
                cache.setHwd(definition.optString("headword", word));
                cache.setPhrase(definition.optString("phrase", ""));
                cache.setSense(definition.optString("sense", ""));
                cache.setPhonetics(definition.optString("phonetics", ""));
                cache.setDefEn(definition.optString("def_en", ""));
                cache.setDefCn(definition.optString("def_cn", ""));
                cache.setExample(definition.optString("example", ""));
                cache.setLlmConfigId(llmConfigId);
                
                results.add(cache);
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
```

### AITranslatorService Integration
```java
private String parseTranslationResponse(String response) throws IOException, AIException {
    try {
        // Parse the JSON response
        JSONObject jsonResponse = new JSONObject(response);
        
        // Check if response is an error
        if (jsonResponse.has("error")) {
            handleErrorResponse(jsonResponse.getJSONObject("error"));
        }
        
        JSONArray choices = jsonResponse.getJSONArray("choices");
        if (choices.length() > 0) {
            JSONObject choice = choices.getJSONObject(0);
            JSONObject message = choice.getJSONObject("message");
            String content = message.getString("content");
            
            // Parse the content as JSON
            JSONObject contentJson = new JSONObject(content);
            
            // Validate the response against schema
            ValidationResult validationResult = TranslationResponseValidator.validateTranslationResponse(contentJson);
            if (!validationResult.isValid()) {
                throw new AIException(AIErrorType.INVALID_RESPONSE, 
                    "Invalid translation response format: " + validationResult.getErrorMessage());
            }
            
            // Process valid response
            return contentJson.getString("translated_text");
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

## Error Response Schema

### Schema Definition
```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "Error Response",
  "description": "Schema for error responses",
  "type": "object",
  "properties": {
    "error": {
      "type": "object",
      "properties": {
        "type": {
          "type": "string",
          "enum": ["timeout", "network_error", "api_error", "rate_limit", "server_error", "invalid_response"]
        },
        "message": {
          "type": "string",
          "description": "Human readable error message"
        },
        "details": {
          "type": "string",
          "description": "Technical details for debugging"
        }
      },
      "required": ["type", "message"],
      "additionalProperties": false
    }
  },
  "required": ["error"],
  "additionalProperties": false
}
```

### Validation Implementation
```java
public class ErrorResponseValidator {
    private static final String TAG = "ErrorResponseValidator";
    private static final Set<String> VALID_ERROR_TYPES = new HashSet<>(Arrays.asList(
        "timeout", "network_error", "api_error", "rate_limit", "server_error", "invalid_response"
    ));
    
    /**
     * Validates an error response against the expected schema
     * 
     * @param response The JSON error response to validate
     * @return ValidationResult containing validation status and errors
     */
    public static ValidationResult validateErrorResponse(JSONObject response) {
        ValidationResult result = new ValidationResult();
        
        try {
            // Check if response has error object
            if (!response.has("error")) {
                result.addError("Missing required field: error");
                return result;
            }
            
            JSONObject error = response.getJSONObject("error");
            
            // Check type field
            if (!error.has("type")) {
                result.addError("Missing required field: error.type");
            } else {
                String type = error.getString("type");
                if (!VALID_ERROR_TYPES.contains(type)) {
                    result.addError("Invalid error type: " + type);
                }
            }
            
            // Check message field
            if (!error.has("message")) {
                result.addError("Missing required field: error.message");
            } else {
                Object message = error.get("message");
                if (!(message instanceof String)) {
                    result.addError("error.message must be a string");
                }
            }
            
            // Check details field (optional)
            if (error.has("details")) {
                Object details = error.get("details");
                if (!(details instanceof String)) {
                    result.addError("error.details must be a string");
                }
            }
            
            // Check for additional properties
            JSONArray errorNames = error.names();
            if (errorNames != null) {
                Set<String> allowedErrorFields = new HashSet<>(Arrays.asList("type", "message", "details"));
                for (int i = 0; i < errorNames.length(); i++) {
                    try {
                        String fieldName = errorNames.getString(i);
                        if (!allowedErrorFields.contains(fieldName)) {
                            result.addError("Unexpected field in error object: " + fieldName);
                        }
                    } catch (Exception e) {
                        result.addError("Error checking error object field names");
                    }
                }
            }
            
            JSONArray responseNames = response.names();
            if (responseNames != null) {
                Set<String> allowedResponseFields = new HashSet<>(Arrays.asList("error"));
                for (int i = 0; i < responseNames.length(); i++) {
                    try {
                        String fieldName = responseNames.getString(i);
                        if (!allowedResponseFields.contains(fieldName)) {
                            result.addError("Unexpected field in response: " + fieldName);
                        }
                    } catch (Exception e) {
                        result.addError("Error checking response field names");
                    }
                }
            }
            
            result.setValid(true);
        } catch (Exception e) {
            Log.e(TAG, "Error validating error response", e);
            result.addError("Exception during validation: " + e.getMessage());
        }
        
        return result;
    }
}
```

## Testing Strategy

### Unit Tests
1. Test valid dictionary responses
2. Test valid translation responses
3. Test various invalid response formats
4. Test error responses
5. Test edge cases (empty arrays, missing fields, etc.)

### Test Cases

#### Valid Dictionary Response
```json
{
  "definitions": [
    {
      "headword": "example",
      "phrase": "",
      "sense": "noun",
      "phonetics": "UK: /ɪɡˈzɑːmpəl/ US: /ɪɡˈzæmpəl/",
      "def_en": "A representative form or pattern",
      "def_cn": "例子；样本",
      "example": "This is an example of a good sentence."
    }
  ]
}
```

#### Invalid Dictionary Response (Missing Field)
```json
{
  "definitions": [
    {
      "headword": "example",
      "sense": "noun",
      "phonetics": "UK: /ɪɡˈzɑːmpəl/ US: /ɪɡˈzæmpəl/",
      "def_en": "A representative form or pattern",
      "def_cn": "例子；样本"
      // Missing "example" field
    }
  ]
}
```

#### Valid Translation Response
```json
{
  "translated_text": "This is an example.",
  "source_language": "en",
  "target_language": "zh"
}
```

#### Invalid Translation Response (Wrong Type)
```json
{
  "translated_text": 123,  // Should be string
  "source_language": "en",
  "target_language": "zh"
}
```

#### Valid Error Response
```json
{
  "error": {
    "type": "timeout",
    "message": "Request timed out",
    "details": "The request took longer than 30 seconds to complete"
  }
}
```

#### Invalid Error Response (Invalid Type)
```json
{
  "error": {
    "type": "unknown_type",  // Not in valid types
    "message": "Request timed out"
  }
}
```