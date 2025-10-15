# Internationalization Audit Report

**Date**: January 2025
**Scope**: Complete app audit for Chinese-only strings
**Status**: Issues Identified - Action Required

## Executive Summary

This audit identified **multiple categories** of hardcoded Chinese strings that lack English translations, affecting the user experience when the app is set to English language.

### Impact Assessment
- **Critical**: Dictionary names, field names, error messages
- **High**: Dropdown options, settings labels
- **Medium**: Log messages, code comments
- **Low**: Layout preview text (tools:text)

---

## 1. Dictionary Field Names (CRITICAL)

**Issue**: All built-in dictionaries use hardcoded Chinese field names that cannot be translated.

### Affected Files

#### `/app/src/main/java/com/mmjang/ankihelper/data/dict/Maldpe.java`
```java
private static final String DICT_NAME = "韦氏高阶英汉双解词典2019完美版";

private static final String[] DICT_FIELDS = {
    "单词",        // Word
    "词性",        // Part of Speech
    "音标",        // Phonetic
    "英文释义",    // English Definition
    "中文释义",    // Chinese Definition
    "有道美式发音", // Youdao US Pronunciation
    "有道英式发音"  // Youdao UK Pronunciation
};
```

**Similar Issues in**:
- `/data/dict/Oalde10.java` - Oxford dictionary (牛津高阶英汉双解词典第10版完美版)
- `/data/dict/Cdepe4.java` - Cambridge dictionary (剑桥在线英汉双解词典完美版)
- `/data/dict/AIDictionary.java` - AI Dictionary fields

---

## 2. Vocabulary Card Model Fields (CRITICAL)

**File**: `/app/src/main/java/com/mmjang/ankihelper/data/plan/VocabularyCardModel.java`

```java
private static final String[] availableFields = {
    "单词",   // Word
    "音标",   // Phonetic
    "释义",   // Definition
    "笔记",   // Notes
    "例句",   // Example Sentence
    "",
    "发音"    // Pronunciation
};
```

**Impact**: These show up in the plan editor when mapping fields to Anki cards.

---

## 3. Field Type Constants (CRITICAL)

**File**: `/app/src/main/java/com/mmjang/ankihelper/util/Constant.java`

```java
public static final String[] FIELD_TYPE_LABELS = {
    "空",                    // Empty
    "例句",                  // Example Sentence
    "加粗的例句",            // Bold Example
    "挖空的例句",            // Cloze Example
    "挖空的例句（全c1模式）", // Cloze Example (Full C1 Mode)
    "笔记",                  // Notes
    "",
    "全部释义",              // All Definitions
    "句子翻译"               // Sentence Translation
};
```

**Impact**: These appear in dropdown menus when configuring field mappings.

---

## 4. Language Pronunciation Labels (HIGH)

**File**: `/app/src/main/java/com/mmjang/ankihelper/domain/PronounceManager.java`

```java
public static final String LANGUAGE_ENGLISH = "英语发音";   // English Pronunciation
public static final String LANGUAGE_FRENCHE = "法语发音";   // French Pronunciation
public static final String LANGUAGE_JAPANESE = "日语发音";  // Japanese Pronunciation
public static final String LANGUAGE_KOREAN = "韩语发音";    // Korean Pronunciation
public static final String LANGUAGE_GERMANY = "德语发音";   // German Pronunciation
```

**Impact**: Shows in pronunciation language selection.

---

## 5. Error Messages (HIGH)

### File: `/app/src/main/java/com/mmjang/ankihelper/ui/popup/PopupActivity.java`

```java
String message = String.format("方案\"%s\"所选词典\"%s\"不存在，请检查是否需要重新导入自定义词典",
    planName, dictionaryName);
// Translation: Plan "%s" selected dictionary "%s" does not exist, please check if you need to re-import custom dictionary
```

### File: `/app/src/main/java/com/mmjang/ankihelper/ui/plan/PlansManagerActivity.java`

```java
Toast.makeText(this, "剪贴板为空！", Toast.LENGTH_SHORT).show();
// Translation: Clipboard is empty!

Toast.makeText(this, "格式错误！", Toast.LENGTH_SHORT).show();
// Translation: Format error!

errorMessage += "\n格式错误，每行项目数应为5";
// Translation: Format error, each line should have 5 items
```

---

## 6. Missing String Resources (HIGH)

**Issue**: These strings exist in Chinese (`values-zh/strings.xml`) but NOT in English (`values/strings.xml`):

```
settings_title
settings_category_general
settings_category_appearance
settings_category_essential
settings_monitor_clipboard
settings_monitor_clipboard_summary
settings_auto_cancel_popup
settings_auto_cancel_popup_summary
settings_left_hand_mode
settings_left_hand_mode_summary
settings_show_read_content
settings_show_read_content_summary
settings_pink_theme
settings_pink_theme_summary
settings_pronounce_language
settings_pronounce_language_summary
```

**Impact**: If a new settings screen is added using these strings, they will appear in Chinese even when app is in English.

---

## 7. Hardcoded Field Checks (HIGH)

**File**: `/app/src/main/java/com/mmjang/ankihelper/util/Utils.java`

```java
if(map.get(key).equals("句子翻译")){  // Sentence Translation
    // special handling
}
```

**File**: `/app/src/main/java/com/mmjang/ankihelper/ui/popup/PopupActivity.java`

```java
if (map.containsValue("原声例句")) {  // Original Example Sentence
if (map.containsValue("音频") || map.containsValue("复合项")) {  // Audio or Composite
if (map.containsValue("离线发音")) {  // Offline Pronunciation
```

**Impact**: These string comparisons will fail if field names are translated to English.

---

## 8. Code Comments (LOW PRIORITY)

Extensive Chinese comments throughout the codebase. Examples:

```java
// 单例，getInstance()得到实例
// 应用设置名称
// 字段映射
// 是否监听剪切板
// 点加号后是否退出
```

**Impact**: Doesn't affect users, but makes code less accessible to international developers.

---

## 9. Log Messages (LOW PRIORITY)

```java
Log.d("", "单词需要查找变形表");      // Word needs inflection lookup
Log.d("", "已变形单词" + s);          // Inflected word
```

**Impact**: Debugging only, not user-facing.

---

## Recommended Solutions

### Priority 1: Critical Issues (Immediate Action)

#### Solution 1A: Move Dictionary Field Names to String Resources

**Current**:
```java
private static final String[] DICT_FIELDS = {
    "单词", "词性", "音标", ...
};
```

**Proposed**:
```java
public String[] getDictFields(Context context) {
    return new String[]{
        context.getString(R.string.dict_field_word),
        context.getString(R.string.dict_field_pos),
        context.getString(R.string.dict_field_phonetic),
        ...
    };
}
```

**Add to `values/strings.xml`**:
```xml
<string name="dict_field_word">Word</string>
<string name="dict_field_pos">Part of Speech</string>
<string name="dict_field_phonetic">Phonetic</string>
<string name="dict_field_en_definition">English Definition</string>
<string name="dict_field_zh_definition">Chinese Definition</string>
<string name="dict_field_us_pronunciation">US Pronunciation</string>
<string name="dict_field_uk_pronunciation">UK Pronunciation</string>
```

**Add to `values-zh/strings.xml`**:
```xml
<string name="dict_field_word">单词</string>
<string name="dict_field_pos">词性</string>
<string name="dict_field_phonetic">音标</string>
<string name="dict_field_en_definition">英文释义</string>
<string name="dict_field_zh_definition">中文释义</string>
<string name="dict_field_us_pronunciation">有道美式发音</string>
<string name="dict_field_uk_pronunciation">有道英式发音</string>
```

#### Solution 1B: Dictionary Names

**Add to `values/strings.xml`**:
```xml
<string name="dict_name_merriam_webster">Merriam-Webster Advanced Learner\'s Dictionary 2019</string>
<string name="dict_name_oxford">Oxford Advanced Learner\'s Dictionary 10th Edition</string>
<string name="dict_name_cambridge">Cambridge English-Chinese Dictionary</string>
<string name="dict_name_ai">AI Dictionary</string>
```

**Add to `values-zh/strings.xml`**:
```xml
<string name="dict_name_merriam_webster">韦氏高阶英汉双解词典2019完美版</string>
<string name="dict_name_oxford">牛津高阶英汉双解词典第10版完美版</string>
<string name="dict_name_cambridge">剑桥在线英汉双解词典完美版</string>
<string name="dict_name_ai">AI 词典</string>
```

### Priority 2: High Issues (Next Sprint)

#### Solution 2A: Field Type Labels to String Resources

**Move** `Constant.FIELD_TYPE_LABELS` to string resources:

```xml
<!-- values/strings.xml -->
<string-array name="field_type_labels">
    <item>Empty</item>
    <item>Example Sentence</item>
    <item>Bold Example</item>
    <item>Cloze Example</item>
    <item>Cloze Example (Full C1)</item>
    <item>Notes</item>
    <item></item>
    <item>All Definitions</item>
    <item>Sentence Translation</item>
</string-array>

<!-- values-zh/strings.xml -->
<string-array name="field_type_labels">
    <item>空</item>
    <item>例句</item>
    <item>加粗的例句</item>
    <item>挖空的例句</item>
    <item>挖空的例句（全c1模式）</item>
    <item>笔记</item>
    <item></item>
    <item>全部释义</item>
    <item>句子翻译</item>
</string-array>
```

**Usage**:
```java
String[] labels = context.getResources().getStringArray(R.array.field_type_labels);
```

#### Solution 2B: Error Messages to String Resources

```xml
<!-- values/strings.xml -->
<string name="error_dict_not_found">Plan "%1$s" selected dictionary "%2$s" does not exist. Please check if you need to re-import custom dictionary</string>
<string name="error_clipboard_empty">Clipboard is empty!</string>
<string name="error_format_invalid">Format error!</string>
<string name="error_format_line_items">Format error, each line should have 5 items</string>

<!-- values-zh/strings.xml -->
<string name="error_dict_not_found">方案"%1$s"所选词典"%2$s"不存在，请检查是否需要重新导入自定义词典</string>
<string name="error_clipboard_empty">剪贴板为空！</string>
<string name="error_format_invalid">格式错误！</string>
<string name="error_format_line_items">格式错误，每行项目数应为5</string>
```

#### Solution 2C: Add Missing Settings Strings

Add the 16 missing strings to `values/strings.xml` (see section 6 above for the list).

### Priority 3: Medium Issues (Future Consideration)

#### Solution 3A: Refactor Field Name Comparisons

**Current Problem**:
```java
if(map.get(key).equals("句子翻译")){  // Will break if translated
```

**Solution**: Use constant keys instead of display strings:

```java
// Constants for field types
public static final String FIELD_TYPE_SENTENCE_TRANSLATION = "sentence_translation";
public static final String FIELD_TYPE_ORIGINAL_EXAMPLE = "original_example";

// Map using keys, display using localized strings
if(map.get(key).equals(FIELD_TYPE_SENTENCE_TRANSLATION)){
    // works in any language
}
```

This requires database schema changes, so it's a larger refactoring.

---

## Implementation Roadmap

### Sprint 1 (Week 1-2)
- [ ] Move dictionary field names to string resources
- [ ] Move dictionary names to string resources
- [ ] Update all dictionary classes to use context.getString()
- [ ] Test with English and Chinese languages

### Sprint 2 (Week 3-4)
- [ ] Move field type labels to string arrays
- [ ] Move error messages to string resources
- [ ] Add missing settings strings
- [ ] Update all usages to use string resources

### Sprint 3 (Week 5-6)
- [ ] Refactor field name comparisons to use constants
- [ ] Update database schema if needed
- [ ] Migration script for existing users
- [ ] Comprehensive testing

### Sprint 4 (Week 7)
- [ ] Code comment translation (optional)
- [ ] Developer documentation in English
- [ ] Final QA testing

---

## Testing Checklist

After implementing fixes:

- [ ] Switch to English → All dictionary names in English
- [ ] Switch to English → All field names in English
- [ ] Switch to English → All error messages in English
- [ ] Switch to English → All dropdown options in English
- [ ] Switch to Chinese → Everything still works in Chinese
- [ ] Create new plan → Field mapping shows correct language
- [ ] Dictionary lookup → Results show correct field names
- [ ] Import plans → Error messages in correct language

---

## Files Requiring Changes

### High Priority
- `app/src/main/java/com/mmjang/ankihelper/data/dict/Maldpe.java`
- `app/src/main/java/com/mmjang/ankihelper/data/dict/Oalde10.java`
- `app/src/main/java/com/mmjang/ankihelper/data/dict/Cdepe4.java`
- `app/src/main/java/com/mmjang/ankihelper/data/dict/AIDictionary.java`
- `app/src/main/java/com/mmjang/ankihelper/data/plan/VocabularyCardModel.java`
- `app/src/main/java/com/mmjang/ankihelper/util/Constant.java`
- `app/src/main/java/com/mmjang/ankihelper/domain/PronounceManager.java`
- `app/src/main/java/com/mmjang/ankihelper/ui/popup/PopupActivity.java`
- `app/src/main/java/com/mmjang/ankihelper/ui/plan/PlansManagerActivity.java`
- `app/src/main/java/com/mmjang/ankihelper/ui/plan/PlanEditorActivity.java`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values-zh/strings.xml`

### Medium Priority
- `app/src/main/java/com/mmjang/ankihelper/util/Utils.java`

---

## Risk Assessment

### Technical Risks

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Breaking existing plans | High | Critical | Gradual migration with fallback |
| Database schema changes | Medium | High | Versioned migration scripts |
| Field name comparison failures | High | High | Maintain backward compatibility |

### User Impact Risks

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Existing plans stop working | Low | Critical | Test thoroughly before release |
| User confusion from name changes | Medium | Low | Clear release notes |
| Data loss during migration | Low | Critical | Backup before migration |

---

## Conclusion

The app has significant internationalization gaps, particularly in:
1. **Dictionary field names** (hardcoded Chinese)
2. **Field type labels** (hardcoded Chinese)
3. **Error messages** (hardcoded Chinese)
4. **Missing English string resources**

**Recommended Approach**: Tackle Priority 1 issues first (dictionary fields and names), as these are most visible to users. Implement a gradual migration strategy to avoid breaking existing user data.

**Estimated Effort**:
- Priority 1: 16-24 hours
- Priority 2: 12-16 hours
- Priority 3: 20-30 hours
- **Total**: 48-70 hours (~2-3 weeks)

---

**Report Generated**: January 2025
**Next Review**: After Priority 1 implementation
