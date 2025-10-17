# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

<!-- OPENSPEC:START -->
# OpenSpec Instructions

These instructions are for AI assistants working in this project.

Always open `@/openspec/AGENTS.md` when the request:
- Mentions planning or proposals (words like proposal, spec, change, plan)
- Introduces new capabilities, breaking changes, architecture shifts, or big performance/security work
- Sounds ambiguous and you need the authoritative spec before coding

Use `@/openspec/AGENTS.md` to learn:
- How to create and apply change proposals
- Spec format and conventions
- Project structure and guidelines

Keep this managed block so 'openspec update' can refresh the instructions.

<!-- OPENSPEC:END -->

---
## ⚠️ ABSOLUTE FIRST PRIORITY - MUST FOLLOW BEFORE ALL OTHER RULES ⚠️

### 🚨 PARALLEL TOOL EXECUTION LIMIT 🚨

**MANDATORY RULE**: Execute a **MAXIMUM OF 2 TOOLS IN PARALLEL** at any time.

**This rule MUST be followed as the FIRST PRIORITY** to prevent resource consumption.

- ✅ **ALLOWED**: Running 2 independent tools in a single message
- ❌ **NOT ALLOWED**: Running 3 or more tools in parallel
- ❌ **NOT ALLOWED**: Running multiple heavy operations simultaneously (builds, large file searches, etc.)

**Examples**:
- Good: `git status` + `git diff`
- Bad: `gradle build` + `Grep` + `Read`

**This rule takes precedence over all project-specific rules.**

---

## Project Overview

AnkiQuicker (formerly Ankihelper) is an Android app for quickly adding vocabulary to Anki flashcard decks. It integrates with AnkiDroid, supports built-in dictionaries (Oxford, Cambridge, Merriam-Webster) and AI-powered dictionary/translation via LLM APIs (OpenAI, DeepSeek, Aliyun).

**Key Context**:
- 100% Kotlin codebase (fully migrated from Java)
- Package: `com.lmyby.ankiquicker` (renamed from `com.mmjang.ankihelper`)
- Room database version 7 (SQLite abstraction)
- External storage for large dictionary databases (100MB+)
- Support for Android 5.0+ (API 21+), target Android 14 (API 34)

## Build & Development Commands

### Building
```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Clean build
./gradlew clean assembleDebug
```

### Code Quality
```bash
# Run Detekt linting (REQUIRED before commits)
./gradlew detekt

# Run ktlint code style check
./gradlew ktlint

# Auto-fix ktlint violations
./gradlew ktlintFormat
```

### Installation
```bash
# Install debug APK to connected device
./gradlew installDebug

# Uninstall from device
adb uninstall com.lmyby.ankiquicker
```

### Environment Setup
```bash
# Set Java home for Gradle (if needed)
export JAVA_HOME=/path/to/jdk-17
export ANDROID_HOME=/path/to/android-sdk

# Gradle daemon (already configured in project)
./gradlew --daemon
```

**Important**: Always run `./gradlew detekt` before committing. All Detekt checks must pass.

## Architecture

### High-Level Structure

The codebase follows a layered architecture with clear separation between data, business logic, and UI:

```
app/src/main/java/com/lmyby/ankiquicker/
├── data/              # Data layer (Room, repositories, services)
│   ├── ai/            # AI configuration, services, caching
│   │   ├── service/   # AIDictionaryService, AITranslatorService
│   │   └── cache/     # AI response caching (reduces LLM API calls)
│   ├── dict/          # Dictionary implementations (built-in + AI)
│   │   └── DictFieldElement.kt  # Dictionary-specific field definitions
│   ├── plan/          # Plan management (links dictionary, deck, fields)
│   └── database/      # Room database entities and DAOs
├── ui/                # Presentation layer (Activities)
│   ├── plan/          # Plan CRUD operations
│   ├── ai/            # AI configuration UI
│   └── popup/         # Word lookup popup (main user interaction)
└── util/              # Utility classes (storage, constants, helpers)
```

### Key Architectural Patterns

#### 1. **Plan System** (Core Abstraction)
A **Plan** links three components:
- Dictionary (built-in or AI)
- AnkiDroid deck
- Field mappings (which dictionary field → which Anki field)

This allows users to create multiple workflows (e.g., English→Chinese vocab, Japanese→English, etc.).

#### 2. **Repository Pattern**
All data access goes through repositories:
- `AIConfigRepository`: AI dictionary/translator configurations
- `AICacheRepository`: Caching LLM responses
- Room DAOs: Database access

**Important**: Never bypass repositories to access data directly.

#### 3. **Dictionary Strategy Pattern**
Different dictionary implementations (Oxford, Cambridge, AI) all implement `IDictionary` interface. The system selects the appropriate dictionary based on the active Plan.

#### 4. **Field Mapping System**
**Critical for i18n**: Field mappings use **language-neutral IDs** (e.g., `field_word`, `dict_field_phonetic`) instead of display names. This allows the UI language to change without breaking saved configurations.

- `FieldElement`: Shared field elements (example, notes, URL, etc.)
- `DictFieldElement`: Dictionary-specific fields (word, phonetic, definition)

When saving Plans, **always convert display names to IDs**. When loading Plans, **convert IDs back to display names** for the current UI language.

#### 5. **AI Service Architecture**
AI services (`AIDictionaryService`, `AITranslatorService`) follow this pattern:
1. Check cache first (keyed by: text + language pair + LLM config ID)
2. If cache miss, build prompts with configured languages
3. Call LLM API via `AIService`
4. Parse JSON response (with fallback for non-JSON responses)
5. Cache results for future use

**Multi-language support**: Prompts dynamically use `sourceLanguage` and `targetLanguage` from config. Language codes (en, zh, ja) are converted to full names (English, Chinese, Japanese) for better LLM understanding.

#### 6. **External Storage Pattern**
Large files (dictionary databases 50-200MB, media files) are stored in external storage:
```
/storage/emulated/0/Android/data/com.lmyby.ankiquicker/
├── databases/       # Dictionary .db files
├── media/
│   ├── images/     # Card images
│   └── audio/      # Pronunciation audio
└── cache/          # Temporary files
```

The `StorageManager` class handles migration from internal to external storage.

### Critical Design Decisions

1. **Room over raw SQLite**: Compile-time SQL verification, type safety, null safety
2. **Kotlin Coroutines over RxJava**: Simpler async patterns, better Kotlin integration
3. **External Storage**: Required for large dictionaries (100MB+)
4. **Language-Neutral IDs**: Enables UI language switching without data migration
5. **AI Caching**: Reduces LLM API costs and improves responsiveness

## Code Style & Quality Rules

### Detekt Compliance (MANDATORY)
- **Max Method Length**: 60 lines
- **Max Cyclomatic Complexity**: 15
- **Max Line Length**: 120 characters
- **Import Ordering**: Alphabetically ordered, no wildcards

**Common Violations & Fixes**:
- **LongMethod**: Extract helper methods with single responsibilities
- **CyclomaticComplexMethod**: Replace large `when` expressions with map lookups
- **MaxLineLength**: Break long strings into multi-line concatenations
- **ArgumentListWrapping**: Put each argument on separate line if doesn't fit

### Kotlin Conventions
- Use `data class` for models
- Use `companion object` for constants and static factories
- Prefer Kotlin null safety over `!!` or manual null checks
- Use Kotlin Coroutines for async operations
- Leverage extension functions for utility methods

### Naming Patterns
- Repository classes: `*Repository` (e.g., `AIConfigRepository`)
- Service classes: `*Service` (e.g., `AIDictionaryService`)
- Manager classes: `*Manager` (e.g., `StorageManager`, `AIManager`)
- DAO interfaces: `*Dao` (e.g., `AIDictionaryConfigDao`)
- Activities: `*Activity` (e.g., `PlanEditorActivity`)

## Domain-Specific Knowledge

### Anki Integration
The app interacts with AnkiDroid via content provider:
- Requires permission: `com.ichi2.anki.permission.READ_WRITE_DATABASE`
- Reads decks and note types (models)
- Creates/updates notes with field mappings
- Adds media files (images, audio) to Anki collection

**Important**: Always check AnkiDroid availability before operations. Show helpful error messages if AnkiDroid is not installed or permission not granted.

### Dictionary Data Flow
1. User selects text or copies to clipboard
2. System looks up word in active Plan's dictionary
3. Dictionary returns structured data (headword, phonetics, definitions, examples)
4. System maps dictionary fields to Anki fields based on Plan's field mappings
5. User reviews and adds to AnkiDroid

### AI Dictionary/Translator Flow
1. Check `AICacheRepository` for cached response
2. If cache miss, get `AIDictionaryConfig` or `AITranslatorConfig`
3. Get associated `LLMConfig` (API endpoint, token, model)
4. Convert language codes to full names (e.g., "en" → "English")
5. Build system and user prompts with language parameters
6. Call LLM API via `AIService.callLLM()`
7. Parse JSON response (with markdown cleanup and fallbacks)
8. Cache results with timestamp

**Prompt Engineering**: Prompts explicitly specify source/target languages and required JSON format. Responses are parsed flexibly to handle various LLM output formats.

## Important Constraints

### Technical Constraints
1. **Android 5.0+ (API 21)** minimum support
2. **Kotlin 1.9+** (Kapt doesn't support 2.0+ yet)
3. **Room database version 7** - maintain migration compatibility
4. **Method length ≤ 60 lines** (Detekt rule)
5. **Cyclomatic complexity ≤ 15** (Detekt rule)

### Business Constraints
1. **Backward Compatibility**: Never break existing user configurations
2. **Performance**: Dictionary lookup < 500ms for built-in dictionaries
3. **Offline Mode**: Built-in dictionaries must work without internet
4. **Storage Efficiency**: Large files on external storage only

### Regulatory Constraints
1. **GPL-3.0 License**: Open source, must preserve license headers
2. **Privacy**: No data collection without consent
3. **API Keys**: User-provided only, never bundled in app

## Commit Conventions

**Format**: `<type>(<scope>): <subject>`

**Types**:
- `feat`: New feature
- `fix`: Bug fix
- `refactor`: Code refactoring (no behavior change)
- `style`: Formatting, linting fixes
- `docs`: Documentation only
- `chore`: Build, dependencies
- `test`: Adding tests

**Footer** (always include):
```
🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
```

**Examples**:
```
feat: add multi-language support for AI dictionary prompts
fix: launch main activity when continuing from migration screen
refactor: extract methods from getWordDefinition to reduce complexity
```

## Pull Request Process

1. Use `gh pr create` for creating PRs
2. Include summary of changes and test plan
3. Ensure all Detekt checks pass: `./gradlew detekt`
4. Squash commits when merging if needed
5. Target `dev` branch for active development

## Common Gotchas

### 1. Field Mapping Language Switching
**Problem**: Field mappings saved with Chinese display names break when switching UI to English.

**Solution**: Always use language-neutral IDs:
```kotlin
// WRONG: Save display name directly
plan.fieldsMap["Word"] = "单词"

// CORRECT: Convert to ID before saving
val id = DictFieldElement.displayNameToId("Word", context)
plan.fieldsMap["Word"] = id

// CORRECT: Convert ID back to display name when loading
val displayName = DictFieldElement.idToDisplayName(id, context)
```

### 2. AI Language Configuration
**Problem**: LLM receives language codes (en, zh) instead of full names, causing confusion.

**Solution**: Always convert codes to full names before building prompts:
```kotlin
val sourceLanguageName = getLanguageName(config.sourceLanguage ?: "en")
val systemMessage = "Provide definitions in $sourceLanguageName..."
```

### 3. External Storage Migration
**Problem**: App fails to access dictionaries after Android 11+ storage changes.

**Solution**: Check `StorageMigrationActivity` - handles permission requests and migration from internal to external storage. Don't bypass this flow.

### 4. Room Database Migrations
**Problem**: Schema changes require migration logic.

**Solution**: Always increment database version in `AppDatabase` and provide migration strategy. Room will error at runtime if migration is missing.

### 5. Detekt Method Length
**Problem**: Long methods exceed 60-line limit.

**Solution**: Extract logical blocks into private helper methods with clear names:
```kotlin
// Extract caching logic
private fun checkCache(word: String, llmConfigId: Long): List<AIDictionaryCache>?

// Extract prompt building
private fun buildDictionaryPrompts(word: String, sourceLang: String, targetLang: String): Pair<String, String>
```

## Testing Strategy

**Current**: Manual testing + Detekt linting

**Before Committing**:
1. Build: `./gradlew assembleDebug`
2. Lint: `./gradlew detekt` (must pass)
3. Manual test on device (if UI changes)

**Future** (in development):
- Unit tests for service layer
- Integration tests for database operations
- UI tests for critical flows

## Documentation

- **Specifications**: `/specification/` directory contains detailed specs for major features
- **openspec/project.md**: Complete project context for AI assistants
- **README.md**: User-facing project overview
- **Code Comments**: Use KDoc format for public APIs

When making significant changes, update specifications in `/specification/` directory.
