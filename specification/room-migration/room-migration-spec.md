# Room Migration Specification

## Overview
This document provides a comprehensive specification for migrating from LitePal and direct SQLite to Room for the main application data models: OutputPlan, History, and UserTag. Room is the recommended persistence library for Android development, providing compile-time verification of SQL queries and better integration with Android Architecture Components.

## Current State Analysis

### Existing Room Implementation
The application already has a complete Room implementation for dictionary data in the `data/dict/` directory:
1. Entities with `@Entity` annotations
2. DAOs with `@Dao` annotations
3. Database classes with `@Database` annotations

This provides a proven pattern to follow for migrating the remaining data models.

### LitePal Models (Target for Migration)
The application currently uses LitePal for three main models:
1. `OutputPlan` - Configuration for dictionary output plans (`data/plan/OutputPlan.java`)
2. `History` - Record of user lookups and translations (`data/history/History.java`)
3. `UserTag` - User-defined tags for organizing content (`data/model/UserTag.java`)

### Direct SQLite Usage (Target for Migration)
The application also uses direct SQLite operations managed by `DatabaseManager.java` for:
1. Book data (`data/book/`)
2. Plan data
3. History data
4. Other application data

### Current Configuration (litepal.xml)
```xml
<litepal>
    <dbname value="ankihelper.db" />
    <version value="3" />
    <list>
        <mapping class="com.mmjang.ankihelper.data.plan.OutputPlan"></mapping>
        <mapping class="com.mmjang.ankihelper.data.model.UserTag"></mapping>
        <mapping class="com.mmjang.ankihelper.data.history.History"></mapping>
    </list>
</litepal>
```

### Dependency
```gradle
implementation 'org.litepal.guolindev:core:3.2.3'
```

## Why Migrate to Room?

1. **Official Recommendation**: Room is Google's recommended approach for data persistence in Android
2. **Compile-time Verification**: Room validates SQL queries at compile time, preventing runtime errors
3. **Better Performance**: Optimized query execution and reduced boilerplate code
4. **Architecture Components Integration**: Seamless integration with LiveData, Flow, and other Android Architecture Components
5. **Active Development**: Room is actively maintained by Google, while LitePal is no longer actively developed
6. **Consistency**: Unifies data persistence approach across the application
7. **Better Threading**: Built-in background thread support

## Room Implementation Structure

Based on the existing Room implementations in the codebase for dictionary data, we will follow the same patterns:

1. **Entities**: Create `@Entity` annotated classes for each data model
2. **DAOs**: Create `@Dao` interfaces with abstract methods for data operations
3. **Database**: Create `@Database` annotated class extending `RoomDatabase`
4. **Type Converters**: Handle complex data types like Maps using `@TypeConverter`
5. **Repository Pattern**: (Optional) Create repository classes for clean separation of concerns

## Detailed Implementation Plan

### 1. Add Room Dependencies

Update `app/build.gradle`:

```gradle
dependencies {
    // Existing dependencies...
    
    // Room dependencies
    implementation "androidx.room:room-runtime:2.6.1"
    implementation "androidx.room:room-ktx:2.6.1"
    kapt "androidx.room:room-compiler:2.6.1"
    
    // For TypeConverter support with Maps
    implementation 'com.google.code.gson:gson:2.10.1'
}
```

### 2. Create Room Entities

#### OutputPlan Entity
File: `app/src/main/java/com/mmjang/ankihelper/data/plan/OutputPlanEntity.java`

```java
package com.mmjang.ankihelper.data.plan;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.mmjang.ankihelper.util.Utils;
import java.util.Map;

@Entity(tableName = "output_plans")
public class OutputPlanEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String planName;
    public String dictionaryKey;
    public long outputDeckId;
    public long outputModelId;
    public String fieldsMap;

    // Required default constructor for Room
    public OutputPlanEntity() {}

    // Convenience methods for Map conversion
    public void setFieldsMap(Map<String, String> fieldsMap) {
        this.fieldsMap = Utils.fieldsMap2Str(fieldsMap);
    }

    public Map<String, String> getFieldsMap() {
        return Utils.fieldsStr2Map(fieldsMap);
    }
}
```

#### History Entity
File: `app/src/main/java/com/mmjang/ankihelper/data/history/HistoryEntity.java`

```java
package com.mmjang.ankihelper.data.history;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "history")
public class HistoryEntity {
    @PrimaryKey
    public long timeStamp;
    
    public int type;
    public String word;
    public String sentence;
    public String dictionary;
    public String definition;
    public String translation;
    public String note;
    public String tag;

    // Required default constructor for Room
    public HistoryEntity() {
        word = "";
        sentence = "";
        dictionary = "";
        definition = "";
        translation = "";
        note = "";
        tag = "";
    }
}
```

#### UserTag Entity
File: `app/src/main/java/com/mmjang/ankihelper/data/model/UserTagEntity.java`

```java
package com.mmjang.ankihelper.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_tags")
public class UserTagEntity {
    @PrimaryKey
    public String tag;

    // Required default constructor for Room
    public UserTagEntity() {}

    public UserTagEntity(String tag) {
        this.tag = tag;
    }
}
```

### 3. Create Type Converters

File: `app/src/main/java/com/mmjang/ankihelper/data/database/Converters.java`

```java
package com.mmjang.ankihelper.data.database;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Map;

public class Converters {
    @TypeConverter
    public static Map<String, String> fromString(String value) {
        if (value == null) {
            return null;
        }
        Type mapType = new TypeToken<Map<String, String>>() {}.getType();
        return new Gson().fromJson(value, mapType);
    }

    @TypeConverter
    public static String fromMap(Map<String, String> map) {
        if (map == null) {
            return null;
        }
        Gson gson = new Gson();
        return gson.toJson(map);
    }
}
```

### 4. Create DAOs

#### OutputPlanDao
File: `app/src/main/java/com/mmjang/ankihelper/data/plan/OutputPlanDao.java`

```java
package com.mmjang.ankihelper.data.plan;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import java.util.List;

@Dao
public interface OutputPlanDao {
    @Query("SELECT * FROM output_plans")
    List<OutputPlanEntity> getAllOutputPlans();

    @Query("SELECT * FROM output_plans WHERE id = :id")
    OutputPlanEntity getOutputPlanById(int id);

    @Insert
    void insertOutputPlan(OutputPlanEntity outputPlan);

    @Insert
    void insertOutputPlans(List<OutputPlanEntity> outputPlans);

    @Update
    void updateOutputPlan(OutputPlanEntity outputPlan);

    @Delete
    void deleteOutputPlan(OutputPlanEntity outputPlan);

    @Query("DELETE FROM output_plans")
    void deleteAllOutputPlans();
}
```

#### HistoryDao
File: `app/src/main/java/com/mmjang/ankihelper/data/history/HistoryDao.java`

```java
package com.mmjang.ankihelper.data.history;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import java.util.List;

@Dao
public interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY timeStamp DESC")
    List<HistoryEntity> getAllHistory();

    @Query("SELECT * FROM history WHERE timeStamp = :timeStamp")
    HistoryEntity getHistoryByTimeStamp(long timeStamp);

    @Insert
    void insertHistory(HistoryEntity history);

    @Insert
    void insertHistories(List<HistoryEntity> histories);

    @Update
    void updateHistory(HistoryEntity history);

    @Delete
    void deleteHistory(HistoryEntity history);

    @Query("DELETE FROM history")
    void deleteAllHistory();
}
```

#### UserTagDao
File: `app/src/main/java/com/mmjang/ankihelper/data/model/UserTagDao.java`

```java
package com.mmjang.ankihelper.data.model;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import java.util.List;

@Dao
public interface UserTagDao {
    @Query("SELECT * FROM user_tags")
    List<UserTagEntity> getAllUserTags();

    @Query("SELECT * FROM user_tags WHERE tag = :tag")
    UserTagEntity getUserTagByTag(String tag);

    @Insert
    void insertUserTag(UserTagEntity userTag);

    @Insert
    void insertUserTags(List<UserTagEntity> userTags);

    @Update
    void updateUserTag(UserTagEntity userTag);

    @Delete
    void deleteUserTag(UserTagEntity userTag);

    @Query("DELETE FROM user_tags")
    void deleteAllUserTags();
}
```

### 5. Create Database Class

File: `app/src/main/java/com/mmjang/ankihelper/data/database/AppDatabase.java`

```java
package com.mmjang.ankihelper.data.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import com.mmjang.ankihelper.data.plan.OutputPlanEntity;
import com.mmjang.ankihelper.data.history.HistoryEntity;
import com.mmjang.ankihelper.data.model.UserTagEntity;
import com.mmjang.ankihelper.data.plan.OutputPlanDao;
import com.mmjang.ankihelper.data.history.HistoryDao;
import com.mmjang.ankihelper.data.model.UserTagDao;

@Database(
    entities = {OutputPlanEntity.class, HistoryEntity.class, UserTagEntity.class},
    version = 1,
    exportSchema = false
)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract OutputPlanDao outputPlanDao();
    public abstract HistoryDao historyDao();
    public abstract UserTagDao userTagDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        "ankihelper.db"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
```

### 6. Create Repository Classes (Optional but Recommended)

To maintain clean separation of concerns, create repository classes:

#### OutputPlanRepository
File: `app/src/main/java/com/mmjang/ankihelper/data/plan/OutputPlanRepository.java`

```java
package com.mmjang.ankihelper.data.plan;

import android.content.Context;
import com.mmjang.ankihelper.data.database.AppDatabase;
import java.util.List;

public class OutputPlanRepository {
    private OutputPlanDao outputPlanDao;
    private static OutputPlanRepository instance;

    private OutputPlanRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        outputPlanDao = db.outputPlanDao();
    }

    public static synchronized OutputPlanRepository getInstance(Context context) {
        if (instance == null) {
            instance = new OutputPlanRepository(context);
        }
        return instance;
    }

    public List<OutputPlanEntity> getAllOutputPlans() {
        return outputPlanDao.getAllOutputPlans();
    }

    public void insertOutputPlan(OutputPlanEntity outputPlan) {
        outputPlanDao.insertOutputPlan(outputPlan);
    }

    public void updateOutputPlan(OutputPlanEntity outputPlan) {
        outputPlanDao.updateOutputPlan(outputPlan);
    }

    public void deleteOutputPlan(OutputPlanEntity outputPlan) {
        outputPlanDao.deleteOutputPlan(outputPlan);
    }
}
```

## Step-by-Step Migration Guide

### Prerequisites

1. Familiarity with the existing codebase
2. Understanding of Room basics (Entities, DAOs, Database)
3. Android Studio with latest updates

### Phase 1: Setup Room Dependencies

#### Step 1: Add Room Dependencies
Edit `app/build.gradle`:

```gradle
dependencies {
    // Add these Room dependencies
    implementation "androidx.room:room-runtime:2.6.1"
    implementation "androidx.room:room-ktx:2.6.1"
    kapt "androidx.room:room-compiler:2.6.1"
    
    // For TypeConverter support with Maps
    implementation 'com.google.code.gson:gson:2.10.1'
}
```

#### Step 2: Sync Project
Click "Sync Now" when Android Studio prompts, or manually sync.

### Phase 2: Create Room Components

#### Step 3-10: Create Entities, Type Converters, DAOs, and Database
Follow the code examples provided in the previous sections to create:
1. OutputPlanEntity, HistoryEntity, UserTagEntity
2. Converters class
3. OutputPlanDao, HistoryDao, UserTagDao
4. AppDatabase class

### Phase 3: Update Application Initialization

#### Step 11: Update MyApplication
Update `MyApplication.java` to initialize Room:

```java
// Add this import at the top
import com.mmjang.ankihelper.data.database.AppDatabase;

// In the onCreate method, replace:
// LitePal.initialize(this);

// With:
AppDatabase.getDatabase(this);
```

### Phase 4: Test Room Implementation

#### Step 12: Create a Simple Test
Create a temporary test in any Activity to verify Room is working:

```java
// In any Activity's onCreate method (remove after testing)
AppDatabase db = AppDatabase.getDatabase(this);
OutputPlanEntity testPlan = new OutputPlanEntity();
testPlan.planName = "Test Plan";
testPlan.dictionaryKey = "test_dict";
testPlan.outputDeckId = 123;
testPlan.outputModelId = 456;
db.outputPlanDao().insertOutputPlan(testPlan);

List<OutputPlanEntity> plans = db.outputPlanDao().getAllOutputPlans();
Log.d("RoomTest", "Number of plans: " + plans.size());
```

Run the app and check that no errors occur and the log shows the correct count.

### Phase 5: Implement Data Migration

#### Step 13: Update MigrationUtil
Update `MigrationUtil.java` to migrate existing LitePal data to Room:

```java
// Add this import
import android.content.Context;
import com.mmjang.ankihelper.data.database.AppDatabase;

// Update the migrate method
public static void migrate(Context context){
    AppDatabase db = AppDatabase.getDatabase(context);
    
    // Migrate OutputPlan
    List<OutputPlan> oldPlans = LitePal.findAll(OutputPlan.class);
    for (OutputPlan oldPlan : oldPlans) {
        com.mmjang.ankihelper.data.plan.OutputPlanEntity newPlan = 
            new com.mmjang.ankihelper.data.plan.OutputPlanEntity();
        newPlan.planName = oldPlan.getPlanName();
        newPlan.dictionaryKey = oldPlan.getDictionaryKey();
        newPlan.outputDeckId = oldPlan.getOutputDeckId();
        newPlan.outputModelId = oldPlan.getOutputModelId();
        newPlan.fieldsMap = oldPlan.getFieldsMapString();
        db.outputPlanDao().insertOutputPlan(newPlan);
    }
    LitePal.deleteAll(OutputPlan.class);
    
    // Migrate History
    List<History> oldHistories = LitePal.findAll(History.class);
    for(History oldHistory : oldHistories){
        com.mmjang.ankihelper.data.history.HistoryEntity newHistory = 
            new com.mmjang.ankihelper.data.history.HistoryEntity();
        newHistory.timeStamp = oldHistory.getTimeStamp();
        newHistory.type = oldHistory.getType();
        newHistory.word = oldHistory.getWord();
        newHistory.sentence = oldHistory.getSentence();
        newHistory.dictionary = oldHistory.getDictionary();
        newHistory.definition = oldHistory.getDefinition();
        newHistory.translation = oldHistory.getTranslation();
        newHistory.note = oldHistory.getNote();
        newHistory.tag = oldHistory.getTag();
        db.historyDao().insertHistory(newHistory);
    }
    LitePal.deleteAll(History.class);
    
    // Migrate UserTag
    List<UserTag> oldTags = LitePal.findAll(UserTag.class);
    for(UserTag oldTag : oldTags){
        com.mmjang.ankihelper.data.model.UserTagEntity newTag = 
            new com.mmjang.ankihelper.data.model.UserTagEntity();
        newTag.tag = oldTag.getTag();
        db.userTagDao().insertUserTag(newTag);
    }
    LitePal.deleteAll(UserTag.class);
}
```

### Phase 6: Update DatabaseManager

#### Step 14: Update DatabaseManager to Use Room
Update `DatabaseManager.java` to replace direct SQLite operations with Room:

```java
// Add this import
import com.mmjang.ankihelper.data.database.AppDatabase;

// In methods that previously used direct SQLite, replace with Room operations
// For example, replace:
// mDatabase.insert(...)

// With:
// AppDatabase db = AppDatabase.getDatabase(mContext);
// db.outputPlanDao().insertOutputPlan(...);
```

### Phase 7: Update Data Access Code

#### Step 15: Replace LitePal Calls
Throughout the application, replace all LitePal calls with Room equivalents:

**Before (LitePal):**
```java
List<OutputPlan> plans = LitePal.findAll(OutputPlan.class);
OutputPlan plan = new OutputPlan();
plan.setPlanName("Test");
plan.save();
```

**After (Room):**
```java
AppDatabase db = AppDatabase.getDatabase(context);
List<OutputPlanEntity> plans = db.outputPlanDao().getAllOutputPlans();
OutputPlanEntity plan = new OutputPlanEntity();
plan.planName = "Test";
db.outputPlanDao().insertOutputPlan(plan);
```

### Phase 8: Clean Up

#### Step 16: Remove LitePal Dependencies
1. Remove from `app/build.gradle`:
```gradle
implementation 'org.litepal.guolindev:core:3.2.3'
```

2. Delete `assets/litepal.xml` file

3. Remove all LitePal imports and code references throughout the application

#### Step 17: Clean Up Incomplete Implementations
1. Remove or complete incomplete Room files in `data/quote/`
2. Verify all Room implementations are consistent

#### Step 18: Final Testing
1. Test data migration from old LitePal database
2. Test all functionality that depends on data persistence
3. Verify no crashes or data loss

## Data Migration Strategy

1. Check if existing LitePal data exists
2. If data exists, migrate it to Room database
3. Delete old LitePal data after successful migration
4. Update all data access code to use Room instead of LitePal and direct SQLite

## Files to be Modified/Added

1. `app/build.gradle` - Add Room dependencies
2. New entity classes for OutputPlan, History, and UserTag
3. New DAO interfaces for each entity
4. New AppDatabase class
5. New TypeConverters class for Map serialization
6. Update MyApplication.java to initialize Room
7. Update MigrationUtil.java to migrate existing data
8. Update DatabaseManager.java to use Room instead of direct SQLite
9. Update all classes that currently use LitePal
10. Remove LitePal dependency and litepal.xml configuration

## Testing Strategy

1. **Unit Tests**: Create unit tests for all DAOs to ensure proper data access
2. **Integration Tests**: Test data migration from LitePal to Room
3. **UI Tests**: Verify that all UI components that depend on data work correctly
4. **Performance Tests**: Ensure Room queries perform as expected

## Common Issues and Solutions

1. **Compilation Errors**: Make sure all Room annotations are correctly imported
2. **Runtime Errors**: Check that all entities have default constructors
3. **Data Not Persisting**: Verify Room is properly initialized in Application class
4. **Migration Failures**: Ensure LitePal data is correctly mapped to Room entities

## Tips for Success

1. **Work incrementally**: Migrate one entity at a time
2. **Test frequently**: Verify each step before moving to the next
3. **Backup data**: Always backup user data before migration
4. **Handle errors gracefully**: Implement proper error handling for migration failures
5. **Update documentation**: Keep documentation updated as you make changes

## Benefits of Migration

1. **Compile-time verification** of SQL queries
2. **Better integration** with Android Architecture Components
3. **Improved performance** with optimized query execution
4. **Reduced boilerplate** code with annotation processing
5. **Better threading** model with built-in background thread support
6. **Consistent approach** across the entire application

## Rollback Plan

If issues are encountered during migration:
1. Revert to the previous version with LitePal
2. Preserve the Room implementation in a separate branch
3. Fix identified issues
4. Attempt migration again after fixes

## Timeline

Estimated migration time: 5 weeks
- Week 1: Setup Room, create entities and DAOs
- Week 2: Create database class, type converters, and repository classes
- Week 3: Implement data migration and update DatabaseManager
- Week 4: Update data access code and clean up
- Week 5: Testing, optimization, and final verification

## Conclusion

Migrating from LitePal and direct SQLite to Room will modernize the application's data persistence layer, improve performance, and align with current Android development best practices. The existing use of Room for dictionary data provides a proven pattern to follow for this migration. This will also unify the data persistence approach across the entire application.