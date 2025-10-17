package com.lmyby.ankiquicker.util

import android.content.Context
import android.os.Environment
import com.lmyby.ankiquicker.data.plan.FieldElement

/**
 * Created by liao on 2017/4/27.
 * Converted to Kotlin as part of Phase 1 utility migration
 */
object Constant {
    /**
     * Get shared export elements localized to current language
     * Returns display names from string resources (values/strings.xml or values-zh/strings.xml)
     *
     * @param context Android context for accessing string resources
     * @return Array of localized field element names for UI display
     */
    @JvmStatic
    fun getSharedExportElements(context: Context): Array<String> {
        return FieldElement.getAllDisplayNames(context)
    }

    /**
     * Get shared export elements as language-neutral IDs
     * Returns stable IDs for database storage (e.g., "field_empty", "field_sentence_translation")
     *
     * @return Array of language-neutral field element IDs
     */
    @JvmStatic
    fun getSharedExportElementIds(): Array<String> {
        return FieldElement.getAllIds()
    }

    const val INTENT_ANKIHELPER_TARGET_WORD = "com.lmyby.ankiquicker.target_word"
    const val INTENT_ANKIHELPER_TARGET_URL = "com.lmyby.ankiquicker.url"
    const val INTENT_ANKIHELPER_NOTE_ID = "com.lmyby.ankiquicker.note_id"
    const val INTENT_ANKIHELPER_UPDATE_ACTION = "com.lmyby.ankiquicker.note_update_action" //replace;append;
    const val INTENT_ANKIHELPER_BASE64 = "com.lmyby.ankiquicker.base64"
    const val INTENT_ANKIHELPER_PLAN_NAME = "com.lmyby.ankiquicker.plan_name"
    const val INTENT_ANKIHELPER_FBREADER_BOOKMARK_ID = "com.lmyby.ankiquicker.fbreader.bookmark.id"
    const val ANKI_PACKAGE_NAME = "com.ichi2.anki"
    const val FBREADER_URL_TMPL = "<a href=\"intent:#Intent;action=android.fbreader.action.VIEW;category=android.intent.category.DEFAULT;type=text/plain;component=org.geometerplus.zlibrary.ui.android/org.geometerplus.android.fbreader.FBReader;S.fbreader.bookmarkid.from.external=%s;end;\">查看原文</a>"
    const val INTENT_ANKIHELPER_NOTE = "com.lmyby.ankiquicker.note"
    const val VIBRATE_DURATION = 10

    const val FLOAT_ACTION_BUTTON_ALPHA = 0.3f

    const val STORAGE_DIRECTORY = "ankihelper"
    const val STORAGE_CONTENT_SUBDIRECTORY = "content"
    const val LEFT_BOLD_SUBSTITUDE = "☾"
    const val RIGHT_BOLD_SUBSTITUDE = "☽"

    const val INTENT_ANKIHELPER_CONTENT_INDEX = "com.lmyby.ankiquicker.content_index"
    const val UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36"

    const val IMAGE_SUB_DIRECTORY = "ankihelper_image"
    const val AUDIO_SUB_DIRECTORY = "ankihelper_audio"

    // Note: These paths are now managed by StorageManager to always use external storage
    // Legacy paths maintained for compatibility, but StorageManager.getExternalStorageDir() should be used
    @JvmField
    val IMAGE_MEDIA_DIRECTORY = "${Environment.getExternalStorageDirectory()}" +
            "/Android/data/com.lmyby.ankiquicker/files/media/ankihelper_image/"

    @JvmField
    val AUDIO_MEDIA_DIRECTORY = "${Environment.getExternalStorageDirectory()}" +
            "/Android/data/com.lmyby.ankiquicker/files/media/ankihelper_audio/"

    const val USE_CLIPBOARD_CONTENT_FLAG = "use_clipboard_content_flag"
}
