package com.lmyby.ankiquicker.data.plan

import android.content.Context
import android.content.res.Configuration
import com.lmyby.ankiquicker.R
import java.util.Locale

/**
 * Enum representing field export element types with internationalization support
 *
 * This enum provides a mapping between:
 * 1. Language-neutral IDs - Used for database storage (e.g., "field_empty", "field_sentence_translation")
 * 2. Localized display strings - For UI display in user's language (from string resources)
 *
 * Architecture:
 * - Database stores only language-neutral IDs (stable, never changes)
 * - All display text comes from XML string resources (values/strings.xml, values-zh/strings.xml)
 * - Adding new languages: just add values-XX/strings.xml files
 * - No hardcoded strings in application code
 *
 * Usage:
 * - Save: Use displayNameToId() to convert UI display name → ID before saving to database
 * - Load: Use idToDisplayName() to convert database ID → current language display name for UI
 */
enum class FieldElement(
    val id: String, // Language-neutral ID for database storage
    val displayNameResId: Int // String resource ID for localized display
) {
    /**
     * Empty field - no content exported
     */
    EMPTY("field_empty", R.string.field_element_empty),

    /**
     * Example sentence from dictionary
     */
    EXAMPLE("field_example", R.string.field_element_example),

    /**
     * Example sentence with bold formatting
     */
    BOLD_EXAMPLE("field_bold_example", R.string.field_element_bold_example),

    /**
     * Cloze deletion example sentence
     */
    CLOZE_EXAMPLE("field_cloze_example", R.string.field_element_cloze_example),

    /**
     * Cloze deletion example in full C1 mode
     */
    CLOZE_EXAMPLE_C1("field_cloze_c1", R.string.field_element_cloze_c1),

    /**
     * User notes
     */
    NOTES("field_notes", R.string.field_element_notes),

    /**
     * Source URL
     */
    URL("field_url", R.string.field_element_url),

    /**
     * All dictionary definitions combined
     */
    ALL_DEFINITIONS("field_all_definitions", R.string.field_element_all_definitions),

    /**
     * Sentence translation
     */
    SENTENCE_TRANSLATION("field_sentence_translation", R.string.field_element_sentence_translation);

    /**
     * Get localized display name for this field element
     */
    fun getDisplayName(context: Context): String {
        return context.getString(displayNameResId)
    }

    companion object {
        /**
         * Find enum value from language-neutral ID
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
         * Get all localized display names in order
         * For UI display in current language
         *
         * @param context Android context for accessing string resources
         * @return Array of localized field element names
         */
        @JvmStatic
        fun getAllDisplayNames(context: Context): Array<String> {
            return values().map { it.getDisplayName(context) }.toTypedArray()
        }

        /**
         * Get all language-neutral IDs in order
         * For database storage and comparisons
         *
         * @return Array of language-neutral IDs
         */
        @JvmStatic
        fun getAllIds(): Array<String> {
            return values().map { it.id }.toTypedArray()
        }

        /**
         * Convert display name to language-neutral ID
         * Supports display names from ANY language (Chinese, English, etc.)
         *
         * @param displayName The localized display name (e.g., "Empty", "空", "Example Sentence", "例句")
         * @param context Android context for accessing string resources
         * @return Language-neutral ID, or original string if not found
         */
        @JvmStatic
        fun displayNameToId(displayName: String, context: Context): String {
            android.util.Log.e("FieldElement", "displayNameToId: input=$displayName")

            // First check if it's already an ID
            fromId(displayName)?.let {
                android.util.Log.e("FieldElement", "displayNameToId: already an ID, returning=$displayName")
                return displayName
            }

            // Try to find matching field element by checking ALL supported languages
            // This allows saving in one language and loading in another
            val found = findElementByDisplayName(displayName, context)
            val result = found?.id ?: displayName
            android.util.Log.e("FieldElement", "displayNameToId: found=$found, result=$result")
            return result
        }

        /**
         * Find field element by display name in any supported language
         */
        private fun findElementByDisplayName(displayName: String, context: Context): FieldElement? {
            val supportedLocales = listOf(
                Locale.ENGLISH,
                Locale.CHINESE,
                Locale.SIMPLIFIED_CHINESE,
                Locale.TRADITIONAL_CHINESE
            )

            for (locale in supportedLocales) {
                val matchedElement = checkDisplayNameInLocale(displayName, context, locale)
                if (matchedElement != null) {
                    return matchedElement
                }
            }

            return null
        }

        /**
         * Check if display name matches any field element in given locale
         */
        private fun checkDisplayNameInLocale(
            displayName: String,
            context: Context,
            locale: Locale
        ): FieldElement? {
            val config = Configuration(context.resources.configuration)
            config.setLocale(locale)
            val localizedContext = context.createConfigurationContext(config)

            return values().firstOrNull { fieldElement ->
                getStringOrNull(localizedContext, fieldElement.displayNameResId) == displayName
            }
        }

        /**
         * Safely get string from resources, return null if error
         */
        private fun getStringOrNull(context: Context, resId: Int): String? {
            return try {
                context.getString(resId)
            } catch (e: android.content.res.Resources.NotFoundException) {
                android.util.Log.w("FieldElement", "Resource not found: $resId", e)
                null
            }
        }

        /**
         * Convert language-neutral ID to display name
         *
         * @param id The language-neutral ID (e.g., "field_empty", "field_sentence_translation")
         * @param context Android context for accessing string resources
         * @return Localized display name, or original string if not found
         */
        @JvmStatic
        fun idToDisplayName(id: String, context: Context): String {
            android.util.Log.e("FieldElement", "idToDisplayName: input=$id")
            val found = fromId(id)
            val result = found?.getDisplayName(context) ?: id
            android.util.Log.e("FieldElement", "idToDisplayName: found=$found, result=$result")
            return result
        }
    }
}
