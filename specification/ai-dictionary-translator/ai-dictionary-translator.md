# AI Dictionary and Translator Feature Specification

This document outlines the requirements for adding AI capabilities to the AnkiHelper app, including AI dictionary lookup, AI translation, and TTS pronunciation features.

## Overview

Added AI as both a dictionary and translator:
- Send selected words to LLM for definitions
- Send sentences to LLM for translation
- Use TTS to pronounce words and sentences

## Requirements

### Configuration Management

#### LLM Configuration
- In LauncherActivity.java, add LLM configuration item
- Clicking enters LLM config page showing all configured LLMs
- Bottom-right add button to create new LLM configuration
- Each LLM config requires:
  - Name (config name)
  - API base URL
  - API token (encrypted storage)
  - Model name
- Top-right save button to save configuration
- After save, return to LLM config page showing all added configurations
- Save info in new database table

- TTS configuration
        - click this item, enter into the TTS config, it wll display all configred TTS; also at the bottom-right, there a add button to add a new tts
      - each tts config has serveral option to input

- AI dictionary Configurarion
    - based on the llm setting, we can create ai dictionary to be used in the plan manager
    - click this item, enter to the display all configured ai dictionay; also at the bottom-right, there a add button to add a new ai dictionary
    - click add button, we can add new ai dictionary
    - each ai dictionary contains
      - dictionary name
      - llm name: llm to be used, drop down list from LLM configration
      - prmopt: prompt used to get the word definitions, inlcuding the output format
      - target language: language to translate to (source language is implied as English)

- AI translator Configurarion
    - based on the llm setting, we can create AI translator to be used in the translation
    - click this item, enter to the display all configured AI translator; also at the bottom-right, there a add button to add a new AI translator
    - click add button, we can add new AI translator
    - each AI translator contains
      - AI translator name
      - llm name: llm to be used, drop down list from LLM configration
      - prmopt: prompt used to get the sentence translation, inlcuding the output format
      - source language: language to translate from
      - target language: language to translate to
    - we can set one as the default translator
    - when `footer_translate` (ImageButton) is clicked in PopupActivity, it uses the default translator to perform the translation
- All LLM and TTS configs saved in new database tables

### Integration

#### Plan Manager
- Can choose AI dictionary as normal dictionary in plan configuration

#### Popup Activity
- `footer_translate` (ImageButton): Triggers translation of selected text using AI translator

## Implementation Details

### Database Structure

The configuration tables for LLM, TTS, AI Dictionary, and AI Translator are created using LitePal ORM and stored in the main application database, following the same pattern as existing tables.

#### LitePal Configuration

The main LitePal configuration includes the new models:

```xml
<!-- assets/litepal.xml -->
<litepal>
    <dbname value="ankihelper.db" />
    <version value="4" />
    <list>
        <mapping class="com.mmjang.ankihelper.data.plan.OutputPlan"></mapping>
        <mapping class="com.mmjang.ankihelper.data.model.UserTag"></mapping>
        <mapping class="com.mmjang.ankihelper.data.history.History"></mapping>
        <mapping class="com.mmjang.ankihelper.data.ai.LLMConfig"></mapping>
        <mapping class="com.mmjang.ankihelper.data.ai.TTSConfig"></mapping>
        <mapping class="com.mmjang.ankihelper.data.ai.AIDictionaryConfig"></mapping>
        <mapping class="com.mmjang.ankihelper.data.ai.AITranslatorConfig"></mapping>
    </list>
</litepal>
```

Note that the database version has been incremented to 4 to trigger the creation of new tables.

#### Model Classes

##### LLMConfig Model
```java
package com.mmjang.ankihelper.data.ai;

import org.litepal.crud.LitePalSupport;

public class LLMConfig extends LitePalSupport {
    private long id;
    private String name;
    private String baseUrl;
    private String apiToken; // Encrypted
    private String modelName;
    
    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
}
```

##### TTSConfig Model
```java
package com.mmjang.ankihelper.data.ai;

import org.litepal.crud.LitePalSupport;

public class TTSConfig extends LitePalSupport {
    private long id;
    private String name;
    private String baseUrl;
    private String apiToken; // Encrypted
    private String modelName;
    
    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
}
```

##### AIDictionaryConfig Model
```java
package com.mmjang.ankihelper.data.ai;

import org.litepal.crud.LitePalSupport;

public class AIDictionaryConfig extends LitePalSupport {
    private long id;
    private String dictionaryName;
    private long llmId; // Foreign key to LLMConfig
    private String prompt;
    private String sourceLanguage;
    private String targetLanguage;
    
    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDictionaryName() {
        return dictionaryName;
    }

    public void setDictionaryName(String dictionaryName) {
        this.dictionaryName = dictionaryName;
    }

    public long getLlmId() {
        return llmId;
    }

    public void setLlmId(long llmId) {
        this.llmId = llmId;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }
}
```

##### AITranslatorConfig Model
```java
package com.mmjang.ankihelper.data.ai;

import org.litepal.crud.LitePalSupport;

public class AITranslatorConfig extends LitePalSupport {
    private long id;
    private String translatorName;
    private long llmId; // Foreign key to LLMConfig
    private String prompt;
    private boolean isDefault;
    private String sourceLanguage;
    private String targetLanguage;
    
    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTranslatorName() {
        return translatorName;
    }

    public void setTranslatorName(String translatorName) {
        this.translatorName = translatorName;
    }

    public long getLlmId() {
        return llmId;
    }

    public void setLlmId(long llmId) {
        this.llmId = llmId;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }
}
```

### Cache Structure

The AI cache tables are created using LitePal ORM and stored in a separate SQLite database.

### Theme Implementation

All AI configuration activities use the standard `Theme.AnkiHelper` theme. This is implemented by:

1. Declaring activities in `AndroidManifest.xml` with the standard theme:
   ```xml
   <activity android:name=".ui.ai.AIConfigActivity"
       android:label="AI Configuration"
       android:theme="@style/Theme.AnkiHelper"
       android:parentActivityName=".ui.LauncherActivity">
   </activity>
   ```

2. Using the built-in ActionBar in activities:
   ```java
   // In onCreate method of activities
   if (getSupportActionBar() != null) {
       getSupportActionBar().setDisplayHomeAsUpEnabled(true);
       getSupportActionBar().setTitle("Activity Title");
   }
   ```

**Note**: The current implementation works correctly as all AI activities are using the standard `Theme.AnkiHelper` theme (which includes an ActionBar) and properly utilizing the ActionBar through `getSupportActionBar()`. There is a `Theme.AnkiHelperPink.NoActionBar` defined in themes.xml for the pink theme variant, but no equivalent for the standard theme.

#### LitePal Configuration for Cache

Create a separate LitePal configuration for AI cache:

```xml
<!-- assets/ai_litepal.xml -->
<litepal>
    <dbname value="ai_cache.db" />
    <version value="1" />
    <list>
        <mapping class="com.mmjang.ankihelper.data.ai.AIDictionaryCache"></mapping>
        <mapping class="com.mmjang.ankihelper.data.ai.AITranslatorCache"></mapping>
    </list>
</litepal>
```

Initialize the AI cache database before creating tables, similar to how the main database is initialized in `MyApplication.java`:

```java
// Initialize AI cache database
LitePalDB aiDB = LitePalDB.fromDefault("ai_cache");
LitePal.use(aiDB);
```

#### Model Classes for Cache

##### AIDictionaryCache Model
```java
import org.litepal.crud.LitePalSupport;

public class AIDictionaryCache extends LitePalSupport {
    private long id;
    private String hwd; // Headword (the word being defined)
    private String phrase; // Fixed phrase with special meaning or usage (empty for single words)
    private String sense; // Part of speech (verb, noun, adjective, adverb, etc.) - empty for phrases
    private String phonetics; // British and American English phonetics (e.g., "UK: /ɡʊd/ US: /ɡʊd/")
    private String defEn; // English definition
    private String defCn; // Chinese definition
    private String example; // Example sentence
    private long llmConfigId; // Foreign key to LLMConfig
    private long timestamp; // For expiration
    
    // Getters and setters
    // Same structure as built-in dictionary tables
    // Each row represents one definition/meaning, so multiple rows for words with multiple meanings
}
```

##### AITranslatorCache Model
```java
import org.litepal.crud.LitePalSupport;

public class AITranslatorCache extends LitePalSupport {
    private long id;
    private String sourceText; // The text to be translated
    private String sourceLanguage;
    private String targetLanguage;
    private String translatedText; // The translated result
    private long llmConfigId; // Foreign key to LLMConfig
    private long timestamp; // For expiration
    
    // Getters and setters
}
```

All tables are automatically created by LitePal ORM based on these model classes, following the same pattern as existing models in the codebase.

#### AI Translator Cache Table
```java
public class AITranslatorCache {
    private long id;
    private String sourceText; // The text to be translated
    private String sourceLanguage;
    private String targetLanguage;
    private String translatedText; // The translated result
    private long llmConfigId; // Foreign key to LLMConfig
    private long timestamp; // For expiration
}
```

### Core Functionality

#### AI Dictionary Function
- Get LLM parameters from configuration
- Make API calls to LLM service
- Receive and parse response
- Response contains same fields as other built-in dictionaries
- Cache results in AIDictionaryCache table

#### AI Translation Function
- Get LLM parameters from configuration
- Make API calls to LLM service
- Receive and parse response
- Cache results in AITranslatorCache table

### API Standards
- Use OpenAI API standard for both LLM and TTS services
- Implement rate limiting and queuing for API calls
- Follow best practices for error handling and retries

### Security
- Encrypt API tokens before storing in database
- Implement secure storage mechanisms

### Offline Handling
- Detect network connectivity
- When offline, notify user and suggest using built-in dictionaries
- Disable AI features gracefully when no network is available

### Default Prompts

#### AI Dictionary Default Prompt
```
You are an English dictionary. Provide definitions for the word or phrase "{query}" in the following JSON format:

Return an array of dictionary entries, where each entry represents one meaning, definition, or phrase:

[
  {
    "hwd": "headword (the main word being defined)",
    "phrase": "fixed phrase containing the word (only for phrases, empty for single words)",
    "sense": "part of speech (noun, verb, adjective, adverb, etc.) - empty for phrases",
    "phonetics": "British and American English phonetics (format: 'UK: /pronunciation/ US: /pronunciation/')",
    "defEn": "English definition",
    "defCn": "Chinese definition",
    "example": "example sentence using the word or phrase"
  }
]

Guidelines:
1. For single words:
   - Provide all meanings/definitions of the word
   - For each word meaning:
     * hwd: the word itself
     * phrase: empty (since it's not a phrase)
     * sense: part of speech (noun, verb, etc.)
     * phonetics: both UK and US phonetics (e.g., "UK: /ɡʊd/ US: /ɡʊd/")
     * defEn/defCn: definitions for this meaning
     * example: example sentence for this meaning

2. For phrases containing the word:
   - If the word is part of common phrases or phrasal verbs, include these as separate entries
   - For each phrase:
     * hwd: the original query word
     * phrase: the complete phrase (e.g., "word up", "break word")
     * sense: empty (phrases don't have part of speech)
     * phonetics: empty (phrases don't have phonetics)
     * defEn/defCn: definitions of the phrase
     * example: example sentence using the phrase

3. Return all definitions and phrases for the given word/phrase
4. Each entry is stored as a separate row in the cache table
```

#### AI Translator Default Prompt
```
Translate the following text from {sourceLanguage} to {targetLanguage}: "{text}"
Provide the response in the following JSON format:
{
  "translatedText": "translated text",
  "sourceLanguage": "{sourceLanguage}",
  "targetLanguage": "{targetLanguage}"
}
```

## Current Implementation Status

The following AI configuration activities have been implemented:

1. `AIConfigActivity` - Main entry point for AI configuration
2. `LLMConfigListActivity` - Lists all LLM configurations
3. `LLMConfigEditorActivity` - Edits a single LLM configuration
4. `TTSConfigListActivity` - Lists all TTS configurations
5. `TTSConfigEditorActivity` - Edits a single TTS configuration
6. `AIDictionaryConfigListActivity` - Lists all AI dictionary configurations
7. `AIDictionaryConfigEditorActivity` - Edits a single AI dictionary configuration
8. `AITranslatorConfigListActivity` - Lists all AI translator configurations
9. `AITranslatorConfigEditorActivity` - Edits a single AI translator configuration

All these activities are correctly using `Theme.AnkiHelper` in the manifest, which works properly with the ActionBar implementation.

### Implementation Notes

1. **Theme Consistency**: The current theme implementation works correctly. There is a `Theme.AnkiHelperPink.NoActionBar` defined in themes.xml for the pink theme variant, but no equivalent for the standard theme. This is not an issue since the standard theme works correctly with the ActionBar.

2. **ActionBar Usage**: All activities are correctly using `getSupportActionBar()` to set up the action bar.

## Future Considerations

Most core functionality has been implemented. Future improvements could include:

- Enhanced UI/UX design for configuration screens (will continue iterating on design)
- Advanced caching strategy with expiration policies
- More sophisticated rate limiting implementation
- Comprehensive error handling for edge cases
- Define `Theme.AnkiHelper.NoActionBar` in themes.xml only if a NoActionBar version of the standard theme is specifically needed (current implementation works correctly with standard theme)