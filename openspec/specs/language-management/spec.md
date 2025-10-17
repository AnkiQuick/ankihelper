# language-management Specification

## Purpose
TBD - created by archiving change fix-system-language-detection. Update Purpose after archive.
## Requirements
### Requirement: System Language Detection
The app SHALL dynamically detect and apply the device's system language when the user selects the "System" language option. The system language SHALL be queried at runtime rather than cached at initialization to ensure the app reflects the current device locale.

#### Scenario: Switch to system language from explicit language
- **GIVEN** the app language is set to English
- **AND** the device system language is Chinese
- **WHEN** user changes app language setting to "System"
- **THEN** the app SHALL immediately display UI in Chinese
- **AND** the language setting SHALL persist as "System" across app restarts

#### Scenario: System language reflects device changes
- **GIVEN** the app language is set to "System"
- **AND** the device system language is English
- **WHEN** the user changes device system language to Chinese
- **AND** returns to the app
- **THEN** the app SHALL display UI in Chinese without requiring manual language selection

#### Scenario: App language persists after selecting system
- **GIVEN** the user has selected "System" as app language
- **WHEN** the app is restarted
- **THEN** the language setting SHALL remain "System"
- **AND** the app SHALL display in the current device system language

#### Scenario: System locale is not cached
- **GIVEN** AppLanguage.SYSTEM enum is initialized
- **WHEN** `getEffectiveLocale()` is called
- **THEN** it SHALL return the current value of `Locale.getDefault()`
- **AND** it SHALL NOT return a cached locale from initialization time

