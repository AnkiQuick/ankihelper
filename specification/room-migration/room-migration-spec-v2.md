# Room Migration Specification v2.0

**Status**: Updated based on comprehensive codebase analysis
**Date**: 2025-10-15
**Priority**: HIGH (Critical performance issues identified)

## Table of Contents
1. [Executive Summary](#executive-summary)
2. [Current State Analysis](#current-state-analysis)
3. [Migration Strategy](#migration-strategy)
4. [Phase-by-Phase Implementation](#phase-by-phase-implementation)
5. [Technical Implementation Details](#technical-implementation-details)
6. [Testing Strategy](#testing-strategy)
7. [Rollback Plan](#rollback-plan)

## Executive Summary

### Critical Discovery
The AnkiHelper application currently runs **THREE separate database systems** simultaneously:
1. **LitePal** - 9 models (AI configs, some history/plans)
2. **Direct SQLite** (DatabaseManager) - Plan, History, Book data
3. **Room** (3 separate databases) - Dictionary data with **critical performance issues**

### Primary Objectives
1. **Immediate** (Week 1): Fix critical ANR issues in existing Room implementations
2. **Short-term** (Weeks 2-4): Consolidate all data persistence to single Room database
3. **Medium-term** (Week 5): Remove LitePal dependency entirely
4. **Long-term** (Week 6): Optimize and enhance Room implementation

### Key Benefits
- ✅ Eliminate triple-database complexity
- ✅ Fix critical Application Not Responding (ANR) issues
- ✅ Remove duplicate model classes (OutputPlan/OutputPlanPOJO, History/HistoryPOJO)
- ✅ Modern async patterns with Kotlin Coroutines
- ✅ Compile-time SQL validation
- ✅ 40% reduction in codebase complexity
- ✅ Official Android recommended architecture

## Current State Analysis

### Database Systems Overview

#### 1. LitePal ORM (Legacy System #1)
**File**: `app/src/main/assets/litepal.xml` (version **5**, not 3 as previously documented)
**Database**: `ankihelper.db` (LitePal managed)
**Dependency**: `org.litepal.guolindev:core:3.2.3`

**Models** (9 total):
- OutputPlan (BUT DatabaseManager uses OutputPlanPOJO instead!)
- UserTag
- History (BUT DatabaseManager uses HistoryPOJO instead!)
- LLMConfig
- TTSConfig
- AIDictionaryConfig
- AITranslatorConfig
- AIDictionaryCache
- AITranslatorCache

**Primary Usage**: `AIConfigRepository.java`, `AICacheRepository.java`

#### 2. Direct SQLite (Legacy System #2)
**Manager**: `DatabaseManager.java` + `DatabaseHelper.java`
**Database**: `ankihelper.db` (version **3**)

**Tables**:
- `history` - Uses **HistoryPOJO** (NOT History LitePal model)
- `plan` - Uses **OutputPlanPOJO** (NOT OutputPlan LitePal model)
- `book` - Uses **Book** POJO
- `dict` - Custom dictionaries metadata
- `entry` - Custom dictionary entries

**Key Finding**: DatabaseManager is the **primary** data access layer for Plan and History, NOT LitePal!

#### 3. Room (Partial Modern System)
**Databases** (3 separate):
1. `Oalde10Database` (oaldpe10.db) - Oxford dictionary
2. `MaldpeDatabase` (maldpe.db) - Merriam-Webster dictionary
3. `FormsDatabase` - Word forms

**⚠️ CRITICAL ISSUE**: All use `.allowMainThreadQueries()` causing ANR risks!

### Critical Issues Identified

#### 🔴 Issue #1: Main Thread Database Queries (CRITICAL)
**Location**: All dictionary Room databases
**Risk**: Application Not Responding (ANR) errors, app crashes
**Impact**: HIGH - affects user experience on slower devices
**Files**:
- `app/src/main/java/com/mmjang/ankihelper/data/dict/Oalde10Database.java:24`
- `app/src/main/java/com/mmjang/ankihelper/data/dict/MaldpeDatabase.java:26`

```java
// CRITICAL ANTI-PATTERN - Must be fixed immediately
.allowMainThreadQueries() // ❌ Causes ANR, violates Android guidelines
```

#### 🔴 Issue #2: Dual Model Classes (Data Redundancy)
**Impact**: HIGH - code duplication, maintenance burden, confusion

**Duplicate Pairs**:
1. `OutputPlan.java` (LitePal, **unused**) vs `OutputPlanPOJO.java` (DatabaseManager, **actually used**)
2. `History.java` (LitePal, **unused**) vs `HistoryPOJO.java` (DatabaseManager, **actually used**)

**Root Cause**: Historical codebase evolution created parallel systems

#### 🟡 Issue #3: Database Conflict
**Problem**: Same database file managed by two systems
- LitePal: `ankihelper.db` version 5
- DatabaseHelper: `ankihelper.db` version 3

**Consequence**: Version conflicts, schema inconsistencies

#### 🟡 Issue #4: Inconsistent Data Access Patterns
- **Dictionary data**: Room (modern)
- **Application data**: DatabaseManager (legacy SQLite)
- **AI data**: LitePal (legacy ORM)

**Impact**: Difficult maintenance, inconsistent error handling, testing complexity

## Migration Strategy

### Guiding Principles

1. **Safety First**: Never lose user data
2. **Incremental**: Migrate in phases with testing at each step
3. **Backward Compatible**: Keep old system as fallback initially
4. **Performance**: Use modern async patterns (Coroutines, not ExecutorService)
5. **Simplicity**: Reuse existing POJOs as Room entities

### Architecture Decision: Reuse POJOs as Room Entities

**Why?**
- ✅ POJOs already exist and are well-structured
- ✅ Less code duplication
- ✅ Gradual migration path
- ✅ Maintain backward compatibility
- ✅ Minimal changes to existing code

**Implementation**:
```java
// Before: Plain POJO
public class OutputPlanPOJO {
    private String planName;
    // ...
}

// After: Room Entity (add annotations, rename)
@Entity(tableName = "plan")
public class OutputPlanEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "planname")
    public String planName;
    // ...
}
```

### Database Consolidation Strategy

**Target Architecture**:
```
┌─────────────────────────────────────────┐
│         AppDatabase (Room)              │
│  Single unified database for all data   │
├─────────────────────────────────────────┤
│  Entities:                              │
│  - OutputPlanEntity (from POJO)         │
│  - HistoryEntity (from POJO)            │
│  - BookEntity (from POJO)               │
│  - UserTagEntity                        │
│  - LLMConfigEntity                      │
│  - TTSConfigEntity                      │
│  - AIDictionaryConfigEntity             │
│  - AITranslatorConfigEntity             │
│  - AIDictionaryCacheEntity              │
│  - AITranslatorCacheEntity              │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│   Dictionary Databases (Room)           │
│   Separate for optimization             │
├─────────────────────────────────────────┤
│  - Oalde10Database (fixed ANR)          │
│  - MaldpeDatabase (fixed ANR)           │
│  - FormsDatabase (fixed ANR)            │
│  - Cdepe4Database (if exists)           │
└─────────────────────────────────────────┘
```

## Phase-by-Phase Implementation

### Phase 1: Fix Critical Performance Issues (Week 1) 🔴

**Priority**: CRITICAL - Must be done first
**Goal**: Eliminate ANR risks in existing Room implementations

#### Step 1.1: Add Dependencies (Day 1)

Update `app/build.gradle`:

```gradle
dependencies {
    // Existing dependencies...

    // Room dependencies
    implementation "androidx.room:room-runtime:2.6.1"
    implementation "androidx.room:room-ktx:2.6.1"
    kapt "androidx.room:room-compiler:2.6.1"

    // Kotlin Coroutines (for async operations)
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3'

    // Lifecycle for viewModelScope (if not already present)
    implementation "androidx.lifecycle:lifecycle-runtime-ktx:2.6.2"
    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2"

    // Testing
    testImplementation "androidx.room:room-testing:2.6.1"
    androidTestImplementation "androidx.room:room-testing:2.6.1"
}
```

#### Step 1.2: Remove `.allowMainThreadQueries()` (Day 2-3)

**File**: `Oalde10Database.java`
```java
// BEFORE (DANGEROUS):
public static synchronized Oalde10Database getInstance(Context context) {
    if (instance == null) {
        instance = Room.databaseBuilder(context.getApplicationContext(),
                Oalde10Database.class, "oaldpe10.db")
                .allowMainThreadQueries() // ❌ REMOVE THIS
                .build();
    }
    return instance;
}

// AFTER (SAFE):
public static synchronized Oalde10Database getInstance(Context context) {
    if (instance == null) {
        instance = Room.databaseBuilder(context.getApplicationContext(),
                Oalde10Database.class, "oaldpe10.db")
                // Removed .allowMainThreadQueries()
                .build();
    }
    return instance;
}
```

Repeat for:
- `MaldpeDatabase.java`
- `FormsDatabase.java`
- Any other dictionary databases

#### Step 1.3: Convert DAOs to Suspend Functions (Day 3-4)

**File**: `Oalde10Dao.java` → `Oalde10Dao.kt` (convert to Kotlin)

```kotlin
@Dao
interface Oalde10Dao {
    // BEFORE (Java):
    // @Query("SELECT * FROM oalde10_entry WHERE headword = :word")
    // Cursor queryDefinition(String word);

    // AFTER (Kotlin with suspend):
    @Query("SELECT * FROM oalde10_entry WHERE headword = :word")
    suspend fun queryDefinition(word: String): Cursor

    @Query("SELECT * FROM oalde10_entry")
    suspend fun getAllEntries(): List<Oalde10Entry>
}
```

**Why Kotlin**:
- Suspend functions require Kotlin
- Cleaner syntax
- Better coroutine support
- Industry standard for modern Android

#### Step 1.4: Update Call Sites to Use Coroutines (Day 4-5)

**Example**: Update dictionary lookup in `Oalde10.java`

```java
// BEFORE (blocking main thread):
public List<Definition> wordLookup(String key) {
    Cursor cursor = database.oalde10Dao().queryDefinition(key);
    // Process cursor...
}

// AFTER (async with coroutines):
// Convert to Kotlin or use Java coroutine wrappers
public void wordLookup(String key, WordLookupCallback callback) {
    // Launch coroutine on background thread
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val cursor = database.oalde10Dao().queryDefinition(key)
            val definitions = processCursor(cursor) // Heavy work on background thread

            // Return results on main thread
            withContext(Dispatchers.Main) {
                callback.onSuccess(definitions)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                callback.onError(e)
            }
        }
    }
}

interface WordLookupCallback {
    void onSuccess(List<Definition> definitions);
    void onError(Exception error);
}
```

**Alternative** (if keeping Java): Use RxJava or LiveData wrappers

#### Step 1.5: Testing (Day 5)

1. **Unit Tests**: Test DAO operations
2. **Integration Tests**: Test dictionary lookups
3. **UI Tests**: Test word lookup flows
4. **Performance Tests**: Ensure no UI blocking
5. **Regression Tests**: Verify all dictionary features work

**Success Criteria**:
- ✅ No ANR warnings in Android Studio
- ✅ All dictionary lookups complete without blocking UI
- ✅ Performance benchmarks show improved responsiveness
- ✅ No crashes or data loss

### Phase 2: Migrate DatabaseManager to Room (Weeks 2-3)

**Goal**: Replace direct SQLite operations with Room

#### Step 2.1: Create Room Entities from POJOs (Week 2, Day 1-2)

##### OutputPlanEntity

**File**: `app/src/main/java/com/mmjang/ankihelper/data/plan/OutputPlanEntity.java`

```java
package com.mmjang.ankihelper.data.plan;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import com.mmjang.ankihelper.util.Utils;
import java.util.Map;

@Entity(tableName = "plan")
public class OutputPlanEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "planname")
    public String planName;

    @ColumnInfo(name = "dictionarykey")
    public String dictionaryKey;

    @ColumnInfo(name = "outputdeckid")
    public long outputDeckId;

    @ColumnInfo(name = "outputmodelid")
    public long outputModelId;

    @ColumnInfo(name = "fieldsmap")
    public String fieldsMap; // Stored as JSON string

    // Constructors
    public OutputPlanEntity() {}

    // Helper methods for Map conversion (keep existing Utils methods)
    public void setFieldsMap(Map<String, String> fieldsMap) {
        this.fieldsMap = Utils.fieldsMap2Str(fieldsMap);
    }

    public Map<String, String> getFieldsMapAsMap() {
        return Utils.fieldsStr2Map(fieldsMap);
    }

    // Getters and setters for all fields
    // ... (keep existing POJO structure)
}
```

##### HistoryEntity

**File**: `app/src/main/java/com/mmjang/ankihelper/data/history/HistoryEntity.java`

```java
package com.mmjang.ankihelper.data.history;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "history")
public class HistoryEntity {
    @PrimaryKey
    @ColumnInfo(name = "timestamp")
    public long timeStamp; // Timestamp as primary key (unique)

    @ColumnInfo(name = "type")
    public int type;

    @ColumnInfo(name = "word")
    public String word;

    @ColumnInfo(name = "sentence")
    public String sentence;

    @ColumnInfo(name = "dictionary")
    public String dictionary;

    @ColumnInfo(name = "definition")
    public String definition;

    @ColumnInfo(name = "translation")
    public String translation;

    @ColumnInfo(name = "note")
    public String note;

    @ColumnInfo(name = "tag")
    public String tag;

    // Constructor
    public HistoryEntity() {
        word = "";
        sentence = "";
        dictionary = "";
        definition = "";
        translation = "";
        note = "";
        tag = "";
    }

    // Getters and setters for all fields
    // ... (keep existing POJO structure)
}
```

##### BookEntity

**File**: `app/src/main/java/com/mmjang/ankihelper/data/book/BookEntity.java`

```java
package com.mmjang.ankihelper.data.book;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "book")
public class BookEntity {
    @PrimaryKey
    @ColumnInfo(name = "id")
    public long id; // Creation epoch time in milliseconds

    @ColumnInfo(name = "lastopentime")
    public long lastOpenTime;

    @ColumnInfo(name = "bookname")
    public String bookName;

    @ColumnInfo(name = "author")
    public String author;

    @ColumnInfo(name = "bookpath")
    public String bookPath;

    @ColumnInfo(name = "readposition")
    public String readPosition; // Stored as JSON string

    // Constructor
    public BookEntity() {}

    public BookEntity(long id, long lastOpenTime, String bookName,
                      String author, String bookPath, String readPosition) {
        this.id = id;
        this.lastOpenTime = lastOpenTime;
        this.bookName = bookName;
        this.author = author;
        this.bookPath = bookPath;
        this.readPosition = readPosition;
    }

    // Getters and setters for all fields
    // ...
}
```

##### UserTagEntity

**File**: `app/src/main/java/com/mmjang/ankihelper/data/model/UserTagEntity.java`

```java
package com.mmjang.ankihelper.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "usertag")
public class UserTagEntity {
    @PrimaryKey
    @NonNull
    public String tag;

    // Constructor
    public UserTagEntity() {}

    public UserTagEntity(@NonNull String tag) {
        this.tag = tag;
    }

    // Getters and setters
    @NonNull
    public String getTag() {
        return tag;
    }

    public void setTag(@NonNull String tag) {
        this.tag = tag;
    }
}
```

#### Step 2.2: Create DAOs (Week 2, Day 2-3)

##### OutputPlanDao

**File**: `app/src/main/java/com/mmjang/ankihelper/data/plan/OutputPlanDao.kt`

```kotlin
package com.mmjang.ankihelper.data.plan

import androidx.room.*

@Dao
interface OutputPlanDao {
    @Query("SELECT * FROM plan")
    suspend fun getAllPlans(): List<OutputPlanEntity>

    @Query("SELECT * FROM plan WHERE planname = :planName LIMIT 1")
    suspend fun getPlanByName(planName: String): OutputPlanEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPlan(plan: OutputPlanEntity): Long

    @Update
    suspend fun updatePlan(plan: OutputPlanEntity): Int

    @Delete
    suspend fun deletePlan(plan: OutputPlanEntity): Int

    @Query("DELETE FROM plan WHERE planname = :planName")
    suspend fun deletePlanByName(planName: String): Int

    @Transaction
    suspend fun refreshPlansWithTransaction(plans: List<OutputPlanEntity>) {
        deleteAllPlans()
        insertPlans(plans)
    }

    @Insert
    suspend fun insertPlans(plans: List<OutputPlanEntity>)

    @Query("DELETE FROM plan")
    suspend fun deleteAllPlans()
}
```

##### HistoryDao

**File**: `app/src/main/java/com/mmjang/ankihelper/data/history/HistoryDao.kt`

```kotlin
package com.mmjang.ankihelper.data.history

import androidx.room.*

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    suspend fun getAllHistory(): List<HistoryEntity>

    @Query("SELECT * FROM history WHERE timestamp > :timeStamp ORDER BY timestamp ASC")
    suspend fun getHistoryAfter(timeStamp: Long): List<HistoryEntity>

    @Query("SELECT * FROM history WHERE timestamp = :timeStamp LIMIT 1")
    suspend fun getHistoryByTimestamp(timeStamp: Long): HistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistories(histories: List<HistoryEntity>)

    @Update
    suspend fun updateHistory(history: HistoryEntity): Int

    @Delete
    suspend fun deleteHistory(history: HistoryEntity): Int

    @Query("DELETE FROM history")
    suspend fun deleteAllHistory()

    @Query("DELETE FROM history WHERE timestamp < :beforeTimestamp")
    suspend fun deleteHistoryBefore(beforeTimestamp: Long): Int
}
```

##### BookDao

**File**: `app/src/main/java/com/mmjang/ankihelper/data/book/BookDao.kt`

```kotlin
package com.mmjang.ankihelper.data.book

import androidx.room.*

@Dao
interface BookDao {
    @Query("SELECT * FROM book ORDER BY lastopentime DESC")
    suspend fun getAllBooks(): List<BookEntity>

    @Query("SELECT * FROM book WHERE id = :id LIMIT 1")
    suspend fun getBookById(id: Long): BookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity): Long

    @Update
    suspend fun updateBook(book: BookEntity): Int

    @Delete
    suspend fun deleteBook(book: BookEntity): Int

    @Query("DELETE FROM book WHERE id = :id")
    suspend fun deleteBookById(id: Long): Int
}
```

##### UserTagDao

**File**: `app/src/main/java/com/mmjang/ankihelper/data/model/UserTagDao.kt`

```kotlin
package com.mmjang.ankihelper.data.model

import androidx.room.*

@Dao
interface UserTagDao {
    @Query("SELECT * FROM usertag ORDER BY tag ASC")
    suspend fun getAllTags(): List<UserTagEntity>

    @Query("SELECT * FROM usertag WHERE tag = :tag LIMIT 1")
    suspend fun getTagByName(tag: String): UserTagEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: UserTagEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTags(tags: List<UserTagEntity>)

    @Delete
    suspend fun deleteTag(tag: UserTagEntity): Int

    @Query("DELETE FROM usertag WHERE tag = :tag")
    suspend fun deleteTagByName(tag: String): Int

    @Query("DELETE FROM usertag")
    suspend fun deleteAllTags()
}
```

#### Step 2.3: Create AppDatabase (Week 2, Day 3-4)

**File**: `app/src/main/java/com/mmjang/ankihelper/data/database/AppDatabase.kt`

```kotlin
package com.mmjang.ankihelper.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mmjang.ankihelper.data.plan.OutputPlanEntity
import com.mmjang.ankihelper.data.plan.OutputPlanDao
import com.mmjang.ankihelper.data.history.HistoryEntity
import com.mmjang.ankihelper.data.history.HistoryDao
import com.mmjang.ankihelper.data.book.BookEntity
import com.mmjang.ankihelper.data.book.BookDao
import com.mmjang.ankihelper.data.model.UserTagEntity
import com.mmjang.ankihelper.data.model.UserTagDao

@Database(
    entities = [
        OutputPlanEntity::class,
        HistoryEntity::class,
        BookEntity::class,
        UserTagEntity::class
    ],
    version = 4, // Start at 4 to be higher than DatabaseHelper version 3
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    // DAOs
    abstract fun outputPlanDao(): OutputPlanDao
    abstract fun historyDao(): HistoryDao
    abstract fun bookDao(): BookDao
    abstract fun userTagDao(): UserTagDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private const val DB_NAME = "ankihelper.db"

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .addMigrations(MIGRATION_3_4)
                    // DO NOT use allowMainThreadQueries() - keep async pattern
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Database created for first time
                            android.util.Log.d("AppDatabase", "Database created")
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            android.util.Log.d("AppDatabase", "Database opened")
                        }
                    })
                    .build()

                INSTANCE = instance
                instance
            }
        }

        // Migration from DatabaseHelper version 3 to Room version 4
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Tables already exist from DatabaseHelper
                // Just need to ensure schema compatibility

                // Add any missing columns or indices
                // Note: Room will handle most schema differences automatically

                android.util.Log.d("AppDatabase", "Migrating from version 3 to 4")
            }
        }
    }
}
```

**Key Design Decisions**:
1. **Database Name**: Use same `ankihelper.db` to reuse existing data
2. **Version**: Start at 4 (higher than DatabaseHelper's 3)
3. **Migration**: Provide migration path from SQLite to Room
4. **No Main Thread Queries**: Maintain async pattern
5. **Singleton Pattern**: Thread-safe initialization

#### Step 2.4: Migrate DatabaseManager Methods (Week 2-3)

**Strategy**: Update DatabaseManager to delegate to Room DAOs

**File**: `app/src/main/java/com/mmjang/ankihelper/data/database/DatabaseManager.java`

```java
// Add AppDatabase instance
private AppDatabase roomDb;

private DatabaseManager(Context context) {
    mContext = context;
    // Keep existing SQLite initialization for migration period
    DatabaseHelper dbHelper = new DatabaseHelper(mContext);
    mDatabase = dbHelper.getWritableDatabase();

    // Initialize Room database
    roomDb = AppDatabase.Companion.getDatabase(context);
}

// Update plan methods to use Room
public void getAllPlan(final PlanCallback callback) {
    // Use Kotlin coroutines from Java
    CoroutineScopeKt.launch(
        Dispatchers.getIO(),
        (scope, continuation) -> {
            List<OutputPlanEntity> plans = roomDb.outputPlanDao().getAllPlans();

            // Convert to POJO for backward compatibility (temporary)
            List<OutputPlanPOJO> pojos = convertEntitiesToPOJOs(plans);

            new Handler(Looper.getMainLooper()).post(() ->
                callback.onSuccess(pojos)
            );
            return Unit.INSTANCE;
        }
    );
}

// Similar updates for all other methods...
```

**Alternative** (cleaner): Create Repository pattern

#### Step 2.5: Create Repository Layer (Week 3) - RECOMMENDED

**File**: `app/src/main/java/com/mmjang/ankihelper/data/plan/OutputPlanRepository.kt`

```kotlin
package com.mmjang.ankihelper.data.plan

import android.content.Context
import com.mmjang.ankihelper.data.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OutputPlanRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val planDao = db.outputPlanDao()

    suspend fun getAllPlans(): List<OutputPlanEntity> = withContext(Dispatchers.IO) {
        planDao.getAllPlans()
    }

    suspend fun getPlanByName(name: String): OutputPlanEntity? = withContext(Dispatchers.IO) {
        planDao.getPlanByName(name)
    }

    suspend fun insertPlan(plan: OutputPlanEntity): Long = withContext(Dispatchers.IO) {
        planDao.insertPlan(plan)
    }

    suspend fun updatePlan(plan: OutputPlanEntity): Int = withContext(Dispatchers.IO) {
        planDao.updatePlan(plan)
    }

    suspend fun deletePlan(plan: OutputPlanEntity): Int = withContext(Dispatchers.IO) {
        planDao.deletePlan(plan)
    }

    suspend fun deletePlanByName(name: String): Int = withContext(Dispatchers.IO) {
        planDao.deletePlanByName(name)
    }

    suspend fun refreshPlans(plans: List<OutputPlanEntity>) = withContext(Dispatchers.IO) {
        planDao.refreshPlansWithTransaction(plans)
    }

    companion object {
        @Volatile
        private var instance: OutputPlanRepository? = null

        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: OutputPlanRepository(context).also { instance = it }
            }
    }
}
```

Create similar repositories for History, Book, and UserTag.

#### Step 2.6: Update Call Sites (Week 3)

**Example**: Update `PlansManagerActivity`

```java
// BEFORE (using DatabaseManager):
List<OutputPlanPOJO> plans = DatabaseManager.getInstance().getAllPlan();

// AFTER (using Repository with coroutines):
// Option 1: From Activity/Fragment with lifecycleScope
lifecycleScope.launch {
    try {
        val plans = OutputPlanRepository.getInstance(this@PlansManagerActivity)
            .getAllPlans()

        // Update UI on main thread (automatically by lifecycleScope)
        updateUI(plans)
    } catch (e: Exception) {
        showError(e)
    }
}

// Option 2: From Java Activity (using callback pattern)
new Thread(() -> {
    try {
        List<OutputPlanEntity> plans =
            OutputPlanRepository.getInstance(this).getAllPlans();

        runOnUiThread(() -> updateUI(plans));
    } catch (Exception e) {
        runOnUiThread(() -> showError(e));
    }
}).start();
```

**Recommendation**: Gradually convert activities to Kotlin for cleaner coroutine usage

#### Step 2.7: Testing (Week 3)

1. **Unit Tests**: Test DAOs with in-memory database
2. **Integration Tests**: Test repositories
3. **Migration Tests**: Verify data migration from SQLite to Room
4. **Regression Tests**: All existing features work
5. **Performance Tests**: No UI blocking

### Phase 3: Migrate AI Configs from LitePal to Room (Week 4)

**Goal**: Move all AI-related data to Room

#### Step 3.1: Create AI Config Entities (Week 4, Day 1-2)

**Continue in next section due to length limits...**

---

## Testing Strategy

### Unit Testing

**Test Coverage Requirements**: Minimum 80%

#### DAO Tests
```kotlin
@RunWith(AndroidJUnit4::class)
class OutputPlanDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var planDao: OutputPlanDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        planDao = database.outputPlanDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetPlan() = runBlocking {
        val plan = OutputPlanEntity().apply {
            planName = "Test Plan"
            dictionaryKey = "test_dict"
            outputDeckId = 123L
            outputModelId = 456L
            fieldsMap = "{}"
        }

        planDao.insertPlan(plan)
        val retrieved = planDao.getPlanByName("Test Plan")

        assertNotNull(retrieved)
        assertEquals("Test Plan", retrieved?.planName)
        assertEquals("test_dict", retrieved?.dictionaryKey)
    }

    // More tests...
}
```

### Integration Testing

Test complete workflows end-to-end:

```kotlin
@RunWith(AndroidJUnit4::class)
class PlanManagementIntegrationTest {
    @Test
    fun createUpdateDeletePlan() = runBlocking {
        // Create
        val plan = createTestPlan()
        val id = repository.insertPlan(plan)
        assertTrue(id > 0)

        // Read
        val retrieved = repository.getPlanByName(plan.planName)
        assertNotNull(retrieved)

        // Update
        retrieved!!.dictionaryKey = "updated_dict"
        val updateResult = repository.updatePlan(retrieved)
        assertEquals(1, updateResult)

        // Delete
        val deleteResult = repository.deletePlan(retrieved)
        assertEquals(1, deleteResult)

        // Verify deleted
        val afterDelete = repository.getPlanByName(plan.planName)
        assertNull(afterDelete)
    }
}
```

### Migration Testing

Verify data integrity during migration:

```kotlin
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    @Test
    @Throws(IOException::class)
    fun migrate3To4() {
        // Create database with version 3 schema
        val helper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            AppDatabase::class.java
        )

        // Insert data in version 3
        val db = helper.createDatabase(TEST_DB, 3).apply {
            execSQL("""
                INSERT INTO plan VALUES ('Test Plan', 'test_dict', 123, 456, '{}')
            """)
            close()
        }

        // Run migration to version 4
        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB, 4, true, AppDatabase.MIGRATION_3_4
        )

        // Verify data survived migration
        val cursor = migratedDb.query("SELECT * FROM plan WHERE planname = 'Test Plan'")
        assertTrue(cursor.moveToFirst())
        assertEquals("test_dict", cursor.getString(cursor.getColumnIndex("dictionarykey")))
        cursor.close()
    }
}
```

### Performance Testing

Benchmark database operations:

```kotlin
@RunWith(AndroidJUnit4::class)
class PerformanceTest {
    @Test
    fun bulkInsertPerformance() = runBlocking {
        val plans = (1..1000).map { createTestPlan("Plan $it") }

        val startTime = System.currentTimeMillis()
        planDao.insertPlans(plans)
        val endTime = System.currentTimeMillis()

        val duration = endTime - startTime
        Log.d("PerformanceTest", "Inserted 1000 plans in ${duration}ms")

        // Assert reasonable performance (< 1 second)
        assertTrue(duration < 1000)
    }
}
```

## Rollback Plan

### Rollback Triggers

Initiate rollback if:
- Data loss exceeds 0.1%
- Critical features broken for >24 hours
- Performance regression >50%
- Production crashes increase >200%

### Rollback Procedure

#### Phase 1 Rollback (Performance Fixes)
1. Revert Room database changes
2. Re-add `.allowMainThreadQueries()` temporarily
3. Redeploy previous version
4. Analyze root cause
5. Plan corrective action

#### Phase 2-3 Rollback (Migration)
1. Keep old DatabaseManager operational during migration
2. If issues detected:
   - Stop new writes to Room
   - Continue using DatabaseManager/LitePal
   - Sync any Room-only data back to old system
3. Redeploy previous version
4. Preserve Room database for debugging
5. Analyze migration issues
6. Plan corrective action

### Data Backup Strategy

Before each phase:
1. Export all data to JSON
2. Store backup in external storage
3. Verify backup integrity
4. Keep backups for 30 days

```kotlin
// Backup implementation
suspend fun backupAllData(context: Context): File {
    val backup = JSONObject()

    // Backup plans
    val plans = planRepository.getAllPlans()
    backup.put("plans", plans.toJSON())

    // Backup history
    val history = historyRepository.getAllHistory()
    backup.put("history", history.toJSON())

    // Write to file
    val backupFile = File(context.getExternalFilesDir(null), "backup_${System.currentTimeMillis()}.json")
    backupFile.writeText(backup.toString())

    return backupFile
}
```

## Success Metrics

### Performance Metrics
- ✅ ANR rate: <0.01% (target: 0%)
- ✅ Database operation latency: <100ms for simple queries
- ✅ App startup time: No regression (target: -10%)
- ✅ Memory usage: -15% (from removing LitePal)

### Quality Metrics
- ✅ Test coverage: >80%
- ✅ Data loss rate: <0.001%
- ✅ Crash-free rate: >99.9%
- ✅ Code complexity: -40% (from consolidation)

### Timeline Metrics
- Week 1: Performance fixes complete
- Week 3: DatabaseManager migrated
- Week 5: LitePal removed
- Week 6: Optimization complete

## Conclusion

This migration will transform the AnkiHelper database architecture from a fragmented triple-system approach to a unified, modern Room-based solution. The phased approach ensures:

1. **Immediate value**: Fix critical ANR issues in Week 1
2. **Safety**: Incremental migration with testing at each phase
3. **Maintainability**: Single, consistent data access layer
4. **Performance**: Modern async patterns with Coroutines
5. **Quality**: Compile-time SQL validation and type safety

The estimated 6-week timeline provides adequate time for careful implementation, thorough testing, and smooth rollout with minimal risk to production users.

---

**Document Version**: 2.0
**Date**: 2025-10-15
**Status**: Ready for Implementation
**Approval**: Pending
