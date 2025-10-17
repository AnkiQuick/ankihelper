# Project Context

## Purpose
AnkiQuicker (formerly Ankihelper/anki划词助手) is an Android application that helps users quickly add vocabulary and sentences to Anki flashcard decks. It enables users to look up words from anywhere on their device and instantly create Anki cards with definitions, examples, and translations.

### Key Goals
- Seamless integration with AnkiDroid for vocabulary learning
- Quick word lookup with built-in and AI-powered dictionaries
- Flexible card creation with customizable plans and field mappings
- Multi-language support for learners of various languages
- Modern, maintainable codebase (100% Kotlin)

### Project History
- **Original Development**: 2017-2021 by mmjang
- **Current Maintenance**: 2024-present by Jennings Liu
- **Package Rename**: From `com.mmjang.ankihelper` to `com.lmyby.ankiquicker`
- **Major Milestone**: 100% Kotlin migration completed (Phase 1-15)

## Tech Stack

### Core Technologies
- **Language**: Kotlin 1.9+ (100% Kotlin, fully migrated from Java)
- **Build System**: Gradle 8.6
- **Minimum SDK**: Android 5.0 (API 21)
- **Target SDK**: Android 14 (API 34)

### Android Framework
- **UI**: Material Components, AndroidX libraries
- **Database**: Room 2.6.1 (SQLite abstraction)
- **Async**: Kotlin Coroutines
- **Architecture**: MVVM-like patterns with Repository layer

### Data Storage
- **Room Database**: Version 7
  - Plans, dictionaries, AI configurations
  - Field mappings, cache data
- **SharedPreferences**: User settings, theme preferences
- **External Storage**: Dictionary databases, media files (images, audio)

### External Dependencies
- **AnkiDroid API**: For creating flashcards
- **LLM APIs**: OpenAI, DeepSeek, Aliyun (configurable)
- **Built-in Dictionaries**:
  - Oxford Advanced Learner's Dictionary (10th Edition)
  - Merriam-Webster Advanced Learner's Dictionary
  - Cambridge English-Chinese Dictionary

### Development Tools
- **Linting**: Detekt (Kotlin static analysis)
- **Version Control**: Git
- **CI/CD**: GitHub Actions (planned)
- **AI-Assisted Development**: Claude Code, Gemini Code, Qwen Code

## Project Conventions

### Code Style
- **Language**: Kotlin following official Kotlin coding conventions
- **Formatting**:
  - 4-space indentation
  - Max line length: 120 characters
  - Max method length: 60 lines
- **Naming Conventions**:
  - Classes: PascalCase (e.g., `AIDictionaryService`)
  - Functions: camelCase (e.g., `getWordDefinition`)
  - Constants: SCREAMING_SNAKE_CASE (e.g., `MAX_CACHE_SIZE`)
  - Private fields: camelCase with underscore prefix (e.g., `_cachedData`)
- **Imports**: Alphabetically ordered, no wildcard imports
- **Null Safety**: Leverage Kotlin's null safety features
- **Data Classes**: Prefer `data class` for models
- **Companion Objects**: For constants and static factories

### Architecture Patterns

#### Layer Structure
```
app/src/main/java/com/lmyby/ankiquicker/
├── data/          # Data layer
│   ├── ai/        # AI configuration and services
│   ├── dict/      # Dictionary implementations
│   ├── plan/      # Plan management
│   └── database/  # Room database
├── ui/            # Presentation layer
│   ├── plan/      # Plan management UI
│   ├── ai/        # AI configuration UI
│   └── popup/     # Word lookup popup
└── util/          # Utility classes
```

#### Patterns Used
- **Repository Pattern**: Data access abstraction (e.g., `AIConfigRepository`, `AICacheRepository`)
- **Factory Pattern**: Dictionary and configuration creation
- **Singleton Pattern**: Managers and services (using companion object)
- **Observer Pattern**: SharedPreferences listeners for settings
- **Strategy Pattern**: Different dictionary implementations

#### Key Design Decisions
- **Room over SQLite**: Type-safe database access with compile-time verification
- **Kotlin Coroutines**: For async operations instead of RxJava
- **External Storage**: Large dictionaries stored outside app data for efficiency
- **Language-Neutral IDs**: Field mappings use IDs instead of display names for i18n

### Testing Strategy

#### Current State
- Manual testing on physical devices
- Build verification for each commit
- Detekt static analysis for code quality

#### Testing Requirements
- **Linting**: All Detekt checks must pass
- **Build**: Must compile without errors or warnings
- **Code Quality**:
  - Cyclomatic complexity ≤ 15
  - Method length ≤ 60 lines
  - No deprecated API usage

#### Future Testing Plans
- Unit tests for service layer (AI, dictionary services)
- Integration tests for database operations
- UI tests for critical user flows
- Test coverage target: 60% for new code

### Git Workflow

#### Branch Strategy
- **Main Branch**: `dev` (active development)
- **Feature Branches**: Descriptive names (e.g., `package-rename-to-lmyby`, `kotlin-migration-phase-15`)
- **Release Branches**: Version-tagged (e.g., `v2.0.0`)

#### Commit Conventions
- **Format**: `<type>(<scope>): <subject>`
- **Types**:
  - `feat`: New feature
  - `fix`: Bug fix
  - `refactor`: Code refactoring
  - `style`: Formatting, linting fixes
  - `docs`: Documentation changes
  - `chore`: Build, dependencies
  - `test`: Adding tests
- **Footer**:
  ```
  🤖 Generated with [Claude Code](https://claude.com/claude-code)

  Co-Authored-By: Claude <noreply@anthropic.com>
  ```
- **Examples**:
  - `feat: add multi-language support for AI dictionary prompts`
  - `fix: launch main activity when continuing from migration screen`
  - `refactor: extract methods from getWordDefinition to reduce complexity`

#### Pull Request Guidelines
- Use `gh pr create` for creating PRs
- Include summary, test plan, and generated notice
- Ensure all Detekt checks pass
- Squash commits when merging if needed

## Domain Context

### Anki & Flashcard Learning
- **Anki**: Spaced repetition flashcard software
- **AnkiDroid**: Android version of Anki
- **Note**: A flashcard entry with multiple fields
- **Deck**: Collection of notes/cards
- **Model/Note Type**: Template defining card structure and fields

### Language Learning Concepts
- **Headword**: The main word being defined
- **Part of Speech (POS)**: Grammatical category (noun, verb, etc.)
- **Phonetics**: Pronunciation notation (IPA format)
- **Example Sentence**: Contextual usage of the word
- **Cloze Deletion**: Fill-in-the-blank format (e.g., "I {{c1::run}} every day")

### AI/LLM Integration
- **System Prompt**: Instructions for AI behavior and output format
- **User Prompt**: Specific query or task for the AI
- **LLM Config**: API endpoint, token, model selection
- **AI Dictionary**: AI-powered word definitions when built-in dictionaries lack entries
- **AI Translator**: AI-powered sentence translation
- **Cache**: Storing AI responses to reduce API calls and costs

### Field Mapping System
- **Plan**: Configuration linking dictionary, Anki deck, and field mappings
- **Field Element**: Source of data (word, definition, example, etc.)
- **Field Mapping**: Which field element goes into which Anki field
- **Export Element**: Processed version of field element (bold, cloze, etc.)

## Important Constraints

### Technical Constraints
1. **Android Version**: Must support Android 5.0+ (API 21+)
2. **Kotlin Version**: 1.9+ (Kapt doesn't support 2.0+ yet)
3. **Method Length**: Maximum 60 lines (Detekt rule)
4. **Cyclomatic Complexity**: Maximum 15 (Detekt rule)
5. **Line Length**: Maximum 120 characters
6. **Database Migration**: Must maintain compatibility with existing Room database version 7

### Business Constraints
1. **Backward Compatibility**: Must not break existing user configurations
2. **Performance**: Dictionary lookup must be fast (<500ms for built-in dictionaries)
3. **Storage**: Large dictionaries (100MB+) must be stored on external storage
4. **Offline Mode**: Built-in dictionaries must work without internet

### Regulatory Constraints
1. **GPL-3.0 License**: Open source under GPL-3.0
2. **Privacy**: No user data collection without explicit consent
3. **API Keys**: User-provided LLM API keys, not bundled in app

### Resource Constraints
1. **Memory**: Efficient memory usage for low-end devices
2. **Storage**: Minimize app size, use external storage for large files
3. **Network**: Minimize API calls through caching
4. **Battery**: Avoid background services, use efficient async patterns

## External Dependencies

### AnkiDroid Integration
- **Package**: `com.ichi2.anki`
- **API Version**: AnkiDroid 2.15+
- **Permissions Required**:
  - `com.ichi2.anki.permission.READ_WRITE_DATABASE`
- **Key Operations**:
  - Read decks and models
  - Create/update notes
  - Add media files (audio, images)

### LLM API Providers

#### OpenAI
- **Endpoint**: `https://api.openai.com/v1/chat/completions`
- **Models**: GPT-3.5, GPT-4, etc.
- **Authentication**: Bearer token

#### DeepSeek
- **Endpoint**: `https://api.deepseek.com/v1/chat/completions`
- **Models**: DeepSeek-V2, DeepSeek-Coder
- **Authentication**: Bearer token

#### Aliyun (Alibaba Cloud)
- **Endpoint**: Custom endpoint per region
- **Models**: Qwen series
- **Authentication**: API key

#### Custom Providers
- **Endpoint**: User-configurable
- **Format**: OpenAI-compatible API format
- **Examples**: Local LLMs (Ollama, LM Studio)

### Dictionary Sources
- **Format**: SQLite databases
- **Size**: 50-200MB per dictionary
- **Source**: https://forum.freemdict.com
- **License**: Various (check individual dictionaries)

### External Storage
- **Permission**: `MANAGE_EXTERNAL_STORAGE` (Android 11+)
- **Path**: `/storage/emulated/0/Android/data/com.lmyby.ankiquicker/`
- **Structure**:
  ```
  ankiquicker/
  ├── databases/       # Dictionary .db files
  ├── media/
  │   ├── images/     # Card images
  │   └── audio/      # Pronunciation audio
  └── cache/          # Temporary files
  ```

## Additional Notes

### Internationalization (i18n)
- **Supported Languages**: English, Chinese (Simplified)
- **Implementation**: XML string resources
- **Language-Neutral IDs**: Used for field mappings to support language switching

### Theme System
- **Themes**: Default, Pink, E-ink, Dark
- **Storage**: SharedPreferences
- **Application**: Activity-level theme switching

### AI Features Evolution
- **Phase 1**: Basic AI dictionary integration
- **Phase 2**: AI translator support
- **Phase 3**: TTS (Text-to-Speech) integration
- **Current**: Multi-language support (12 languages)

### Migration History
- **Java to Kotlin**: 15-phase migration completed
- **Package Rename**: From `com.mmjang.ankihelper` to `com.lmyby.ankiquicker`
- **Storage Migration**: Internal to external storage
- **Room Migration**: SQLite to Room database

### Documentation
- **Specifications**: `/specification/` directory
- **README**: Project overview and setup instructions
- **Code Comments**: KDoc format for public APIs
- **Commit Messages**: Detailed descriptions with context
