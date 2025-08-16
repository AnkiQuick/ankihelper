# AI Dictionary and Translator Specifications

This directory contains all the specifications for implementing the AI dictionary and translator features in the AnkiHelper app.

## Table of Contents

1. [Functional Specification](ai-functions-spec.md) - Overview of AI features including system prompts, user messages, output formats, and error handling
2. [Implementation Plan](ai-implementation-plan.md) - Detailed plan for implementing the AI features with timeline and risk mitigation
3. [Technical Specification for AI Services](ai-service-technical-spec.md) - Detailed technical specifications for AIService, AIDictionaryService, and AITranslatorService
4. [JSON Schema Validation](json-schema-validation-spec.md) - Specifications for validating AI service responses
5. [Markdown Parsing Enhancement](markdown-parsing-enhancement.md) - Implementation details for handling markdown-wrapped JSON responses
6. [Final Implementation Summary](ai-dictionary-translator-final-summary.md) - Summary of completed implementation
7. [Implementation Summary](ai-dictionary-translator-implementation-summary.md) - Detailed implementation summary
8. [Feature Specification](ai-dictionary-translator.md) - Original feature specification

## Overview

The AI dictionary and translator features enhance the AnkiHelper app by integrating Large Language Models (LLMs) to provide:

1. **AI Dictionary Lookup** - Get comprehensive definitions for words and phrases
2. **AI Translation** - Translate text between languages accurately
3. **Text-to-Speech (TTS)** - Pronounce words and sentences (partially implemented)

## Key Components

### Configuration Management
- LLM configurations (API endpoints, tokens, models)
- TTS configurations
- AI Dictionary configurations (linked to LLMs with custom prompts)
- AI Translator configurations (linked to LLMs with custom prompts)

### Data Models
- `LLMConfig` - Configuration for Large Language Models
- `TTSConfig` - Configuration for Text-to-Speech services
- `AIDictionaryConfig` - Configuration for AI dictionaries
- `AITranslatorConfig` - Configuration for AI translators
- `AIDictionaryCache` - Cache for AI dictionary results
- `AITranslatorCache` - Cache for AI translation results

### Services
- `AIService` - Base class for making API calls to LLMs
- `AIDictionaryService` - Handles AI dictionary functionality
- `AITranslatorService` - Handles AI translation functionality
- `AIManager` - Coordinates all AI services

### UI Components
- `AIConfigActivity` - Main entry point for AI configuration
- `LLMConfigListActivity`/`LLMConfigEditorActivity` - Manage LLM configurations
- `TTSConfigListActivity`/`TTSConfigEditorActivity` - Manage TTS configurations
- `AIDictionaryConfigListActivity`/`AIDictionaryConfigEditorActivity` - Manage AI dictionary configurations
- `AITranslatorConfigListActivity`/`AITranslatorConfigEditorActivity` - Manage AI translator configurations

## Integration Points

### Popup Activity
- AI dictionary selection in plan dropdown
- AI translation via footer translate button

### Plan Manager
- Ability to select AI dictionaries as normal dictionaries in plan configuration

## Security
- API tokens are encrypted before storage
- Secure storage mechanisms for sensitive data

## Caching
- Results from AI services are cached to reduce API calls
- Separate SQLite database for AI cache data
- Cache expiration mechanism

## Error Handling
- Timeout handling with retry mechanism
- Network error detection and graceful degradation
- User-friendly error messages

## Future Enhancements
- Complete TTS configuration implementation
- Advanced caching strategy with expiration policies
- More sophisticated rate limiting
- Enhanced UI/UX design for configuration screens
- Comprehensive error handling for edge cases