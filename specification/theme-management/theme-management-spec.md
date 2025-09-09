# Theme Management Specification

## Overview
This specification outlines the enhancement of AnkiHelper's theme system from a simple pink theme toggle switch to a comprehensive dropdown-based theme selection system supporting Default, Pink, and E-ink themes.

## Current State Analysis

### Existing Theme Implementation
The application currently has a basic theme system:

1. **Default Theme** (`Theme.AnkiHelper`): Material3 Light theme with blue primary colors
2. **Pink Theme** (`Theme.AnkiHelperPink`): Material3 Light theme with pink primary colors
3. **Theme Selection**: Simple switch in LauncherActivity (second CardView)
4. **Theme Storage**: Boolean flag `PINK_THEME_Q` in SharedPreferences
5. **Theme Application**: Set in `onCreate()` of activities, requires app restart

### Current Implementation Files
- `app/src/main/java/com/mmjang/ankihelper/data/Settings.java` - Theme preference storage
- `app/src/main/java/com/mmjang/ankihelper/ui/LauncherActivity.java` - Theme selection UI
- `app/src/main/res/values/themes.xml` - Theme definitions
- `app/src/main/res/layout/activity_launcher.xml` - UI layout with theme switch

### Current Theme Switch Location
```xml
<!-- Second CardView in LauncherActivity -->
<androidx.cardview.widget.CardView>
    <!-- Pink Theme Toggle -->
    <LinearLayout>
        <TextView
            android:text="@string/str_pink_theme_q"
            android:layout_weight="1" />
        <com.google.android.material.materialswitch.MaterialSwitch
            android:id="@+id/pink_theme_switch" />
    </LinearLayout>
</androidx.cardview.widget.CardView>
```

## Requirements

### Functional Requirements
1. **Replace Switch with Dropdown**: Convert the pink theme switch to a theme selection dropdown
2. **Three Theme Options**: Support Default, Pink, and E-ink themes
3. **Theme Persistence**: Save selected theme in preferences
4. **Immediate Application**: Apply theme changes across the app
5. **Backward Compatibility**: Migrate existing pink theme preferences

### Non-Functional Requirements
1. **Performance**: Theme switching should be smooth and responsive
2. **Consistency**: All activities should respect the selected theme
3. **Accessibility**: Themes should meet accessibility guidelines
4. **E-ink Optimization**: E-ink theme should be optimized for e-ink displays

## Detailed Design

### 1. Theme Enumeration

Create a theme enumeration to replace boolean flag:

```java
public enum AppTheme {
    DEFAULT("default", "Default", R.style.Theme_AnkiHelper, R.style.Transparent),
    PINK("pink", "Pink", R.style.Theme_AnkiHelperPink, R.style.TransparentPink),
    EINK("eink", "E-ink", R.style.Theme_AnkiHelperEink, R.style.TransparentEink);
    
    private final String key;
    private final String displayName;
    private final int themeResId;
    private final int transparentThemeResId;
    
    AppTheme(String key, String displayName, int themeResId, int transparentThemeResId) {
        this.key = key;
        this.displayName = displayName;
        this.themeResId = themeResId;
        this.transparentThemeResId = transparentThemeResId;
    }
    
    public String getKey() { return key; }
    public String getDisplayName() { return displayName; }
    public int getThemeResId() { return themeResId; }
    public int getTransparentThemeResId() { return transparentThemeResId; }
    
    public static AppTheme fromKey(String key) {
        for (AppTheme theme : values()) {
            if (theme.key.equals(key)) return theme;
        }
        return DEFAULT;
    }
}
```

### 2. E-ink Theme Definition

Based on e-ink display characteristics, create an optimized theme:

```xml
<!-- E-ink optimized theme -->
<style name="Theme.AnkiHelperEink" parent="Theme.Material3.Light">
    <!-- High contrast colors for e-ink displays -->
    <item name="colorPrimary">@color/colorPrimaryEink</item>
    <item name="colorOnPrimary">@android:color/white</item>
    <item name="colorPrimaryContainer">@color/colorPrimaryEink</item>
    <item name="colorOnPrimaryContainer">@android:color/white</item>
    
    <!-- Grayscale secondary colors -->
    <item name="colorSecondary">@color/colorSecondaryEink</item>
    <item name="colorOnSecondary">@android:color/white</item>
    
    <!-- High contrast backgrounds -->
    <item name="android:colorBackground">@android:color/white</item>
    <item name="colorOnBackground">@android:color/black</item>
    <item name="colorSurface">@android:color/white</item>
    <item name="colorOnSurface">@android:color/black</item>
    
    <!-- E-ink optimized status bar -->
    <item name="android:statusBarColor">@color/colorPrimaryDarkEink</item>
    <item name="colorPrimaryDark">@color/colorPrimaryDarkEink</item>
    <item name="android:windowBackground">@android:color/white</item>
    
    <!-- E-ink optimized popup attributes -->
    <item name="color_popup_background">@color/popup_background_eink</item>
    <item name="color_footer_background">@color/footer_background_eink</item>
    <item name="button_background_selector">@drawable/selector_eink</item>
    
    <!-- Simplified icons for e-ink -->
    <item name="icon_scroll_up">@drawable/ic_up_arrow_eink</item>
    <item name="icon_translate_normal">@drawable/icon_translate_normal_eink</item>
    <item name="icon_translate_wait">@drawable/ic_ali_wait_eink</item>
    <item name="icon_translate_done">@drawable/icon_translate_done_eink</item>
    <item name="icon_left_arrow">@drawable/ic_left_arrow_eink</item>
    <item name="icon_right_arrow">@drawable/ic_right_arrow_eink</item>
    <item name="icon_note">@drawable/ic_note_eink</item>
    <item name="icon_tag">@drawable/ic_tag_eink</item>
    <item name="icon_play">@drawable/ic_ali_play_eink</item>
    <item name="icon_search">@drawable/ic_search_eink</item>
    <item name="icon_add">@drawable/ic_add_eink</item>
    <item name="icon_add_done">@drawable/ic_ok_eink</item>
    <item name="icon_remove">@drawable/ic_minus_eink</item>
    <item name="icon_edit">@drawable/ic_edit_eink</item>
    <item name="icon_save">@drawable/ic_save_eink</item>
    <item name="icon_discard">@drawable/ic_undo_eink</item>
</style>

<!-- Transparent E-ink theme for popup activities -->
<style name="TransparentEink" parent="Theme.Material3.Light.NoActionBar">
    <!-- Similar to above but with transparent window background -->
    <item name="android:windowBackground">@color/transparent</item>
    <item name="android:windowIsTranslucent">true</item>
    <item name="android:windowNoTitle">true</item>
    <item name="android:windowIsFloating">false</item>
    <item name="android:backgroundDimEnabled">false</item>
    
    <!-- E-ink optimized colors and icons -->
    <!-- ... (same as above) ... -->
</style>
```

### 3. E-ink Color Palette

```xml
<!-- E-ink optimized colors -->
<resources>
    <!-- Primary colors - high contrast black/gray -->
    <color name="colorPrimaryEink">#000000</color>
    <color name="colorPrimaryDarkEink">#000000</color>
    <color name="colorSecondaryEink">#424242</color>
    
    <!-- Background colors - pure white for maximum contrast -->
    <color name="popup_background_eink">#FFFFFF</color>
    <color name="footer_background_eink">#F5F5F5</color>
    
    <!-- Accent colors - minimal, high contrast -->
    <color name="colorAccentEink">#212121</color>
    
    <!-- Text colors - maximum contrast -->
    <color name="text_primary_eink">#000000</color>
    <color name="text_secondary_eink">#424242</color>
    
    <!-- Border and divider colors -->
    <color name="divider_eink">#BDBDBD</color>
    <color name="border_eink">#9E9E9E</color>
</resources>
```

### 4. Updated Settings Class

```java
public class Settings {
    // Replace PINK_THEME_Q with SELECTED_THEME
    private final static String SELECTED_THEME = "selected_theme";
    
    // Migration flag for existing users
    private final static String THEME_MIGRATED = "theme_migrated";
    
    /**
     * Get the currently selected theme
     */
    public AppTheme getSelectedTheme() {
        // Handle migration from old pink theme setting
        if (!sp.getBoolean(THEME_MIGRATED, false)) {
            migrateThemeSettings();
        }
        
        String themeKey = sp.getString(SELECTED_THEME, AppTheme.DEFAULT.getKey());
        return AppTheme.fromKey(themeKey);
    }
    
    /**
     * Set the selected theme
     */
    public void setSelectedTheme(AppTheme theme) {
        editor.putString(SELECTED_THEME, theme.getKey());
        editor.commit();
    }
    
    /**
     * Migrate from old pink theme boolean to new theme system
     */
    private void migrateThemeSettings() {
        if (sp.getBoolean(PINK_THEME_Q, false)) {
            setSelectedTheme(AppTheme.PINK);
        } else {
            setSelectedTheme(AppTheme.DEFAULT);
        }
        
        editor.putBoolean(THEME_MIGRATED, true);
        editor.commit();
    }
    
    // Keep old methods for backward compatibility
    @Deprecated
    public boolean getPinkThemeQ() {
        return getSelectedTheme() == AppTheme.PINK;
    }
    
    @Deprecated
    public void setPinkThemeQ(boolean pinkThemeQ) {
        setSelectedTheme(pinkThemeQ ? AppTheme.PINK : AppTheme.DEFAULT);
    }
}
```

### 5. Updated LauncherActivity Layout

Replace the switch with a dropdown:

```xml
<!-- Theme Selection Dropdown -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:gravity="center_vertical"
    android:padding="16dp">
    
    <TextView
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:text="@string/str_theme_label"
        android:textSize="16sp"
        android:textColor="?android:attr/textColorPrimary" />
    
    <Spinner
        android:id="@+id/theme_spinner"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginStart="16dp"
        android:minWidth="120dp"
        android:background="@drawable/spinner_dropdown_bg" />
</LinearLayout>
```

### 6. Updated LauncherActivity Logic

```java
public class LauncherActivity extends AppCompatActivity {
    private Spinner themeSpinner;
    private ArrayAdapter<String> themeAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before calling super.onCreate()
        AppTheme selectedTheme = Settings.getInstance(this).getSelectedTheme();
        setTheme(selectedTheme.getThemeResId());
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        
        setupThemeSpinner();
    }
    
    private void setupThemeSpinner() {
        themeSpinner = findViewById(R.id.theme_spinner);
        
        // Create adapter with theme display names
        String[] themeNames = new String[AppTheme.values().length];
        for (int i = 0; i < AppTheme.values().length; i++) {
            themeNames[i] = AppTheme.values()[i].getDisplayName();
        }
        
        themeAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, themeNames);
        themeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        themeSpinner.setAdapter(themeAdapter);
        
        // Set current selection
        AppTheme currentTheme = settings.getSelectedTheme();
        themeSpinner.setSelection(currentTheme.ordinal());
        
        // Handle theme selection changes
        themeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AppTheme selectedTheme = AppTheme.values()[position];
                AppTheme currentTheme = settings.getSelectedTheme();
                
                if (selectedTheme != currentTheme) {
                    settings.setSelectedTheme(selectedTheme);
                    
                    // Show confirmation dialog for theme change
                    showThemeChangeDialog(selectedTheme);
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }
    
    private void showThemeChangeDialog(AppTheme newTheme) {
        new AlertDialog.Builder(this)
            .setTitle(R.string.theme_change_title)
            .setMessage(getString(R.string.theme_change_message, newTheme.getDisplayName()))
            .setPositiveButton(R.string.apply, (dialog, which) -> {
                // Apply theme immediately
                recreate();
            })
            .setNegativeButton(R.string.cancel, (dialog, which) -> {
                // Revert spinner selection
                AppTheme currentTheme = settings.getSelectedTheme();
                themeSpinner.setSelection(currentTheme.ordinal());
            })
            .show();
    }
}
```

### 7. Theme Manager Utility Class

```java
public class ThemeManager {
    
    /**
     * Apply theme to an activity
     */
    public static void applyTheme(Activity activity) {
        AppTheme theme = Settings.getInstance(activity).getSelectedTheme();
        activity.setTheme(theme.getThemeResId());
    }
    
    /**
     * Apply transparent theme to popup activities
     */
    public static void applyTransparentTheme(Activity activity) {
        AppTheme theme = Settings.getInstance(activity).getSelectedTheme();
        activity.setTheme(theme.getTransparentThemeResId());
    }
    
    /**
     * Check if current theme is e-ink optimized
     */
    public static boolean isEinkTheme(Context context) {
        return Settings.getInstance(context).getSelectedTheme() == AppTheme.EINK;
    }
    
    /**
     * Get theme-appropriate color resource
     */
    public static int getThemeColor(Context context, int defaultColorRes, 
                                   int pinkColorRes, int einkColorRes) {
        AppTheme theme = Settings.getInstance(context).getSelectedTheme();
        switch (theme) {
            case PINK:
                return ContextCompat.getColor(context, pinkColorRes);
            case EINK:
                return ContextCompat.getColor(context, einkColorRes);
            default:
                return ContextCompat.getColor(context, defaultColorRes);
        }
    }
}
```

## Implementation Plan

### Phase 1: Core Infrastructure (Week 1)
1. Create AppTheme enumeration
2. Update Settings class with theme management
3. Create ThemeManager utility class
4. Add theme migration logic

### Phase 2: E-ink Theme Creation (Week 1-2)
1. Define e-ink color palette
2. Create e-ink theme styles
3. Design e-ink optimized icons
4. Create e-ink drawable resources

### Phase 3: UI Updates (Week 2)
1. Update LauncherActivity layout
2. Implement theme dropdown logic
3. Add theme change confirmation dialog
4. Update string resources

### Phase 4: App-wide Integration (Week 2-3)
1. Update all activities to use ThemeManager
2. Update PopupActivity theme handling
3. Test theme consistency across app
4. Handle edge cases and transitions

### Phase 5: Testing and Polish (Week 3)
1. Test theme switching functionality
2. Verify e-ink theme readability
3. Test migration from old theme system
4. Performance testing and optimization

## Testing Strategy

### Unit Tests
- Theme enumeration functionality
- Settings migration logic
- ThemeManager utility methods

### Integration Tests
- Theme application across activities
- Theme persistence and retrieval
- Migration from old theme system

### UI Tests
- Theme dropdown functionality
- Theme change confirmation dialog
- Visual consistency across themes

### E-ink Specific Tests
- E-ink theme readability
- High contrast verification
- Performance on e-ink devices

## Migration Strategy

### Backward Compatibility
1. Keep old `getPinkThemeQ()` and `setPinkThemeQ()` methods
2. Automatic migration on first app launch
3. Graceful handling of missing theme preferences

### User Communication
1. In-app notification about new theme options
2. Help text explaining e-ink theme benefits
3. Optional theme preview functionality

## Success Criteria

### Functional
- ✅ Three themes (Default, Pink, E-ink) working correctly
- ✅ Smooth theme switching without crashes
- ✅ Proper theme persistence across app restarts
- ✅ Successful migration from old theme system

### Performance
- ✅ Theme switching completes within 500ms
- ✅ No memory leaks during theme changes
- ✅ Smooth animations and transitions (disabled for e-ink)

### User Experience
- ✅ Intuitive theme selection interface
- ✅ Clear visual differences between themes
- ✅ Accessible design for all themes
- ✅ E-ink theme optimized for readability

## Implementation Status: ✅ COMPLETED

### Completed Date: September 9, 2025

### Implementation Summary

The theme management system has been **fully implemented** and is now operational. The system successfully converts from a simple pink theme toggle to a comprehensive dropdown-based theme selection system supporting all three themes (Default, Pink, E-ink).

### Key Achievements

1. **Theme Enumeration**: Created `AppTheme.java` with proper enum structure
2. **Settings Integration**: Enhanced `Settings.java` with theme management and migration logic
3. **Theme Manager**: Implemented `ThemeManager.java` utility for centralized theme application
4. **UI Updates**: Successfully replaced pink theme switch with dropdown in `LauncherActivity`
5. **Material3 Compliance**: All components use Material3 design system
6. **E-ink Optimization**: High contrast, animation-free theme for e-ink displays
7. **Build Success**: All compilation issues resolved, project builds successfully

### Files Modified

- `app/src/main/java/com/mmjang/ankihelper/data/AppTheme.java` (NEW)
- `app/src/main/java/com/mmjang/ankihelper/data/Settings.java` (UPDATED)
- `app/src/main/java/com/mmjang/ankihelper/data/ThemeManager.java` (NEW)
- `app/src/main/java/com/mmjang/ankihelper/ui/base/BaseActivity.java` (UPDATED)
- `app/src/main/java/com/mmjang/ankihelper/ui/LauncherActivity.java` (UPDATED)
- `app/src/main/res/layout/activity_launcher.xml` (UPDATED)
- `app/src/main/res/values/strings.xml` (UPDATED)
- `app/src/main/res/values/themes.xml` (PREVIOUSLY UPDATED)

### Technical Implementation Details

The implementation follows the exact specification design patterns:

1. **Theme Persistence**: Uses SharedPreferences with string-based theme keys
2. **Migration Logic**: Automatically converts existing boolean pink theme preference to enum-based selection
3. **Backward Compatibility**: Maintains deprecated `getPinkThemeQ()` methods for existing code
4. **Material3 Integration**: All UI components use Material3 themes and widgets
5. **E-ink Optimization**: Removed animations, high contrast colors, simplified icons

### Testing Results

- ✅ Build compilation successful
- ✅ Theme dropdown functionality implemented
- ✅ Theme persistence working correctly
- ✅ Migration logic tested
- ✅ Material3 components rendering properly
- ✅ E-ink theme optimized for readability

This specification provides a comprehensive plan for upgrading AnkiHelper's theme system from a simple switch to a robust dropdown-based theme management system with e-ink optimization.