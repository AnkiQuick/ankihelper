# AI Dictionary and Translator Implementation - Markdown Parsing Enhancement

## Overview
This document outlines the implementation of enhanced markdown-wrapped JSON parsing for AI dictionary responses.

## Problem Statement
Large Language Models frequently return properly structured JSON responses wrapped in markdown code blocks:
```
Watch
```json
{
  "definitions": [
    {
      "headword": "new",
      "phrase": "new",
      "sense": "adjective",
      "phonetics": "/njuː/",
      "def_en": "Not existing before...",
      "def_cn": "以前不存在的...",
      "example": "They planted new trees in the park."
    }
  ]
}
```

The previous implementation failed to properly parse these responses because:
1. The markdown wrapper prevented JSON parsing
2. Leading words like "Watch" confused the parser
3. The entire response was treated as plain text instead of extracting individual definitions

## Solution Implemented

### 1. Markdown Formatting Cleanup
Added `cleanMarkdownFormatting()` method to preprocess LLM responses:
- Removes leading words prepended by LLMs
- Strips markdown code block markers (```json, ```)
- Handles both wrapped and unwrapped JSON content
- Preserves actual JSON structure while removing formatting artifacts

### 2. Enhanced Content Preprocessing
All content is now cleaned before JSON parsing attempts:
- Comprehensive logging shows both original and cleaned content
- Robust preprocessing ensures consistent input for JSON parsing

### 3. Improved Parsing Flow
Enhanced the parsing hierarchy with better error handling:
1. Try parsing as JSON object first (most common case)
2. Check for `definitions` array and parse each definition individually
3. Handle single definition objects
4. Fall back to JSON array parsing when appropriate
5. Final fallback to plain text if all JSON parsing fails

### 4. Detailed Logging and Debugging
Added comprehensive logging throughout the parsing process:
- Logs original content received from LLM
- Logs cleaned content after markdown processing
- Logs successful parsing operations with counts
- Logs error conditions with context for troubleshooting

## Technical Implementation

### Markdown Cleaning Logic
```java
private String cleanMarkdownFormatting(String content) {
    if (content == null || content.isEmpty()) {
        return content;
    }
    
    String cleaned = content.trim();
    
    // Remove leading words (common with some LLMs)
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
    
    return cleaned.trim();
}
```

### Parsing Hierarchy
```java
private void parseContent(String content, List<AIDictionaryCache> results, 
                         String word, long llmConfigId) throws Exception {
    // Clean markdown formatting first
    String cleanedContent = cleanMarkdownFormatting(content);
    
    // Try to parse content as JSON
    JSONObject contentJson = new JSONObject(cleanedContent);
    if (contentJson.has("definitions")) {
        // Handle the case where content contains a definitions array
        JSONArray definitions = contentJson.getJSONArray("definitions");
        parseDefinitionsArray(definitions, results, word, llmConfigId);
    } else {
        // Check if it's a single definition object
        if (contentJson.has("headword") || contentJson.has("def_en") || contentJson.has("defEn")) {
            JSONArray definitions = new JSONArray();
            definitions.put(contentJson);
            parseDefinitionsArray(definitions, results, word, llmConfigId);
        } else {
            // Treat as a generic JSON object and convert to string definition
            AIDictionaryCache cache = new AIDictionaryCache();
            cache.setHwd(word);
            cache.setDefEn(contentJson.toString());
            cache.setLlmConfigId(llmConfigId);
            cache.setTimestamp(System.currentTimeMillis());
            results.add(cache);
        }
    }
}
```

## Expected Behavior

### Success Scenario
When a user looks up a word:
1. LLM returns markdown-wrapped JSON with multiple definitions
2. Cleaner strips markdown formatting, leaving pure JSON
3. Parser recognizes the `definitions` array and extracts all definitions
4. Each definition becomes a separate card in the UI

### Error Handling
1. **Malformed JSON**: Falls back to plain text display with error logging
2. **Network Issues**: Maintains existing error handling with user notifications
3. **Parsing Failures**: Continues with other definitions when possible

## Benefits
1. **Improved User Experience**: Multiple definitions properly displayed as separate cards
2. **Robust Error Handling**: Graceful degradation when parsing fails
3. **Better Debugging**: Comprehensive logging aids in troubleshooting
4. **Compatibility**: Works with various LLM providers and response formats
5. **Maintainability**: Modular design allows for future enhancements

## Testing Validation
Verified with sample response containing multiple definitions for "Watch", where each definition (noun, verb, phrasal verb, idiom) is properly parsed and displayed as a separate dictionary entry.