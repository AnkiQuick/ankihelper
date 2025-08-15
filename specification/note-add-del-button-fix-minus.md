# Note Add/Delete Button Fix Specification (Minus Icon Implementation)

## Problem Description

The note add/remove button in the PopupActivity was not providing clear visual feedback to users about its current state. Specifically:

1. Button starts with "add" icon (working correctly)
2. User clicks button to add a note (working correctly)
3. After note is added, button should show "minus" icon to indicate it can now be used to cancel/remove the note
4. User clicks minus button again to cancel/remove the note (should work)
5. After note is cancelled/removed, button should go back to "add" icon (working correctly)

The problem was in step 3: After adding a note, instead of showing a clear minus icon, it was showing `ic_ok.png` which is a checkmark, not a minus.

## Solution Overview

The fix implements a clear visual toggle between add and remove states using a proper minus icon:

1. **Add State**: Uses existing PNG icons (`ic_add_grey.png` or `ic_add_blue.png`)
2. **Remove State**: Uses a new minus icon (`ic_minus.xml`) instead of the ambiguous checkmark
3. **Toggle Logic**: Properly switches between states when notes are added/removed

## Implementation Details

### 1. Attribute Definitions (attrs.xml)

Added new `icon_remove` attribute while preserving existing attributes:
```xml
<attr name="icon_add" format="reference"/>
<attr name="icon_add_done" format="reference"/>
<attr name="icon_remove" format="reference"/>
```

### 2. New Asset Creation

Created a new vector drawable `ic_minus.xml` with a proper minus icon:
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="#FF000000">
  <path
      android:fillColor="@android:color/white"
      android:pathData="M19,13H5v-2h14v2z"/>
</vector>
```

### 3. Theme Configuration (themes.xml)

Updated all themes to use the new minus icon:

**Main Theme:**
- `icon_add`: `@drawable/ic_add_grey`
- `icon_add_done`: `@drawable/ic_ok` (backward compatibility)
- `icon_remove`: `@drawable/ic_minus`

**Pink Theme:**
- `icon_add`: `@drawable/ic_add_blue`
- `icon_add_done`: `@drawable/ic_ok` (backward compatibility)
- `icon_remove`: `@drawable/ic_minus`

**Transparent Themes:**
- Follow same pattern as above

### 4. Code Changes (PopupActivity.java)

Modified button state transitions:

**After Successful Note Addition:**
- **Before**: `icon_add_done` (checkmark icon)
- **After**: `icon_remove` (minus icon)

**After Successful Note Update:**
- **Before**: `icon_add_done` (checkmark icon)
- **After**: `icon_remove` (minus icon)

**After Note Deletion:**
- **Before/After**: `icon_add` (add icon) - unchanged

### 5. Layout Files (definition_item.xml, definition_item_left.xml)

Preserved original padding and background attribute references:
```xml
android:background="?attr/icon_add"
android:padding="10dp"
```

## Button State Flow

1. **Initial State**: Button displays add icon (`icon_add`) - Plus sign
2. **After Adding Note**: Button displays remove icon (`icon_remove`) - Minus sign
3. **After Removing Note**: Button displays add icon (`icon_add`) - Plus sign

## Assets Used

### Existing Assets (Preserved)
- `ic_add_grey.png` - Grey add icon for main theme
- `ic_add_blue.png` - Blue add icon for pink theme
- `ic_ok.png` - Checkmark icon (maintained for backward compatibility)

### New Asset
- `ic_minus.xml` - Vector drawable minus icon

## Technical Considerations

1. **Backward Compatibility**: Preserved `icon_add_done` attribute for existing code
2. **Modern Asset Format**: Used vector drawable for the minus icon for better scalability
3. **Theme Consistency**: Applied changes uniformly across all application themes
4. **Layout Preservation**: Maintained original padding and sizing for visual consistency
5. **Color Consistency**: Used fixed black color (`#FF000000`) for visibility

## Verification

The implementation ensures:
- Clear visual distinction between add (+) and remove (-) states
- Consistent behavior across all application themes
- Proper state transitions when adding/removing notes
- Backward compatibility with existing functionality
- Use of Material Design-compliant minus icon

## Files Modified

1. `app/src/main/res/values/attrs.xml` - Added `icon_remove` attribute
2. `app/src/main/res/values/themes.xml` - Updated icon references for all themes
3. `app/src/main/res/drawable/ic_minus.xml` - New vector drawable minus icon
4. `app/src/main/java/com/mmjang/ankihelper/ui/popup/PopupActivity.java` - Modified state transitions

## Expected User Experience

Users will now clearly see:
1. **"+" icon** when they can add a note
2. **"-" icon** when they can remove a note
3. **"+" icon** again after removing a note

This provides intuitive, Material Design-compliant feedback about button functionality with the exact minus icon requested.