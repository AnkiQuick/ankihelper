# Room Migration - Detailed Task List

**Version**: 1.0
**Date**: 2025-10-15
**Estimated Duration**: 6 weeks
**Priority**: HIGH (Critical performance issues)

## Task Status Legend
- ⬜ Not Started
- 🟦 In Progress
- ✅ Completed
- ⚠️ Blocked
- ❌ Cancelled

---

## Phase 1: Fix Critical Performance Issues (Week 1)

### Priority: 🔴 CRITICAL - Start Immediately

#### Day 1: Setup and Dependencies ⬜
- [ ] Task 1.1: Review current Room implementations
  - File: `Oalde10Database.java`, `MaldpeDatabase.java`, `FormsDatabase.java`
  - Identify all `.allowMainThreadQueries()` usage
  - Document current query patterns
  - Estimate: 2 hours

- [ ] Task 1.2: Update build.gradle with Room and Coroutines dependencies
  - Add Room runtime, compiler, ktx
  - Add Kotlin Coroutines dependencies
  - Add Lifecycle dependencies for viewModelScope
  - Add testing dependencies
  - Sync project
  - Estimate: 1 hour

- [ ] Task 1.3: Create test project to verify Kotlin/Coroutines setup
  - Create simple suspend function test
  - Verify compilation
  - Run basic coroutine test
  - Estimate: 1 hour

#### Day 2-3: Remove Main Thread Queries ⬜
- [ ] Task 1.4: Fix Oalde10Database
  - Remove `.allowMainThreadQueries()` from database builder
  - Document change
  - Compile and verify
  - File: `app/src/main/java/com/mmjang/ankihelper/data/dict/Oalde10Database.java`
  - Estimate: 30 minutes

- [ ] Task 1.5: Fix MaldpeDatabase
  - Remove `.allowMainThreadQueries()` from database builder
  - Document change
  - Compile and verify
  - File: `app/src/main/java/com/mmjang/ankihelper/data/dict/MaldpeDatabase.java`
  - Estimate: 30 minutes

- [ ] Task 1.6: Fix FormsDatabase
  - Remove `.allowMainThreadQueries()` from database builder
  - Document change
  - Compile and verify
  - File: `app/src/main/java/com/mmjang/ankihelper/data/dict/FormsDatabase.java`
  - Estimate: 30 minutes

- [ ] Task 1.7: Check for any other dictionary databases
  - Search for other Room database implementations
  - Fix if found
  - Estimate: 1 hour

#### Day 3-4: Convert DAOs to Suspend Functions ⬜
- [ ] Task 1.8: Convert Oalde10Dao to Kotlin
  - Create `Oalde10Dao.kt`
  - Convert all methods to suspend functions
  - Handle Cursor return types (consider alternatives)
  - Test compilation
  - File: Create `app/src/main/java/com/mmjang/ankihelper/data/dict/Oalde10Dao.kt`
  - Estimate: 3 hours

- [ ] Task 1.9: Convert MaldpeDao to Kotlin
  - Create `MaldpeDao.kt`
  - Convert all methods to suspend functions
  - Handle Cursor return types
  - Test compilation
  - File: Create `app/src/main/java/com/mmjang/ankihelper/data/dict/MaldpeDao.kt`
  - Estimate: 3 hours

- [ ] Task 1.10: Convert FormsDao to Kotlin (if exists)
  - Create `FormsDao.kt`
  - Convert all methods to suspend functions
  - Test compilation
  - Estimate: 2 hours

#### Day 4-5: Update Call Sites with Coroutines ⬜
- [ ] Task 1.11: Identify all dictionary DAO call sites
  - Search for `.queryDefinition()` calls
  - Search for other DAO method calls
  - Create list of files to update
  - Files: `Oalde10.java`, `Maldpe.java`, `Cdepe4.java`, others
  - Estimate: 2 hours

- [ ] Task 1.12: Update Oalde10.java wordLookup method
  - Implement coroutine wrapper for Java interop
  - Create callback interface
  - Launch coroutine on IO dispatcher
  - Return results on main thread
  - Test functionality
  - File: `app/src/main/java/com/mmjang/ankihelper/data/dict/Oalde10.java`
  - Estimate: 4 hours

- [ ] Task 1.13: Update Maldpe.java wordLookup method
  - Same pattern as Oalde10
  - Test functionality
  - File: `app/src/main/java/com/mmjang/ankihelper/data/dict/Maldpe.java`
  - Estimate: 3 hours

- [ ] Task 1.14: Update Cdepe4.java wordLookup method (if uses Room)
  - Same pattern as above
  - Test functionality
  - File: `app/src/main/java/com/mmjang/ankihelper/data/dict/Cdepe4.java`
  - Estimate: 3 hours

- [ ] Task 1.15: Update any other dictionary call sites
  - Search for remaining direct DAO calls
  - Update with coroutine wrapper
  - Test functionality
  - Estimate: 2 hours

#### Day 5: Testing ⬜
- [ ] Task 1.16: Create DAO unit tests
  - Test Oalde10Dao with in-memory database
  - Test MaldpeDao with in-memory database
  - Verify suspend functions work correctly
  - Create: `app/src/androidTest/java/.../*DaoTest.kt`
  - Estimate: 4 hours

- [ ] Task 1.17: Integration testing
  - Test dictionary lookup flow end-to-end
  - Test with Oxford dictionary
  - Test with Merriam-Webster dictionary
  - Verify no UI blocking
  - Estimate: 3 hours

- [ ] Task 1.18: Performance testing
  - Use Android Profiler to monitor main thread
  - Verify no ANR warnings
  - Benchmark query times
  - Compare before/after performance
  - Estimate: 2 hours

- [ ] Task 1.19: Regression testing
  - Test all dictionary features
  - Test word lookup
  - Test autocomplete
  - Test history
  - Verify all functionality works
  - Estimate: 3 hours

- [ ] Task 1.20: Fix any bugs found during testing
  - Address test failures
  - Fix performance issues
  - Resolve crashes
  - Estimate: 4 hours (buffer)

#### Week 1 Deliverables
✅ All dictionary Room databases no longer use main thread queries
✅ All DAO operations use Coroutines
✅ No ANR issues in dictionary lookups
✅ All tests passing
✅ Performance benchmarks show improvement

---

## Phase 2A: Migrate DatabaseManager - Plan & History (Week 2-3)

### Week 2: Entity and DAO Creation

#### Day 1: Create Room Entities ⬜
- [ ] Task 2.1: Create OutputPlanEntity
  - Convert OutputPlanPOJO to Room entity
  - Add @Entity, @PrimaryKey, @ColumnInfo annotations
  - Match existing database column names
  - Keep existing helper methods
  - File: Create `app/src/main/java/com/mmjang/ankihelper/data/plan/OutputPlanEntity.java`
  - Estimate: 2 hours

- [ ] Task 2.2: Create HistoryEntity
  - Convert HistoryPOJO to Room entity
  - Add @Entity, @PrimaryKey (timestamp), @ColumnInfo annotations
  - Match existing database column names
  - Keep existing constructors
  - File: Create `app/src/main/java/com/mmjang/ankihelper/data/history/HistoryEntity.java`
  - Estimate: 2 hours

- [ ] Task 2.3: Create BookEntity
  - Convert Book POJO to Room entity
  - Add annotations
  - Match existing database schema
  - File: Create `app/src/main/java/com/mmjang/ankihelper/data/book/BookEntity.java`
  - Estimate: 1.5 hours

- [ ] Task 2.4: Create UserTagEntity
  - Create from scratch (only LitePal version exists)
  - Simple entity with tag as primary key
  - File: Create `app/src/main/java/com/mmjang/ankihelper/data/model/UserTagEntity.java`
  - Estimate: 1 hour

#### Day 2: Create DAOs ⬜
- [ ] Task 2.5: Create OutputPlanDao
  - All CRUD operations as suspend functions
  - getAllPlans(), getPlanByName(), insertPlan(), updatePlan(), deletePlan()
  - Special methods: refreshPlansWithTransaction(), deletePlanByName()
  - File: Create `app/src/main/java/com/mmjang/ankihelper/data/plan/OutputPlanDao.kt`
  - Estimate: 3 hours

- [ ] Task 2.6: Create HistoryDao
  - All CRUD operations as suspend functions
  - getAllHistory(), getHistoryAfter(), insertHistory(), insertHistories()
  - Sorting by timestamp
  - File: Create `app/src/main/java/com/mmjang/ankihelper/data/history/HistoryDao.kt`
  - Estimate: 2.5 hours

- [ ] Task 2.7: Create BookDao
  - All CRUD operations as suspend functions
  - getAllBooks() with sort by lastopentime
  - getBookById(), insertBook(), updateBook(), deleteBook()
  - File: Create `app/src/main/java/com/mmjang/ankihelper/data/book/BookDao.kt`
  - Estimate: 2 hours

- [ ] Task 2.8: Create UserTagDao
  - CRUD operations for tags
  - getAllTags(), getTagByName(), insertTag(), insertTags(), deleteTag()
  - File: Create `app/src/main/java/com/mmjang/ankihelper/data/model/UserTagDao.kt`
  - Estimate: 1.5 hours

#### Day 3-4: Create AppDatabase ⬜
- [ ] Task 2.9: Create AppDatabase class
  - Define all entities
  - Abstract DAO methods
  - Set version to 4 (higher than DatabaseHelper's 3)
  - Implement singleton pattern
  - Use same database name: "ankihelper.db"
  - File: Create `app/src/main/java/com/mmjang/ankihelper/data/database/AppDatabase.kt`
  - Estimate: 3 hours

- [ ] Task 2.10: Create migration from version 3 to 4
  - Implement Migration(3, 4)
  - Handle schema differences
  - Ensure data preservation
  - Add to AppDatabase
  - Estimate: 4 hours

- [ ] Task 2.11: Test AppDatabase initialization
  - Create test app that initializes database
  - Verify database file created
  - Verify tables exist
  - Verify migration runs successfully
  - Estimate: 3 hours

- [ ] Task 2.12: Write DAO unit tests
  - Test OutputPlanDao with in-memory database
  - Test HistoryDao with in-memory database
  - Test BookDao with in-memory database
  - Test UserTagDao with in-memory database
  - Create: `app/src/androidTest/java/.../Dao*Test.kt`
  - Estimate: 6 hours

#### Day 5: Create Repositories ⬜
- [ ] Task 2.13: Create OutputPlanRepository
  - Singleton pattern
  - Wrap all DAO operations
  - Proper coroutine dispatchers
  - Error handling
  - File: Create `app/src/main/java/com/mmjang/ankihelper/data/plan/OutputPlanRepository.kt`
  - Estimate: 2.5 hours

- [ ] Task 2.14: Create HistoryRepository
  - Singleton pattern
  - Wrap all DAO operations
  - File: Create `app/src/main/java/com/mmjang/ankihelper/data/history/HistoryRepository.kt`
  - Estimate: 2 hours

- [ ] Task 2.15: Create BookRepository
  - Singleton pattern
  - Wrap all DAO operations
  - File: Create `app/src/main/java/com/mmjang/ankihelper/data/book/BookRepository.kt`
  - Estimate: 1.5 hours

- [ ] Task 2.16: Create UserTagRepository
  - Singleton pattern
  - Wrap all DAO operations
  - File: Create `app/src/main/java/com/mmjang/ankihelper/data/model/UserTagRepository.kt`
  - Estimate: 1 hour

- [ ] Task 2.17: Write repository unit tests
  - Test each repository
  - Mock DAOs
  - Verify coroutine behavior
  - Test error handling
  - Create: `app/src/test/java/.../*RepositoryTest.kt`
  - Estimate: 4 hours

### Week 3: Update Call Sites

#### Day 1-2: Update Plan-Related Code ⬜
- [ ] Task 2.18: Update PlansManagerActivity
  - Replace DatabaseManager calls with OutputPlanRepository
  - Use lifecycleScope for coroutines
  - Update getAllPlans() call
  - Update delete operations
  - File: `app/src/main/java/com/mmjang/ankihelper/ui/plan/PlansManagerActivity.java`
  - Estimate: 4 hours

- [ ] Task 2.19: Update PlanEditorActivity
  - Replace DatabaseManager calls with repository
  - Update getPlanByName()
  - Update insertPlan()
  - Update updatePlan()
  - File: `app/src/main/java/com/mmjang/ankihelper/ui/plan/PlanEditorActivity.java`
  - Estimate: 4 hours

- [ ] Task 2.20: Update plan import/export functionality
  - Update refreshPlanWith() usage
  - Test import/export flows
  - Files: Search for `getAllPlan()`, `insertPlan()` usage
  - Estimate: 3 hours

- [ ] Task 2.21: Test all plan-related features
  - Create plan
  - Edit plan
  - Delete plan
  - Import plans
  - Export plans
  - Estimate: 3 hours

#### Day 2-3: Update History-Related Code ⬜
- [ ] Task 2.22: Update PopupActivity
  - Replace insertHistory() with repository
  - Use coroutines for insertion
  - File: `app/src/main/java/com/mmjang/ankihelper/ui/popup/PopupActivity.java`
  - Estimate: 3 hours

- [ ] Task 2.23: Update HistoryActivity (if exists)
  - Replace getHistoryAfter() with repository
  - Use coroutines for queries
  - Update UI with results
  - Estimate: 3 hours

- [ ] Task 2.24: Update history sync functionality
  - Replace insertManyHistory() with repository
  - Use transaction for bulk insert
  - Estimate: 2 hours

- [ ] Task 2.25: Test all history-related features
  - Add history entry
  - View history
  - Sync history
  - Delete history
  - Estimate: 3 hours

#### Day 3-4: Update Book-Related Code ⬜
- [ ] Task 2.26: Update EPUB reader activity
  - Replace book operations with BookRepository
  - Update insertBook(), updateBook(), getLastBooks()
  - File: Search for Book CRUD operations
  - Estimate: 4 hours

- [ ] Task 2.27: Update book management UI
  - Update book list display
  - Update book operations
  - Test book features
  - Estimate: 3 hours

#### Day 4: Update UserTag Code ⬜
- [ ] Task 2.28: Identify UserTag usage locations
  - Search for LitePal UserTag operations
  - List all files using UserTag
  - Estimate: 1 hour

- [ ] Task 2.29: Update UserTag operations
  - Replace LitePal calls with UserTagRepository
  - Update tag CRUD operations
  - Test tag functionality
  - Estimate: 3 hours

#### Day 5: Integration Testing ⬜
- [ ] Task 2.30: End-to-end testing
  - Test complete user flows
  - Test plan creation and usage
  - Test history logging
  - Test book management
  - Test tags
  - Estimate: 4 hours

- [ ] Task 2.31: Data migration testing
  - Test with existing user data
  - Verify no data loss
  - Verify data integrity
  - Test migration edge cases
  - Estimate: 4 hours

- [ ] Task 2.32: Performance testing
  - Benchmark database operations
  - Compare with old system
  - Verify no performance regression
  - Estimate: 2 hours

#### Week 2-3 Deliverables
✅ All Plan, History, Book, UserTag operations use Room
✅ DatabaseManager no longer used for these entities
✅ All tests passing
✅ No data loss during migration
✅ Performance maintained or improved

---

## Phase 3: Migrate AI Configs from LitePal (Week 4)

### Day 1-2: Create AI Config Entities ⬜
- [ ] Task 3.1: Create LLMConfigEntity
  - Convert LitePal model to Room entity
  - Handle encrypted API token field
  - File: Create `app/src/main/java/com/mmjang/ankihelper/data/ai/LLMConfigEntity.java`
  - Estimate: 2 hours

- [ ] Task 3.2: Create TTSConfigEntity
  - Similar to LLMConfig
  - File: Create `app/src/main/java/com/mmjang/ankihelper/data/ai/TTSConfigEntity.java`
  - Estimate: 1.5 hours

- [ ] Task 3.3: Create AIDictionaryConfigEntity
  - Include LLM foreign key
  - File: Create `app/src/main/java/com/mmjang/ankihelper/data/ai/AIDictionaryConfigEntity.java`
  - Estimate: 2 hours

- [ ] Task 3.4: Create AITranslatorConfigEntity
  - Include LLM foreign key
  - Include isDefault flag
  - File: Create `app/src/main/java/com/mmjang/ankihelper/data/ai/AITranslatorConfigEntity.java`
  - Estimate: 2 hours

- [ ] Task 3.5: Create AIDictionaryCacheEntity
  - Multiple fields for cache data
  - Include timestamp for expiration
  - File: Create `app/src/main/java/com/mmjang/ankihelper/data/ai/cache/AIDictionaryCacheEntity.java`
  - Estimate: 2.5 hours

- [ ] Task 3.6: Create AITranslatorCacheEntity
  - Similar to dictionary cache
  - File: Create `app/src/main/java/com/mmjang/ankihelper/data/ai/cache/AITranslatorCacheEntity.java`
  - Estimate: 2 hours

### Day 2-3: Create AI DAOs ⬜
- [ ] Task 3.7: Create LLMConfigDao
  - CRUD operations
  - File: Create `app/src/main/java/com/mmjang/ankihelper/data/ai/LLMConfigDao.kt`
  - Estimate: 2 hours

- [ ] Task 3.8: Create TTSConfigDao
  - CRUD operations
  - File: Create `app/src/main/java/com/mmjang/ankihelper/data/ai/TTSConfigDao.kt`
  - Estimate: 1.5 hours

- [ ] Task 3.9: Create AIDictionaryConfigDao
  - CRUD operations
  - File: Create `app/src/main/java/com/mmjang/ankihelper/data/ai/AIDictionaryConfigDao.kt`
  - Estimate: 2 hours

- [ ] Task 3.10: Create AITranslatorConfigDao
  - CRUD operations
  - Special query for default config
  - File: Create `app/src/main/java/com/mmjang/ankihelper/data/ai/AITranslatorConfigDao.kt`
  - Estimate: 2 hours

- [ ] Task 3.11: Create AIDictionaryCacheDao
  - CRUD operations
  - Query by word and LLM config
  - Delete old cache entries
  - File: Create `app/src/main/java/com/mmjang/ankihelper/data/ai/cache/AIDictionaryCacheDao.kt`
  - Estimate: 3 hours

- [ ] Task 3.12: Create AITranslatorCacheDao
  - Similar to dictionary cache DAO
  - File: Create `app/src/main/java/com/mmjang/ankihelper/data/ai/cache/AITranslatorCacheDao.kt`
  - Estimate: 2.5 hours

### Day 3: Update AppDatabase ⬜
- [ ] Task 3.13: Add AI entities to AppDatabase
  - Update @Database annotation
  - Add abstract DAO methods
  - Increment version to 5
  - Create migration 4 → 5
  - File: Update `app/src/main/java/com/mmjang/ankihelper/data/database/AppDatabase.kt`
  - Estimate: 3 hours

- [ ] Task 3.14: Create data migration from LitePal to Room
  - Read all LitePal AI config data
  - Insert into Room database
  - Verify data integrity
  - Delete LitePal data after confirmation
  - Estimate: 4 hours

- [ ] Task 3.15: Test AppDatabase with AI entities
  - Test database initialization
  - Test all new DAOs
  - Test migration
  - Estimate: 3 hours

### Day 4: Update AIConfigRepository ⬜
- [ ] Task 3.16: Update AIConfigRepository to use Room
  - Replace all `LitePal.findAll()` with DAO calls
  - Replace all `.save()` with DAO insert/update
  - Replace all `LitePal.delete()` with DAO delete
  - Use coroutines for all operations
  - File: Update `app/src/main/java/com/mmjang/ankihelper/data/ai/AIConfigRepository.java`
  - Estimate: 6 hours

- [ ] Task 3.17: Update AICacheRepository to use Room
  - Replace LitePal operations with DAO calls
  - Update cache cleanup logic
  - File: Update `app/src/main/java/com/mmjang/ankihelper/data/ai/cache/AICacheRepository.java`
  - Estimate: 4 hours

### Day 5: Testing AI Features ⬜
- [ ] Task 3.18: Update AI editor activities
  - Update LLM editor
  - Update TTS editor
  - Update AI Dictionary editor
  - Update AI Translator editor
  - Replace LitePal calls with repositories
  - Estimate: 6 hours

- [ ] Task 3.19: Test all AI features end-to-end
  - Test LLM config CRUD
  - Test TTS config CRUD
  - Test AI Dictionary config CRUD
  - Test AI Translator config CRUD
  - Test cache operations
  - Test AI dictionary lookup
  - Test AI translation
  - Estimate: 6 hours

- [ ] Task 3.20: Fix any bugs found
  - Address test failures
  - Fix data migration issues
  - Resolve crashes
  - Estimate: 4 hours (buffer)

#### Week 4 Deliverables
✅ All AI configs migrated to Room
✅ All AI cache operations use Room
✅ AIConfigRepository fully Room-based
✅ All AI features work correctly
✅ Data migrated from LitePal successfully

---

## Phase 4: Remove LitePal (Week 5)

### Day 1: Verify Complete Migration ⬜
- [ ] Task 4.1: Search for all LitePal usage
  - Find all `import org.litepal` statements
  - Find all `LitePal.` method calls
  - Find all `.save()` calls on models
  - Find all `extends LitePalSupport` classes
  - Create comprehensive list
  - Estimate: 3 hours

- [ ] Task 4.2: Verify all usage migrated
  - Check each LitePal usage against migration tasks
  - Ensure all replaced with Room
  - Identify any missed usage
  - Estimate: 3 hours

- [ ] Task 4.3: Full regression testing
  - Test ALL application features
  - Test all data operations
  - Test all user flows
  - Verify no LitePal dependencies remain
  - Estimate: 6 hours

### Day 2: Remove LitePal Dependency ⬜
- [ ] Task 4.4: Remove LitePal from build.gradle
  - Remove `implementation 'org.litepal.guolindev:core:3.2.3'`
  - Sync project
  - Verify compilation
  - File: Update `app/build.gradle`
  - Estimate: 30 minutes

- [ ] Task 4.5: Delete litepal.xml
  - Remove configuration file
  - File: Delete `app/src/main/assets/litepal.xml`
  - Estimate: 15 minutes

- [ ] Task 4.6: Remove LitePal initialization
  - Remove `LitePal.initialize(this)` from MyApplication
  - File: Update `app/src/main/java/com/mmjang/ankihelper/MyApplication.java`
  - Estimate: 15 minutes

- [ ] Task 4.7: Clean up old LitePal model classes
  - Delete or deprecate OutputPlan (LitePal version)
  - Delete or deprecate History (LitePal version)
  - Keep if still needed for reference
  - Files: Review and clean up
  - Estimate: 2 hours

### Day 3-4: Final Testing ⬜
- [ ] Task 4.8: Clean build and test
  - Run `./gradlew clean`
  - Run `./gradlew build`
  - Verify successful build
  - Run all unit tests
  - Run all instrumentation tests
  - Estimate: 4 hours

- [ ] Task 4.9: Full application testing
  - Test on multiple devices
  - Test with fresh install
  - Test with upgrade from previous version
  - Test all features end-to-end
  - Estimate: 8 hours

- [ ] Task 4.10: Performance benchmarking
  - Measure app startup time
  - Measure database operation times
  - Compare with pre-migration baseline
  - Verify performance improvements
  - Estimate: 4 hours

- [ ] Task 4.11: Data integrity verification
  - Verify all user data intact
  - Check for any data corruption
  - Verify data consistency
  - Test data export/import
  - Estimate: 3 hours

### Day 5: Documentation and Cleanup ⬜
- [ ] Task 4.12: Update code documentation
  - Add KDoc/JavaDoc to new classes
  - Document migration process
  - Update architecture diagrams
  - Estimate: 4 hours

- [ ] Task 4.13: Create migration guide for users
  - Document user-facing changes
  - Create FAQ for migration
  - Document any known issues
  - Estimate: 3 hours

- [ ] Task 4.14: Code cleanup
  - Remove commented-out code
  - Remove unused imports
  - Format code consistently
  - Run code inspection
  - Estimate: 3 hours

#### Week 5 Deliverables
✅ LitePal completely removed
✅ All tests passing
✅ Performance benchmarks meet targets
✅ Documentation complete
✅ Code cleaned up

---

## Phase 5: Optimization (Week 6)

### Day 1: Database Optimization ⬜
- [ ] Task 5.1: Analyze query performance
  - Use Android Profiler to analyze queries
  - Identify slow queries
  - Document optimization opportunities
  - Estimate: 4 hours

- [ ] Task 5.2: Add database indices
  - Add index on history.word
  - Add index on history.timestamp
  - Add index on plan.planname
  - Add composite indices as needed
  - File: Update `AppDatabase.kt` migrations
  - Estimate: 3 hours

- [ ] Task 5.3: Optimize frequently-used queries
  - Rewrite complex queries
  - Use @Transaction for multi-table operations
  - Add @RawQuery for dynamic queries if needed
  - Estimate: 4 hours

### Day 2: Code Quality Improvements ⬜
- [ ] Task 5.4: Add LiveData/Flow support (optional)
  - Convert DAO queries to return Flow
  - Update repositories to expose Flow
  - Update UI to observe Flow
  - Estimate: 6 hours

- [ ] Task 5.5: Improve error handling
  - Add try-catch in repositories
  - Create custom exception types
  - Add error logging
  - Improve user-facing error messages
  - Estimate: 4 hours

- [ ] Task 5.6: Add database migrations for future
  - Document migration strategy
  - Create template for future migrations
  - Add migration tests
  - Estimate: 3 hours

### Day 3: Testing Enhancements ⬜
- [ ] Task 5.7: Increase test coverage
  - Add more unit tests
  - Add more integration tests
  - Target >80% coverage
  - Estimate: 6 hours

- [ ] Task 5.8: Add performance tests
  - Create benchmark tests
  - Test with large datasets
  - Test concurrent operations
  - Estimate: 4 hours

- [ ] Task 5.9: Add UI tests
  - Test key user flows with Espresso
  - Test data entry flows
  - Test data display flows
  - Estimate: 4 hours

### Day 4: Documentation ⬜
- [ ] Task 5.10: Create architecture documentation
  - Document database schema
  - Document entity relationships
  - Create ER diagrams
  - Document repository pattern
  - Estimate: 6 hours

- [ ] Task 5.11: Create developer guide
  - How to add new entities
  - How to create migrations
  - Best practices guide
  - Common pitfalls to avoid
  - Estimate: 4 hours

- [ ] Task 5.12: Update README
  - Document Room usage
  - Update dependency list
  - Update build instructions
  - Estimate: 2 hours

### Day 5: Final Review ⬜
- [ ] Task 5.13: Code review
  - Review all new code
  - Check for code smells
  - Verify best practices followed
  - Get peer review
  - Estimate: 6 hours

- [ ] Task 5.14: Security review
  - Check for SQL injection risks (Room prevents this)
  - Verify encryption for sensitive data
  - Check for data leaks
  - Estimate: 3 hours

- [ ] Task 5.15: Performance final check
  - Run full performance suite
  - Verify all benchmarks pass
  - Document performance improvements
  - Estimate: 3 hours

#### Week 6 Deliverables
✅ Database optimized with indices
✅ Test coverage >80%
✅ Comprehensive documentation
✅ Code review complete
✅ Performance targets met

---

## Summary Statistics

### Total Tasks: 110
- Phase 1: 20 tasks (Week 1)
- Phase 2: 35 tasks (Weeks 2-3)
- Phase 3: 20 tasks (Week 4)
- Phase 4: 14 tasks (Week 5)
- Phase 5: 15 tasks (Week 6)
- Additional: 6 ongoing tasks

### Estimated Hours: ~300 hours
- Phase 1: ~40 hours
- Phase 2: ~90 hours
- Phase 3: ~60 hours
- Phase 4: ~50 hours
- Phase 5: ~60 hours

### Team Size: 1-2 developers
- 1 developer: 6 weeks full-time
- 2 developers: 4 weeks with parallel work

### Risk Buffer: 20%
- Built-in buffer for unexpected issues
- Testing time accounts for bug fixes
- Flexible timeline allows for quality assurance

---

## Ongoing Tasks (Throughout Project)

- [ ] Ongoing: Daily standup / status updates
  - Document progress
  - Identify blockers
  - Adjust timeline as needed
  - Estimate: 15 min/day

- [ ] Ongoing: Git commits and documentation
  - Commit frequently with clear messages
  - Document major decisions
  - Update task list regularly
  - Estimate: 30 min/day

- [ ] Ongoing: Code backup
  - Push to remote regularly
  - Tag major milestones
  - Keep migration scripts backed up
  - Estimate: 10 min/day

- [ ] Ongoing: Stakeholder communication
  - Weekly progress reports
  - Demo new functionality
  - Get feedback early
  - Estimate: 1 hour/week

- [ ] Ongoing: Monitor production metrics
  - Watch crash reports
  - Monitor performance metrics
  - Collect user feedback
  - Estimate: 30 min/day

- [ ] Ongoing: Rollback readiness
  - Keep old system functional
  - Maintain rollback scripts
  - Test rollback procedure
  - Estimate: 1 hour/week

---

## Success Criteria Checklist

### Phase 1 Success
- [ ] No `.allowMainThreadQueries()` in codebase
- [ ] All DAOs use suspend functions
- [ ] ANR rate < 0.01%
- [ ] All dictionary tests passing
- [ ] Performance improved or maintained

### Phase 2 Success
- [ ] All Plan/History/Book operations use Room
- [ ] DatabaseManager deprecated for these entities
- [ ] Data migration successful (0% data loss)
- [ ] All features working as before
- [ ] Performance maintained

### Phase 3 Success
- [ ] All AI configs migrated to Room
- [ ] AIConfigRepository fully Room-based
- [ ] LitePal only in MyApplication (initialization)
- [ ] All AI features functional
- [ ] Cache operations working

### Phase 4 Success
- [ ] LitePal dependency removed
- [ ] litepal.xml deleted
- [ ] No LitePal imports in code
- [ ] All tests passing
- [ ] App builds and runs without LitePal

### Phase 5 Success
- [ ] Database indices added
- [ ] Test coverage > 80%
- [ ] Documentation complete
- [ ] Performance targets met
- [ ] Code review approved

### Overall Success
- [ ] App crashes reduced or stable
- [ ] ANR rate < 0.01%
- [ ] Database operations faster
- [ ] Code complexity reduced
- [ ] Maintainability improved
- [ ] No data loss
- [ ] All features functional
- [ ] User satisfaction maintained

---

**Next Steps**:
1. Review and approve this task list
2. Assign tasks to developers
3. Begin Phase 1 immediately (critical priority)
4. Set up task tracking system (Jira, Trello, GitHub Issues)
5. Schedule daily standups
6. Create development branch

**Document Version**: 1.0
**Last Updated**: 2025-10-15
