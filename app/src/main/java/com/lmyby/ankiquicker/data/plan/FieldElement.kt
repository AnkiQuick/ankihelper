package com.lmyby.ankiquicker.data.plan

import android.content.Context
import com.lmyby.ankiquicker.R

/**
 * Enum representing field export element types with internationalization support
 *
 * This enum provides a mapping between:
 * 1. Language-neutral IDs - Used for database storage (e.g., "field_empty", "field_sentence_translation")
 * 2. Legacy Chinese strings - For backward compatibility with existing saved plans
 * 3. Localized display strings - For UI display in user's language
 *
 * Database Schema Approach:
 * - Instead of storing localized strings, store language-neutral IDs
 * - IDs are stable and don't change with language
 * - UI retrieves localized display name based on ID
 *
 * Migration Strategy:
 * - Phase 1: Code uses enum for comparisons, database still uses legacy Chinese strings
 * - Phase 2: Database migration converts Chinese strings to language-neutral IDs
 * - Phase 3: UI displays localized strings based on IDs
 *
 * Created as part of field element internationalization migration
 */
enum class FieldElement(
    val id: String,                 // Language-neutral ID for database storage
    val legacyValue: String,        // Legacy Chinese string for backward compatibility
    val displayNameResId: Int       // String resource ID for localized display
) {
    /**
     * Empty field - no content exported
     */
    EMPTY("field_empty", "空", R.string.field_element_empty),

    /**
     * Example sentence from dictionary
     */
    EXAMPLE("field_example", "例句", R.string.field_element_example),

    /**
     * Example sentence with bold formatting
     */
    BOLD_EXAMPLE("field_bold_example", "加粗的例句", R.string.field_element_bold_example),

    /**
     * Cloze deletion example sentence
     */
    CLOZE_EXAMPLE("field_cloze_example", "挖空的例句", R.string.field_element_cloze_example),

    /**
     * Cloze deletion example in full C1 mode
     */
    CLOZE_EXAMPLE_C1("field_cloze_c1", "挖空的例句（全c1模式）", R.string.field_element_cloze_c1),

    /**
     * User notes
     */
    NOTES("field_notes", "笔记", R.string.field_element_notes),

    /**
     * Source URL
     */
    URL("field_url", "URL", R.string.field_element_url),

    /**
     * All dictionary definitions combined
     */
    ALL_DEFINITIONS("field_all_definitions", "全部释义", R.string.field_element_all_definitions),

    /**
     * Sentence translation
     */
    SENTENCE_TRANSLATION("field_sentence_translation", "句子翻译", R.string.field_element_sentence_translation);

    /**
     * Get localized display name for this field element
     */
    fun getDisplayName(context: Context): String {
        return context.getString(displayNameResId)
    }

    companion object {
        /**
         * Find enum value from legacy Chinese string
         * Used for backward compatibility when loading old database entries
         *
         * @param legacyString The Chinese string from database or old code
         * @return Matching FieldElement, or null if not found
         */
        @JvmStatic
        fun fromLegacy(legacyString: String?): FieldElement? {
            if (legacyString == null) return null
            return values().firstOrNull { it.legacyValue == legacyString }
        }

        /**
         * Find enum value from language-neutral ID
         * Used when reading from migrated database
         *
         * @param id The language-neutral ID (e.g., "field_empty", "field_sentence_translation")
         * @return Matching FieldElement, or null if not found
         */
        @JvmStatic
        fun fromId(id: String?): FieldElement? {
            if (id == null) return null
            return values().firstOrNull { it.id == id }
        }

        /**
         * Get all legacy Chinese strings in order
         * For backward compatibility with existing code
         */
        @JvmStatic
        fun getAllLegacyValues(): Array<String> {
            return values().map { it.legacyValue }.toTypedArray()
        }

        /**
         * Get all localized display names in order
         * For UI display in current language
         */
        @JvmStatic
        fun getAllDisplayNames(context: Context): Array<String> {
            return values().map { it.getDisplayName(context) }.toTypedArray()
        }

        /**
         * Convert legacy Chinese string to language-neutral ID
         * Used during database migration
         *
         * @param legacyString The Chinese string to convert
         * @return Language-neutral ID, or original string if not found
         */
        @JvmStatic
        fun legacyToId(legacyString: String): String {
            return fromLegacy(legacyString)?.id ?: legacyString
        }

        /**
         * Convert language-neutral ID to legacy Chinese string
         * Used for backward compatibility
         *
         * @param id The language-neutral ID
         * @return Legacy Chinese string, or original ID if not found
         */
        @JvmStatic
        fun idToLegacy(id: String): String {
            return fromId(id)?.legacyValue ?: id
        }

        /**
         * Get all language-neutral IDs in order
         * For database storage
         */
        @JvmStatic
        fun getAllIds(): Array<String> {
            return values().map { it.id }.toTypedArray()
        }
    }
}
