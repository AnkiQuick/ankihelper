# AI Dictionary and Translator Configuration UI Improvement Specification

## Overview
This document specifies the improvements to be made to the AI Dictionary and AI Translator configuration editor UIs to add proper labels for each input field.

## Current State
Currently, the configuration editor UIs for both AI Dictionary and AI Translator use placeholder hints inside input fields to indicate what information should be entered. This approach is not ideal for usability as:
1. Hints disappear when users start typing
2. Users may forget what information is required for each field
3. There's no clear visual distinction between labels and user input

## Target State
The improved UI should feature clear labels positioned above each input field, following Material Design guidelines. This will make it immediately clear what information is required in each field.

## UI Requirements

### Common Elements for Both Editors
All input fields should follow this pattern:
- Clear label text positioned above the input field
- Input field (text input or dropdown) positioned below its label
- Consistent spacing between elements

### AI Dictionary Configuration Editor
The following fields should be properly labeled:
1. **Dictionary Name** - Text input field for naming the dictionary configuration
2. **Model Name** - Dropdown list for selecting the LLM model
3. **Source Language** - Dropdown list for selecting the source language
4. **Target Language** - Dropdown list for selecting the target language

### AI Translator Configuration Editor
The following fields should be properly labeled:
1. **Translator Name** - Text input field for naming the translator configuration
2. **Model Name** - Dropdown list for selecting the LLM model
3. **Set as Default Translator** - Checkbox for marking as default (this already has a label)
4. **Source Language** - Dropdown list for selecting the source language
5. **Target Language** - Dropdown list for selecting the target language

### LLM Configuration Editor
The following fields should be properly labeled:
1. **LLM Name** - Text input field for naming the LLM configuration
2. **Base URL** - Text input field for the API base URL
3. **API Token** - Password input field for the API token
4. **Model Name** - Text input field for the model name

### TTS Configuration Editor
The following fields should be properly labeled:
1. **TTS Name** - Text input field for naming the TTS configuration
2. **Base URL** - Text input field for the API base URL
3. **API Token** - Password input field for the API token
4. **Model Name** - Text input field for the model name

## Implementation Plan

### 1. Layout Files Modification
- Modify `activity_ai_dictionary_config_editor.xml`
- Modify `activity_ai_translator_config_editor.xml`
- Modify `activity_llm_config_editor.xml`
- Modify `activity_tts_config_editor.xml`
- Replace hint-based labeling with explicit TextView labels positioned to the left of input fields

### 2. Java Code Updates
- Ensure Java code properly binds to the updated layout elements
- No functional changes required, only UI layout changes

### 3. String Resources
- Add new string resources for all labels
- Add Chinese translations for all new string resources

### 3. Testing
- Verify that all fields are properly labeled
- Ensure that the UI is responsive and follows Material Design guidelines
- Test on different screen sizes to ensure proper layout

## Visual Representation

### Before (Current Implementation)
```
[Dictionary Name           ]
[LLM                      ▼]
[Source Language          ▼]
[Target Language          ▼]
```

### After (Improved Implementation)
#### AI Dictionary and Translator Editors
```
Dictionary Name     [                           ]
Model Name          [                           ▼]
Source Language     [                           ▼]
Target Language     [                           ▼]
```

#### LLM and TTS Editors
```
LLM Name            [                           ]
Base URL            [                           ]
API Token           [                           ]
Model Name          [                           ]
```

## Files to be Modified
1. `app/src/main/res/layout/activity_ai_dictionary_config_editor.xml`
2. `app/src/main/res/layout/activity_ai_translator_config_editor.xml`
3. `app/src/main/res/layout/activity_llm_config_editor.xml`
4. `app/src/main/res/layout/activity_tts_config_editor.xml`

## Acceptance Criteria
- All input fields have clear labels positioned above them
- Labels are properly localized using string resources
- UI follows Material Design guidelines
- No functional changes to the underlying implementation
- Both editors maintain consistent styling and spacing