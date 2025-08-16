# AI Dictionary and Translator Functions Specification

This document outlines the functional specifications for implementing the AI dictionary lookup and translation features in the AnkiHelper app.

## Overview

The AI functions will leverage LLM APIs to provide dictionary definitions and translations. This specification covers the system prompts, user messages, output formats, error handling, and integration requirements.

## System Prompts

### AI Dictionary System Prompt
```
You are an experienced dictionary assistant. Your task is to provide accurate and comprehensive definitions for words and phrases. You should be able to handle complex queries and provide detailed explanations. Your responses should be clear, concise, and easy to understand.
```

### AI Translator System Prompt
```
You are an experienced translator. Your task is to translate text from one language to another accurately and fluently. You should be able to handle complex sentences and idioms. Your responses should be clear, concise, and easy to understand.
```

## User Messages

### AI Dictionary User Message
```
Please provide the definitions of the word or phrase "<headword/phrase>".
```

### AI Translator User Message
```
Please provide the translation of the text "<text>".
```

## API Request Format

### Generic Request Structure
```json
{
  "model": "model_name",
  "messages": [
    {
      "role": "system",
      "content": "<system_message>"
    },
    {
      "role": "user",
      "content": "<user_message>"
    }
  ],
  "response_format": {
    "type": "json_schema",
    "json_schema": {
      "name": "response_schema",
      "schema": {},
      "strict": true
    }
  },
  "temperature": 0.3,
  "max_tokens": 1000
}
```

## Output Formats

### AI Dictionary Output Format

#### JSON Schema
```json
{
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
      }
    }
  },
  "required": ["definitions"],
  "additionalProperties": false
}
```

#### Example Response
```json
{
  "definitions": [
    {
      "headword": "new",
      "phrase": "new",
      "sense": "adjective",
      "phonetics": "UK: /njuː/ US: /njuː/",
      "def_en": "Not existing before; made, introduced, or discovered recently or now for the first time.",
      "def_cn": "以前不存在的；最近或现在首次制造、引入或发现的。",
      "example": "They planted new trees in the park."
    },
    {
      "headword": "new",
      "phrase": "new",
      "sense": "adverb",
      "phonetics": "UK: /njuː/ US: /njuː/",
      "def_en": "Newly; recently.",
      "def_cn": "新近地；最近。",
      "example": "The newly painted walls looked bright and clean."
    }
  ]
}
```

### Markdown-Wrapped JSON Handling
LLMs often return responses wrapped in markdown code blocks:
```
Watch
```json
{
  "definitions": [
    // ... definition objects
  ]
}
```

The AI service now automatically cleans these responses by:
1. Removing prepended words (like "Watch")
2. Stripping markdown code block markers (```json and ```)
3. Parsing the clean JSON content
4. Extracting individual definitions from the `definitions` array

### AI Translator Output Format

#### JSON Schema
```json
{
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

#### Example Response
```json
{
  "translated_text": "Serendipity is the art of finding beautiful things by accident.",
  "source_language": "en",
  "target_language": "zh"
}
```

## Error Handling

### Timeout Handling
- Default timeout: 30 seconds
- Retry mechanism: 2 retries with exponential backoff (1s, 2s, 4s)
- User notification: Display appropriate error message when timeout occurs
- Graceful degradation: Fallback to built-in dictionaries when AI service is unavailable

### Error Response Format
```json
{
  "error": {
    "type": "timeout|network_error|api_error|invalid_response",
    "message": "Human readable error message",
    "details": "Technical details for debugging"
  }
}
```

## Integration Requirements

### Dictionary Registration
Ensure the `DictionaryRegister` class includes all AI dictionaries:
- AI dictionaries should be dynamically loaded from the database
- Each AI dictionary configuration should create an instance of `AIDictionary`
- The `getDictionaryObjectList()` method should return all AI dictionaries alongside built-in dictionaries

### Caching Strategy
- Cache dictionary definitions with a TTL of 7 days
- Cache translations with a TTL of 7 days
- Implement cache invalidation based on LLM configuration changes
- Provide cache statistics for monitoring

### Rate Limiting
- Implement request queuing to handle API rate limits
- Add delay between consecutive requests (configurable)
- Display queue position to user during busy periods

## Security Considerations

### API Token Management
- Encrypt API tokens before storing in database
- Use secure storage mechanisms for sensitive data
- Rotate tokens periodically (optional feature)

### Data Privacy
- Do not send personally identifiable information to LLM APIs
- Log only non-sensitive information for debugging
- Allow users to delete their AI cache data

## Performance Requirements

### Response Time Targets
- Dictionary lookup: < 5 seconds (95th percentile)
- Translation: < 3 seconds (95th percentile)
- Cache hit: < 100ms

### Resource Usage
- Memory footprint: < 10MB additional memory for AI operations
- Network usage: Optimize requests to minimize data transfer
- Battery usage: Minimize CPU usage during API calls

## Monitoring and Analytics

### Metrics to Track
- Success rate of AI dictionary lookups
- Success rate of AI translations
- Average response time
- Cache hit ratio
- Error rates by type

### Logging
- Log API request/response for debugging (without sensitive data)
- Log performance metrics
- Log error conditions with context

## Future Enhancements

### Planned Features
- Support for image-based definitions (diagrams, illustrations)
- Pronunciation guides with audio samples
- Context-aware definitions based on usage examples
- Multi-language support for dictionary definitions
- Customizable output formats