# Popup Window Design Documentation

## Overview
The popup window in `PopupActivity.java` is the main interface for the AnkiHelper application. It allows users to look up definitions, create Anki flashcards, and manage various aspects of word processing. The layout is organized into distinct sections from top to bottom.

## Layout Structure (Top to Bottom)

### 1. Header Section
**Purpose**: Provides navigation and general controls

**Elements**:
- `btn_cancel_blank` (Button): Invisible button that takes up the top portion of the screen. When clicked, it closes the popup.

### 2. Main Content Area
**Purpose**: Contains the primary functionality of the application

#### 2.1 Control Bar
**Elements**:
- `plan_spinner` (Spinner): Allows users to select output plans for creating flashcards
- `language_spinner` (Spinner): Allows users to select pronunciation language
- `btn_pronounce` (ImageButton): Plays pronunciation audio for the text in the search field
  - Icon: `?attr/icon_play` (typically `ic_ali_play.xml` or `ic_ali_play_pink.xml`)
  - Visibility: Hidden by default, becomes visible when there's text in the search field
  - The pronunciation button (btn_pronounce) is defined in the layout with android:visibility="gone" by default
  - The button is only made visible when showPronounce(true) is called
  - showPronounce(true) is only called within the asyncSearch() method
  - asyncSearch() is only triggered when a user explicitly performs a search or when a word is selected from the text


#### 2.2 Text Selection Area
**Purpose**: Displays the processed text with word selection capabilities

**Elements**:
- `bigbang_wrapper` (BigBangLayoutWrapper): Wrapper container for the BigBangLayout
  - `bigbang` (BigBangLayout): Custom layout for word selection and highlighting
    - Individual text items added via `addTextItem()` method

**Text Source**: The text comes from `mTextToProcess` which is populated from various sources:
- Intent extras when the popup is launched from another app
- Clipboard content when using the "Use clipboard content" feature
- Processed text from ACTION_PROCESS_TEXT intents

**Text Processing**:
1. Text is first split into segments using `TextSplitter.getLocalSegments()`
2. Segments are added to the BigBangLayout using `bigBangLayout.addTextItem()`
3. Each segment is displayed as a separate selectable item

**Word Selection Mechanism**:
- Users can tap individual words to select them
- Selected words are visually highlighted using background drawables
- Long press on a word automatically selects it
- Drag selection is also supported for selecting multiple words
- The selected text is automatically populated in the search field (`edit_text_hwd`)

**Customization Options**:
- `setShowSymbol()`: Controls whether symbols are displayed
- `setShowSpace()`: Controls whether spaces are displayed as separate items
- `setShowSection()`: Controls whether sections/paragraphs are visually separated
- `setItemSpace()`: Controls spacing between items
- `setLineSpace()`: Controls spacing between lines
- `setTextSize()`: Controls font size of text items
- `setTextPadding()` and related methods: Control padding around text items

#### 2.3 Translation Card (Optional)
**Purpose**: Displays translated text

**Elements**:
- `cardview_translation` (CardView): Container for translation results (hidden by default)
  - `edittext_translation` (EditText): Displays translated text

**Functionality**:
- When the translate button is clicked, the app checks if translation text is already present
- If not, it triggers `asyncTranslate()` with the current `mTextToProcess`
- Translation results are displayed in the `edittext_translation` field
- The card view becomes visible when translation is complete

#### 2.4 Search Input Area
**Elements**:
- `edit_text_hwd` (AutoCompleteTextView): Input field for entering/searching words with autocomplete functionality
- `btn_search` (Button): Triggers word lookup/search
  - Icon: `?attr/icon_search` (typically `ic_ali_search.xml` or `ic_ali_search_pink.xml`)
- `btn_pronounce` (ImageButton): Plays pronunciation audio for the text in the search field
  - Icon: `?attr/icon_play` (typically `ic_ali_play.xml` or `ic_ali_play_pink.xml`)
  - Visibility: Hidden by default (`android:visibility="gone"`), becomes visible when there's text in the search field
- `progress_bar` (ProgressBar): Shows loading state during searches

**Pronunciation Button Visibility Issue and Fix**:
The pronunciation button (`btn_pronounce`) had a visibility issue where it would only appear after a search was performed, not when text was simply entered into the search box. This was counter-intuitive for users who expected to be able to hear pronunciation as soon as they typed or selected text.

**Original Behavior**:
- The button was initially hidden (`android:visibility="gone"`)
- It was only made visible when `showPronounce(true)` was called
- `showPronounce(true)` was only called within the `asyncSearch()` method
- This meant the button only appeared after an explicit search was triggered

**Root Cause**:
There was no mechanism to monitor text changes in the search box and show/hide the pronunciation button accordingly.

**Fix Implemented**:
Added a `TextWatcher` to the search box (`edit_text_hwd`) that monitors text changes:
- When text is entered, the pronunciation button becomes visible
- When the search box is cleared, the pronunciation button is hidden
- This provides immediate feedback to users that pronunciation is available

**Implementation Details**:
1. Added `TextWatcher` import: `import android.text.TextWatcher;`
2. Attached `TextWatcher` to `act` in the `setEventListener()` method
3. In `afterTextChanged()`, check if search box has text and show/hide button accordingly
4. Call `showPronounce(s.length() > 0)` to control button visibility

#### 2.5 Definition Results Area
**Elements**:
- `view_definition_list` (LinearLayout): Container that holds definition cards
  - Multiple `definition_item` views (see section 2.6)

### 3. Footer Section
**Purpose**: Quick access to common actions

**Elements** (from left to right):
- `footer_scroll_up` (ImageButton): Scrolls the content to the top
  - Icon: `?attr/icon_scroll_up` (typically `ic_ali_arrow_up.xml` or `ic_ali_arrow_up_pink.xml`)
- `footer_translate` (ImageButton): Triggers translation of the selected text
  - Icon: `?attr/icon_translate_normal` (changes based on state)
- `footer_rotate_left` (ImageButton): Switches to the previous output plan
  - Icon: `?attr/icon_left_arrow` (typically `ic_left_arrow.png`)
- `footer_rotate_right` (ImageButton): Switches to the next output plan
  - Icon: `?attr/icon_right_arrow` (typically `ic_right_arrow.png`)
- `footer_note` (ImageButton): Opens dialog to edit notes
  - Icon: `?attr/icon_note` (typically `ic_ali_note.xml` or `ic_ali_note_pink.xml`)
- `footer_tag` (ImageButton): Opens dialog to edit tags
  - Icon: `?attr/icon_tag` (typically `ic_ali_tag.xml` or `ic_ali_tag_pink.xml`)

### 4. Definition Item Layout
**Purpose**: Displays individual definitions

**Elements**:
- `textview_definition` (TextView): Shows the formatted definition text
- `def_img` (ImageView): Displays associated images (hidden by default, shown when available)
- `btn_add_definition_large` (LinearLayout): Container for the add button
  - `btn_add_definition` (ImageButton): Adds the definition as a flashcard to Anki
    - Icon: `?attr/icon_add` (typically `ic_ali_add.xml` or changes to `?attr/icon_add_done` when added)

**Note**: There are two layouts for definition items:
- `definition_item.xml`: Standard layout with add button on the right
- `definition_item_left.xml`: Left-handed layout with add button on the left

### 5. Global Elements
**Elements**:
- `audio_progress` (ProgressBar): Shows progress when downloading audio files
- `scrollView` (ScrollView): Contains the main content area for scrolling

## Icons Summary
| Element | Attribute | Typical Drawable |
|---------|-----------|------------------|
| Pronounce | `?attr/icon_play` | `ic_ali_play.xml` |
| Search | `?attr/icon_search` | `ic_ali_search.xml` |
| Scroll Up | `?attr/icon_scroll_up` | `ic_ali_arrow_up.xml` |
| Translate Normal | `?attr/icon_translate_normal` | `ic_ali_translate.xml` |
| Translate Loading | `?attr/icon_translate_wait` | `ic_ali_wait.xml` |
| Translate Done | `?attr/icon_translate_done` | `icon_translate_done.png` |
| Left Arrow | `?attr/icon_left_arrow` | `ic_left_arrow.png` |
| Right Arrow | `?attr/icon_right_arrow` | `ic_right_arrow.png` |
| Note | `?attr/icon_note` | `ic_ali_note.xml` |
| Tag | `?attr/icon_tag` | `ic_ali_tag.xml` |
| Add Card | `?attr/icon_add` | `ic_ali_add.xml` |
| Add Card Done | `?attr/icon_add_done` | (Dynamically changed when card is added) |

## Detailed Functionality

### Text Selection Area
**How text is populated**:
1. When the popup is launched, `handleIntent()` extracts text from the intent
2. `populateWordSelectBox()` processes the text using `TextSplitter.getLocalSegments()`
3. Each segment is added to the BigBangLayout using `bigBangLayout.addTextItem()`

**Text Splitting Process**:
- The `TextSplitter` class segments text based on language boundaries
- Chinese/English boundaries are separated
- Symbols and spaces are handled according to settings
- HTML tags like `<b>` are converted to special markers

**Selection Mechanism**:
- Users can tap individual words to select/deselect them
- Selected words are visually highlighted
- The combined selected text is automatically placed in the search field
- Long press on a word selects it automatically
- Drag selection allows selecting multiple words at once

### Translation Toggle
**What happens when clicked**:
1. The `mBtnTranslation` click listener checks if translation text is already present
2. If empty, it triggers `asyncTranslate(mTextToProcess)`
3. The button icon changes to a loading state
4. Translation is performed in a background thread
5. Results are displayed in the translation card view

**Translation Process**:
1. `asyncTranslate()` determines source language using `RegexUtil.isChineseSentence()`
2. For Chinese text: translates from "zh" to "en"
3. For non-Chinese text: translates from "auto" to "zh"
4. Uses Baidu Translate API via the `Translator` class
5. Results are processed and displayed in the UI

**Translation Configuration**:
- Language detection is automatic based on content
- Chinese sentences (more than 50% Chinese characters) are translated to English
- All other text is translated to Chinese
- API keys can be customized in app settings

**UI Updates During Translation**:
- Button icon changes through three states: normal → loading → done
- Translation card view becomes visible when results are available
- Progress bar is shown during audio downloads

### Flashcard Creation
**Process**:
1. User selects a definition item
2. Clicks the add button (`btn_add_definition`)
3. Fields are populated based on the output plan configuration
4. Media (images/audio) are downloaded if needed
5. Note is added to Anki via AnkiDroid API

## Functionality Overview
1. **Word Selection**: Users can select words from the BigBangLayout or type them in the search box
2. **Word Lookup**: Definitions are fetched from various dictionaries based on the selected plan
3. **Flashcard Creation**: Users can add definitions as flashcards to Anki with the add button
4. **Translation**: Text can be automatically translated between languages
5. **Note/Tag Management**: Users can add custom notes and tags to flashcards
6. **Plan Switching**: Users can quickly switch between different output plans using the footer arrows

This design provides a comprehensive interface for creating Anki flashcards while maintaining a clean and organized layout.