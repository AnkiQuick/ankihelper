# Language Management Specifications

This folder contains all specifications and documentation related to AnkiHelper's language management system.

## Document Overview

### 1. `language-management-spec.md` (Original Implementation)
**Status**: ⚠️ DEPRECATED - Replaced by AndroidX implementation
**Date**: September 2025
**Description**: Original specification for the language management system using context wrapper pattern.

**Key Features**:
- Language enumeration (SYSTEM, ENGLISH, CHINESE)
- Context wrapper approach for language switching
- Manual configuration management
- Activity-level language application

**Issues Found**:
- App name not changing when switching languages
- Broken `onConfigurationChanged()` in Application
- Missing `Locale.setDefault()` handling
- Over-engineered solution for modern Android

**Recommendation**: Reference for historical context only. Use AndroidX implementation for new development.

---

### 2. `LANGUAGE_IMPLEMENTATION_ANALYSIS.md` (Problem Analysis)
**Status**: ✅ CURRENT
**Date**: January 2025
**Description**: Detailed analysis of issues with the original implementation and best practices for Android 2024.

**Contents**:
- Root cause analysis of app name translation issue
- Breakdown of `onConfigurationChanged()` problems
- Modern AndroidX approach vs legacy approach
- Recommendations for refactoring

**Audience**: Developers investigating language issues or planning improvements

---

### 3. `MODERN_LANGUAGE_IMPLEMENTATION.md` (Refactoring Summary)
**Status**: ✅ CURRENT
**Date**: January 2025
**Description**: Complete summary of the refactoring from legacy to AndroidX implementation.

**Contents**:
- What changed in each file
- How the new system works
- Code examples and usage
- Testing checklist
- Benefits and improvements

**Audience**: Developers onboarding to the project or implementing language features

---

### 4. `androidx-language-refactoring-spec.md` (Official Specification)
**Status**: ✅ CURRENT - Official Spec
**Date**: January 2025
**Description**: Comprehensive technical specification for the modern AndroidX-based language management system.

**Contents**:
- Executive summary and problem statement
- Technical design and architecture
- Component-by-component changes
- Code metrics and performance impact
- Testing strategy
- Migration plan
- Future enhancements
- Complete API documentation

**Audience**: All developers working on language features, technical reviewers, future maintainers

---

## Quick Reference

### For New Developers

**Start here**:
1. Read `MODERN_LANGUAGE_IMPLEMENTATION.md` for overview
2. Reference `androidx-language-refactoring-spec.md` for details
3. Check `LANGUAGE_IMPLEMENTATION_ANALYSIS.md` if investigating issues

**Ignore**: `language-management-spec.md` (legacy, deprecated)

### For Adding New Language

```java
// 1. Add to AppLanguage.java
JAPANESE("ja", "ja", Locale.JAPANESE)

// 2. Create values-ja/strings.xml
// 3. That's it! AndroidX handles the rest.
```

See: `androidx-language-refactoring-spec.md` → Appendix → "Adding New Language"

### For Troubleshooting

**App name not changing?**
- Check: AndroidX is calling `setApplicationLocales()`
- Check: `app_name` exists in both `values/` and `values-zh/`
- See: `LANGUAGE_IMPLEMENTATION_ANALYSIS.md` → "Root Cause Analysis"

**Language not persisting?**
- Check: `LanguageManager.syncWithSettings()` called on startup
- Check: AndroidX version is 1.6.0+
- See: `androidx-language-refactoring-spec.md` → "Persistence Strategy"

## Implementation Timeline

| Date | Event | Document |
|------|-------|----------|
| Sep 2025 | Initial implementation with context wrapper | `language-management-spec.md` |
| Jan 2025 | App name translation issue identified | `LANGUAGE_IMPLEMENTATION_ANALYSIS.md` |
| Jan 2025 | Refactored to AndroidX API | `androidx-language-refactoring-spec.md` |
| Jan 2025 | Refactoring completed and deployed | `MODERN_LANGUAGE_IMPLEMENTATION.md` |

## Architecture Evolution

```
Legacy (Sep 2025):
User → Settings → LanguageManager → LanguageContextWrapper → Manual Config → Activity

Modern (Jan 2025):
User → Settings → LanguageManager → AppCompatDelegate (AndroidX) → [Automatic Everything]
```

## Key Files Modified

### Implementation Files
- `app/src/main/java/com/mmjang/ankihelper/data/AppLanguage.java` ✏️
- `app/src/main/java/com/mmjang/ankihelper/data/LanguageManager.java` ✏️
- `app/src/main/java/com/mmjang/ankihelper/MyApplication.java` ✏️
- `app/src/main/java/com/mmjang/ankihelper/ui/LauncherActivity.java` ✏️
- `app/src/main/java/com/mmjang/ankihelper/data/LanguageContextWrapper.java` ❌ (deleted)

### Resource Files
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values-zh/strings.xml`

## Dependencies

```gradle
implementation 'androidx.appcompat:appcompat:1.6.1'  // Required for AppCompatDelegate
implementation 'androidx.core:core-ktx:1.10.0'       // Required for LocaleListCompat
```

## Supported Languages

- 🌐 System Default (follows device language)
- 🇬🇧 English
- 🇨🇳 Chinese (Simplified)

## Future Plans

- 🇯🇵 Japanese support
- 🇰🇷 Korean support
- 🌍 Community translations via Crowdin

## Related Documentation

- [Android Per-app Language Docs](https://developer.android.com/guide/topics/resources/app-languages)
- [AppCompatDelegate API Reference](https://developer.android.com/reference/androidx/appcompat/app/AppCompatDelegate)

---

**Last Updated**: January 2025
**Maintained By**: AnkiHelper Development Team
