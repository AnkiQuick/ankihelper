## Why
When users set app language to "System" after previously selecting a specific language (e.g., English), the UI does not reflect the actual system language (e.g., Chinese). The app continues displaying content in the previously selected language instead of detecting and applying the current system locale.

## What Changes
- Fix `AppLanguage.SYSTEM` to dynamically retrieve system locale instead of caching it at initialization
- Update `AppLanguage.getEffectiveLocale()` to always query current system default for SYSTEM option
- Ensure `LanguageManager.setAppLanguage()` properly triggers locale refresh when switching to system default

## Impact
- Affected specs: language-management
- Affected code:
  - `app/src/main/java/com/lmyby/ankiquicker/data/AppLanguage.kt` - Fix cached locale issue
  - `app/src/main/java/com/lmyby/ankiquicker/data/LanguageManager.kt` - Verify system language detection
- User impact: When switching language setting to "System", app will immediately reflect the actual device language
- No breaking changes or database migrations required
