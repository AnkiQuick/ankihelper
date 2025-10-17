# Standardize History Manager UI

## Why

In the AI Manager screen (`activity_ai_config.xml`), the History Management button has inconsistent styling compared to other manager buttons:

1. **Icon Inconsistency**: Uses `@android:drawable/ic_menu_delete` (a removal/delete icon) while all other buttons use `@drawable/ic_settings_outline` (settings/gear icon)
2. **Label Inconsistency**: Uses "History Management" while other buttons follow the pattern "X Manager" (e.g., "LLM Manager", "AI Dictionary Manager")

This creates visual and textual inconsistency in the UI, making the History button appear out of place and potentially confusing users about its function.

## What Changes

Standardize the History Management button to match the visual and textual patterns of other manager buttons:

1. **Icon**: Change from `@android:drawable/ic_menu_delete` to `@drawable/ic_settings_outline`
2. **Label**: Change from "History Management" to "History Manager"
3. **String Resource Key**: Rename from `history_management` to `history_manager`

This is a cosmetic change with no functional impact - the button will still navigate to the same activity (`CleanAIHistoryActivity`).

## Impact

**Scope**: UI only - minimal impact
- 2 string resources (English + Chinese)
- 1 layout file

**Users**: No behavioral changes, just improved visual consistency
**Data**: No data migration required
**Breaking Changes**: None

## Alternatives Considered

1. **Keep delete icon but change label**: Rejected because the delete icon suggests immediate deletion rather than management
2. **Use a history-specific icon**: Rejected because maintaining visual consistency with other manager buttons is more important than semantic precision

## Dependencies

None - this is an isolated UI change with no dependencies on other features or changes.
