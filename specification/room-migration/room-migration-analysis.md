# Room Migration - Current State Analysis

## Executive Summary

The AnkiHelper application currently operates with **three separate database systems** running simultaneously, creating significant architectural complexity, maintenance overhead, and performance concerns. This analysis identifies critical issues and provides recommendations for consolidation.

## Critical Finding: Triple Database Architecture

### Current Database Systems

#### 1. LitePal ORM (org.litepal.guolindev:core:3.2.3)
**Configuration**: `app/src/main/assets/litepal.xml` (version 5)
**Database File**: `ankihelper.db` (LitePal managed)

**Models** (9 total):
1. `OutputPlan` - Plan configurations (extends LitePalSupport)
2. `UserTag` - User-defined tags (extends LitePalSupport)
3. `History` - Lookup history (extends LitePalSupport)
4. `LLMConfig` - Large Language Model configurations
5. `TTSConfig` - Text-to-Speech configurations
6. `AIDictionaryConfig` - AI Dictionary configurations
7. `AITranslatorConfig` - AI Translator configurations
8. `AIDictionaryCache` - AI Dictionary cache
9. `AITranslatorCache` - AI Translator cache

**Usage Locations**:
- `AIConfigRepository.java` - All AI config CRUD operations use LitePal
- `AICacheRepository.java` - Cache operations use LitePal
- Various UI activities

#### 2. Direct SQLite (DatabaseManager + DatabaseHelper)
**Database File**: `ankihelper.db` (version 3)
**Management**: `DatabaseManager.java` + `DatabaseHelper.java`

**Tables**:
1. `history` - History records (POJO: `HistoryPOJO`)
2. `plan` - Plan configurations (POJO: `OutputPlanPOJO`)
3. `book` - Book data (POJO: `Book`)
4. `dict` - Dictionary metadata
5. `entry` - Dictionary entries

**Key Methods**:
- `insertPlan()`, `updatePlan()`, `deletePlanByName()`, `getAllPlan()`, `getPlanByName()`
- `insertHistory()`, `getHistoryAfter()`
- `insertBook()`, `updateBook()`, `deleteBook()`, `getLastBooks()`, `refreshBook()`
- Dictionary operations: `addDictionaryInformation()`, `addEntries()`, `queryHeadword()`

#### 3. Room Persistence Library (Partial Implementation)
**Dictionary Databases** (3 separate databases):

1. **Oalde10Database** (`oaldpe10.db`)
   - Entity: `Oalde10Entry`, `Form`
   - DAO: `Oalde10Dao`
   - ⚠️ **CRITICAL**: Uses `.allowMainThreadQueries()` (line 24)

2. **MaldpeDatabase** (`maldpe.db`)
   - Entity: `MaldpeEntry`, `Form`
   - DAO: `MaldpeDao`
   - ⚠️ **CRITICAL**: Uses `.allowMainThreadQueries()` (line 26)

3. **FormsDatabase** (name not checked)
   - ⚠️ **LIKELY**: Uses `.allowMainThreadQueries()` (pattern matches others)

## Critical Issues Identified

### 1. 🔴 Dual Model Classes (Data Redundancy)

The codebase has **two different class representations** for the same data:

**OutputPlan Data**:
- `OutputPlan.java` (extends LitePalSupport) - LitePal model
- `OutputPlanPOJO.java` (plain Java class) - DatabaseManager model
- **Both have identical fields**: planName, dictionaryKey, outputDeckId, outputModelId, fieldsMap

**History Data**:
- `History.java` (extends LitePalSupport) - LitePal model
- `HistoryPOJO.java` (plain Java class) - DatabaseManager model
- **Both have identical fields**: timeStamp, type, word, sentence, dictionary, definition, translation, note, tag

**Impact**:
- Code duplication and maintenance burden
- Confusion about which class to use
- Potential data inconsistency
- DatabaseManager actually **uses POJOs**, not LitePal models

### 2. 🔴 Critical Performance Issue: Main Thread Database Queries

All Room-based dictionary databases use `.allowMainThreadQueries()`:

```java
// Oalde10Database.java:24
instance = Room.databaseBuilder(context.getApplicationContext(),
        Oalde10Database.class, "oaldpe10.db")
        .allowMainThreadQueries() // ❌ For demonstration, don't use this in production
        .build();
```

**Consequences**:
- **ANR (Application Not Responding)** errors on slow devices
- Poor user experience with UI freezing
- Violates Android best practices
- Can cause app crashes on slow operations
- Google Play Store may flag the app

**Affected Files**:
- `app/src/main/java/com/mmjang/ankihelper/data/dict/Oalde10Database.java:24`
- `app/src/main/java/com/mmjang/ankihelper/data/dict/MaldpeDatabase.java:26`
- `app/src/main/java/com/mmjang/ankihelper/data/dict/FormsDatabase.java` (likely)

### 3. 🟡 Database System Conflicts

**Confusion**: Multiple systems managing the same logical database (`ankihelper.db`):
- LitePal manages `ankihelper.db` with version 5
- DatabaseHelper manages `ankihelper.db` with version 3
- **Potential conflict**: Two systems trying to manage the same file

**Evidence**:
```xml
<!-- litepal.xml -->
<dbname value="ankihelper.db" />
<version value="5" />
```

```java
// DatabaseHelper.java
private static final String DB_NAME = "ankihelper.db";
private static final int VERSION = 3;
```

### 4. 🟡 Missing Room Dependencies

Current `build.gradle` has:
- ✅ `kapt` plugin already configured
- ✅ Kotlin support already present
- ✅ LitePal dependency: `'org.litepal.guolindev:core:3.2.3'`
- ❌ **No Room dependencies**
- ❌ **No Coroutines dependencies** (needed for async operations)

### 5. 🟡 Architectural Inconsistency

**Dictionary Data**: Uses Room (modern, type-safe)
**Application Data**: Uses LitePal + Direct SQLite (legacy, error-prone)
**AI Data**: Uses LitePal only

**Impact**:
- Inconsistent data access patterns
- Different error handling approaches
- Difficult to maintain
- Harder to test

## Current Usage Patterns

### DatabaseManager Usage (Direct SQLite)

**Plan Operations** (uses OutputPlanPOJO):
- `PlansManagerActivity` - getAllPlan()
- `PlanEditorActivity` - getPlanByName(), insertPlan(), updatePlan()
- Plan import/export - refreshPlanWith()

**History Operations** (uses HistoryPOJO):
- `PopupActivity` - insertHistory()
- `HistoryActivity` - getHistoryAfter()
- History sync - insertManyHistory()

**Book Operations**:
- `EpubReaderActivity` - insertBook(), updateBook(), getLastBooks()

### LitePal Usage

**AI Config Operations** (AIConfigRepository):
```java
// All methods use LitePal
LitePal.findAll(LLMConfig.class)
LitePal.find(LLMConfig.class, id)
config.save()  // LitePalSupport method
LitePal.delete(LLMConfig.class, id)
```

**Used in**:
- `AIConfigRepository.java` (13 methods)
- `AICacheRepository.java` (cache operations)
- `CleanAIHistoryActivity.java` (counting records)
- Various AI editor activities

### Room Usage (Dictionary Only)

**Dictionary Queries**:
- `Oalde10.java` - wordLookup() uses Oalde10Database
- `Maldpe.java` - wordLookup() uses MaldpeDatabase
- Word definition lookups in dictionary classes

## Data Model Analysis

### OutputPlan Data Model

**Fields**:
- `planName` (String) - Plan identifier
- `dictionaryKey` (String) - Stable dictionary reference (language-independent)
- `outputDeckId` (long) - AnkiDroid deck ID
- `outputModelId` (long) - AnkiDroid model ID
- `fieldsMap` (String) - Serialized field mappings

**Current Implementations**:
1. `OutputPlan.java` (LitePal) - extends LitePalSupport
2. `OutputPlanPOJO.java` (POJO) - **Actually used by DatabaseManager**

**Migration Note**: Can reuse OutputPlanPOJO with Room annotations

### History Data Model

**Fields**:
- `timeStamp` (long) - Primary key, unique timestamp
- `type` (int) - Entry type (dictionary lookup, translation, etc.)
- `word` (String) - Looked up word
- `sentence` (String) - Source sentence context
- `dictionary` (String) - Dictionary used
- `definition` (String) - Definition found
- `translation` (String) - Translation result
- `note` (String) - User notes
- `tag` (String) - User tags

**Current Implementations**:
1. `History.java` (LitePal) - extends LitePalSupport with @Column(unique = true) on timeStamp
2. `HistoryPOJO.java` (POJO) - **Actually used by DatabaseManager**

**Migration Note**: Can reuse HistoryPOJO with Room annotations

### AI Config Models (LitePal Only)

**LLMConfig** (6 fields):
- id, name, baseUrl, apiToken (encrypted), modelName, endpointPath

**TTSConfig** (Similar structure to LLMConfig)

**AIDictionaryConfig**:
- id, dictionaryName, llmId (foreign key), sourceLanguage, targetLanguage, prompt

**AITranslatorConfig**:
- id, translatorName, llmId (foreign key), sourceLanguage, targetLanguage, prompt, isDefault

**AIDictionaryCache** (8 fields):
- id, hwd (headword), phrase, sense, phonetics, defEn, defCn, example, llmConfigId, timestamp

**AITranslatorCache**:
- id, sourceText, translatedText, sourceLanguage, targetLanguage, llmConfigId, timestamp

## Database Schema Comparison

### LitePal Schema (litepal.xml version 5)
```xml
<list>
    <mapping class="com.mmjang.ankihelper.data.plan.OutputPlan"></mapping>
    <mapping class="com.mmjang.ankihelper.data.model.UserTag"></mapping>
    <mapping class="com.mmjang.ankihelper.data.history.History"></mapping>
    <mapping class="com.mmjang.ankihelper.data.ai.LLMConfig"></mapping>
    <mapping class="com.mmjang.ankihelper.data.ai.TTSConfig"></mapping>
    <mapping class="com.mmjang.ankihelper.data.ai.AIDictionaryConfig"></mapping>
    <mapping class="com.mmjang.ankihelper.data.ai.AITranslatorConfig"></mapping>
    <mapping class="com.mmjang.ankihelper.data.ai.cache.AIDictionaryCache"></mapping>
    <mapping class="com.mmjang.ankihelper.data.ai.cache.AITranslatorCache"></mapping>
</list>
```

### DatabaseHelper Schema (version 3)
```sql
-- History table
CREATE TABLE history (
    timestamp INTEGER PRIMARY KEY,
    type INTEGER,
    word TEXT,
    sentence TEXT,
    dictionary TEXT,
    definition TEXT,
    translation TEXT,
    note TEXT,
    tag TEXT
)

-- Plan table
CREATE TABLE plan (
    planname TEXT,
    dictionarykey TEXT,
    outputdeckid INTEGER,
    outputmodelid INTEGER,
    fieldsmap TEXT
)

-- Book table
CREATE TABLE book (
    id INTEGER,
    lastopentime INTEGER,
    bookname TEXT,
    author TEXT,
    bookpath TEXT,
    readposition TEXT
)

-- Dictionary tables
CREATE TABLE dict (...) -- Custom dictionary metadata
CREATE TABLE entry (...) -- Custom dictionary entries
CREATE INDEX headword_index ON entry (headword)
```

## Recommendations

### 1. Immediate Action (Week 1): Fix Performance Issues

**Priority**: 🔴 **CRITICAL**

Remove `.allowMainThreadQueries()` from all Room databases:

**Files to Fix**:
- `Oalde10Database.java`
- `MaldpeDatabase.java`
- `FormsDatabase.java`
- `Cdepe4Database.java` (if exists)

**Implementation**:
1. Add Kotlin Coroutines dependencies
2. Convert all DAO methods to `suspend` functions
3. Update all dictionary query call sites to use `viewModelScope.launch` or similar
4. Test thoroughly on slow devices

### 2. Short-term (Weeks 2-4): Consolidate to Single Room Database

**Strategy**: Migrate DatabaseManager and LitePal to unified Room database

**Phase 2A: Migrate DatabaseManager** (Week 2-3)
1. Add Room dependencies to build.gradle
2. Convert POJOs to Room entities (add annotations)
3. Create DAOs for Plan, History, Book operations
4. Create `AppDatabase` class with all entities
5. Create migration from SQLite tables to Room
6. Update DatabaseManager to delegate to Room DAOs
7. Test all functionality

**Phase 2B: Migrate AI Configs** (Week 4)
1. Create Room entities for AI configs and caches
2. Add to `AppDatabase`
3. Create DAOs for AI operations
4. Migrate data from LitePal to Room
5. Update `AIConfigRepository` to use Room
6. Update `AICacheRepository` to use Room
7. Test all AI features

### 3. Medium-term (Week 5): Remove LitePal

**Steps**:
1. Search codebase for all `LitePal.` references
2. Verify all functionality uses Room
3. Remove LitePal dependency from build.gradle
4. Delete litepal.xml
5. Delete old LitePal model classes (OutputPlan, History if not used)
6. Final integration testing

### 4. Long-term (Week 6): Optimize and Enhance

**Optimizations**:
1. Add indices on frequently queried columns
2. Implement database migrations for future schema changes
3. Add Room instrumentation tests
4. Optimize query performance
5. Implement proper transaction handling
6. Add LiveData/Flow for reactive queries (optional)
7. Document database architecture

## Migration Benefits

### Technical Benefits
- ✅ Single, consistent database access layer
- ✅ Compile-time SQL validation
- ✅ Type-safe database queries
- ✅ Better performance with optimized Room queries
- ✅ Eliminates dual model classes
- ✅ Fixes critical ANR issues
- ✅ Modern async patterns with Coroutines
- ✅ Better testability with Room testing framework

### Maintenance Benefits
- ✅ Easier to understand and maintain
- ✅ Single source of truth for data access
- ✅ Consistent error handling
- ✅ Easier to add new features
- ✅ Better IDE support and autocomplete
- ✅ Clearer architecture documentation

### Performance Benefits
- ✅ No more main thread blocking
- ✅ Optimized query execution
- ✅ Better memory management
- ✅ Reduced app size (remove LitePal dependency)
- ✅ Faster app startup (single database initialization)

## Risk Assessment

### High Risk
⚠️ **Data loss during migration**
- Mitigation: Comprehensive backup strategy, dual-write period, extensive testing

⚠️ **App crashes if migration fails**
- Mitigation: Graceful fallback to old system, error logging, gradual rollout

### Medium Risk
⚠️ **Performance regression**
- Mitigation: Benchmark before/after, proper indexing, query optimization

⚠️ **Breaking AI features**
- Mitigation: Thorough AI feature testing, parallel system during transition

### Low Risk
⚠️ **Build issues with new dependencies**
- Mitigation: Standard dependencies, well-documented, widely used

## Conclusion

The current triple-database architecture creates significant complexity and performance issues. A phased migration to a unified Room-based system will:

1. **Immediately fix** critical ANR issues
2. **Eliminate** database system conflicts
3. **Reduce** code duplication and maintenance burden
4. **Improve** performance and user experience
5. **Modernize** the codebase with industry best practices

The migration should begin with fixing the critical performance issues (Week 1), then proceed with systematic consolidation over 5-6 weeks, with comprehensive testing at each phase.

---

**Document Version**: 1.0
**Date**: 2025-10-15
**Author**: Room Migration Analysis Team
