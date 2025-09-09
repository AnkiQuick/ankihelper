# AnkiQuick Android App Modernization Specification

## Executive Summary

This specification outlines a comprehensive modernization plan for the AnkiQuick Android application to align with current Android development best practices, modern UI standards, and performance optimization. The upgrade focuses on updating deprecated APIs, implementing modern popup behavior, improving UI consistency, and enhancing overall code quality.

## Current State Analysis

### Architecture Overview
- **Codebase Structure**: Java-based with some Kotlin components
- **Target API**: 34 (Android 14) - **Needs update to 35 (Android 15)**
- **Min API**: 34 (Android 14) - **Should be reduced to 26 for broader compatibility**
- **Build System**: Gradle with Kotlin support
- **UI Framework**: Material Design 3 (partially implemented)
- **Target SDK**: Currently targeting Android 14, needs Android 15 support
- **Kotlin Version**: Using older Kotlin version, needs update to 2.1.0
- **AGP Version**: Using older Android Gradle Plugin, needs update to 8.7.2

### Key Issues Identified

#### 1. Outdated APIs and Dependencies
- **CardView**: Using deprecated `androidx.cardview:cardview:1.0.0`
- **RecyclerView**: Using older adapter patterns without modern optimizations
- **AsyncTask**: Legacy threading patterns in PopupActivity
- **Handler**: Memory leak potential with non-weak references
- **Material Components**: Mixed Material 2 and 3 implementations
- **Kotlin Version**: Not using latest Kotlin 2.1.0 with K2 compiler
- **Target SDK**: Not targeting Android 15 (API 35)
- **Min SDK**: Too high (API 34), should be API 26 for broader reach

#### 2. Missing Modern Android Features (2025 Trends)
- **Jetpack Compose**: Not prepared for UI modernization
- **Paging 3**: Not using efficient data loading
- **DataStore**: Still using SharedPreferences instead of DataStore
- **Room with Flow**: Not using reactive database operations
- **Hilt Navigation**: Not using modern navigation patterns
- **Biometric Authentication**: Missing modern security features
- **Material You**: Not supporting dynamic theming
- **Privacy Sandbox**: Not prepared for privacy changes
- **Performance Classes**: Not optimized for different device capabilities
- **Foldable Support**: Not optimized for foldable devices
- **K2 Compiler**: Not using latest Kotlin K2 compiler for better performance

#### 2. UI/UX Modernization Needs
- **Popup Window**: Currently blocks entire screen, needs modern overlay behavior
- **Theming**: Inconsistent Material 3 implementation
- **Layouts**: Using legacy layout patterns without modern constraints
- **Accessibility**: Missing proper accessibility attributes

#### 3. Performance Issues
- **Memory Leaks**: Handler references and static contexts
- **Thread Management**: Manual thread creation instead of coroutines
- **UI Blocking**: Synchronous operations on main thread
- **Resource Management**: Improper lifecycle handling

## Modernization Plan

### Phase 1: Core Infrastructure Updates

#### 1.1 Build Configuration Update
Update build.gradle files to use latest Android Gradle Plugin and Kotlin version:

```gradle
// project-level build.gradle
plugins {
    id 'com.android.application' version '8.7.2' apply false
    id 'com.android.library' version '8.7.2' apply false
    id 'org.jetbrains.kotlin.android' version '2.1.0' apply false
    id 'com.google.dagger.hilt.android' version '2.53.1' apply false
    id 'org.jetbrains.kotlin.kapt' version '2.1.0' apply false
}

// app-level build.gradle
android {
    namespace 'com.mmjang.ankihelper'
    compileSdk 35  // Updated to Android 15
    
    defaultConfig {
        applicationId "com.mmjang.ankihelper"
        minSdk 26      // Updated minimum SDK level
        targetSdk 35   // Updated to Android 15
        versionCode 102
        versionName "3.2.0"
        
        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
        
        // Enable vector drawables
        vectorDrawables.useSupportLibrary = true
        
        // Enable jetifier for compatibility
        android.jetifier=true
    }
    
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = '17'
        // Enable new Kotlin features
        freeCompilerArgs += [
            '-opt-in=androidx.compose.material3.ExperimentalMaterial3Api',
            '-opt-in=androidx.paging.ExperimentalPagingApi'
        ]
    }
    
    buildFeatures {
        viewBinding true
        dataBinding true
        // Enable Compose (for future migration)
        compose true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion '1.5.16'
    }
    
    packaging {
        resources {
            excludes += '/META-INF/{AL2.0,LGPL2.1}'
            excludes += '/META-INF/DEPENDENCIES'
            excludes += '/META-INF/LICENSE'
            excludes += '/META-INF/LICENSE.txt'
            excludes += '/META-INF/NOTICE'
            excludes += '/META-INF/NOTICE.txt'
        }
    }
}

dependencies {
    // Dependencies will be listed in 1.2
}
```

#### 1.2 Dependencies Update
```gradle
// Update to latest stable versions (as of 2025)
def core_version = "1.15.0"
def material_version = "1.13.0"
def lifecycle_version = "2.8.7"
def navigation_version = "2.8.4"
def room_version = "2.7.0"
def hilt_version = "2.53.1"
def coroutines_version = "1.9.0"
def compose_version = "1.7.5"
def activity_version = "1.9.3"

// Core AndroidX libraries
implementation "androidx.core:core-ktx:$core_version"
implementation "androidx.appcompat:appcompat:1.7.0"
implementation "com.google.android.material:material:$material_version"
implementation "androidx.recyclerview:recyclerview:1.3.2"
implementation "androidx.constraintlayout:constraintlayout:2.2.0"
implementation "androidx.fragment:fragment-ktx:1.8.5"

// Lifecycle components
implementation "androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_version"
implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version"
implementation "androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version"
implementation "androidx.lifecycle:lifecycle-service:$lifecycle_version"
implementation "androidx.lifecycle:lifecycle-runtime-compose:$lifecycle_version"

// Navigation
implementation "androidx.navigation:navigation-fragment-ktx:$navigation_version"
implementation "androidx.navigation:navigation-ui-ktx:$navigation_version"
implementation "androidx.navigation:navigation-dynamic-features-fragment:$navigation_version"

// Dependency Injection
implementation "com.google.dagger:hilt-android:$hilt_version"
kapt "com.google.dagger:hilt-compiler:$hilt_version"
implementation "androidx.hilt:hilt-navigation-compose:1.2.0"

// Coroutines
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines_version"
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version"
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-play-services:$coroutines_version"

// Database
implementation "androidx.room:room-runtime:$room_version"
implementation "androidx.room:room-ktx:$room_version"
implementation "androidx.room:room-paging:$room_version"
kapt "androidx.room:room-compiler:$room_version"

// Activity and Fragment
implementation "androidx.activity:activity-ktx:$activity_version"
implementation "androidx.activity:activity-compose:$activity_version"

// Modern UI Components (preparing for Compose migration)
implementation "androidx.compose.ui:ui:$compose_version"
implementation "androidx.compose.material3:material3-android:1.3.1"
implementation "androidx.compose.runtime:runtime:$compose_version"
implementation "androidx.compose.foundation:foundation:$compose_version"

// DataStore (Preferences replacement)
implementation "androidx.datastore:datastore-preferences:1.1.1"
implementation "androidx.datastore:datastore-preferences-core:1.1.1"

// Paging 3 for efficient data loading
implementation "androidx.paging:paging-runtime-ktx:3.3.2"
implementation "androidx.paging:paging-compose:3.3.2"

// WorkManager for background tasks
implementation "androidx.work:work-runtime-ktx:2.10.0"
implementation "androidx.work:work-runtime:2.10.0"

// Modern alternatives for deprecated components
implementation "androidx.cardview:cardview:1.0.0" // Keep for compatibility, phase out gradually
implementation "androidx.swiperefreshlayout:swiperefreshlayout:1.2.0"
implementation "androidx.viewpager2:viewpager2:1.1.0"

// Security and Crypto
implementation "androidx.security:security-crypto:1.1.0-alpha06"
implementation "androidx.biometric:biometric:1.4.0-alpha02"

// Testing dependencies (updated for 2025)
testImplementation "junit:junit:4.13.2"
testImplementation "org.mockito:mockito-core:5.14.2"
testImplementation "org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutines_version"
testImplementation "androidx.test:core:1.6.1"
testImplementation "androidx.test.ext:junit:1.2.1"
testImplementation "androidx.test.espresso:espresso-core:3.6.1"
testImplementation "androidx.test:runner:1.6.2"
testImplementation "androidx.test:rules:1.6.1"
testImplementation "androidx.arch.core:core-testing:2.2.0"

// Android test dependencies
androidTestImplementation "androidx.test.ext:junit:1.2.1"
androidTestImplementation "androidx.test.espresso:espresso-core:3.6.1"
androidTestImplementation "androidx.test:runner:1.6.2"
androidTestImplementation "androidx.test:rules:1.6.1"
androidTestImplementation "androidx.room:room-testing:$room_version"
androidTestImplementation "com.google.dagger:hilt-android-testing:$hilt_version"
kaptAndroidTest "com.google.dagger:hilt-compiler:$hilt_version"

// Leak detection
debugImplementation "com.squareup.leakcanary:leakcanary-android:2.14"
```

#### 1.2 Migration to Kotlin Coroutines
Replace all AsyncTask and manual Thread usage with Kotlin Coroutines:

```kotlin
// Replace AsyncTask pattern
private fun asyncSearch(word: String) = viewModelScope.launch {
    try {
        showProgressBar()
        val definitions = withContext(Dispatchers.IO) {
            currentDictionary.wordLookup(word)
        }
        processDefinitionList(definitions)
    } catch (e: Exception) {
        showError(e.message ?: "Unknown error")
    }
}
```

#### 1.3 Modern Lifecycle Management
Implement proper lifecycle-aware components:

#### 1.4 Modern Features Implementation (2025)
Add cutting-edge Android features for better user experience:

```kotlin
// 1. Material You Dynamic Theming
class ThemeManager {
    companion object {
        fun applyDynamicTheme(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.theme.applyStyle(
                    android.R.style.Theme_Material3_DynamicColors_DayNight,
                    true
                )
            }
        }
        
        fun isDynamicThemeAvailable(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        }
    }
}

// 2. Biometric Authentication for sensitive operations
class BiometricAuthManager {
    suspend fun authenticate(
        context: Context,
        title: String,
        subtitle: String = ""
    ): Boolean {
        return suspendCancellableCoroutine { continuation ->
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setNegativeButtonText("Cancel")
                .build()
                
            val biometricPrompt = BiometricPrompt(
                context as FragmentActivity,
                ContextCompat.getMainExecutor(context),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        continuation.resume(true)
                    }
                    
                    override fun onAuthenticationFailed() {
                        continuation.resume(false)
                    }
                    
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        continuation.resume(false)
                    }
                }
            )
            
            biometricPrompt.authenticate(promptInfo)
        }
    }
}

// 3. Privacy-Safe Data Collection
class PrivacyManager {
    companion object {
        fun isConsentGiven(context: Context): Boolean {
            val prefs = context.getSharedPreferences("privacy", Context.MODE_PRIVATE)
            return prefs.getBoolean("consent_given", false)
        }
        
        fun requestConsent(context: Context, callback: (Boolean) -> Unit) {
            // Show privacy consent dialog
            // This is a simplified example
            callback(true)
        }
        
        fun collectAnalytics(context: Context, eventName: String, data: Map<String, Any>) {
            if (isConsentGiven(context)) {
                // Collect analytics respecting privacy
                Log.d("Analytics", "$eventName: $data")
            }
        }
    }
}

// 4. Performance Class Optimization
class PerformanceOptimizer {
    companion object {
        fun getPerformanceClass(context: Context): Int {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService<PerformanceManager>()
                    ?.mediaPerformanceClass ?: 0
            } else {
                0
            }
        }
        
        fun adjustFeaturesBasedOnPerformance(context: Context) {
            val perfClass = getPerformanceClass(context)
            when {
                perfClass >= 33 -> {
                    // High performance - enable all features
                    enableHighPerformanceFeatures()
                }
                perfClass >= 31 -> {
                    // Medium performance - enable most features
                    enableMediumPerformanceFeatures()
                }
                else -> {
                    // Lower performance - basic features only
                    enableBasicFeatures()
                }
            }
        }
        
        private fun enableHighPerformanceFeatures() {
            // Enable animations, complex layouts, etc.
        }
        
        private fun enableMediumPerformanceFeatures() {
            // Enable moderate features
        }
        
        private fun enableBasicFeatures() {
            // Enable only essential features
        }
    }
}

// 5. Foldable Device Support
class FoldableSupport {
    companion object {
        fun isFoldable(context: Context): Boolean {
            return context.packageManager.hasSystemFeature(
                PackageManager.FEATURE_SENSOR_HINGE_ANGLE
            )
        }
        
        fun getDevicePosture(context: Context): Int {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val display = context.display ?: return DEVICE_POSTURE_UNKNOWN
                val windowMetrics = WindowMetricsCalculator.getOrCreate()
                    .computeCurrentWindowMetrics(display)
                
                // Determine posture based on window metrics
                return when {
                    windowMetrics.bounds.width() > windowMetrics.bounds.height() -> DEVICE_POSTURE_LANDSCAPE
                    else -> DEVICE_POSTURE_PORTRAIT
                }
            }
            return DEVICE_POSTURE_UNKNOWN
        }
    }
    
    companion object {
        const val DEVICE_POSTURE_UNKNOWN = 0
        const val DEVICE_POSTURE_PORTRAIT = 1
        const val DEVICE_POSTURE_LANDSCAPE = 2
        const val DEVICE_POSTURE_FLAT = 3
    }
}
```

#### 1.5 Modern Lifecycle Management
Implement proper lifecycle-aware components:

```kotlin
class PopupViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PopupUiState())
    val uiState: StateFlow<PopupUiState> = _uiState.asStateFlow()
    
    fun searchWord(word: String) {
        viewModelScope.launch {
            // Search logic
        }
    }
}
```

### Phase 2: UI/UX Modernization

#### 2.1 Modern Popup Window Implementation

**Current Issue**: Popup blocks entire screen and underlying content
**Solution**: Implement modern overlay popup with proper touch handling

```kotlin
class ModernPopupActivity : AppCompatActivity() {
    private lateinit var popupWindow: PopupWindow
    private lateinit var overlayView: View
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_container)
        
        setupOverlay()
        setupPopupWindow()
        handleIncomingIntent()
    }
    
    private fun setupOverlay() {
        overlayView = View(this).apply {
            setBackgroundColor(Color.parseColor("#80000000"))
            setOnClickListener { dismissPopup() }
        }
        
        // Add overlay to root view
        val rootView = findViewById<ViewGroup>(android.R.id.content)
        rootView.addView(overlayView)
    }
    
    private fun setupPopupWindow() {
        val popupView = LayoutInflater.from(this)
            .inflate(R.layout.modern_popup_layout, null)
        
        popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            isFocusable = true
            isOutsideTouchable = true
            elevation = 16f
            animationStyle = R.style.PopupAnimation
        }
        
        // Show popup centered on screen
        popupWindow.showAtLocation(
            findViewById(android.R.id.content),
            Gravity.CENTER,
            0,
            0
        )
    }
    
    private fun dismissPopup() {
        overlayView.visibility = View.GONE
        popupWindow.dismiss()
        finish()
    }
}
```

#### 2.2 Modern Popup Layout (modern_popup_layout.xml)
```xml
<androidx.cardview.widget.CardView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="16dp"
    app:cardCornerRadius="16dp"
    app:cardElevation="8dp">
    
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">
        
        <!-- Modern Search Interface -->
        <com.google.android.material.textfield.TextInputLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="@string/search_word"
            style="@style/Widget.Material3.TextInputLayout.OutlinedBox">
            
            <com.google.android.material.textfield.TextInputEditText
                android:id="@+id/searchEditText"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:imeOptions="actionSearch"
                android:inputType="text"
                android:maxLines="1"/>
        </com.google.android.material.textfield.TextInputLayout>
        
        <!-- Results Container -->
        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/resultsRecyclerView"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:maxHeight="400dp"/>
        
        <!-- Action Buttons -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:orientation="horizontal">
            
            <com.google.android.material.button.MaterialButton
                android:id="@+id/saveButton"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="@string/save"
                style="@style/Widget.Material3.Button"/>
            
            <com.google.android.material.button.MaterialButton
                android:id="@+id/cancelButton"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:layout_marginStart="8dp"
                android:text="@string/cancel"
                style="@style/Widget.Material3.Button.OutlinedButton"/>
        </LinearLayout>
    </LinearLayout>
</androidx.cardview.widget.CardView>
```

#### 2.3 Touch Handling for Modern Popup
```kotlin
class ModernPopupActivity : AppCompatActivity() {
    private fun setupTouchHandling() {
        // Allow touches outside popup to dismiss it
        popupWindow.setTouchInterceptor { view, event ->
            if (event.action == MotionEvent.ACTION_OUTSIDE) {
                dismissPopup()
                return@setTouchInterceptor true
            }
            false
        }
        
        // Allow scrolling of underlying content when not touching popup
        overlayView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Check if touch is outside popup area
                    val popupRect = Rect()
                    popupWindow.getDecorView().getGlobalVisibleRect(popupRect)
                    val touchPoint = Point(event.rawX.toInt(), event.rawY.toInt())
                    
                    if (!popupRect.contains(touchPoint.x, touchPoint.y)) {
                        // Allow underlying views to handle the touch
                        v.performClick()
                    }
                }
            }
            false
        }
    }
}
```

#### 2.4 Material 3 Theme Consolidation
Update themes.xml to use consistent Material 3:

```xml
<!-- Base Theme -->
<style name="Theme.AnkiHelper" parent="Theme.Material3.DayNight.NoActionBar">
    <!-- Primary Colors -->
    <item name="colorPrimary">@color/md_theme_light_primary</item>
    <item name="colorOnPrimary">@color/md_theme_light_onPrimary</item>
    <item name="colorPrimaryContainer">@color/md_theme_light_primaryContainer</item>
    <item name="colorOnPrimaryContainer">@color/md_theme_light_onPrimaryContainer</item>
    
    <!-- Secondary Colors -->
    <item name="colorSecondary">@color/md_theme_light_secondary</item>
    <item name="colorOnSecondary">@color/md_theme_light_onSecondary</item>
    <item name="colorSecondaryContainer">@color/md_theme_light_secondaryContainer</item>
    <item name="colorOnSecondaryContainer">@color/md_theme_light_onSecondaryContainer</item>
    
    <!-- Tertiary Colors -->
    <item name="colorTertiary">@color/md_theme_light_tertiary</item>
    <item name="colorOnTertiary">@color/md_theme_light_onTertiary</item>
    
    <!-- Background Colors -->
    <item name="android:colorBackground">@color/md_theme_light_background</item>
    <item name="colorOnBackground">@color/md_theme_light_onBackground</item>
    <item name="colorSurface">@color/md_theme_light_surface</item>
    <item name="colorOnSurface">@color/md_theme_light_onSurface</item>
    
    <!-- Modern Component Styles -->
    <item name="materialButtonStyle">@style/Widget.Material3.Button</item>
    <item name="materialCardViewStyle">@style/Widget.Material3.CardView.Elevated</item>
    <item name="textInputStyle">@style/Widget.Material3.TextInputLayout.OutlinedBox</item>
</style>
```

### Phase 3: Architecture Improvements

#### 3.1 MVVM Architecture Implementation
```kotlin
// ViewModel
class PopupViewModel @Inject constructor(
    private val dictionaryRepository: DictionaryRepository,
    private val ankiRepository: AnkiRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PopupUiState())
    val uiState: StateFlow<PopupUiState> = _uiState.asStateFlow()
    
    private val _searchResults = MutableStateFlow<List<Definition>>(emptyList())
    val searchResults: StateFlow<List<Definition>> = _searchResults.asStateFlow()
    
    fun searchWord(word: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val results = withContext(Dispatchers.IO) {
                    dictionaryRepository.searchWord(word)
                }
                _searchResults.value = results
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message
                ) }
            }
        }
    }
}

// UI State
data class PopupUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedPlan: OutputPlan? = null,
    val currentWord: String = ""
)
```

#### 3.2 Repository Pattern Implementation
```kotlin
interface DictionaryRepository {
    suspend fun searchWord(word: String): List<Definition>
    suspend fun getAutoCompleteSuggestions(query: String): List<String>
}

class DictionaryRepositoryImpl @Inject constructor(
    private val dictionaryDao: DictionaryDao,
    private val aiDictionaryService: AIDictionaryService
) : DictionaryRepository {
    
    override suspend fun searchWord(word: String): List<Definition> {
        return withContext(Dispatchers.IO) {
            // Search logic combining multiple dictionary sources
            val localResults = dictionaryDao.searchWord(word)
            val aiResults = aiDictionaryService.searchWord(word)
            
            // Combine and deduplicate results
            (localResults + aiResults).distinctBy { it.getDisplayHtml() }
        }
    }
}
```

#### 3.3 Dependency Injection with Hilt
```kotlin
@HiltAndroidApp
class AnkiApplication : Application()

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    abstract fun bindDictionaryRepository(
        dictionaryRepositoryImpl: DictionaryRepositoryImpl
    ): DictionaryRepository
    
    @Binds
    abstract fun bindAnkiRepository(
        ankiRepositoryImpl: AnkiRepositoryImpl
    ): AnkiRepository
}

@AndroidEntryPoint
class ModernPopupActivity : AppCompatActivity() {
    private val viewModel: PopupViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modern_popup)
        
        observeViewModel()
        setupViews()
    }
}
```

### Phase 4: Performance Optimization

#### 4.1 Memory Leak Prevention
```kotlin
class ModernPopupActivity : AppCompatActivity() {
    private var searchJob: Job? = null
    
    override fun onDestroy() {
        super.onDestroy()
        // Cancel coroutines
        searchJob?.cancel()
        
        // Clear references
        popupWindow?.let { window ->
            if (window.isShowing) {
                window.dismiss()
            }
        }
        
        // Remove listeners
        searchEditText.removeTextChangedListener(textWatcher)
    }
}
```

#### 4.2 RecyclerView Optimization
```kotlin
class DefinitionAdapter(
    private val onItemClick: (Definition) -> Unit
) : ListAdapter<Definition, DefinitionViewHolder>(DiffCallback) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DefinitionViewHolder {
        return DefinitionViewHolder(
            ItemDefinitionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }
    
    override fun onBindViewHolder(holder: DefinitionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    companion object DiffCallback : DiffUtil.ItemCallback<Definition>() {
        override fun areItemsTheSame(oldItem: Definition, newItem: Definition): Boolean {
            return oldItem.getDisplayHtml() == newItem.getDisplayHtml()
        }
        
        override fun areContentsTheSame(oldItem: Definition, newItem: Definition): Boolean {
            return oldItem == newItem
        }
    }
}
```

### Phase 5: Testing and Quality Assurance

#### 5.1 Database Migration Strategy
Since the app uses LitePal for database operations, implement a gradual migration to Room:

```kotlin
// Migration strategy
class DatabaseMigration {
    companion object {
        private const val DATABASE_VERSION = 2
        
        @JvmField
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Migrate LitePal tables to Room schema
                database.execSQL("CREATE TABLE IF NOT EXISTS definitions_new (...)")
                database.execSQL("INSERT INTO definitions_new SELECT * FROM definitions")
                database.execSQL("DROP TABLE definitions")
                database.execSQL("ALTER TABLE definitions_new RENAME TO definitions")
            }
        }
    }
}

// Database configuration
@Database(
    entities = [Definition::class, OutputPlan::class, DictionarySetting::class],
    version = DATABASE_VERSION,
    autoMigrations = []
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun definitionDao(): DefinitionDao
    abstract fun outputPlanDao(): OutputPlanDao
    abstract fun dictionarySettingDao(): DictionarySettingDao
}
```

#### 5.2 Gradual Rollout Strategy
Implement feature flags to control the rollout of new features:

```kotlin
class FeatureFlags {
    companion object {
        const val USE_MODERN_POPUP = "use_modern_popup"
        const val USE_NEW_SEARCH = "use_new_search"
        const val USE_MATERIAL3 = "use_material3"
        
        fun isFeatureEnabled(context: Context, feature: String): Boolean {
            val prefs = context.getSharedPreferences("feature_flags", Context.MODE_PRIVATE)
            return prefs.getBoolean(feature, false)
        }
        
        fun enableFeature(context: Context, feature: String, enabled: Boolean) {
            val prefs = context.getSharedPreferences("feature_flags", Context.MODE_PRIVATE)
            prefs.edit().putBoolean(feature, enabled).apply()
        }
    }
}

// Usage in activities
class PopupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (FeatureFlags.isFeatureEnabled(this, FeatureFlags.USE_MODERN_POPUP)) {
            // Use modern popup implementation
            startActivity(Intent(this, ModernPopupActivity::class.java))
            finish()
        } else {
            // Use legacy implementation
            setContentView(R.layout.activity_popup)
            setupLegacyPopup()
        }
    }
}
```

#### 5.3 Unit Testing
```kotlin
@ExperimentalCoroutinesTest
class PopupViewModelTest {
    private lateinit var viewModel: PopupViewModel
    private lateinit var mockDictionaryRepository: DictionaryRepository
    
    @Before
    fun setUp() {
        mockDictionaryRepository = mockk()
        viewModel = PopupViewModel(mockDictionaryRepository)
    }
    
    @Test
    fun `searchWord should update loading state`() = runTest {
        // Given
        coEvery { mockDictionaryRepository.searchWord("test") } returns emptyList()
        
        // When
        viewModel.searchWord("test")
        
        // Then
        assertEquals(true, viewModel.uiState.value.isLoading)
    }
}
```

#### 5.2 UI Testing
```kotlin
@RunWith(AndroidJUnit4::class)
class ModernPopupActivityTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(ModernPopupActivity::class.java)
    
    @Test
    fun popup_should_dismiss_on_outside_touch() {
        activityRule.scenario.onActivity { activity ->
            // Simulate outside touch
            activity.overlayView.performClick()
            
            // Verify popup is dismissed
            assertFalse(activity.popupWindow.isShowing)
        }
    }
}
```

## Implementation Timeline

### Week 1-2: Infrastructure Setup
- [ ] Update dependencies and build configuration
- [ ] Set up Hilt dependency injection
- [ ] Create base ViewModel and Repository classes
- [ ] Implement basic MVVM structure

### Week 3-4: UI Modernization
- [ ] Create modern popup layout and behavior
- [ ] Implement proper touch handling
- [ ] Update themes to Material 3
- [ ] Create new UI components

### Week 5-6: Core Functionality
- [ ] Migrate PopupActivity to modern architecture
- [ ] Implement new search functionality
- [ ] Update dictionary integration
- [ ] Add proper error handling

### Week 7-8: Testing and Polish
- [ ] Write unit and UI tests
- [ ] Performance optimization
- [ ] Accessibility improvements
- [ ] Documentation and cleanup

## Success Criteria

### Technical Metrics (2025 Standards)
- [ ] Zero memory leaks (verified with LeakCanary 2.14)
- [ ] UI response time < 100ms (measured with Jetpack Benchmark)
- [ ] Search operations complete in < 500ms
- [ ] 90%+ test coverage for critical paths
- [ ] Android 15 (API 35) compatibility
- [ ] Material You dynamic theming support
- [ ] K2 compiler optimization enabled
- [ ] Performance class optimization implemented

### User Experience Metrics
- [ ] Popup can be dismissed by touching outside
- [ ] Underlying content remains interactive when not touching popup
- [ ] Smooth animations and transitions (120fps where supported)
- [ ] Consistent Material 3 design language
- [ ] Biometric authentication for sensitive operations
- [ ] Privacy-respecting data collection
- [ ] Foldable device optimization
- [ ] Adaptive layouts for different screen sizes

### Code Quality Metrics
- [ ] 100% Kotlin codebase with K2 compiler
- [ ] No deprecated API usage (Android 15 compliant)
- [ ] Clean architecture with proper separation of concerns
- [ ] Comprehensive documentation
- [ ] Hilt dependency injection implemented
- [ ] Room database with Flow support
- [ ] DataStore instead of SharedPreferences
- [ ] Modern error handling with sealed classes
- [ ] Accessibility compliance (WCAG 2.1)

### Performance Metrics
- [ ] APK size reduction by 20% through R8 optimization
- [ ] Cold start time < 1000ms
- [ ] Memory usage < 100MB for typical operations
- [ ] Battery optimization with WorkManager
- [ ] Network usage optimization with caching strategies
- [ ] Background job efficiency improvements

## Risk Assessment

### High Risk
- **Breaking Changes**: Major UI overhaul may affect user workflow
- **Performance**: New architecture may introduce performance regressions
- **Compatibility**: Changes to popup behavior may break third-party integrations
- **Data Loss**: Database migration failures could result in data loss

### Mitigation Strategies
- Implement gradual rollout with A/B testing
- Maintain legacy mode option during transition
- Comprehensive testing across different Android versions
- Create detailed migration guide for users

### Backup and Rollback Strategy

#### 6.1 Automated Backup System
```kotlin
class BackupManager {
    companion object {
        private const val BACKUP_PREFS = "backup_settings"
        private const val LAST_BACKUP_KEY = "last_backup_timestamp"
        
        fun createBackup(context: Context): Boolean {
            return try {
                val timestamp = System.currentTimeMillis()
                val backupDir = File(context.getExternalFilesDir("backups"), "auto_$timestamp")
                backupDir.mkdirs()
                
                // Backup user settings
                val settingsPrefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
                val settingsFile = File(backupDir, "settings.xml")
                settingsPrefs.copyTo(settingsFile)
                
                // Backup dictionary configurations
                val dictionaryPrefs = context.getSharedPreferences("dictionary_settings", Context.MODE_PRIVATE)
                val dictionaryFile = File(backupDir, "dictionary_settings.xml")
                dictionaryPrefs.copyTo(dictionaryFile)
                
                // Backup database
                val databaseFile = context.getDatabasePath("ankihelper.db")
                val backupDbFile = File(backupDir, "ankihelper.db")
                databaseFile.copyTo(backupDbFile)
                
                // Update last backup timestamp
                val backupPrefs = context.getSharedPreferences(BACKUP_PREFS, Context.MODE_PRIVATE)
                backupPrefs.edit().putLong(LAST_BACKUP_KEY, timestamp).apply()
                
                true
            } catch (e: Exception) {
                Log.e("BackupManager", "Backup failed", e)
                false
            }
        }
        
        fun restoreBackup(context: Context, backupTimestamp: Long): Boolean {
            return try {
                val backupDir = File(context.getExternalFilesDir("backups"), "auto_$backupTimestamp")
                
                // Restore settings
                val settingsFile = File(backupDir, "settings.xml")
                val settingsPrefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
                settingsPrefs.copyFrom(settingsFile)
                
                // Restore dictionary settings
                val dictionaryFile = File(backupDir, "dictionary_settings.xml")
                val dictionaryPrefs = context.getSharedPreferences("dictionary_settings", Context.MODE_PRIVATE)
                dictionaryPrefs.copyFrom(dictionaryFile)
                
                // Restore database
                val backupDbFile = File(backupDir, "ankihelper.db")
                val databaseFile = context.getDatabasePath("ankihelper.db")
                backupDbFile.copyTo(databaseFile, overwrite = true)
                
                true
            } catch (e: Exception) {
                Log.e("BackupManager", "Restore failed", e)
                false
            }
        }
        
        fun isRollbackAvailable(context: Context): Boolean {
            val backupPrefs = context.getSharedPreferences(BACKUP_PREFS, Context.MODE_PRIVATE)
            val lastBackup = backupPrefs.getLong(LAST_BACKUP_KEY, 0)
            return lastBackup > 0
        }
    }
}
```

#### 6.2 Rollback Procedure
```kotlin
class RollbackManager {
    companion object {
        fun performRollback(context: Context): Boolean {
            return try {
                // Disable all new features
                FeatureFlags.enableFeature(context, FeatureFlags.USE_MODERN_POPUP, false)
                FeatureFlags.enableFeature(context, FeatureFlags.USE_NEW_SEARCH, false)
                FeatureFlags.enableFeature(context, FeatureFlags.USE_MATERIAL3, false)
                
                // Restore from last backup
                val backupPrefs = context.getSharedPreferences("backup_settings", Context.MODE_PRIVATE)
                val lastBackup = backupPrefs.getLong("last_backup_timestamp", 0)
                
                if (lastBackup > 0) {
                    BackupManager.restoreBackup(context, lastBackup)
                }
                
                // Clear caches and restart app
                clearApplicationData(context)
                restartApplication(context)
                
                true
            } catch (e: Exception) {
                Log.e("RollbackManager", "Rollback failed", e)
                false
            }
        }
        
        private fun clearApplicationData(context: Context) {
            try {
                val cacheDir = context.cacheDir
                val appDir = File(cacheDir.parent)
                if (appDir.exists()) {
                    val children = appDir.listFiles()
                    children?.forEach { child ->
                        if (child.name != "lib") {
                            deleteDir(child)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("RollbackManager", "Failed to clear app data", e)
            }
        }
        
        private fun deleteDir(dir: File): Boolean {
            if (dir.isDirectory) {
                dir.listFiles()?.forEach { child ->
                    deleteDir(child)
                }
            }
            return dir.delete()
        }
    }
}
```

## Conclusion

This modernization plan will transform AnkiQuick into a modern, performant Android application that follows current best practices. The implementation will improve user experience, developer productivity, and long-term maintainability. The phased approach ensures minimal disruption while delivering significant improvements.

## CI/CD and Monitoring

### 7.1 Continuous Integration Pipeline
```yaml
# .github/workflows/android.yml
name: Android CI

on:
  push:
    branches: [ dev, master ]
  pull_request:
    branches: [ dev ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v4
    
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        
    - name: Setup Android SDK
      uses: android-actions/setup-android@v3
      
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
      
    - name: Run unit tests
      run: ./gradlew test
      
    - name: Run instrumented tests
      run: ./gradlew connectedAndroidTest
      
    - name: Upload test results
      uses: actions/upload-artifact@v4
      if: always()
      with:
        name: test-results
        path: app/build/test-results/
        
    - name: Generate APK
      run: ./gradlew assembleDebug
      
    - name: Upload APK
      uses: actions/upload-artifact@v4
      with:
        name: app-debug
        path: app/build/outputs/apk/debug/app-debug.apk
```

### 7.2 Performance Monitoring
```kotlin
class PerformanceMonitor {
    companion object {
        private const val PREFS_NAME = "performance_metrics"
        
        fun trackPopupLoadTime(context: Context, loadTime: Long) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val currentAvg = prefs.getLong("avg_popup_load_time", 0)
            val count = prefs.getInt("popup_load_count", 0)
            
            val newAvg = if (count == 0) loadTime else (currentAvg * count + loadTime) / (count + 1)
            
            prefs.edit()
                .putLong("avg_popup_load_time", newAvg)
                .putInt("popup_load_count", count + 1)
                .apply()
                
            // Log performance metrics
            if (loadTime > 1000) { // More than 1 second
                Log.w("Performance", "Slow popup load: ${loadTime}ms")
            }
        }
        
        fun trackSearchTime(context: Context, searchTime: Long) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val currentAvg = prefs.getLong("avg_search_time", 0)
            val count = prefs.getInt("search_count", 0)
            
            val newAvg = if (count == 0) searchTime else (currentAvg * count + searchTime) / (count + 1)
            
            prefs.edit()
                .putLong("avg_search_time", newAvg)
                .putInt("search_count", count + 1)
                .apply()
        }
        
        fun getPerformanceReport(context: Context): Map<String, Any> {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return mapOf(
                "avg_popup_load_time" to prefs.getLong("avg_popup_load_time", 0),
                "popup_load_count" to prefs.getInt("popup_load_count", 0),
                "avg_search_time" to prefs.getLong("avg_search_time", 0),
                "search_count" to prefs.getInt("search_count", 0)
            )
        }
    }
}
```

### 7.3 Quality Gates
```kotlin
object QualityGates {
    const val MAX_POPUP_LOAD_TIME = 1000L // 1 second
    const val MAX_SEARCH_TIME = 500L // 500ms
    const val MIN_TEST_COVERAGE = 0.8 // 80%
    const val MAX_CRITICAL_BUGS = 0
    
    fun validatePerformance(context: Context): List<String> {
        val issues = mutableListOf<String>()
        val metrics = PerformanceMonitor.getPerformanceReport(context)
        
        val avgPopupLoad = metrics["avg_popup_load_time"] as Long
        if (avgPopupLoad > MAX_POPUP_LOAD_TIME) {
            issues.add("Popup load time too slow: ${avgPopupLoad}ms (max: ${MAX_POPUP_LOAD_TIME}ms)")
        }
        
        val avgSearchTime = metrics["avg_search_time"] as Long
        if (avgSearchTime > MAX_SEARCH_TIME) {
            issues.add("Search time too slow: ${avgSearchTime}ms (max: ${MAX_SEARCH_TIME}ms)")
        }
        
        return issues
    }
}
```

## Next Steps

1. **Stakeholder Review**: Present this specification to all stakeholders
2. **Resource Allocation**: Assign team members to each phase
3. **Tool Setup**: Configure CI/CD pipelines and testing infrastructure
4. **Initial Sprint**: Begin with infrastructure updates
5. **Regular Reviews**: Weekly progress reviews and adjustments
6. **Performance Baseline**: Establish current performance metrics before starting modernization
7. **User Communication**: Prepare communication plan for users about upcoming changes

---

**Document Version**: 2.0
**Last Updated**: 2025
**Status**: Updated for 2025 Android Development Standards
**Key Updates for 2025**:
- Updated all dependencies to latest 2025 versions
- Added Android 15 (API 35) support
- Implemented Kotlin 2.1.0 with K2 compiler
- Added Material You dynamic theming
- Included biometric authentication
- Added privacy-safe data collection
- Implemented performance class optimization
- Added foldable device support
- Enhanced security and privacy features
- Updated success criteria for 2025 standards