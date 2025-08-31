# Clean AI History Feature

## Overview
This feature adds functionality to clean the AI dictionary and AI translator cache records that accumulate over time in the application. It provides users with a way to manage their cache storage and remove old records.

## UI Implementation

### AI Configuration Screen
- Added a new "History Management" button as the fifth button in the AI Configuration activity
- Positioned below the "TTS Manager" button
- Uses the same styling as other buttons in the activity
- Text button with a delete icon

### Clean AI History Screen
- Dedicated activity with two sections:
  1. AI Dictionary cleaning section
  2. AI Translator cleaning section
- Each section contains:
  - Label showing "Total records" with current count
  - Label and input field for specifying number of records to clean (default: 100)
  - "Clean All" switch positioned below the input field
  - "Clean" button to perform the cleaning operation
- When "Clean All" switch is checked, the numeric input field is disabled
- Back button in the action bar for navigation
- Full localization support with English and Chinese translations

## Functionality

### Input Validation
- Numeric input fields only accept numbers
- Negative numbers are rejected with an error message
- Default value of 100 is pre-filled in input fields

### Cleaning Operations
- **Clean Oldest Records**: When a specific count is provided, the oldest records are deleted
- **Clean All Records**: When "Clean All" is selected, all records from the respective table are deleted
- **Confirmation Dialog**: All cleaning operations show a confirmation dialog with "Confirm/Cancel" buttons
- **Feedback**: Toast messages show the results of cleaning operations (success/failure counts)
- **Auto-refresh**: Total records display is automatically updated after cleaning operations

### Data Access
- Added new methods to `AICacheRepository`:
  - `deleteOldestDictionaryCache(int count)`: Delete oldest AI dictionary cache records
  - `deleteAllDictionaryCache()`: Delete all AI dictionary cache records
  - `deleteOldestTranslatorCache(int count)`: Delete oldest AI translator cache records
  - `deleteAllTranslatorCache()`: Delete all AI translator cache records
- Added new methods to `AIConfigRepository` to expose cache cleaning functionality:
  - `deleteOldestAIDictionaryCache(int count)`
  - `deleteAllAIDictionaryCache()`
  - `deleteOldestAITranslatorCache(int count)`
  - `deleteAllAITranslatorCache()`

## Implementation Details

### New Files Created
1. `app/src/main/java/com/mmjang/ankihelper/ui/ai/CleanAIHistoryActivity.java` - Activity for the clean AI history screen
2. `app/src/main/res/layout/activity_clean_ai_history.xml` - Layout for the clean AI history screen

### Modified Files
1. `app/src/main/res/layout/activity_ai_config.xml` - Added "History Management" button as the fifth button
2. `app/src/main/java/com/mmjang/ankihelper/ui/ai/AIConfigActivity.java` - Added button initialization and click handler
3. `app/src/main/res/layout/activity_launcher.xml` - Removed "History Management" button from launcher
4. `app/src/main/java/com/mmjang/ankihelper/ui/LauncherActivity.java` - Removed button initialization and click handler
5. `app/src/main/java/com/mmjang/ankihelper/data/ai/cache/AICacheRepository.java` - Added cache cleaning methods
6. `app/src/main/java/com/mmjang/ankihelper/data/ai/AIConfigRepository.java` - Added public methods to expose cache cleaning functionality
7. `app/src/main/res/values/strings.xml` - Added English string resources
8. `app/src/main/res/values-zh/strings.xml` - Added Chinese translations
9. `app/src/main/AndroidManifest.xml` - Registered CleanAIHistoryActivity

## Fixes Applied
1. **Type Casting Fix**: Updated `CleanAIHistoryActivity.java` to use `MaterialSwitch` instead of `Switch` to match the layout definition
2. **Activity Registration**: Added proper registration for `CleanAIHistoryActivity` in `AndroidManifest.xml`
3. **UI Organization**: Moved "History Management" button to the AI Configuration activity as requested
4. **UI Layout Improvements**: Restructured the Clean AI History activity layout to have labels on the left and input fields on the right, with the "Clean All" switch positioned below the input fields. Added proper spacing between the "Total records" label and its value for better readability. Controlled the height of input fields to match the height of the total records value by setting minHeight properties on TextInputLayout containers and removing unnecessary padding from inner TextInputEditText elements.
5. **Localization**: Added full localization support with English and Chinese translations for all UI elements
6. **Hardcoded Text Fix**: Fixed hardcoded text in the AI Configuration activity layout to use proper string resources

## Verification
- Successfully compiled the project with no errors
- All changes are consistent with the requirements
- The application maintains the same theme and styling as other pages
- Input validation and error handling are properly implemented
- Total records display is updated after cleaning operations
- Activity is properly registered in AndroidManifest.xml and launches correctly
- UI is organized logically with History Management as part of AI Configuration
- Full localization support with English and Chinese translations