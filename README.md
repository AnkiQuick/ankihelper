# AnkiQuicker (formerly Ankihelper/anki划词助手)

AnkiQuicker is an Android application that helps you quickly add vocabulary and sentences to Anki flashcard decks. It allows you to look up words from anywhere on your device and instantly create Anki cards with definitions, examples, and translations.

## About This Fork

This application was originally developed by [mmjang](https://github.com/mmjang) as "Ankihelper" (anki划词助手). Since 2021, the original development has been inactive. This fork continues active maintenance and development.

**Package Rename**: The application has been renamed from `com.mmjang.ankihelper` to `com.lmyby.ankiquicker` to reflect new ownership and continued development.

### Maintainer
- **Jennings Liu** (2024-present)

## Features

### Core Features
- **Quick Word Lookup**: Tap any word to instantly look it up in built-in dictionaries
- **Anki Integration**: Seamlessly add cards directly to AnkiDroid
- **Multiple Dictionaries**:
  - Oxford Advanced Learner's Dictionary (10th Edition)
  - Merriam-Webster Advanced Learner's Dictionary
  - Cambridge English-Chinese Dictionary
  - AI-powered dictionaries (via LLM APIs)
- **Clipboard Monitoring**: Automatically detect and look up copied text
- **EPUB Reader**: Read ebooks with integrated dictionary lookup
- **Customizable Plans**: Create multiple card templates for different learning needs
- **Multi-language Support**: English and Chinese UI

### AI Features (New!)
- **AI Dictionary**: Get word definitions using Large Language Models when built-in dictionaries don't have the word
- **AI Translation**: Translate sentences using customizable AI translators
- **TTS Support**: Text-to-speech pronunciation for words and sentences
- **Configurable LLM Providers**: Support for OpenAI, DeepSeek, Aliyun, and custom API endpoints


## Requirements

- AnkiDroid installed and configured
- Storage permissions for database and media files


### Basic Workflow
1. **Setup**: Create a plan in Plan Manager, selecting:
   - Dictionary (Oxford, Cambridge, AI, etc.)
   - Anki deck
   - Card template
   - Field mappings

2. **Look up words**:
   - Copy text from any app (if clipboard monitoring is enabled)
   - Or use the floating popup to select text

3. **Add to Anki**: Review the definition and tap "Add" to create an Anki card

### AI Configuration
1. Go to **AI Manager** from the main menu
2. Configure **LLM** settings (API endpoint, token, model)
3. Set up **AI Dictionary** or **AI Translator** with custom prompts
4. Select AI as your dictionary in Plan Manager

## Development

### Project Structure
```
app/src/main/java/com/lmyby/ankiquicker/
├── data/          # Data models, database, repositories
│   ├── ai/        # AI configuration and cache
│   ├── dict/      # Dictionary implementations
│   ├── plan/      # Plan management
│   └── database/  # Room database
├── ui/            # Activities and UI components
│   ├── plan/      # Plan management UI
│   ├── ai/        # AI configuration UI
│   └── popup/     # Word lookup popup
└── util/          # Utility classes
```

### Building from Source
```bash
git clone https://github.com/AnkiQuick/ankihelper.git
cd ankihelper
./gradlew assembleDebug
```

### Tech Stack
- **Language**: Kotlin 1.9+
- **Build**: Gradle 8.6
- **Database**: Room 2.6.1
- **UI**: Material Components, AndroidX
- **Async**: Kotlin Coroutines
- **Linting**: Detekt

## Roadmap

### Completed ✅
- [x] Update deprecated packages
- [x] Migrate from Java to Kotlin (100% complete)
- [x] Overhaul UI to meet modern Material Design
- [x] Add AI dictionary features
- [x] Add AI translation features
- [x] Add TTS features
- [x] Implement proper internationalization (i18n)
- [x] Migrate to Room database
- [x] Package rename to com.lmyby.ankiquicker

### In Progress 🚧
- [ ] Improve AI prompt customization
- [ ] Add more dictionary sources
- [ ] Performance optimizations

### Planned 📋
- [ ] Dark mode improvements
- [ ] Widget support
- [ ] Export/import settings
- [ ] More language support (French, German, Japanese)

## Contributing

Contributions are welcome! Please feel free to submit issues or pull requests.

### Development Guidelines
- Follow Kotlin coding conventions
- Run detekt linting before committing
- Write descriptive commit messages
- Test on multiple Android versions

## License

GPL-3.0 License

## Acknowledgments

- **mmjang** (2017-2021) - Original creator and developer

## Links

- [Original Project](https://github.com/mmjang/ankihelper)
- [AnkiDroid](https://github.com/ankidroid/Anki-Android)
- [Issue Tracker](https://github.com/AnkiQuick/ankihelper/issues)

---

**Note**: This is an independent fork and is not officially affiliated with the original Ankihelper project or AnkiDroid.
