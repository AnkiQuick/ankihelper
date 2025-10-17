## 1. Investigation & Root Cause Analysis
- [x] 1.1 Confirm bug: Set language to English, then switch to System (with Chinese system locale)
- [x] 1.2 Verify AppLanguage.SYSTEM initializes locale at class load time (line 18)
- [x] 1.3 Check LanguageManager.getCurrentAppLanguage() behavior with empty locale list

## 2. Implementation
- [x] 2.1 Modify AppLanguage.SYSTEM to use Locale.getDefault() dynamically in getEffectiveLocale()
- [x] 2.2 Update AppLanguage.getEffectiveLocale() to always query system locale when isSystem() is true
- [x] 2.3 Remove cached locale field from SYSTEM enum value (line 18)
- [x] 2.4 Verify LanguageManager.setAppLanguage() sends empty locale list for SYSTEM

## 3. Testing
- [ ] 3.1 Manual test: English → System (Chinese device) - UI should show Chinese
- [ ] 3.2 Manual test: Chinese → System (English device) - UI should show English
- [ ] 3.3 Manual test: System → English → System - should toggle correctly
- [ ] 3.4 Verify Settings.getSelectedLanguage() persists correctly
- [ ] 3.5 Test app restart maintains system language selection

## 4. Code Quality
- [x] 4.1 Run `./gradlew detekt` - ensure all checks pass
- [x] 4.2 Verify no line length violations (120 char limit)
- [x] 4.3 Build successfully: `./gradlew assembleDebug`
