## ADDED Requirements

### Requirement: AI Manager UI Consistency
All management buttons in the AI Manager screen SHALL maintain consistent visual styling and labeling patterns to provide a cohesive user experience.

#### Scenario: History Manager button matches other manager buttons
- **GIVEN** the user opens the AI Manager screen
- **WHEN** the user views the list of management buttons
- **THEN** all manager buttons SHALL use the same icon (`ic_settings_outline`)
- **AND** all manager buttons SHALL follow the naming pattern "X Manager"
- **AND** the History Manager button SHALL be visually indistinguishable from LLM Manager, AI Dictionary Manager, AI Translator Manager, and TTS Manager buttons in terms of icon and layout

#### Scenario: History Manager button label is consistent
- **GIVEN** the user opens the AI Manager screen
- **WHEN** the user views the History button label
- **THEN** the label SHALL display "History Manager" in English
- **AND** the label SHALL display "历史记录管理器" in Chinese
- **AND** the label SHALL NOT contain the word "Management" or "管理" without the "er" suffix ("器")

#### Scenario: History Manager icon is consistent
- **GIVEN** the user opens the AI Manager screen
- **WHEN** the user views the History Manager button icon
- **THEN** the icon SHALL be `ic_settings_outline` (settings/gear icon)
- **AND** the icon SHALL NOT be a delete/removal icon (`ic_menu_delete`)
- **AND** the icon SHALL be positioned and sized identically to icons on other manager buttons
