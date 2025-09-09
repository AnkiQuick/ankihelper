# UI Consistency Specification

## Overview

This document addresses UI inconsistencies found throughout the AnkiHelper codebase and establishes standards for a consistent, modern user interface following Material Design 3 principles.

## Current State Analysis

### Critical Issues Identified

1. **Mixed Theme Application Patterns**
   - Some activities manually set themes programmatically
   - Others rely on manifest declarations
   - Inconsistent dark mode handling

2. **Menu Implementation Inconsistencies**
   - Mix of onCreateOptionsMenu and onOptionsItemSelected patterns
   - Non-standard menu item handling
   - Inconsistent back navigation behavior

3. **Button and Icon Variations**
   - Mix of legacy Button and MaterialButton
   - Inconsistent icon usage and sizing
   - Non-standard button styles and colors

4. **Layout Pattern Inconsistencies**
   - Mixed use of LinearLayout and ConstraintLayout
   - Inconsistent padding and margin patterns
   - Non-standard spacing and sizing

5. **Dialog and Popup Variations**
   - Mix of AlertDialog and custom dialogs
   - Inconsistent styling and behavior
   - Non-standard button arrangements

## UI Consistency Standards

### 1. Theme Management

#### 1.1 Theme Application
- **Standard**: All activities MUST use themes declared in AndroidManifest.xml
- **Exception**: Only dynamic theme switching (like E-ink mode) may use programmatic theme setting
- **Pattern**: Remove all `setTheme()` calls except for theme switching functionality

#### 1.2 Theme Structure
```xml
<!-- Base Theme -->
<style name="AppTheme" parent="Theme.Material3.DayNight">
    <!-- Standard Material3 attributes -->
</style>

<!-- Pink Theme Variant -->
<style name="AppTheme.Pink" parent="AppTheme">
    <!-- Pink theme overrides -->
</style>

<!-- E-ink Theme Variant -->
<style name="AppTheme.Eink" parent="AppTheme">
    <!-- High contrast, minimal color theme -->
</style>
```

### 2. Activity Patterns

#### 2.1 Base Activity Structure
```java
public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Standard setup
        setupToolbar();
        applyThemeIfNeeded();
    }
    
    protected void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getTitle());
        }
    }
    
    protected void applyThemeIfNeeded() {
        // Only for dynamic theme switching
    }
}
```

#### 2.2 Editor Activity Pattern
```java
public abstract class BaseEditorActivity extends BaseActivity {
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        } else if (itemId == R.id.action_save) {
            if (saveData()) {
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected abstract boolean saveData();
}
```

### 3. Menu Standards

#### 3.1 Standard Menu Files
- **menu_main.xml**: Main navigation menu
- **menu_save.xml**: Standard save menu for all editors
- **menu_settings.xml**: Settings menu
- **menu_context.xml**: Context menu for lists

#### 3.2 Menu Item Standards
```xml
<menu xmlns:android="http://schemas.android.com/apk/res/android"
      xmlns:app="http://schemas.android.com/apk/res-auto">
    
    <!-- Save Action -->
    <item
        android:id="@+id/action_save"
        android:icon="@drawable/ic_save"
        android:title="@string/save"
        app:showAsAction="ifRoom"/>
    
    <!-- Settings Action -->
    <item
        android:id="@+id/action_settings"
        android:icon="@drawable/ic_settings"
        android:title="@string/settings"
        app:showAsAction="never"/>
</menu>
```

### 4. Button Standards

#### 4.1 Button Types
- **MaterialButton**: Primary choice for all buttons
- **MaterialButtonToggleGroup**: For toggle selections
- **MaterialCardView**: For clickable cards

#### 4.2 Button Styles
```xml
<!-- Primary Button -->
<style name="Widget.App.Button.Primary" parent="Widget.Material3.Button">
    <item name="android:textColor">@color/button_text_primary</item>
    <item name="backgroundTint">@color/button_background_primary</item>
</style>

<!-- Secondary Button -->
<style name="Widget.App.Button.Secondary" parent="Widget.Material3.Button.OutlinedButton">
    <item name="android:textColor">@color/button_text_secondary</item>
    <item name="strokeColor">@color/button_stroke_secondary</item>
</style>

<!-- Text Button -->
<style name="Widget.App.Button.Text" parent="Widget.Material3.Button.TextButton">
    <item name="android:textColor">@color/button_text_secondary</item>
</style>
```

#### 4.3 Button Usage Patterns
```xml
<!-- Confirmation Dialog -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:gravity="end"
    android:padding="16dp">
    
    <com.google.android.material.button.MaterialButton
        android:id="@+id/button_cancel"
        style="@style/Widget.App.Button.Text"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginEnd="8dp"
        android:text="@string/cancel"/>
    
    <com.google.android.material.button.MaterialButton
        android:id="@+id/button_confirm"
        style="@style/Widget.App.Button.Primary"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/confirm"/>
</LinearLayout>
```

### 5. Dialog Standards

#### 5.1 Dialog Pattern
```java
public class DialogHelper {
    public static void showConfirmDialog(Context context, String title, 
                                        String message, DialogInterface.OnClickListener onConfirm) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.confirm, onConfirm)
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
    
    public static void showInputDialog(Context context, String title, 
                                      String hint, DialogInterface.OnClickListener onConfirm) {
        EditText input = new EditText(context);
        input.setHint(hint);
        
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(input)
                .setPositiveButton(R.string.confirm, onConfirm)
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
```

### 6. Layout Standards

#### 6.1 Layout Patterns
- **ConstraintLayout**: Preferred for complex screens
- **LinearLayout**: For simple vertical/horizontal arrangements
- **MaterialCardView**: For content sections

#### 6.2 Spacing Standards
```xml
<!-- Standard Dimensions -->
<dimen name="spacing_small">8dp</dimen>
<dimen name="spacing_medium">16dp</dimen>
<dimen name="spacing_large">24dp</dimen>
<dimen name="spacing_extra_large">32dp</dimen>

<!-- Standard Padding -->
<dimen name="padding_screen">16dp</dimen>
<dimen name="padding_card">16dp</dimen>
<dimen name="padding_small">8dp</dimen>
```

#### 6.3 Layout Structure Example
```xml
<androidx.constraintlayout.widget.ConstraintLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="@dimen/padding_screen">
    
    <com.google.android.material.appbar.MaterialToolbar
        android:id="@+id/toolbar"
        android:layout_width="match_parent"
        android:layout_height="?attr/actionBarSize"
        app:layout_constraintTop_toTopOf="parent"/>
    
    <androidx.core.widget.NestedScrollView
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_marginTop="@dimen/spacing_medium"
        app:layout_constraintTop_toBottomOf="@id/toolbar"
        app:layout_constraintBottom_toBottomOf="parent">
        
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:spacing="@dimen/spacing_medium">
            
            <!-- Content Cards -->
            <com.google.android.material.card.MaterialCardView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="@dimen/spacing_medium">
                
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:padding="@dimen/padding_card">
                    
                    <!-- Card Content -->
                    
                </LinearLayout>
            </com.google.android.material.card.MaterialCardView>
            
        </LinearLayout>
    </androidx.core.widget.NestedScrollView>
    
</androidx.constraintlayout.widget.ConstraintLayout>
```

### 7. List and RecyclerView Standards

#### 7.1 Item Layout Pattern
```xml
<com.google.android.material.card.MaterialCardView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginHorizontal="@dimen/spacing_small"
    android:layout_marginVertical="@dimen/spacing_small"
    app:cardElevation="2dp"
    app:cardCornerRadius="8dp">
    
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:padding="@dimen/padding_card"
        android:gravity="center_vertical">
        
        <!-- Icon/Thumbnail -->
        <ImageView
            android:id="@+id/icon"
            android:layout_width="48dp"
            android:layout_height="48dp"
            android:layout_marginEnd="@dimen/spacing_medium"
            android:src="@drawable/ic_default"/>
        
        <!-- Content -->
        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:orientation="vertical">
            
            <TextView
                android:id="@+id/title"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:textAppearance="?attr/textAppearanceTitleMedium"/>
            
            <TextView
                android:id="@+id/subtitle"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:textAppearance="?attr/textAppearanceBodyMedium"
                android:textColor="?android:attr/textColorSecondary"/>
            
        </LinearLayout>
        
        <!-- Action Buttons -->
        <LinearLayout
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:orientation="horizontal">
            
            <com.google.android.material.button.MaterialButton
                android:id="@+id/button_edit"
                style="@style/Widget.App.Button.Text"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="@string/edit"/>
            
            <com.google.android.material.button.MaterialButton
                android:id="@+id/button_delete"
                style="@style/Widget.App.Button.Text"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="@string/delete"/>
            
        </LinearLayout>
        
    </LinearLayout>
    
</com.google.android.material.card.MaterialCardView>
```

### 8. Color System

#### 8.1 Color Palette
```xml
<!-- Primary Colors -->
<color name="color_primary">#6750A4</color>
<color name="color_primary_variant">#6750A4</color>
<color name="color_on_primary">#FFFFFF</color>

<!-- Secondary Colors -->
<color name="color_secondary">#625B71</color>
<color name="color_secondary_variant">#625B71</color>
<color name="color_on_secondary">#FFFFFF</color>

<!-- Background Colors -->
<color name="color_background">#FFFBFE</color>
<color name="color_surface">#FFFBFE</color>
<color name="color_surface_variant">#E7E0EC</color>

<!-- Error Colors -->
<color name="color_error">#BA1A1A</color>
<color name="color_on_error">#FFFFFF</color>

<!-- Pink Theme Colors -->
<color name="color_pink_primary">#FF0266</color>
<color name="color_pink_secondary">#FF4081</color>

<!-- E-ink Theme Colors -->
<color name="color_eink_primary">#000000</color>
<color name="color_eink_background">#FFFFFF</color>
<color name="color_eink_surface">#F0F0F0</color>
```

### 9. Typography Standards

#### 9.1 Text Appearance Usage
```xml
<!-- Display Text -->
<TextView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:textAppearance="?attr/textAppearanceDisplayLarge"/>

<!-- Headings -->
<TextView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:textAppearance="?attr/textAppearanceHeadlineMedium"/>

<!-- Titles -->
<TextView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:textAppearance="?attr/textAppearanceTitleMedium"/>

<!-- Body Text -->
<TextView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:textAppearance="?attr/textAppearanceBodyMedium"/>

<!-- Caption -->
<TextView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:textAppearance="?attr/textAppearanceCaptionMedium"/>
```

### 10. Input Field Standards

#### 10.1 TextInputLayout Pattern
```xml
<com.google.android.material.textfield.TextInputLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="@dimen/spacing_medium"
    android:hint="@string/hint_text"
    style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox">
    
    <com.google.android.material.textfield.TextInputEditText
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:inputType="text"
        android:maxLines="1"/>
    
</com.google.android.material.textfield.TextInputLayout>
```

## Implementation Status

### Completed Theme Management Implementation (September 9, 2025)

The theme management system has been **fully implemented**, addressing key UI consistency requirements:

#### ✅ **Theme System Achievements**
1. **Centralized Theme Management**: Created `ThemeManager.java` utility class
2. **Theme Enumeration**: Implemented `AppTheme.java` with DEFAULT, PINK, and EINK options
3. **Consistent Theme Application**: Updated `BaseActivity.java` to use `ThemeManager.applyTheme()`
4. **Material3 Compliance**: All themes now use Material3 design system consistently
5. **E-ink Optimization**: High contrast, animation-free theme for e-ink displays
6. **Migration Logic**: Automatic conversion from old pink theme preferences
7. **UI Component Updates**: Replaced theme switch with dropdown in `LauncherActivity`

#### ✅ **Files Modified/Created**
- `app/src/main/java/com/mmjang/ankihelper/data/AppTheme.java` (NEW)
- `app/src/main/java/com/mmjang/ankihelper/data/ThemeManager.java` (NEW)
- `app/src/main/java/com/mmjang/ankihelper/data/Settings.java` (UPDATED)
- `app/src/main/java/com/mmjang/ankihelper/ui/base/BaseActivity.java` (UPDATED)
- `app/src/main/java/com/mmjang/ankihelper/ui/LauncherActivity.java` (UPDATED)
- `app/src/main/res/layout/activity_launcher.xml` (UPDATED)
- `app/src/main/res/values/strings.xml` (UPDATED)
- `app/src/main/res/values/themes.xml` (PREVIOUSLY UPDATED)

#### ✅ **Build Status**
- All compilation issues resolved
- Material3 components properly integrated
- Project builds successfully without errors

## Implementation Priority

### Phase 1: Foundation (High Priority)
1. Create BaseActivity and BaseEditorActivity
2. Standardize menu files and patterns
3. Implement DialogHelper
4. Create dimension and color resources

### Phase 2: Components (Medium Priority)
1. Update all activities to extend BaseActivity
2. Standardize button usage across all screens
3. Implement consistent layout patterns
4. Update RecyclerView item layouts

### Phase 3: Polish (Low Priority)
1. ✅ **Implement theme switching system** - COMPLETED
2. ✅ **Add E-ink theme optimizations** - COMPLETED
3. Fine-tune spacing and typography
4. Add animations and transitions

## Validation Criteria

### Automated Checks
- All activities extend BaseActivity or BaseEditorActivity
- No legacy Button components in new layouts
- Consistent menu file usage
- Proper dimension resource usage

### Manual Review
- Visual consistency across all screens
- Proper Material Design 3 adherence
- Accessibility compliance
- Performance optimization

## Success Metrics

1. **Code Consistency**: 95% reduction in UI pattern variations
2. **Maintenance Efficiency**: 50% reduction in UI-related bug fixes
3. **User Experience**: Improved user satisfaction scores
4. **Development Speed**: 30% faster UI implementation

## Conclusion

This UI consistency specification provides a comprehensive framework for standardizing the AnkiHelper user interface. By implementing these standards, the application will achieve:

- ✅ **Consistent visual design across all screens** - Theme management completed
- ✅ **Improved maintainability and development efficiency** - Centralized theme system
- ✅ **Better user experience and accessibility** - E-ink optimization implemented
- ✅ **Easier theme customization and dark mode support** - 3-theme system operational
- ✅ **Reduced code duplication and improved reusability** - ThemeManager utility created

### Current Status: Theme Management ✅ COMPLETED

The theme management system has been successfully implemented, providing a solid foundation for UI consistency. The remaining work focuses on extending the BaseActivity pattern, standardizing components, and fine-tuning the user interface across all screens.

The implementation is being phased to minimize disruption while gradually improving the overall user interface quality and consistency. The completed theme system demonstrates the effectiveness of this approach and provides a template for future UI consistency improvements.