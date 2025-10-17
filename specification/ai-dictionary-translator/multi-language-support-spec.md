# Multi-Language AI Features Specification

## Overview
This document specifies the multi-language support implementation for AI Dictionary and AI Translator features in AnkiQuicker.

## Background
Prior to this implementation, the AI Dictionary and AI Translator had language configuration fields in the database schema and UI but were not utilizing them. Prompts were hardcoded for English-to-Chinese translation only.

## Requirements

### Functional Requirements
1. **FR-1**: AI Dictionary shall support configurable source and target languages
2. **FR-2**: AI Translator shall support configurable source and target languages
3. **FR-3**: System shall support at least 12 common languages
4. **FR-4**: Language codes shall be converted to full names in LLM prompts
5. **FR-5**: System shall maintain backward compatibility with existing configurations

### Non-Functional Requirements
1. **NFR-1**: Code shall comply with Detekt linting rules
2. **NFR-2**: Methods shall not exceed 60 lines (LongMethod rule)
3. **NFR-3**: Cyclomatic complexity shall not exceed 15
4. **NFR-4**: Line length shall not exceed 120 characters

## Supported Languages

| Language Code | Language Name | Supported Features |
|--------------|---------------|-------------------|
| en | English | Dictionary, Translator |
| zh | Chinese | Dictionary, Translator |
| ja | Japanese | Dictionary, Translator |
| ko | Korean | Dictionary, Translator |
| fr | French | Dictionary, Translator |
| de | German | Dictionary, Translator |
| es | Spanish | Dictionary, Translator |
| it | Italian | Dictionary, Translator |
| pt | Portuguese | Dictionary, Translator |
| ru | Russian | Dictionary, Translator |
| ar | Arabic | Dictionary, Translator |
| hi | Hindi | Dictionary, Translator |
| auto | Auto-detect | Translator only |

## Architecture

### Database Schema
Both features utilize existing database schema:

#### AIDictionaryConfig Table
```kotlin
@Entity(tableName = "aidictionaryconfig")
data class AIDictionaryConfig(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var dictionaryName: String?,
    var llmId: Long,
    var prompt: String?,
    var sourceLanguage: String?,  // Language code (e.g., "en", "zh")
    var targetLanguage: String?   // Language code (e.g., "en", "zh")
)
```

#### AITranslatorConfig Table
```kotlin
@Entity(tableName = "aitranslatorconfig")
data class AITranslatorConfig(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var translatorName: String?,
    var llmId: Long,
    var prompt: String?,
    var isDefault: Boolean = false,
    var sourceLanguage: String?,  // Language code (e.g., "en", "zh")
    var targetLanguage: String?   // Language code (e.g., "en", "zh")
)
```

### Service Layer

#### AIDictionaryService
**Purpose**: Provides word definition lookup with multi-language support

**Key Methods**:
- `getWordDefinition(word, config, llmConfig)`: Main entry point (42 lines)
- `checkCache(word, llmConfigId)`: Cache lookup (13 lines)
- `buildDictionaryPrompts(word, sourceLanguageName, targetLanguageName)`: Prompt building (30 lines)
- `cacheResults(results, word)`: Result caching (9 lines)
- `getLanguageName(languageCode)`: Code to name conversion (1 line)

**Language Map**:
```kotlin
companion object {
    private val LANGUAGE_NAMES = mapOf(
        "en" to "English",
        "zh" to "Chinese",
        // ... 10 more language pairs
    )
}
```

#### AITranslatorService
**Purpose**: Provides text translation with multi-language support

**Key Methods**:
- `translateText(text, sourceLanguage, targetLanguage, config, llmConfig)`: Main entry point
- `getLanguageName(languageCode)`: Code to name conversion with "auto" support

**Special Features**:
- Supports "auto" language code for automatic source language detection

## Prompt Engineering

### AI Dictionary System Prompt Template
```
You are an experienced dictionary assistant.
Your task is to provide accurate and comprehensive definitions for words
and phrases in {sourceLanguageName}.
You should provide translations and explanations in {targetLanguageName}.
You should be able to handle complex queries and provide detailed explanations.
Your responses should be clear, concise, and easy to understand.
IMPORTANT: You MUST respond with valid JSON format.
Your response should be a JSON object with a 'definitions' array
containing definition objects.
Each definition object should have: 'headword', 'phrase', 'sense',
'phonetics', 'def_en', 'def_cn', and 'example' fields.
headword: the key word to look up in {sourceLanguageName}.
phrase: the phrase that the word belongs to.
If not empty, the definitions will be for the entire phrase.
sense: the Part of Speech (grammatical category), such as nouns, verbs,
adjectives, adverbs, pronouns, prepositions, conjunctions, and interjections.
phonetics: contains phonetic transcription
(e.g., 'UK/kaɪnd/ US/kaɪnd/' for English words).
def_en: definition in {sourceLanguageName}.
def_cn: definition/translation in {targetLanguageName}.
example: example sentence in {sourceLanguageName}.
```

### AI Dictionary User Prompt Template
```
Please provide the definitions of the word or phrase "{word}"
(in {sourceLanguageName}) in JSON format with a 'definitions' array
containing definition objects.
Each definition should have: 'headword', 'phrase', 'sense', 'phonetics',
'def_en' ({sourceLanguageName} definition),
'def_cn' ({targetLanguageName} translation), and 'example' fields.
```

### AI Translator System Prompt Template
```
You are an experienced translator.
Your task is to translate text from {sourceLanguageName} to {targetLanguageName}
accurately and fluently. You should be able to handle complex sentences and idioms.
Your responses should be clear, concise, and easy to understand.
Key Points: Accuracy is Paramount; Fluent and Natural Writing; Standardized Terminology.
IMPORTANT: You MUST respond with valid JSON format.
Your response should be a JSON object with a 'translation' map containing
a translation object, which contains:
translatedText (the translated text in {targetLanguageName}),
sourceLanguage (language code: {sourceLanguageCode}),
targetLanguage (language code: {targetLanguageCode}).
```

### AI Translator User Prompt Template
```
Please provide the translation of the following text
from {sourceLanguageName} to {targetLanguageName}: "{text}"
```

## Usage Examples

### Example 1: English to Japanese Dictionary
**Configuration**:
- Source Language: en (English)
- Target Language: ja (Japanese)

**System Prompt** (excerpt):
```
Your task is to provide accurate and comprehensive definitions for words
and phrases in English.
You should provide translations and explanations in Japanese.
```

**Expected Response**:
```json
{
  "definitions": [
    {
      "headword": "run",
      "phrase": "",
      "sense": "verb",
      "phonetics": "UK/rʌn/ US/rʌn/",
      "def_en": "to move using your legs, going faster than walking",
      "def_cn": "走ることよりも速く脚を使って移動すること",
      "example": "I run every morning before work."
    }
  ]
}
```

### Example 2: French to English Translation
**Configuration**:
- Source Language: fr (French)
- Target Language: en (English)

**System Prompt** (excerpt):
```
Your task is to translate text from French to English
accurately and fluently.
```

**Input**: "Bonjour, comment allez-vous?"

**Expected Response**:
```json
{
  "translation": {
    "translatedText": "Hello, how are you?",
    "sourceLanguage": "fr",
    "targetLanguage": "en"
  }
}
```

### Example 3: Auto-Detect to Chinese Translation
**Configuration**:
- Source Language: auto (Auto-detect)
- Target Language: zh (Chinese)

**System Prompt** (excerpt):
```
Your task is to translate text from auto-detect to Chinese
accurately and fluently.
```

**Input**: "Good morning"

**Expected Response**:
```json
{
  "translation": {
    "translatedText": "早上好",
    "sourceLanguage": "en",
    "targetLanguage": "zh"
  }
}
```

## Code Quality Standards

### Method Length Limits
- Maximum method length: 60 lines
- `getWordDefinition()`: Refactored from 65 to 42 lines
- Helper methods: All under 30 lines

### Cyclomatic Complexity
- Maximum complexity: 15
- `getLanguageName()`: Reduced from 15 to ~3 using map lookup

### Line Length
- Maximum line length: 120 characters
- All prompt strings properly wrapped

## Testing Considerations

### Unit Testing
1. Test language code to name conversion
2. Test prompt building with various language combinations
3. Test cache operations
4. Test error handling for unsupported language codes

### Integration Testing
1. Test end-to-end dictionary lookup with different language pairs
2. Test end-to-end translation with different language pairs
3. Test auto-detect language mode
4. Test cache behavior across language changes

### Edge Cases
1. Unsupported language code → fallback to uppercase code
2. Null language values → default to "en" and "zh"
3. "auto" language code in dictionary → treated as uppercase "AUTO"
4. "auto" language code in translator → converted to "auto-detect"

## Migration Notes

### Backward Compatibility
- Existing configurations without language fields default to English (en) and Chinese (zh)
- No database migration required (fields already exist)
- No changes to public API contracts

### User Impact
- Users with existing AI Dictionary/Translator configs will see no functional change
- Users can now edit configs to support new language pairs
- UI already supported language selection (no UI changes needed)

## Performance Considerations

### Caching Strategy
- Cache key includes: word/text + source language + target language + LLM config ID
- Cache invalidation: Time-based (managed by AICacheRepository)
- Language change clears relevant cache entries

### Memory Impact
- Language name map: ~300 bytes (12 entries × ~25 bytes)
- Minimal memory overhead per service instance

## Security Considerations

### Input Validation
- Language codes validated against known set
- Unknown codes fallback to uppercase display (no injection risk)
- Prompt injection mitigation: User input properly escaped in JSON

### API Key Security
- LLM API keys stored encrypted (handled by existing LLMConfig)
- No language-specific security requirements

## Future Enhancements

### Potential Improvements
1. Add more languages (Hebrew, Turkish, Vietnamese, Thai, etc.)
2. Support regional variants (en-US, en-GB, zh-CN, zh-TW)
3. Dynamic language detection for dictionary lookups
4. Language-specific prompt templates
5. User-customizable prompt templates per language pair

### Extensibility Points
1. Language map can be extended in companion objects
2. Prompt templates can be moved to configuration
3. Language detection service can be added

## References

### Related Files
- `app/src/main/java/com/lmyby/ankiquicker/data/ai/AIDictionaryConfig.kt`
- `app/src/main/java/com/lmyby/ankiquicker/data/ai/AITranslatorConfig.kt`
- `app/src/main/java/com/lmyby/ankiquicker/data/ai/service/AIDictionaryService.kt`
- `app/src/main/java/com/lmyby/ankiquicker/data/ai/service/AITranslatorService.kt`
- `app/src/main/java/com/lmyby/ankiquicker/ui/ai/AIDictionaryConfigEditorActivity.kt`
- `app/src/main/java/com/lmyby/ankiquicker/ui/ai/AITranslatorConfigEditorActivity.kt`

### Git Commits
- `43ff7eb`: feat: add multi-language support for AI dictionary prompts
- `fe35290`: feat: improve AI translator prompts with full language names
- `b64eb18`: style: fix Detekt linting issues in AI service files
- `a59eaef`: refactor: extract methods from getWordDefinition to reduce complexity
