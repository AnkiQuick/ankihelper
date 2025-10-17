## 1. Update Layout File
- [x] 1.1 Change icon in activity_ai_config.xml line 77 from `@android:drawable/ic_menu_delete` to `@drawable/ic_settings_outline`
- [x] 1.2 Update string reference from `@string/history_management` to `@string/history_manager`

## 2. Update String Resources
- [x] 2.1 Rename string resource key from `history_management` to `history_manager` in values/strings.xml
- [x] 2.2 Change English string text from "History Management" to "History Manager"
- [x] 2.3 Rename string resource key from `history_management` to `history_manager` in values-zh/strings.xml
- [x] 2.4 Change Chinese string text from "历史记录管理" to "历史记录管理器"

## 3. Verification
- [x] 3.1 Build project: `./gradlew assembleDebug`
- [ ] 3.2 Visual verification: Launch app → AI Manager → verify History Manager button matches other buttons
- [x] 3.3 Run detekt: `./gradlew detekt`
