# Remove "Add Default Plan" Functionality Specification

## Overview

This specification outlines the complete removal of the "add default plan" functionality from AnkiHelper. The removal will eliminate the automatic creation of default plans while preserving user guidance when no plans exist and directing users to the plan manager.

## Current State Analysis

### Existing Default Plan Functionality

The current implementation includes:

1. **Automatic Default Plan Creation**: Creates an Oxford dictionary-based plan with specific model and deck
2. **User Interface**: "Add Default Plan" button in LauncherActivity
3. **Validation Logic**: Checks for existing plans and prevents duplicates
4. **Database Operations**: Creates Anki models ("划词助手Antimoon模板") and decks ("划词助手默认牌组")
5. **User Confirmation**: Dialog prompts for default plan creation
6. **Error Handling**: Duplicate plan prevention and user feedback

### Files Involved

- `LauncherActivity.java` - UI and logic
- `DefaultPlan.java` - Complete default plan creation class
- `Settings.java` - Default plan preferences
- `activity_launcher.xml` - UI button
- String resources in both English and Chinese

## Requirements

### Functional Requirements

1. **Complete Removal**: Remove all default plan creation functionality
2. **Preserve Validation**: Keep logic that checks for existing plans
3. **User Guidance**: When no plans exist, direct users to plan manager
4. **Clean UI**: Remove "Add Default Plan" button and related elements
5. **No Database Changes**: Eliminate automatic model/deck creation
6. **Maintain Error Handling**: Remove duplicate plan validation since no default plans will be created

### Non-Functional Requirements

1. **No Breaking Changes**: Existing plans should continue to work
2. **Clean Code**: Remove unused imports, methods, and variables
3. **Resource Cleanup**: Remove unused string resources
4. **User Experience**: Smooth transition without missing functionality

## Detailed Design

### 1. Remove DefaultPlan.java Class

**Action**: Delete entire file
**File**: `app/src/main/java/com/mmjang/ankihelper/data/plan/DefaultPlan.java`

**Rationale**: This class contains all the logic for creating default Oxford dictionary plans, including:
- Default plan creation method
- Anki model and deck creation
- Dictionary field mappings
- Output field configurations

### 2. Update LauncherActivity.java

#### Remove UI Elements
```java
// Remove these lines:
private TextView textViewAddDefaultPlan;  // Line 63

// Remove from onCreate():
textViewAddDefaultPlan = (TextView) findViewById(R.id.btn_add_default_plan);  // Line 96

// Remove entire click handler block (Lines 177-195):
textViewAddDefaultPlan.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        askIfAddDefaultPlan();
    }
});
```

#### Replace askIfAddDefaultPlan() Method
**Current Method** (Lines 295-341):
```java
private void askIfAddDefaultPlan() {
    // Logic to add default plan
}
```

**Replacement Method**:
```java
private void showNoPlansGuidance() {
    new AlertDialog.Builder(this)
            .setTitle(R.string.no_plans_found_title)
            .setMessage(R.string.no_plans_found_message)
            .setPositiveButton(R.string.go_to_plan_manager, (dialog, which) -> {
                Intent intent = new Intent(this, PlansManagerActivity.class);
                startActivity(intent);
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
}
```

#### Update Plan Validation Logic
**Current Logic** (around line 293):
```java
if (plans.size() == 0) {
    askIfAddDefaultPlan();
}
```

**Updated Logic**:
```java
if (plans.size() == 0) {
    showNoPlansGuidance();
}
```

#### Remove Unused Imports
Remove if no longer used:
```java
import com.mmjang.ankihelper.data.plan.DefaultPlan;
import com.mmjang.ankihelper.data.plan.OutputPlanPOJO;
```

### 3. Update Settings.java

**Remove Default Plan Constants and Methods**:
```java
// Remove this constant:
private final static String DEFAULT_PLAN = "default_plan";  // Line 29

// Remove these methods (Lines 136-141):
public String getDefaultPlan() {
    return sp.getString(DEFAULT_PLAN, "");
}

public void setDefaultPlan(String defaultPlan) {
    editor.putString(DEFAULT_PLAN, defaultPlan);
}
```

### 4. Update Layout File

**Remove Button from activity_launcher.xml** (Lines 55-66):
```xml
<!-- Remove this entire section -->
<com.google.android.material.button.MaterialButton
    android:id="@+id/btn_add_default_plan"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:strokeWidth="0dp"
    android:text="@string/btn_add_default_plan_str"
    android:gravity="start|center_vertical"
    app:icon="@drawable/ic_add"
    app:iconGravity="end"
    style="@style/Widget.Material3.Button.OutlinedButton" />
```

### 5. Update String Resources

#### Remove Default Plan Strings from values/strings.xml
Remove these lines:
```xml
<string name="confirm_add_default_plan">No plans found, add default plan (Oxford Dictionary)?</string>
<string name="default_plan_added">Default plan added, check out at plans manager</string>
<string name="confirm_add_default_plan_when_exists_already">Add default plan (Oxford Dictionary)?</string>
<string name="btn_add_default_plan_str">add default plan</string>
<string name="duplicate_plan_name_complain">Default plan already exists, delete it in plans manager first.</string>
```

#### Remove Default Plan Strings from values-zh/strings.xml
Remove these lines:
```xml
<string name="confirm_add_default_plan">您还没有任何划词方案，是否写入默认方案(牛津词典)？</string>
<string name="default_plan_added">默认方案已添加，可在方案管理器中查看</string>
<string name="confirm_add_default_plan_when_exists_already">已存在划词方案，确认添加默认方案（牛津词典）？</string>
<string name="btn_add_default_plan_str">添加默认方案</string>
<string name="duplicate_plan_name_complain">已存在默认方案，不得重复添加。可在方案管理器中删除后再添加。</string>
```

#### Add New Guidance Strings

**Add to values/strings.xml**:
```xml
<!-- No Plans Guidance -->
<string name="no_plans_found_title">No Plans Available</string>
<string name="no_plans_found_message">You don\'t have any plans yet. Go to Plan Manager to create your first plan.</string>
<string name="go_to_plan_manager">Go to Plan Manager</string>
```

**Add to values-zh/strings.xml**:
```xml
<!-- No Plans Guidance -->
<string name="no_plans_found_title">没有可用方案</string>
<string name="no_plans_found_message">您还没有任何划词方案。请前往方案管理器创建您的第一个方案。</string>
<string name="go_to_plan_manager">前往方案管理器</string>
```

## Implementation Plan

### Phase 1: Backend Cleanup (Priority 1)
1. Delete `DefaultPlan.java` file
2. Remove default plan methods from `Settings.java`
3. Remove unused imports from `LauncherActivity.java`

### Phase 2: UI Updates (Priority 1)
1. Remove "Add Default Plan" button from layout
2. Remove button binding from `LauncherActivity.java`
3. Remove button click handler

### Phase 3: Logic Updates (Priority 1)
1. Replace `askIfAddDefaultPlan()` with `showNoPlansGuidance()`
2. Update plan validation logic to call new guidance method
3. Test plan validation flow

### Phase 4: Resource Cleanup (Priority 2)
1. Remove default plan string resources
2. Add new guidance string resources
3. Test localization

### Phase 5: Testing and Verification (Priority 1)
1. Test app behavior with no existing plans
2. Test app behavior with existing plans
3. Verify plan manager integration
4. Test both English and Chinese languages
5. Verify no breaking changes for existing users

## Risk Assessment

### Technical Risks
- **Missing Dependencies**: Other parts of the app might reference DefaultPlan class
- **Import Issues**: Removed imports might break compilation
- **Resource References**: Missing strings might cause runtime errors

### Mitigation Strategies
1. **Incremental Testing**: Test compilation after each major change
2. **Search Verification**: Double-check for any remaining references
3. **Backup Strategy**: Keep version control commits for easy rollback

## Success Criteria

### Functional Requirements
- ✅ Default plan creation completely removed
- ✅ No automatic model/deck creation
- ✅ User guidance shown when no plans exist
- ✅ Direct integration with plan manager
- ✅ All existing plans continue to work

### Non-Functional Requirements
- ✅ Clean compilation without errors
- ✅ No unused imports or resources
- ✅ Consistent user experience
- ✅ Proper localization support

### Testing Criteria
- ✅ App launches successfully with no existing plans
- ✅ Guidance dialog appears and plan manager opens correctly
- ✅ App works normally with existing plans
- ✅ No references to DefaultPlan class remain
- ✅ Both English and Chinese interfaces work correctly

## Files to be Modified

### Files to Delete
- `app/src/main/java/com/mmjang/ankihelper/data/plan/DefaultPlan.java`

### Files to Modify
- `app/src/main/java/com/mmjang/ankihelper/ui/LauncherActivity.java`
- `app/src/main/java/com/mmjang/ankihelper/data/Settings.java`
- `app/src/main/res/layout/activity_launcher.xml`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values-zh/strings.xml`

## User Impact

### Positive Impact
- Simplified user interface
- Eliminated automatic database changes
- More control over plan creation
- Cleaner user experience

### Negative Impact
- New users must manually create plans
- No automatic Oxford dictionary setup
- Slightly more initial setup required

## Rollback Strategy

If issues arise during implementation:
1. Revert `DefaultPlan.java` from version control
2. Restore removed UI elements and logic
3. Revert string resource changes
4. Test functionality restoration

---

## Implementation Status: ✅ COMPLETED

### Completed Date: September 9, 2025

### Implementation Summary

The "add default plan" functionality has been **completely removed** from AnkiHelper according to the specification. All phases of implementation have been successfully completed, with clean compilation and no remaining references to DefaultPlan in the main source code.

### Phase Completion Status

#### ✅ Phase 1: Backend Cleanup (COMPLETED)
- **Deleted DefaultPlan.java file** - Removed entire default plan creation class
- **Removed default plan methods from Settings.java** - Eliminated DEFAULT_PLAN constant and related methods
- **Removed unused imports from LauncherActivity.java** - Cleaned up DefaultPlan import

#### ✅ Phase 2: UI Updates (COMPLETED)
- **Removed Add Default Plan button from layout** - Eliminated button from activity_launcher.xml
- **Removed button binding and click handler** - Cleaned up UI element references and event handlers

#### ✅ Phase 3: Logic Updates (COMPLETED)
- **Replaced askIfAddDefaultPlan with showNoPlansGuidance** - New method shows guidance dialog and directs to plan manager
- **Updated plan validation logic** - Simplified logic since no default plans will be created

#### ✅ Phase 4: Resource Cleanup (COMPLETED)
- **Removed default plan string resources** - Cleaned up all default plan strings from both English and Chinese files
- **Added new guidance string resources** - Added bilingual support for new user guidance

#### ✅ Phase 5: Testing and Verification (COMPLETED)
- **Build compilation successful** - No compilation errors or warnings
- **No remaining DefaultPlan references** - Complete cleanup verified
- **All string resources properly updated** - No orphaned resource references

### Files Successfully Modified

#### Files Deleted
- `app/src/main/java/com/mmjang/ankihelper/data/plan/DefaultPlan.java` ✅

#### Files Modified
- `app/src/main/java/com/mmjang/ankihelper/data/Settings.java` ✅
- `app/src/main/java/com/mmjang/ankihelper/ui/LauncherActivity.java` ✅
- `app/src/main/res/layout/activity_launcher.xml` ✅
- `app/src/main/res/values/strings.xml` ✅
- `app/src/main/res/values-zh/strings.xml` ✅

### Key Implementation Details

#### New User Experience Flow
1. **When no plans exist**: Shows guidance dialog with option to go to Plan Manager
2. **User guidance**: Clear message directing users to create their first plan
3. **Plan manager integration**: Direct link to PlansManagerActivity for plan creation
4. **No automatic database changes**: Eliminated automatic model/deck creation

#### String Resource Updates
**Removed (English)**:
- `confirm_add_default_plan`: "No plans found, add default plan (Oxford Dictionary)?"
- `default_plan_added`: "Default plan added, check out at plans manager"
- `confirm_add_default_plan_when_exists_already`: "Add default plan (Oxford Dictionary)?"
- `btn_add_default_plan_str`: "add default plan"
- `duplicate_plan_name_complain`: "Default plan already exists, delete it in plans manager first."

**Added (English)**:
- `no_plans_found_title`: "No Plans Available"
- `no_plans_found_message`: "You don't have any plans yet. Go to Plan Manager to create your first plan."
- `go_to_plan_manager`: "Go to Plan Manager"

**Added (Chinese)**:
- `no_plans_found_title`: "没有可用方案"
- `no_plans_found_message`: "您还没有任何划词方案。请前往方案管理器创建您的第一个方案。"
- `go_to_plan_manager`: "前往方案管理器"

### Testing Results

- ✅ **Build Success**: Project compiles without errors
- ✅ **Clean References**: No remaining DefaultPlan references in source code
- ✅ **Resource Cleanup**: No orphaned string resource references
- ✅ **UI Consistency**: Clean interface without default plan button
- ✅ **Logic Integrity**: New guidance flow properly implemented
- ✅ **Internationalization**: Bilingual support maintained

### Impact Assessment

#### Positive Impact
- **Simplified User Interface**: Removed unnecessary automatic plan creation
- **User Control**: Users must manually create plans, ensuring intentionality
- **Cleaner Codebase**: Eliminated complex default plan creation logic
- **No Database Side Effects**: Removed automatic Anki model/deck creation

#### User Experience Changes
- **New Users**: Must manually create first plan via Plan Manager
- **Existing Users**: No impact on existing plans
- **Guidance**: Clear direction when no plans exist
- **Workflow**: More intentional plan creation process

### Success Criteria Met

✅ **Complete Functionality Removal**: All default plan creation eliminated
✅ **User Guidance Preserved**: New users directed to Plan Manager
✅ **Clean Implementation**: No compilation errors or references
✅ **Resource Management**: All string resources properly updated
✅ **Internationalization**: Bilingual support maintained
✅ **Backward Compatibility**: Existing plans continue to work

### Risk Mitigation

✅ **No Breaking Changes**: Existing functionality preserved
✅ **Clean Codebase**: No unused imports or references
✅ **Proper Error Handling**: Simplified logic without default plan complications
✅ **User Experience**: Smooth transition with clear guidance

This implementation successfully removes the "add default plan" functionality while maintaining a positive user experience and clean codebase architecture.

**Note**: This specification ensures complete removal of default plan functionality while maintaining a smooth user experience and proper guidance for new users.