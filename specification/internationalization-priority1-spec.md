# Priority 1 Internationalization Specification

## Document Metadata
- **Feature**: Dictionary Names and Field Names Internationalization
- **Priority**: P1 - Critical
- **Status**: 📋 Planned
- **Estimated Effort**: 16-24 hours
- **Target Sprint**: Sprint 1 (Week 1-2)
- **Dependencies**: None

## Problem Statement

Currently, all dictionary names and field names are hardcoded in Chinese within the Java classes. When users switch the app to English, these critical UI elements remain in Chinese, creating a poor user experience and breaking the language consistency.

### Affected Components

1. **Dictionary Classes** (4 files):
   - `Maldpe.java` - Merriam-Webster Dictionary
   - `Oalde10.java` - Oxford Dictionary
   - `Cdepe4.java` - Cambridge Dictionary
   - `AIDictionary.java` - AI Dictionary

2. **Model Classes** (1 file):
   - `VocabularyCardModel.java` - Card field definitions

### Current State Examples

```java
// Maldpe.java
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

### User Impact

**Before Fix**:
```
[English App] → Dictionary: "韦氏高阶英汉双解词典2019完美版"
[English App] → Fields: "单词", "词性", "音标", "英文释义"...
```

**After Fix**:
```
[English App] → Dictionary: "Merriam-Webster Advanced Learner's Dictionary 2019"
[English App] → Fields: "Word", "Part of Speech", "Phonetic", "English Definition"...

[Chinese App] → Dictionary: "韦氏高阶英汉双解词典2019完美版"
[Chinese App] → Fields: "单词", "词性", "音标", "英文释义"...
```

## Technical Design

### Architecture Change

**Current Architecture**:
```
Dictionary Class
  ↓
  Static final String DICT_NAME = "韦氏..."
  Static final String[] DICT_FIELDS = {"单词", "词性", ...}
  ↓
  getDictName() → Returns hardcoded Chinese
  getDictFields() → Returns hardcoded Chinese array
```

**New Architecture**:
```
Dictionary Class
  ↓
  Static final String DICT_NAME_RES_ID = R.string.dict_name_merriam_webster
  Static final int[] DICT_FIELD_RES_IDS = {R.string.dict_field_word, ...}
  ↓
  getDictName(Context) → Returns context.getString(DICT_NAME_RES_ID)
  getDictFields(Context) → Returns array from context.getString() calls
```

### String Resource Structure

#### New String Resource IDs

**Dictionary Names** (4 dictionaries × 2 languages = 8 strings):
- `dict_name_merriam_webster`
- `dict_name_oxford`
- `dict_name_cambridge`
- `dict_name_ai`

**Dictionary Field Names** (7 fields × 2 languages = 14 strings):
- `dict_field_word`
- `dict_field_pos` (Part of Speech)
- `dict_field_phonetic`
- `dict_field_en_definition`
- `dict_field_zh_definition`
- `dict_field_us_pronunciation`
- `dict_field_uk_pronunciation`

**Vocabulary Card Fields** (6 fields × 2 languages = 12 strings):
- `vocab_field_word`
- `vocab_field_phonetic`
- `vocab_field_definition`
- `vocab_field_notes`
- `vocab_field_example`
- `vocab_field_pronunciation`

**Total New Strings**: 34 strings (17 English + 17 Chinese)

### Implementation Details

#### Step 1: Add String Resources

**File**: `app/src/main/res/values/strings.xml`

```xml
<!-- Dictionary Names -->
<string name="dict_name_merriam_webster">Merriam-Webster Advanced Learner\'s Dictionary 2019</string>
<string name="dict_name_oxford">Oxford Advanced Learner\'s Dictionary 10th Edition</string>
<string name="dict_name_cambridge">Cambridge English-Chinese Dictionary</string>
<string name="dict_name_ai">AI Dictionary</string>

<!-- Dictionary Field Names -->
<string name="dict_field_word">Word</string>
<string name="dict_field_pos">Part of Speech</string>
<string name="dict_field_phonetic">Phonetic</string>
<string name="dict_field_en_definition">English Definition</string>
<string name="dict_field_zh_definition">Chinese Definition</string>
<string name="dict_field_us_pronunciation">US Pronunciation (Youdao)</string>
<string name="dict_field_uk_pronunciation">UK Pronunciation (Youdao)</string>

<!-- Vocabulary Card Field Names -->
<string name="vocab_field_word">Word</string>
<string name="vocab_field_phonetic">Phonetic</string>
<string name="vocab_field_definition">Definition</string>
<string name="vocab_field_notes">Notes</string>
<string name="vocab_field_example">Example Sentence</string>
<string name="vocab_field_pronunciation">Pronunciation</string>

<!-- Dictionary Descriptions -->
<string name="dict_desc_merriam_webster">Merriam-Webster Advanced Learner\'s Dictionary 2019, from https://forum.freemdict.com/t/topic/25795</string>
<string name="dict_desc_oxford">Oxford Advanced Learner\'s Dictionary 10th Edition, from https://forum.freemdict.com/t/topic/25795</string>
<string name="dict_desc_cambridge">Cambridge English-Chinese Dictionary, from https://forum.freemdict.com/t/topic/25795</string>
```

**File**: `app/src/main/res/values-zh/strings.xml`

```xml
<!-- Dictionary Names -->
<string name="dict_name_merriam_webster">韦氏高阶英汉双解词典2019完美版</string>
<string name="dict_name_oxford">牛津高阶英汉双解词典第10版完美版</string>
<string name="dict_name_cambridge">剑桥在线英汉双解词典完美版</string>
<string name="dict_name_ai">AI 词典</string>

<!-- Dictionary Field Names -->
<string name="dict_field_word">单词</string>
<string name="dict_field_pos">词性</string>
<string name="dict_field_phonetic">音标</string>
<string name="dict_field_en_definition">英文释义</string>
<string name="dict_field_zh_definition">中文释义</string>
<string name="dict_field_us_pronunciation">有道美式发音</string>
<string name="dict_field_uk_pronunciation">有道英式发音</string>

<!-- Vocabulary Card Field Names -->
<string name="vocab_field_word">单词</string>
<string name="vocab_field_phonetic">音标</string>
<string name="vocab_field_definition">释义</string>
<string name="vocab_field_notes">笔记</string>
<string name="vocab_field_example">例句</string>
<string name="vocab_field_pronunciation">发音</string>

<!-- Dictionary Descriptions -->
<string name="dict_desc_merriam_webster">韦氏高阶英汉双解词典2019完美版,来自https://forum.freemdict.com/t/topic/25795</string>
<string name="dict_desc_oxford">牛津高阶英汉双解词典第10版完美版,来自https://forum.freemdict.com/t/topic/25795</string>
<string name="dict_desc_cambridge">剑桥在线英汉双解词典完美版,来自https://forum.freemdict.com/t/topic/25795</string>
```

#### Step 2: Update Maldpe.java (Merriam-Webster Dictionary)

**Current Code**:
```java
private static final String DICT_NAME = "韦氏高阶英汉双解词典2019完美版";

private static final String[] DICT_FIELDS = {
    "单词", "词性", "音标", "英文释义", "中文释义",
    "有道美式发音", "有道英式发音"
};

@Override
public String getDictName() {
    return DICT_NAME;
}

@Override
public String getDictIntroduction() {
    return "韦氏高阶英汉双解词典2019完美版,来自https://forum.freemdict.com/t/topic/25795";
}

@Override
public String[] getAvailableFields() {
    return DICT_FIELDS;
}
```

**New Code**:
```java
// Resource IDs for localization
private static final int DICT_NAME_RES_ID = R.string.dict_name_merriam_webster;
private static final int DICT_DESC_RES_ID = R.string.dict_desc_merriam_webster;
private static final int[] DICT_FIELD_RES_IDS = {
    R.string.dict_field_word,
    R.string.dict_field_pos,
    R.string.dict_field_phonetic,
    R.string.dict_field_en_definition,
    R.string.dict_field_zh_definition,
    R.string.dict_field_us_pronunciation,
    R.string.dict_field_uk_pronunciation
};

@Override
public String getDictName() {
    Context context = MyApplication.getContext();
    return context.getString(DICT_NAME_RES_ID);
}

@Override
public String getDictIntroduction() {
    Context context = MyApplication.getContext();
    return context.getString(DICT_DESC_RES_ID);
}

@Override
public String[] getAvailableFields() {
    Context context = MyApplication.getContext();
    String[] fields = new String[DICT_FIELD_RES_IDS.length];
    for (int i = 0; i < DICT_FIELD_RES_IDS.length; i++) {
        fields[i] = context.getString(DICT_FIELD_RES_IDS[i]);
    }
    return fields;
}
```

**Pattern**: Repeat for Oalde10.java, Cdepe4.java, AIDictionary.java

#### Step 3: Update VocabularyCardModel.java

**Current Code**:
```java
private static final String[] availableFields = {
    "单词", "音标", "释义", "笔记", "例句", "", "发音"
};

@Override
public String[] getAvailableFields() {
    return availableFields;
}
```

**New Code**:
```java
private static final int[] FIELD_RES_IDS = {
    R.string.vocab_field_word,
    R.string.vocab_field_phonetic,
    R.string.vocab_field_definition,
    R.string.vocab_field_notes,
    R.string.vocab_field_example,
    0, // Empty field
    R.string.vocab_field_pronunciation
};

@Override
public String[] getAvailableFields() {
    Context context = MyApplication.getContext();
    String[] fields = new String[FIELD_RES_IDS.length];
    for (int i = 0; i < FIELD_RES_IDS.length; i++) {
        if (FIELD_RES_IDS[i] == 0) {
            fields[i] = "";
        } else {
            fields[i] = context.getString(FIELD_RES_IDS[i]);
        }
    }
    return fields;
}
```

#### Step 4: Update Abstract Dictionary Interface (if needed)

Check if `AbstractDictionary` or `IDictionary` interface needs updates to support Context parameter.

**Current**:
```java
public interface IDictionary {
    String getDictName();
    String[] getAvailableFields();
}
```

**Potential Update** (if signatures need to change):
```java
public interface IDictionary {
    String getDictName(); // Uses MyApplication.getContext() internally
    String[] getAvailableFields(); // Uses MyApplication.getContext() internally
}
```

**Alternative** (if we want explicit Context):
```java
public interface IDictionary {
    String getDictName(Context context);
    String[] getAvailableFields(Context context);
}
```

**Decision**: Keep current interface, use `MyApplication.getContext()` internally to minimize changes.

### Backward Compatibility

#### Database Considerations

**Question**: Do field names get stored in the database?

**Investigation Required**:
1. Check if field names are stored in database
2. Check if field names are used as keys
3. If yes, we need migration strategy

**Mitigation**:
- If field names are stored: Add migration to update stored strings
- If field names are display-only: No migration needed
- **Assumption**: Field names are display-only (needs verification)

#### User Data Migration

**Scenarios**:
1. **User has existing plans with Chinese field names** → Should still work (field order preserved)
2. **User imports/exports plans** → Field names will be in current language
3. **User switches language** → Field names update automatically

**Testing Required**:
- Test with existing plans
- Test import/export
- Test language switching

## Testing Strategy

### Unit Tests

Create new test class: `DictionaryInternationalizationTest.java`

```java
@Test
public void testMerriamWebsterDictionaryNameInEnglish() {
    setLocale(Locale.ENGLISH);
    Maldpe dict = new Maldpe();
    assertEquals("Merriam-Webster Advanced Learner's Dictionary 2019",
                 dict.getDictName());
}

@Test
public void testMerriamWebsterDictionaryNameInChinese() {
    setLocale(Locale.SIMPLIFIED_CHINESE);
    Maldpe dict = new Maldpe();
    assertEquals("韦氏高阶英汉双解词典2019完美版",
                 dict.getDictName());
}

@Test
public void testDictionaryFieldsInEnglish() {
    setLocale(Locale.ENGLISH);
    Maldpe dict = new Maldpe();
    String[] fields = dict.getAvailableFields();
    assertEquals("Word", fields[0]);
    assertEquals("Part of Speech", fields[1]);
    assertEquals("Phonetic", fields[2]);
}

@Test
public void testDictionaryFieldsInChinese() {
    setLocale(Locale.SIMPLIFIED_CHINESE);
    Maldpe dict = new Maldpe();
    String[] fields = dict.getAvailableFields();
    assertEquals("单词", fields[0]);
    assertEquals("词性", fields[1]);
    assertEquals("音标", fields[2]);
}
```

### Manual Testing Checklist

**Pre-Implementation**:
- [ ] Document current behavior with screenshots
- [ ] Test dictionary selection screen (English mode)
- [ ] Test field mapping screen (English mode)
- [ ] Note which elements are in Chinese

**Post-Implementation**:
- [ ] Switch app to English
  - [ ] Verify dictionary names show in English
  - [ ] Verify field names show in English in dictionary selection
  - [ ] Verify field names show in English in plan editor
  - [ ] Verify field mapping dropdown shows English
- [ ] Switch app to Chinese
  - [ ] Verify dictionary names show in Chinese
  - [ ] Verify field names show in Chinese
  - [ ] Verify all UI elements maintain Chinese
- [ ] Test with existing plan
  - [ ] Open existing plan in English mode
  - [ ] Verify plan still works
  - [ ] Verify no data loss
- [ ] Test plan creation
  - [ ] Create new plan in English mode
  - [ ] Map fields using English names
  - [ ] Save and verify
  - [ ] Switch to Chinese, verify plan still works
- [ ] Test import/export
  - [ ] Export plan in English mode
  - [ ] Import plan in Chinese mode
  - [ ] Verify functionality

### Edge Cases

1. **Context is null**: Fallback to English
2. **String resource missing**: Log error, use English fallback
3. **Array index mismatch**: Validate array lengths
4. **Empty fields**: Handle empty string fields correctly

## Implementation Plan

### Task Breakdown

#### Task 1: Add String Resources (2-3 hours)
- [ ] Add English strings to `values/strings.xml`
- [ ] Add Chinese strings to `values-zh/strings.xml`
- [ ] Verify XML syntax
- [ ] Build project to confirm resources compile

#### Task 2: Update Maldpe.java (2 hours)
- [ ] Replace hardcoded strings with resource IDs
- [ ] Update `getDictName()` method
- [ ] Update `getDictIntroduction()` method
- [ ] Update `getAvailableFields()` method
- [ ] Build and verify no compilation errors

#### Task 3: Update Oalde10.java (1.5 hours)
- [ ] Apply same pattern as Maldpe.java
- [ ] Update all methods
- [ ] Build and verify

#### Task 4: Update Cdepe4.java (1.5 hours)
- [ ] Apply same pattern
- [ ] Update all methods
- [ ] Build and verify

#### Task 5: Update AIDictionary.java (1.5 hours)
- [ ] Apply same pattern
- [ ] Update all methods
- [ ] Build and verify

#### Task 6: Update VocabularyCardModel.java (1.5 hours)
- [ ] Replace hardcoded fields with resource IDs
- [ ] Update `getAvailableFields()` method
- [ ] Handle empty field case
- [ ] Build and verify

#### Task 7: Testing (4-6 hours)
- [ ] Write unit tests
- [ ] Run unit tests
- [ ] Manual testing in English mode
- [ ] Manual testing in Chinese mode
- [ ] Test language switching
- [ ] Test with existing plans
- [ ] Test plan creation
- [ ] Fix any bugs found

#### Task 8: Code Review & Cleanup (2 hours)
- [ ] Review all changes
- [ ] Remove debug logs
- [ ] Add inline documentation
- [ ] Verify code style
- [ ] Final build verification

#### Task 9: Documentation (1-2 hours)
- [ ] Update CHANGELOG
- [ ] Update implementation notes
- [ ] Create migration guide (if needed)
- [ ] Update user documentation

**Total Estimated Time**: 17-22 hours

### Dependencies & Risks

**Dependencies**:
- None (self-contained change)

**Risks**:

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Field names used as database keys | Medium | High | Investigate database schema first |
| Context not available in static context | Low | Medium | Use MyApplication.getContext() |
| Breaking existing plans | Medium | Critical | Extensive testing with existing data |
| String resources not found | Low | Low | Add fallback logic |

### Rollback Plan

If critical issues are found:

1. **Immediate Rollback**: Revert to previous commit
2. **Partial Rollback**: Keep string resources, revert code changes
3. **Forward Fix**: Fix issues in place if minor

**Rollback Commits**:
- Tag current commit as `pre-dict-i18n` before starting
- Can use `git revert` if issues found after merge

## Success Criteria

### Functional Requirements
- ✅ All dictionary names display in correct language
- ✅ All field names display in correct language
- ✅ Language switches work seamlessly
- ✅ Existing plans continue to work
- ✅ No data loss or corruption

### Quality Requirements
- ✅ All unit tests pass
- ✅ All manual tests pass
- ✅ No compilation errors or warnings
- ✅ Code review approved
- ✅ Performance impact < 1ms per method call

### User Experience
- ✅ Natural English translations (not literal)
- ✅ Consistent terminology across app
- ✅ No mixed languages in any screen
- ✅ Smooth language switching (no crashes)

## Post-Implementation

### Monitoring

After deployment, monitor for:
- Crash reports related to dictionaries
- User feedback about translations
- Performance metrics
- Database migration issues

### Follow-up Work

After Priority 1 completion:
- [ ] Priority 2: Field type labels internationalization
- [ ] Priority 2: Error messages internationalization
- [ ] Priority 2: Add missing settings strings
- [ ] Priority 3: Refactor field name comparisons

---

## Appendix A: Complete File List

### Files to Modify

1. `app/src/main/res/values/strings.xml` - Add English strings
2. `app/src/main/res/values-zh/strings.xml` - Add Chinese strings
3. `app/src/main/java/com/mmjang/ankihelper/data/dict/Maldpe.java` - Refactor
4. `app/src/main/java/com/mmjang/ankihelper/data/dict/Oalde10.java` - Refactor
5. `app/src/main/java/com/mmjang/ankihelper/data/dict/Cdepe4.java` - Refactor
6. `app/src/main/java/com/mmjang/ankihelper/data/dict/AIDictionary.java` - Refactor
7. `app/src/main/java/com/mmjang/ankihelper/data/plan/VocabularyCardModel.java` - Refactor

### Files to Create

1. `app/src/test/java/com/mmjang/ankihelper/DictionaryInternationalizationTest.java` - Tests

**Total Files**: 8 files (7 modified + 1 created)

## Appendix B: Translation Reference

| English | Chinese | Notes |
|---------|---------|-------|
| Word | 单词 | Noun |
| Part of Speech | 词性 | Grammar term |
| Phonetic | 音标 | IPA notation |
| English Definition | 英文释义 | Dictionary entry |
| Chinese Definition | 中文释义 | Dictionary entry |
| US Pronunciation | 有道美式发音 | Keep "Youdao" in full? |
| UK Pronunciation | 有道英式发音 | Keep "Youdao" in full? |
| Definition | 释义 | Generic definition |
| Notes | 笔记 | User annotations |
| Example Sentence | 例句 | Sample usage |
| Pronunciation | 发音 | Audio/phonetic |

**Translation Decision**: US/UK Pronunciation shortened from "Youdao US/UK Pronunciation" to keep UI cleaner. The source attribution is in the dictionary description.

---

**Specification Version**: 1.0
**Created**: January 2025
**Status**: Ready for Implementation
