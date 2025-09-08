# SDK 31 Compatibility Fix Specification

## Issue
The AnkiHelper app fails to connect to AnkiDroid when running on Android SDK 31 (Android 12), showing the error message "Api not available!" despite working correctly on SDK 34 (Android 14).

## Root Causes
1. **Package Visibility Changes**: Android 12 introduced stricter package visibility rules requiring explicit declaration of accessed packages
2. **Permission Handling**: Stricter permission enforcement in Android 12 affecting content provider access
3. **Exception Handling**: Unhandled exceptions in content provider queries causing silent failures
4. **Connection Detection**: Single-point failure in AnkiDroid connection detection logic

## Solution Implementation

### 1. Enhanced AnkiDroid Connection Detection
**File**: `app/src/main/java/com/mmjang/ankihelper/anki/AnkiDroidHelper.java`
- Modified `isAnkiDroidRunning()` method to use multiple verification approaches:
  - Primary: Check deck list availability
  - Secondary: Check model list availability
  - Tertiary: Verify AnkiDroid package existence
- Added comprehensive exception handling to prevent crashes

### 2. Improved Error Handling
**File**: `app/src/main/java/com/ichi2/anki/api/AddContentApi.java`
- Wrapped `getDeckList()` and `getModelList()` methods in try-catch blocks
- Added specific error handling for Android 12+ compatibility issues
- Enhanced `getAnkiDroidPackageName()` with exception handling

### 3. UI/UX Improvements
**File**: `app/src/main/java/com/mmjang/ankihelper/ui/LauncherActivity.java`
- Modified button click handlers to attempt starting AnkiDroid if not running
- Updated permission checking logic with better Android version handling
- Fixed notification permission check for Android 13+ (SDK 33+)

### 4. Manifest Updates
**File**: `app/src/main/AndroidManifest.xml`
- Added `<queries>` element declaring access to AnkiDroid package (`com.ichi2.anki`)
- Ensures proper package visibility for Android 11+ (SDK 30+) compatibility

### 5. Build Configuration
**File**: `app/build.gradle`
- Maintained `minSdkVersion 31` as required
- Kept `targetSdkVersion 34` for broad compatibility

## Testing
- Verified successful build with `./gradlew assembleDebug`
- Confirmed compatibility with both SDK 31 and SDK 34 targets

## Expected Behavior
- App successfully connects to AnkiDroid on SDK 31
- Error handling provides meaningful feedback to users
- Resource consumption remains within acceptable limits
- Backward compatibility with newer SDK versions maintained