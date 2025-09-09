# AnkiQuick External Storage Migration Specification

## Executive Summary

This specification outlines the complete migration of AnkiQuick app data from internal storage to external storage using `MANAGE_EXTERNAL_STORAGE` permission. All app data including databases, media files, user settings, EPUB files, and cache will be stored in external storage to ensure data persistence and user control over app data location.

## Current State Analysis

### Current Storage Usage
- **Database**: Stored in internal app storage (`getFilesDir()/ankihelper/databases/`)
- **Media Files**: Stored in AnkiDroid directory (`Environment.getDataDirectory()/AnkiDroid/collection.media/`)
- **EPUB Files**: Stored in external storage (`Environment.getExternalStorageDirectory()/folioreader/`)
- **Settings**: Stored in internal SharedPreferences
- **Cache**: Stored in internal cache directory
- **Images**: Stored in AnkiDroid media directory

### Current Permissions
- No external storage permissions declared
- Using scoped storage access where available
- Some components already use external storage for EPUB files

## Target Architecture

### Storage Structure
```
/Android/data/com.mmjang.ankihelper/
├── files/
│   ├── databases/           # App databases
│   ├── shared_prefs/        # SharedPreferences
│   ├── ankihelper_image/    # Image files
│   ├── ankihelper_audio/    # Audio files
│   └── content/            # User content
├── cache/
│   ├── images/             # Image cache
│   ├── audio/              # Audio cache
│   └── webview/            # WebView cache
└── folioreader/            # EPUB files
    └── [book_folders]/
```

### Permission Strategy
- **Primary**: `MANAGE_EXTERNAL_STORAGE` for full external storage access
- **Fallback**: Graceful degradation to internal storage if permission denied
- **Request Timing**: On first launch and when external storage features are accessed

## Implementation Plan

### Phase 1: Permission & Storage Infrastructure

#### 1.1 Update AndroidManifest.xml
```xml
<!-- Add before application tag -->
<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE"
                     android:maxSdkVersion="32" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
                     android:maxSdkVersion="28" />

<!-- Inside application tag -->
<application
    android:requestLegacyExternalStorage="true"
    android:preserveLegacyExternalStorage="true"
    android:hasFragileUserData="true">
    
    <!-- Add for API 30+ -->
    <meta-data
        android:name="android.max_aspect"
        android:value="2.1" />
        
    <!-- Declare external storage access -->
    <meta-data
        android:name="android.content.APP_RESTRICTIONS"
        android:resource="@xml/app_restrictions" />
</application>
```

#### 1.2 Create Storage Manager
```kotlin
class StorageManager @Inject constructor(
    private val context: Context,
    private val preferences: SharedPreferences
) {
    companion object {
        private const val TAG = "StorageManager"
        private const val PREF_STORAGE_MODE = "storage_mode"
        private const val PREF_EXTERNAL_STORAGE_GRANTED = "external_storage_granted"
        private const val PREF_MIGRATION_COMPLETED = "migration_completed"
        
        const val STORAGE_MODE_EXTERNAL = "external"
        const val STORAGE_MODE_INTERNAL = "internal"
        const val STORAGE_MODE_MIXED = "mixed"
    }
    
    private val storageMode: String
        get() = preferences.getString(PREF_STORAGE_MODE, STORAGE_MODE_EXTERNAL) ?: STORAGE_MODE_EXTERNAL
    
    val isExternalStorageAvailable: Boolean
        get() = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    
    val isExternalStorageGranted: Boolean
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    
    val shouldUseExternalStorage: Boolean
        get() = isExternalStorageAvailable && isExternalStorageGranted
    
    fun getStorageDir(): File {
        return if (shouldUseExternalStorage) {
            context.getExternalFilesDir(null) ?: context.filesDir
        } else {
            context.filesDir
        }
    }
    
    fun getCacheDir(): File {
        return if (shouldUseExternalStorage) {
            context.externalCacheDir ?: context.cacheDir
        } else {
            context.cacheDir
        }
    }
    
    fun getDatabasePath(name: String): File {
        val storageDir = getStorageDir()
        val dbDir = File(storageDir, "databases")
        if (!dbDir.exists()) {
            dbDir.mkdirs()
        }
        return File(dbDir, "$name.db")
    }
    
    fun getSharedPreferences(name: String, mode: Int): SharedPreferences {
        return if (shouldUseExternalStorage) {
            val storageDir = getStorageDir()
            val prefsDir = File(storageDir, "shared_prefs")
            if (!prefsDir.exists()) {
                prefsDir.mkdirs()
            }
            context.getSharedPreferences(prefsDir.absolutePath + File.separator + name, mode)
        } else {
            context.getSharedPreferences(name, mode)
        }
    }
    
    suspend fun requestExternalStoragePermission(activity: Activity): Boolean {
        return withContext(Dispatchers.Main) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    activity.startActivityForResult(intent, REQUEST_CODE_MANAGE_EXTERNAL_STORAGE)
                    true
                } catch (e: Exception) {
                    // Fallback for devices that don't support MANAGE_EXTERNAL_STORAGE
                    requestLegacyPermissions(activity)
                }
            } else {
                requestLegacyPermissions(activity)
            }
        }
    }
    
    private fun requestLegacyPermissions(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                REQUEST_CODE_EXTERNAL_STORAGE
            )
            return true
        }
        return false
    }
    
    fun migrateToExternalStorage(): Boolean {
        if (!shouldUseExternalStorage) return false
        
        return try {
            // Migrate databases
            migrateDatabases()
            
            // Migrate SharedPreferences
            migrateSharedPreferences()
            
            // Migrate media files
            migrateMediaFiles()
            
            // Migrate cache
            migrateCache()
            
            // Mark migration as completed
            preferences.edit()
                .putBoolean(PREF_MIGRATION_COMPLETED, true)
                .putString(PREF_STORAGE_MODE, STORAGE_MODE_EXTERNAL)
                .apply()
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Migration failed", e)
            false
        }
    }
    
    private fun migrateDatabases() {
        val internalDbDir = File(context.filesDir, "ankihelper/databases")
        val externalDbDir = File(getStorageDir(), "databases")
        
        if (internalDbDir.exists()) {
            internalDbDir.listFiles()?.forEach { file ->
                val targetFile = File(externalDbDir, file.name)
                file.copyTo(targetFile, overwrite = true)
                file.delete()
            }
            internalDbDir.delete()
        }
    }
    
    private fun migrateSharedPreferences() {
        val sharedPrefsFiles = context.filesDir.listFiles { _, name ->
            name.endsWith(".xml") && name.contains("shared_prefs")
        }
        
        sharedPrefsFiles?.forEach { file ->
            val prefsName = file.name.replace(".xml", "")
            val sourcePrefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
            val targetPrefs = getSharedPreferences(prefsName, Context.MODE_PRIVATE)
            
            // Copy all preferences
            sourcePrefs.all.forEach { (key, value) ->
                when (value) {
                    is String -> targetPrefs.edit().putString(key, value).apply()
                    is Int -> targetPrefs.edit().putInt(key, value).apply()
                    is Boolean -> targetPrefs.edit().putBoolean(key, value).apply()
                    is Long -> targetPrefs.edit().putLong(key, value).apply()
                    is Float -> targetPrefs.edit().putFloat(key, value).apply()
                    is Set<*> -> targetPrefs.edit().putStringSet(key, value as Set<String>).apply()
                }
            }
            
            // Clear old preferences
            sourcePrefs.edit().clear().apply()
            file.delete()
        }
    }
    
    private fun migrateMediaFiles() {
        // Migrate AnkiDroid media files
        val ankiMediaDir = File(Environment.getDataDirectory(), "AnkiDroid/collection.media")
        val externalImageDir = File(getStorageDir(), "ankihelper_image")
        val externalAudioDir = File(getStorageDir(), "ankihelper_audio")
        
        if (ankiMediaDir.exists()) {
            // Migrate image files
            val imageDir = File(ankiMediaDir, "ankihelper_image")
            if (imageDir.exists()) {
                imageDir.listFiles()?.forEach { file ->
                    val targetFile = File(externalImageDir, file.name)
                    file.copyTo(targetFile, overwrite = true)
                }
            }
            
            // Migrate audio files
            val audioDir = File(ankiMediaDir, "ankihelper_audio")
            if (audioDir.exists()) {
                audioDir.listFiles()?.forEach { file ->
                    val targetFile = File(externalAudioDir, file.name)
                    file.copyTo(targetFile, overwrite = true)
                }
            }
        }
    }
    
    private fun migrateCache() {
        val internalCacheDir = context.cacheDir
        val externalCacheDir = getCacheDir()
        
        if (internalCacheDir.exists()) {
            internalCacheDir.listFiles()?.forEach { file ->
                val targetFile = File(externalCacheDir, file.name)
                file.copyTo(targetFile, overwrite = true)
                file.delete()
            }
        }
    }
    
    companion object {
        const val REQUEST_CODE_EXTERNAL_STORAGE = 1001
        const val REQUEST_CODE_MANAGE_EXTERNAL_STORAGE = 1002
    }
}
```

### Phase 2: Database Context Update

#### 2.1 Update DatabaseContext
```kotlin
class DatabaseContext @Inject constructor(
    private val context: Context,
    private val storageManager: StorageManager
) : ContextWrapper(context) {

    override fun getDatabasePath(name: String): File {
        return storageManager.getDatabasePath(name)
    }

    override fun openOrCreateDatabase(
        name: String,
        mode: Int,
        factory: SQLiteDatabase.CursorFactory?,
        errorHandler: DatabaseErrorHandler?
    ): SQLiteDatabase {
        return SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), factory, errorHandler)
    }

    override fun openOrCreateDatabase(
        name: String,
        mode: Int,
        factory: SQLiteDatabase.CursorFactory?
    ): SQLiteDatabase {
        return SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), factory)
    }

    override fun getSharedPreferences(name: String, mode: Int): SharedPreferences {
        return storageManager.getSharedPreferences(name, mode)
    }

    override fun getDatabasePath(name: String): File {
        return storageManager.getDatabasePath(name)
    }

    override fun openOrCreateDatabase(
        name: String,
        mode: Int,
        factory: SQLiteDatabase.CursorFactory?,
        errorHandler: DatabaseErrorHandler?
    ): SQLiteDatabase {
        return SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), factory, errorHandler)
    }
}
```

### Phase 3: Utility Classes Update

#### 3.1 Update FileUtils
```kotlin
object FileUtils {
    private const val TAG = "FileUtils"
    
    @JvmStatic
    fun getExternalStorageDirectory(context: Context): File {
        val storageManager = context.applicationContext
            .getSystemService(Context.STORAGE_SERVICE) as StorageManager
        return storageManager.getStorageDir()
    }
    
    @JvmStatic
    fun getExternalCacheDirectory(context: Context): File {
        val storageManager = context.applicationContext
            .getSystemService(Context.STORAGE_SERVICE) as StorageManager
        return storageManager.getCacheDir()
    }
    
    @JvmStatic
    fun EnsureAnkiImageDirectory(context: Context): Boolean {
        return try {
            val storageManager = context.applicationContext
                .getSystemService(Context.STORAGE_SERVICE) as StorageManager
            val imageDir = File(storageManager.getStorageDir(), "ankihelper_image")
            if (!imageDir.exists()) {
                imageDir.mkdirs()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create Anki image directory", e)
            false
        }
    }
    
    @JvmStatic
    fun getAnkiImageDirectory(context: Context): File {
        val storageManager = context.applicationContext
            .getSystemService(Context.STORAGE_SERVICE) as StorageManager
        return File(storageManager.getStorageDir(), "ankihelper_image")
    }
    
    @JvmStatic
    fun getAnkiAudioDirectory(context: Context): File {
        val storageManager = context.applicationContext
            .getSystemService(Context.STORAGE_SERVICE) as StorageManager
        return File(storageManager.getStorageDir(), "ankihelper_audio")
    }
    
    @JvmStatic
    fun getContentDirectory(context: Context): File {
        val storageManager = context.applicationContext
            .getSystemService(Context.STORAGE_SERVICE) as StorageManager
        return File(storageManager.getStorageDir(), "content")
    }
}
```

#### 3.2 Update Constant Class
```kotlin
object Constant {
    // ... existing constants ...
    
    @JvmStatic
    fun getStorageDirectory(context: Context): String {
        val storageManager = context.applicationContext
            .getSystemService(Context.STORAGE_SERVICE) as StorageManager
        return storageManager.getStorageDir().absolutePath
    }
    
    @JvmStatic
    fun getImageMediaDirectory(context: Context): String {
        val storageManager = context.applicationContext
            .getSystemService(Context.STORAGE_SERVICE) as StorageManager
        return File(storageManager.getStorageDir(), "ankihelper_image").absolutePath
    }
    
    @JvmStatic
    fun getAudioMediaDirectory(context: Context): String {
        val storageManager = context.applicationContext
            .getSystemService(Context.STORAGE_SERVICE) as StorageManager
        return File(storageManager.getStorageDir(), "ankihelper_audio").absolutePath
    }
    
    @JvmStatic
    fun getFolioReaderDirectory(context: Context): String {
        val storageManager = context.applicationContext
            .getSystemService(Context.STORAGE_SERVICE) as StorageManager
        return File(storageManager.getStorageDir(), "folioreader").absolutePath
    }
}
```

### Phase 4: Application Integration

#### 4.1 Update Application Class
```kotlin
@HiltAndroidApp
class AnkiApplication : Application() {
    @Inject
    lateinit var storageManager: StorageManager
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize storage manager
        initializeStorage()
    }
    
    private fun initializeStorage() {
        // Check if migration is needed
        val migrationCompleted = getSharedPreferences("storage_prefs", MODE_PRIVATE)
            .getBoolean("migration_completed", false)
        
        if (!migrationCompleted && storageManager.isExternalStorageAvailable) {
            // Request permission and migrate data
            CoroutineScope(Dispatchers.IO).launch {
                if (storageManager.migrateToExternalStorage()) {
                    Log.i("AnkiApplication", "Successfully migrated to external storage")
                } else {
                    Log.w("AnkiApplication", "Failed to migrate to external storage")
                }
            }
        }
    }
}
```

#### 4.2 Update LauncherActivity
```kotlin
@AndroidEntryPoint
class LauncherActivity : AppCompatActivity() {
    @Inject
    lateinit var storageManager: StorageManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        checkStoragePermission()
    }
    
    private fun checkStoragePermission() {
        if (!storageManager.isExternalStorageGranted) {
            showStoragePermissionDialog()
        } else {
            proceedToMainApp()
        }
    }
    
    private fun showStoragePermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Storage Permission Required")
            .setMessage("This app requires access to external storage to store all app data. Without this permission, the app may not function properly.")
            .setPositiveButton("Grant Permission") { _, _ ->
                CoroutineScope(Dispatchers.Main).launch {
                    storageManager.requestExternalStoragePermission(this@LauncherActivity)
                }
            }
            .setNegativeButton("Use Internal Storage") { _, _ ->
                proceedToMainApp()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun proceedToMainApp() {
        // Continue with app initialization
        setContentView(R.layout.activity_launcher)
        // ... rest of the initialization
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == StorageManager.REQUEST_CODE_MANAGE_EXTERNAL_STORAGE) {
            if (storageManager.isExternalStorageGranted) {
                proceedToMainApp()
            } else {
                showStoragePermissionDialog()
            }
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == StorageManager.REQUEST_CODE_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                proceedToMainApp()
            } else {
                showStoragePermissionDialog()
            }
        }
    }
}
```

### Phase 5: FolioReader Integration

#### 5.1 Update FolioReader FileUtil
```kotlin
object FileUtil {
    private const val TAG = "FileUtil"
    private const val FOLIO_READER_ROOT = "folioreader"
    
    @JvmStatic
    fun getFolioEpubFolderPath(context: Context, epubFileName: String): String {
        val storageManager = context.applicationContext
            .getSystemService(Context.STORAGE_SERVICE) as StorageManager
        return File(storageManager.getStorageDir(), "$FOLIO_READER_ROOT/$epubFileName").absolutePath
    }
    
    @JvmStatic
    fun getFolioEpubFilePath(
        context: Context,
        sourceType: FolioActivity.EpubSourceType,
        epubFilePath: String,
        epubFileName: String
    ): String {
        return if (FolioActivity.EpubSourceType.SD_CARD.equals(sourceType)) {
            epubFilePath
        } else {
            "${getFolioEpubFolderPath(context, epubFileName)}/$epubFileName.epub"
        }
    }
    
    @JvmStatic
    fun isFolderAvailable(context: Context, epubFileName: String): Boolean {
        val file = File(getFolioEpubFolderPath(context, epubFileName))
        return file.isDirectory
    }
    
    @JvmStatic
    fun saveTempEpubFile(
        context: Context,
        filePath: String,
        fileName: String,
        inputStream: InputStream
    ): Boolean {
        return try {
            val file = File(filePath)
            if (!file.exists()) {
                val folder = File(getFolioEpubFolderPath(context, fileName))
                folder.mkdirs()
                
                FileOutputStream(file).use { outputStream ->
                    val buffer = ByteArray(1024)
                    var length: Int
                    while (inputStream.read(buffer).also { length = it } != -1) {
                        outputStream.write(buffer, 0, length)
                    }
                }
            }
            true
        } catch (e: IOException) {
            Log.e(TAG, "Failed to save EPUB file", e)
            false
        }
    }
}
```

### Phase 6: Testing & Validation

#### 6.1 Unit Tests
```kotlin
@ExperimentalCoroutinesTest
class StorageManagerTest {
    private lateinit var storageManager: StorageManager
    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences
    
    @Before
    fun setUp() {
        context = mockk()
        sharedPreferences = mockk()
        storageManager = StorageManager(context, sharedPreferences)
    }
    
    @Test
    fun `getStorageDir returns external storage when available and granted`() {
        // Given
        every { Environment.getExternalStorageState() } returns Environment.MEDIA_MOUNTED
        every { Environment.isExternalStorageManager() } returns true
        every { context.getExternalFilesDir(null) } returns File("/storage/emulated/0/Android/data/com.mmjang.ankihelper/files")
        
        // When
        val result = storageManager.getStorageDir()
        
        // Then
        assertEquals("/storage/emulated/0/Android/data/com.mmjang.ankihelper/files", result.absolutePath)
    }
    
    @Test
    fun `getStorageDir returns internal storage when external not available`() {
        // Given
        every { Environment.getExternalStorageState() } returns Environment.MEDIA_UNMOUNTED
        every { context.filesDir } returns File("/data/user/0/com.mmjang.ankihelper/files")
        
        // When
        val result = storageManager.getStorageDir()
        
        // Then
        assertEquals("/data/user/0/com.mmjang.ankihelper/files", result.absolutePath)
    }
}
```

#### 6.2 Integration Tests
```kotlin
@RunWith(AndroidJUnit4::class)
class StorageMigrationTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(LauncherActivity::class.java)
    
    @Test
    fun testStorageMigration() {
        activityRule.scenario.onActivity { activity ->
            val storageManager = activity.getSystemService(Context.STORAGE_SERVICE) as StorageManager
            
            // Test migration
            val migrationResult = storageManager.migrateToExternalStorage()
            assertTrue(migrationResult)
            
            // Verify files are moved
            val externalDbDir = File(storageManager.getStorageDir(), "databases")
            assertTrue(externalDbDir.exists())
            
            val externalPrefsDir = File(storageManager.getStorageDir(), "shared_prefs")
            assertTrue(externalPrefsDir.exists())
        }
    }
}
```

## Risk Assessment

### High Risk
- **Data Loss**: Migration process could fail and cause data loss
- **Permission Issues**: Users may deny storage permission
- **Device Compatibility**: Some devices may not support external storage properly
- **Performance**: External storage may be slower than internal storage

### Mitigation Strategies
- **Backup Before Migration**: Create backup of all data before migration
- **Graceful Degradation**: Fall back to internal storage if external storage fails
- **User Choice**: Allow users to choose storage location
- **Progress Monitoring**: Show migration progress to users
- **Rollback Capability**: Ability to rollback if migration fails

## Success Criteria

### Technical Metrics
- [ ] All app data stored in external storage when permission granted
- [ ] Graceful fallback to internal storage when permission denied
- [ ] Zero data loss during migration
- [ ] All existing functionality preserved
- [ ] Performance impact < 10% compared to internal storage

### User Experience Metrics
- [ ] Clear permission request dialog
- [ ] Migration progress indicator
- [ ] Option to use internal storage if preferred
- [ ] No data loss during migration
- [ ] All features work with external storage

### Code Quality Metrics
- [ ] 100% test coverage for storage-related code
- [ ] Proper error handling and logging
- [ ] Clean separation of storage logic
- [ ] Comprehensive documentation
- [ ] No hardcoded paths

## Implementation Timeline

### Week 1: Infrastructure Setup
- [ ] Update AndroidManifest.xml with required permissions
- [ ] Create StorageManager class
- [ ] Set up dependency injection for storage management
- [ ] Write unit tests for storage manager

### Week 2: Core Components Update
- [ ] Update DatabaseContext
- [ ] Update FileUtils and Constant classes
- [ ] Update Application class
- [ ] Write integration tests

### Week 3: UI & Permission Handling
- [ ] Update LauncherActivity with permission handling
- [ ] Create permission request dialogs
- [ ] Add migration progress UI
- [ ] Test permission handling flow

### Week 4: FolioReader Integration
- [ ] Update FolioReader FileUtil
- [ ] Test EPUB file handling with external storage
- [ ] Verify all media operations work with external storage
- [ ] Performance testing

### Week 5: Testing & Polish
- [ ] Comprehensive testing across different Android versions
- [ ] Test on different storage types (internal SD, external SD)
- [ ] Performance optimization
- [ ] Documentation and cleanup

## Next Steps

1. **Review Specification**: Validate this specification meets all requirements
2. **Resource Allocation**: Assign team members to each phase
3. **Set Up Testing**: Configure test devices with different storage configurations
4. **Backup Strategy**: Implement backup mechanism before migration
5. **User Communication**: Prepare in-app messaging about storage changes

---

**Document Version**: 1.0
**Last Updated**: 2025
**Status**: Ready for Implementation
**Key Features**: Complete external storage migration with graceful fallback