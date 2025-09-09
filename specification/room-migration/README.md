# Room Migration Project

This project aims to migrate the AnkiHelper application from LitePal to Room for data persistence.

## Documents

1. [Room Migration Specification](room-migration-spec.md) - Complete specification including overview, implementation plan, and step-by-step guide
2. [Migration Checklist](migration-checklist.md) - Detailed checklist to ensure all steps are completed
3. [Critical Improvements](room-migration-improvements.md) - Analysis of gaps and comprehensive improvement plan

## Current State Analysis

### Room Implementation Status
The application already has a mixed persistence implementation:

1. **Fully implemented with Room** - `data/dict/` directory contains complete Room implementations for dictionary data:
   - Entities with `@Entity` annotations
   - DAOs with `@Dao` annotations  
   - Database classes with `@Database` annotations

2. **Using LitePal** - Main application models that need migration:
   - `data/plan/OutputPlan.java`
   - `data/history/History.java`
   - `data/model/UserTag.java`

3. **Using direct SQLite** - Managed by `DatabaseManager.java`:
   - Book data (`data/book/`)
   - Plan data (OutputPlan)
   - History data
   - Other application data

4. **Incomplete/Unused**:
   - `data/quote/` - Contains empty/incomplete Room files
   - `data/content/` - Simple POJOs without persistence
   - `data/read/` - Simple POJOs without persistence

### Migration Scope
The Room migration focuses on replacing LitePal and direct SQLite usage with Room for the main application models:

1. **Primary targets** (currently using LitePal):
   - `OutputPlan` - Configuration for dictionary output plans
   - `History` - Record of user lookups and translations  
   - `UserTag` - User-defined tags for organizing content

2. **Secondary targets** (currently using direct SQLite):
   - Book data
   - Other data managed by `DatabaseManager`

3. **Already Room-implemented** (no changes needed):
   - All dictionary data in `data/dict/`

## Benefits

- Compile-time verification of SQL queries
- Better performance with optimized query execution
- Improved threading model with built-in background thread support
- Better integration with Android Architecture Components
- Reduced boilerplate code with annotation processing
- Consistent persistence approach across the application

## Status

- [ ] Phase 1: Add Room dependencies and create entities for LitePal models
- [ ] Phase 2: Create DAOs and database class
- [ ] Phase 3: Implement data migration from LitePal to Room
- [ ] Phase 4: Update DatabaseManager to use Room instead of direct SQLite
- [ ] Phase 5: Update application code to use Room instead of LitePal
- [ ] Phase 6: Remove LitePal dependencies and test
- [ ] Phase 7: Clean up incomplete Room implementations

## Migration Approach

We recommend following the [Room Migration Specification](room-migration-spec.md) for the actual implementation. The guide provides practical instructions for each phase of the migration.