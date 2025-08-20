# LLM Provider Selection Feature Specification

## Overview
This document describes the implementation of the LLM provider selection feature for the Anki Helper application. This feature enhances the user experience by providing predefined provider options while maintaining flexibility for custom configurations.

## Feature Description

### Provider Selection Dropdown
The LLM configuration editor now includes a provider selection dropdown with the following options:
- **Custom** - Allows users to specify their own base URL
- **DeepSeek** - Preconfigured with `https://api.deepseek.com`
- **OpenAI** - Preconfigured with `https://api.openai.com`
- **Aliyun** - Preconfigured with `https://dashscope.aliyuncs.com`

### Behavior
1. When a predefined provider is selected:
   - The base URL field is automatically populated with the provider's endpoint
   - The base URL field is disabled to prevent editing
   - The correct endpoint is used for API calls

2. When "Custom" provider is selected:
   - The base URL field becomes editable
   - Users can input any custom endpoint
   - The application will use the provided URL for API calls

3. API Request Formatting:
   - All providers now consistently receive the `response_format` parameter
   - This ensures compatibility with providers that expect structured output
   - Eliminates "Bad Request" errors related to missing response format specifications

## Implementation Details

### UI Changes
- Added a Spinner component to the LLM configuration editor layout
- Modified the layout to accommodate the provider selection dropdown
- Updated the base URL field behavior based on provider selection

### Backend Changes
- Modified `LLMConfigEditorActivity.java` to handle provider selection logic
- Updated `AIService.java` to include `response_format` parameter for all providers
- Added string resources for provider names and labels

### Data Model
- The `LLMConfig` model remains unchanged
- Provider information is determined at runtime based on the base URL
- No database schema changes were required

## Benefits
1. **Improved User Experience**: Users can quickly select from popular providers without needing to remember their endpoints
2. **Reduced Configuration Errors**: Predefined URLs prevent typos and incorrect endpoint specifications
3. **Enhanced Compatibility**: Consistent API request formatting ensures compatibility with all supported providers
4. **Maintainability**: Centralized provider management makes it easier to add new providers in the future

## Future Considerations
- Additional providers can be easily added to the dropdown by extending the provider mapping
- Provider-specific configurations could be added in future versions if needed
- Validation could be added to ensure selected providers are functioning correctly