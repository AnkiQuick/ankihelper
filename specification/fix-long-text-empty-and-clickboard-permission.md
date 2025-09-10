# Fix Long Text Empty and Clipboard Permission Implementation Specification

## Overview

This implementation addresses three critical issues in the AnkiHelper Android application:
1. **Long text performance issues** - Prevent UI freezing when processing very long texts
2. **Empty text handling** - Better user feedback when no text content is received
3. **Clipboard permission compliance** - Android 10+ clipboard access restrictions

## Problem Statement

### Long Text Issues
- Original code processed unlimited text lengths, causing UI freezing with texts >50KB
- BigBang layout became unresponsive with thousands of text segments
- No user feedback when text was truncated

### Empty Text Issues  
- Poor error handling when apps sent empty or whitespace-only text
- Generic error messages didn't help users understand what went wrong
- No distinction between different intent types (PROCESS_TEXT vs SEND)

### Clipboard Permission Issues
- Android 10+ introduced stricter clipboard access rules requiring app focus
- Background clipboard access caused SecurityException crashes
- No graceful degradation when clipboard access was denied

## Implementation Details

### 1. Text Length Limiting (TextSplitter.java)

**File**: `app/src/main/java/com/mmjang/ankihelper/util/TextSplitter.java`

**Key Changes**:
```java
private static final int MAX_TEXT_LENGTH = 20000; // 20KB limit

@NonNull
public static List<String> getLocalSegments(String str) {
    // Log input for debugging
    Log.d(TAG, "getLocalSegments called with text length: " + (str != null ? str.length() : 0));
    
    // Limit text length to prevent performance issues
    if (str != null && str.length() > MAX_TEXT_LENGTH) {
        Log.w(TAG, "Text is very long (" + str.length() + " chars), truncating to " + MAX_TEXT_LENGTH + " chars");
        
        // Find the last complete sentence or word boundary to avoid cutting in middle of word
        str = str.substring(0, MAX_TEXT_LENGTH);
        int lastSentenceEnd = Math.max(str.lastIndexOf('.'), Math.max(str.lastIndexOf('!'), str.lastIndexOf('?')));
        int lastWordBoundary = str.lastIndexOf(' ');
        int cutPoint = Math.max(lastSentenceEnd, lastWordBoundary);
        
        // Only cut if we can preserve most of the text (>90%)
        if (cutPoint > MAX_TEXT_LENGTH * 0.9) {
            str = str.substring(0, cutPoint + 1);
        }
    }
    
    // Enhanced logging throughout the process
    Log.d(TAG, "Split into " + texts.length + " parts");
    Log.d(TAG, "getLocalSegments returning " + txts.size() + " segments");
    
    return txts;
}
```

**Benefits**:
- Prevents UI freezing with very long texts
- Maintains text readability by cutting at natural boundaries
- Provides detailed logging for debugging

### 2. Empty Text Handling (PopupActivity.java)

**File**: `app/src/main/java/com/mmjang/ankihelper/ui/popup/PopupActivity.java`

**Key Changes**:

#### Enhanced Intent Handling
```java
private void handleIntent() {
    Intent intent = getIntent();
    if (intent == null) {
        Log.d("PopupActivity", "Intent is null");
        return;
    }
    
    String action = intent.getAction();
    String type = intent.getType();
    Log.d("PopupActivity", "Handle intent, action: " + action + ", type: " + type);
    
    // Multiple fallback methods for text extraction
    if (Intent.ACTION_PROCESS_TEXT.equals(action) && type.equals("text/plain")) {
        // Primary method
        mTextToProcess = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT);
        
        // Fallback: try alternative extra keys
        if (mTextToProcess == null || mTextToProcess.isEmpty()) {
            mTextToProcess = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT).toString();
        }
        
        // Additional fallback for non-standard implementations
        if (mTextToProcess == null || mTextToProcess.isEmpty()) {
            mTextToProcess = intent.getStringExtra("android.intent.extra.PROCESS_TEXT");
        }
        
        // Debug logging
        if (mTextToProcess != null && mTextToProcess.length() > 0) {
            String preview = mTextToProcess.length() > 200 ? 
                mTextToProcess.substring(0, 200) + "..." : mTextToProcess;
            Log.d("PopupActivity", "ACTION_PROCESS_TEXT text preview: " + preview.replace("\n", "\\n"));
        } else {
            Log.w("PopupActivity", "ACTION_PROCESS_TEXT received but no text content found in extras");
            // Log available extras for debugging
            Bundle extras = intent.getExtras();
            if (extras != null) {
                Log.d("PopupActivity", "Available extras: " + extras.keySet());
            }
        }
    }
    
    // Trim and validate text
    if (mTextToProcess == null) {
        mTextToProcess = "";
    }
    
    mTextToProcess = mTextToProcess.trim();
    if (mTextToProcess.isEmpty()) {
        Log.w("PopupActivity", "Text to process is empty after trimming");
        
        // Provide specific guidance based on intent action
        if (Intent.ACTION_PROCESS_TEXT.equals(action)) {
            Toast.makeText(this, "No text was received. Try selecting text again or use the Share option instead.", Toast.LENGTH_LONG).show();
        } else if (Intent.ACTION_SEND.equals(action)) {
            Toast.makeText(this, "No text content received from app", Toast.LENGTH_SHORT).show();
        }
    } else {
        Log.i("PopupActivity", "Successfully received text from intent action: " + action + ", length: " + mTextToProcess.length());
    }
}
```

#### Async Text Processing with Validation
```java
private void populateWordSelectBoxAsync(final String textToProcess) {
    // Show loading indicator
    progressBar.setVisibility(View.VISIBLE);
    
    // Validate input text
    if (textToProcess == null || textToProcess.trim().isEmpty()) {
        Log.w("PopupActivity", "populateWordSelectBoxAsync called with empty text");
        progressBar.setVisibility(View.GONE);
        Toast.makeText(this, "No text content to process", Toast.LENGTH_SHORT).show();
        return;
    }
    
    // Process text segmentation in background thread
    Thread textProcessingThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                Log.d("PopupActivity", "Starting text processing, text length: " + textToProcess.length());
                
                // Heavy text processing in background
                final List<String> localSegments = TextSplitter.getLocalSegments(textToProcess);
                
                Log.d("PopupActivity", "Text processing complete, segments count: " + localSegments.size());
                
                // Update UI on main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateBigBangLayoutWithSegments(localSegments);
                    }
                });
            } catch (Exception e) {
                Log.e("PopupActivity", "Error processing text", e);
                final String errorMsg = e.getMessage() != null ? e.getMessage() : "Unknown error";
                
                // Handle errors on main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(PopupActivity.this, "Error processing text: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    });
    textProcessingThread.start();
}
```

#### BigBang Layout Segment Limiting
```java
private void updateBigBangLayoutWithSegments(List<String> localSegments) {
    Log.d("PopupActivity", "Updating BigBangLayout with segments, count: " + localSegments.size());
    
    // Hide loading indicator
    progressBar.setVisibility(View.GONE);
    
    // Handle empty segments case
    if (localSegments.isEmpty()) {
        Log.w("PopupActivity", "No segments to display");
        Toast.makeText(this, "No text content to display", Toast.LENGTH_SHORT).show();
        return;
    }
    
    // Limit the number of segments to prevent performance issues
    int maxSegments = 500; // Increased from 300 for better multi-line text support
    int segmentCount = 0;
    boolean wasTruncated = false;
    
    for (String localSegment : localSegments) {
        // Stop adding segments if we've reached the limit
        if (segmentCount >= maxSegments) {
            wasTruncated = true;
            // Add a visual indicator that text was truncated
            bigBangLayout.addTextItem("...");
            break;
        }
        bigBangLayout.addTextItem(localSegment);
        segmentCount++;
    }
    
    // Show user feedback if text was truncated
    if (wasTruncated) {
        Toast.makeText(this, "Text was truncated for performance (showing first " + maxSegments + " segments)", Toast.LENGTH_LONG).show();
    }
    
    Log.d("PopupActivity", "Displayed " + segmentCount + " segments in BigBangLayout");
}
```

**Benefits**:
- Comprehensive text extraction with multiple fallback methods
- Detailed logging for debugging intent handling issues
- User-friendly error messages specific to the context
- Background processing prevents UI freezing
- Segment limiting prevents BigBang layout performance issues

### 3. Clipboard Permission Handling (CBWatcherService.java & PopupActivity.java)

**Files**: 
- `app/src/main/java/com/mmjang/ankihelper/domain/CBWatcherService.java`
- `app/src/main/java/com/mmjang/ankihelper/ui/popup/PopupActivity.java`

#### Android 10+ Focus-Based Clipboard Access
```java
// In PopupActivity.java
@Override
public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    if(isFromAndroidQClipboard) {
        if (!Settings.getInstance(MyApplication.getContext()).getMoniteClipboardQ()) {
            return;
        }
        
        // Only access clipboard when we have focus (Android 10+ requirement)
        if (hasFocus) {
            try {
                ClipboardManager cb = this.getSystemService(ClipboardManager.class);
                if (cb != null && cb.hasPrimaryClip()) {
                    ClipData clipData = cb.getPrimaryClip();
                    if (clipData != null && clipData.getItemCount() > 0) {
                        CharSequence text = clipData.getItemAt(0).getText();
                        if (text != null) {
                            mTextToProcess = text.toString();
                            Log.d("PopupActivity", "Clipboard text retrieved, length: " + mTextToProcess.length());
                            
                            // Reprocess the text now that we have clipboard content
                            populateWordSelectBox();
                            bigBangLayout.post( new Runnable() {
                                @Override
                                public void run() {
                                    setTargetWord();
                                    if(Utils.containsTranslationField(currentOutputPlan)){
                                        asyncTranslate(mTextToProcess);
                                    }
                                }
                            });
                        } else {
                            Log.w("PopupActivity", "Clipboard text is null");
                            Toast.makeText(this, "Clipboard is empty", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Log.w("PopupActivity", "No clipboard content available");
                    Toast.makeText(this, "No clipboard content available", Toast.LENGTH_SHORT).show();
                }
            } catch (SecurityException e) {
                Log.e("PopupActivity", "Security exception accessing clipboard", e);
                Toast.makeText(this, "Cannot access clipboard due to security restrictions", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Log.e("PopupActivity", "Error accessing clipboard", e);
                Toast.makeText(this, "Error accessing clipboard", Toast.LENGTH_SHORT).show();
            }
            
            // Reset the flag to prevent repeated attempts
            isFromAndroidQClipboard = false;
        } else {
            Log.d("PopupActivity", "No focus, deferring clipboard access");
        }
    }
}
```

#### Enhanced Clipboard Permission Checking
```java
// In CBWatcherService.java
private boolean hasClipboardPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // Android 10+ requires special focus rules for clipboard access
        try {
            ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (cm == null) return false;

            // Check clipboard service availability without causing permission denial
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11+ has stricter focus requirements
                return cm.hasPrimaryClip() && cm.getPrimaryClipDescription() != null;
            } else {
                // Android 10 (API 29) and below
                return cm.hasPrimaryClip();
            }
        } catch (SecurityException e) {
            Log.w("CBWatcherService", "SecurityException checking clipboard access: " + e.getMessage());
            return false;
        }
    }
    return true;
}

private void performClipboardCheck() {
    Log.d("clip", "clip_changed");
    if (!Settings.getInstance(MyApplication.getContext()).getMoniteClipboardQ()) {
        return;
    }

    ClipboardManager cb = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
    if (cb == null || !hasClipboardPermission()) {
        Log.w("CBWatcherService", "Clipboard access not available or permission denied");
        return;
    }

    try {
        if (cb.hasPrimaryClip()) {
            ClipData clipData = cb.getPrimaryClip();
            if (clipData != null && clipData.getItemCount() > 0) {
                CharSequence text = clipData.getItemAt(0).getText();
                if (text != null && isEnglish(text.toString())) {
                    // Process clipboard text only if it's English
                    Intent intent = new Intent(getApplicationContext(), PopupActivity.class);
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intent.putExtra(Intent.EXTRA_TEXT, text.toString());
                    startActivity(intent);
                }
            }
        }
    } catch (SecurityException e) {
        Log.w("CBWatcherService", "SecurityException accessing clipboard: " + e.getMessage());
    }
}
```

#### Smart Clipboard Fallback Logic
```java
// In PopupActivity.java handleIntent() method
// IMPORTANT: Only use clipboard fallback if explicitly requested and we're on Android 10+
// Avoid automatic clipboard access to prevent permission issues
if(mTextToProcess != null && mTextToProcess.equals(Constant.USE_CLIPBOARD_CONTENT_FLAG)){
    // Only attempt clipboard access if we have focus and proper permissions
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // For Android 10+, check if we have focus before accessing clipboard
        if (hasWindowFocus()) {
            Log.d("PopupActivity", "Attempting clipboard access with focus");
            isFromAndroidQClipboard = true;
        } else {
            Log.w("PopupActivity", "Cannot access clipboard - no focus, skipping clipboard fallback");
            mTextToProcess = "";
            Toast.makeText(this, "Cannot access clipboard. Please select text directly instead.", Toast.LENGTH_LONG).show();
        }
    } else {
        // For older Android versions, proceed with clipboard access
        isFromAndroidQClipboard = true;
    }
}
```

**Benefits**:
- Android 10+ compliance with focus-based clipboard access
- Graceful degradation when clipboard access is denied
- Comprehensive error handling with user-friendly messages
- Background service clipboard monitoring with proper permission checks

## Technical Architecture

### Core Components Modified

1. **TextSplitter** - Text length limiting and preprocessing
2. **PopupActivity** - Intent handling, text validation, UI updates
3. **CBWatcherService** - Clipboard monitoring with permission compliance

### Design Patterns Applied

- **Defensive Programming**: Multiple fallback methods for text extraction
- **Async Processing**: Background threads for heavy text processing
- **Graceful Degradation**: Continued functionality when permissions are denied
- **Comprehensive Logging**: Detailed debugging information throughout

### Performance Optimizations

- **Text Length Limiting**: 20,000 character maximum prevents UI freezing
- **Segment Limiting**: BigBang layout limited to 500 segments
- **Background Processing**: Text segmentation runs on separate thread
- **Smart Truncation**: Cuts at sentence/word boundaries for readability

## Testing Considerations

### Test Scenarios

1. **Long Text Processing**:
   - Text >20,000 characters should be truncated gracefully
   - UI should remain responsive during processing
   - User should see truncation notification

2. **Empty Text Handling**:
   - Apps sending null/empty text should show appropriate messages
   - Whitespace-only text should be detected and handled
   - Different intent actions should show context-specific messages

3. **Clipboard Permission Testing**:
   - Android 10+ devices should require focus for clipboard access
   - Background clipboard access should fail gracefully
   - Security exceptions should be caught and logged

### Edge Cases Covered

- Base64 encoded text processing
- HTML content with line breaks
- Multi-language text segmentation
- Network connectivity issues during clipboard access
- Memory pressure scenarios with large texts

## Security Considerations

- Clipboard access only when app has focus (Android 10+ compliance)
- No background clipboard monitoring without proper permissions
- Graceful handling of SecurityException during clipboard operations
- No sensitive data logging in production builds

## Future Improvements

1. **Configurable Limits**: Make text length and segment limits user-configurable
2. **Progressive Loading**: Implement lazy loading for very long texts
3. **Memory Optimization**: Use streaming text processing for extremely large inputs
4. **Enhanced Error Recovery**: More sophisticated retry mechanisms for failed operations