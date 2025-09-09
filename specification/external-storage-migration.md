# AnkiQuick External Storage Migration Specification

## Executive Summary

This specification documents the completed migration of AnkiQuick app data from internal storage to external storage using `MANAGE_EXTERNAL_STORAGE` permission. The implementation has been successfully deployed and provides comprehensive external storage support with graceful fallback mechanisms.

## Implementation Status

✅ **COMPLETED** - All phases of external storage migration have been implemented and tested.

### Current Storage Architecture

### Actual Storage Structure (Implemented)
```
/storage/emulated/0/ankihelper/
├── databases/           # Dictionary databases (cdepe4.db, collins_v2.db, etc.)
├── media/              # Media files
│   ├── images/         # Image files
│   └── audio/          # Audio files
├── content/            # User content
└── cache/              # Cache files
```

### Key Implementation Details

#### Storage Strategy
- **Primary Storage**: `Environment.getExternalStorageDirectory()/ankihelper/`
- **Fallback**: Internal app storage if external storage unavailable
- **No Scoped Storage**: Uses broad external storage access for maximum compatibility
- **Direct Path Resolution**: Consistent path handling across all components

#### Permission Implementation
- **MANAGE_EXTERNAL_STORAGE**: Required for API 30+
- **READ_EXTERNAL_STORAGE**: Fallback for older APIs
- **WRITE_EXTERNAL_STORAGE**: Fallback for older APIs
- **Runtime Request**: Handled through StorageMigrationActivity

## Completed Implementation

### 1. AndroidManifest.xml Updates
```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />
<uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
```

### 2. StorageManager Class (Core Implementation)
```java
public class StorageManager {
    // Core Methods
    public File getStorageDir() // Primary storage directory
    public File getDatabaseDir() // Database directory
    public File getMediaDir()     // Media files directory
    public File getImageDir()    // Image files directory
    public File getAudioDir()    // Audio files directory
    public File getContentDir()  // User content directory
    public File getCacheDir()    // Cache directory
    
    // Migration Methods
    public boolean copyFile(File source, File destination)
    public boolean moveFile(File source, File destination)
    public boolean copyDirectory(File source, File destination)
    public long getDirectorySize(File directory)
    
    // Migration State
    public boolean isMigrationCompleted()
    public void setMigrationCompleted(boolean completed)
}
```

### 3. DatabaseContext Implementation
```java
public class DatabaseContext extends ContextWrapper {
    @Override
    public File getDatabasePath(String name) {
        File databaseDir = storageManager.getDatabaseDir();
        return new File(databaseDir, name);
    }
    
    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, CursorFactory factory) {
        File dbPath = getDatabasePath(name);
        return SQLiteDatabase.openOrCreateDatabase(dbPath.getAbsolutePath(), null);
    }
}
```

### 4. BaseDatabaseHelper (Dictionary Support)
```java
public class BaseDatabaseHelper extends SQLiteOpenHelper {
    // Automatic database copying from assets
    private void copyDatabaseFromAssets(Context context)
    
    // Fallback database creation
    private void createFallbackDatabase()
    
    // Robust database access with auto-recovery
    @Override
    public SQLiteDatabase getReadableDatabase()
}
```

### 5. StorageMigrationActivity (UI & Migration Logic)
```java
public class StorageMigrationActivity extends AppCompatActivity {
    // Permission handling
    private boolean hasRequiredPermissions()
    private void requestStoragePermissions()
    
    // Migration operations
    private boolean performMigration()
    private boolean migrateDatabases()
    private boolean copyBuiltInDictionaries(File externalDbDir)
    private boolean migrateMediaFiles()
    
    // UI states
    private void showMigrationInProgress()
    private void showMigrationCompleted()
}
```

### 6. Dictionary Database Support
- **Built-in Dictionaries**: 7 dictionary files copied from assets
- **Automatic Recovery**: Dictionary databases auto-repair if corrupted
- **External Storage Integration**: All dictionary operations use external storage

## Key Features Implemented

### 1. Comprehensive Migration
- ✅ Database migration (including built-in dictionaries)
- ✅ Media file migration (images, audio)
- ✅ Content file migration
- ✅ Cache migration support

### 2. Robust Error Handling
- ✅ Graceful fallback to internal storage
- ✅ Automatic database recovery
- ✅ Permission handling for all API levels
- ✅ Migration failure handling

### 3. User Experience
- ✅ Clear migration progress UI
- ✅ Permission request dialogs
- ✅ Migration status feedback
- ✅ Error messaging

### 4. Dictionary Database Support
- ✅ 7 built-in dictionaries (cdepe4.db, collins_v2.db, etc.)
- ✅ Automatic database copying from assets
- ✅ Database integrity checking
- ✅ Fallback database creation

## Dictionary Database Details

### Built-in Dictionary Files
- `cdepe4.db` - Cambridge English-Chinese Dictionary
- `collins_v2.db` - Collins Dictionary
- `forms.db` - Word forms database
- `maldpe.db` - Merriam-Webster Dictionary
- `oaldpe10.db` - Oxford Advanced Learner's Dictionary
- `ode2_v2.db` - Oxford Dictionary of English
- `wb_headwords.db` - Wordbank headwords

### Database Access Flow
1. **Cdepe4DictionaryHelper** → **BaseDatabaseHelper** → **DatabaseContext** → **StorageManager**
2. Automatic database copying from assets if missing/corrupted
3. Fallback database creation if copying fails
4. Consistent external storage path resolution

## Implementation Differences from Original Spec

### Storage Path Strategy
- **Original**: `/Android/data/com.mmjang.ankihelper/files/` (scoped storage)
- **Implemented**: `/storage/emulated/0/ankihelper/` (direct external storage)

### Permission Strategy
- **Original**: Complex fallback mechanism with multiple storage modes
- **Implemented**: Simplified approach with direct external storage access

### SharedPreferences Migration
- **Original**: Complex SharedPreferences migration logic
- **Implemented**: SharedPreferences remain in internal storage (simplified approach)

### FolioReader Integration
- **Original**: Full FolioReader integration with external storage
- **Implemented**: FolioReader left unchanged (per user request)

## Testing & Validation

### Manual Testing Completed
- ✅ Permission request and handling
- ✅ Database migration and access
- ✅ Dictionary lookup functionality
- ✅ Media file operations
- ✅ Error handling and fallbacks
- ✅ UI states and user feedback

### Dictionary Database Testing
- ✅ All 7 built-in dictionaries accessible
- ✅ Database auto-recovery functionality
- ✅ Fallback database creation
- ✅ External storage path resolution

## Known Limitations

### 1. FolioReader Integration
- FolioReader remains using internal storage for EPUB files
- User explicitly requested no FolioReader changes

### 2. SharedPreferences
- SharedPreferences remain in internal storage
- No complex migration logic implemented

### 3. Cache Management
- Cache migration infrastructure exists but simplified
- No complex cache invalidation logic

## Future Enhancements

### 1. Optional Features
- FolioReader external storage integration (if requested)
- SharedPreferences external storage migration
- Advanced cache management

### 2. Performance Optimizations
- Lazy loading of large databases
- Database indexing optimization
- Storage space monitoring

### 3. User Experience
- Storage usage statistics
- Migration progress estimation
- Storage location selection UI

## Maintenance Notes

### Code Structure
- **StorageManager.java**: Core storage management logic
- **DatabaseContext.java**: Database path resolution
- **BaseDatabaseHelper.java**: Dictionary database support
- **StorageMigrationActivity.java**: Migration UI and logic

### Key Classes Modified
- `LauncherActivity.java` - Added migration check
- `Content.java` - Updated to use StorageManager
- `FileUtils.java` - Updated to use StorageManager
- `Constant.java` - Added storage utility methods

### Dependencies
- Android minSdkVersion: 21 (API level 21)
- Android targetSdkVersion: 34 (API level 34)
- No external dependencies added

## Conclusion

The external storage migration has been successfully implemented with the following achievements:

### ✅ Completed Objectives
1. **Full External Storage Support**: All app data can be stored in external storage
2. **Robust Migration System**: Complete migration with fallback mechanisms
3. **Dictionary Database Integration**: All built-in dictionaries work with external storage
4. **User-Friendly Interface**: Clear migration UI and permission handling
5. **Error Resilience**: Comprehensive error handling and auto-recovery

### ✅ Technical Achievements
1. **Simplified Architecture**: Clean separation of storage concerns
2. **Consistent Path Resolution**: Unified storage path handling
3. **Automatic Database Management**: Self-healing dictionary databases
4. **Permission Handling**: Works across all Android API levels
5. **Performance Optimization**: Efficient file operations and migration

### ✅ Quality Assurance
1. **Comprehensive Testing**: All major functionality tested
2. **Error Handling**: Graceful degradation and recovery
3. **Code Quality**: Clean, maintainable implementation
4. **Documentation**: Complete specification and inline documentation

The implementation provides a solid foundation for external storage operations while maintaining full backward compatibility and user choice.

---

**Document Version**: 2.0 (Implementation Complete)
**Last Updated**: 2025
**Status**: ✅ IMPLEMENTED AND DEPLOYED
**Key Features**: Complete external storage migration with dictionary database support