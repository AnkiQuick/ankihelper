# Room Migration Checklist

This checklist ensures all steps are completed during the migration from LitePal and direct SQLite to Room.

## Pre-Migration

- [ ] Backup current codebase
- [ ] Document current data models and their usage
- [ ] Identify all classes that use LitePal
- [ ] Identify all classes that use direct SQLite in DatabaseManager
- [ ] Create branch for Room migration work

## Phase 1: Setup and Dependencies

- [ ] Add Room dependencies to `app/build.gradle`
- [ ] Add Gson dependency for TypeConverters
- [ ] Sync project successfully
- [ ] Verify no compilation errors

## Phase 2: Create Room Components for LitePal Models

### Entities
- [ ] Create `OutputPlanEntity.java`
- [ ] Create `HistoryEntity.java`
- [ ] Create `UserTagEntity.java`
- [ ] Verify all entities have required annotations
- [ ] Verify all entities have default constructors

### Type Converters
- [ ] Create/update `Converters.java`
- [ ] Implement Map to String conversion
- [ ] Implement String to Map conversion
- [ ] Test type converters with sample data

### DAOs
- [ ] Create `OutputPlanDao.java`
- [ ] Create `HistoryDao.java`
- [ ] Create `UserTagDao.java`
- [ ] Verify all required CRUD operations are implemented
- [ ] Test DAOs with sample data

### Database Class
- [ ] Create/update `AppDatabase.java`
- [ ] Include all entities in @Database annotation
- [ ] Add TypeConverters annotation
- [ ] Implement singleton pattern
- [ ] Test database initialization

## Phase 3: Integration

### Application Initialization
- [ ] Update `MyApplication.java` to initialize Room
- [ ] Remove LitePal initialization
- [ ] Test application starts without errors

### Data Migration
- [ ] Update `MigrationUtil.java` to migrate LitePal data to Room
- [ ] Test migration with sample data
- [ ] Verify data integrity after migration
- [ ] Handle migration errors gracefully

## Phase 4: Update DatabaseManager

### Replace Direct SQLite with Room
- [ ] Update DatabaseManager to use Room for book data
- [ ] Update DatabaseManager to use Room for plan data
- [ ] Update DatabaseManager to use Room for history data
- [ ] Test all DatabaseManager operations

## Phase 5: Code Migration

### Find and Replace LitePal Usage
For each class that uses LitePal:

- [ ] Replace `LitePal.findAll()` with Room DAO queries
- [ ] Replace `object.save()` with Room DAO insert operations
- [ ] Replace `object.update()` with Room DAO update operations
- [ ] Replace `LitePal.deleteAll()` with Room DAO delete operations
- [ ] Update imports to use Room components
- [ ] Test functionality after each class update

### Replace Direct SQLite Usage
For each class that uses DatabaseManager directly:

- [ ] Replace direct SQLite operations with Room DAO queries
- [ ] Update imports to use Room components
- [ ] Test functionality after each class update

### Specific Classes to Update
- [ ] `PlansManagerActivity.java`
- [ ] `PlanEditorActivity.java`
- [ ] `PopupActivity.java`
- [ ] `StatActivity.java`
- [ ] `MigrationUtil.java`
- [ ] Any other classes that use LitePal models
- [ ] All classes that use DatabaseManager for direct SQLite operations

## Phase 6: Testing

### Unit Tests
- [ ] Create unit tests for all DAOs
- [ ] Test all CRUD operations
- [ ] Test data migration functionality
- [ ] Verify type converters work correctly

### Integration Tests
- [ ] Test data persistence across application restarts
- [ ] Test data migration from LitePal to Room
- [ ] Test concurrent access to database
- [ ] Test error handling

### UI Tests
- [ ] Test all UI components that display data
- [ ] Test all UI components that modify data
- [ ] Verify no performance regressions
- [ ] Test on different device sizes and orientations

## Phase 7: Cleanup

### Remove LitePal
- [ ] Remove LitePal dependency from `app/build.gradle`
- [ ] Delete `assets/litepal.xml`
- [ ] Remove all LitePal imports
- [ ] Remove unused LitePal model classes (if keeping Room entities separately)
- [ ] Verify app builds successfully

### Clean Up Incomplete Implementations
- [ ] Remove or complete incomplete Room files in `data/quote/`
- [ ] Verify all Room implementations are complete and consistent

### Code Cleanup
- [ ] Remove temporary test code
- [ ] Update documentation
- [ ] Format code according to project standards
- [ ] Run lint checks and fix issues

## Phase 8: Final Verification

### Data Verification
- [ ] Verify all existing data is migrated correctly
- [ ] Verify no data loss during migration
- [ ] Verify new data is saved correctly
- [ ] Verify data is persisted across app restarts

### Performance Verification
- [ ] Compare performance with LitePal version
- [ ] Verify no memory leaks
- [ ] Verify database operations are responsive
- [ ] Test with large datasets

### Compatibility Verification
- [ ] Test on minimum supported Android version
- [ ] Test on latest Android version
- [ ] Test on different device manufacturers
- [ ] Verify backward compatibility if needed

## Post-Migration

- [ ] Update project documentation
- [ ] Update README with new architecture details
- [ ] Create release notes
- [ ] Merge changes to main branch
- [ ] Monitor crash reports after release

## Rollback Plan

If critical issues are found after migration:

- [ ] Revert to previous version with LitePal
- [ ] Preserve Room implementation in separate branch
- [ ] Fix identified issues
- [ ] Attempt migration again after fixes

## Timeline

- **Week 1**: Setup, dependencies, and entity creation for LitePal models
- **Week 2**: DAOs, database class, and type converters
- **Week 3**: Data migration and DatabaseManager updates
- **Week 4**: Code updates and testing
- **Week 5**: Cleanup, optimization, and final verification