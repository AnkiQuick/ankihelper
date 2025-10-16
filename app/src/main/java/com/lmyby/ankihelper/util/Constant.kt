package com.lmyby.ankihelper.util

import android.os.Environment

/**
 * Created by liao on 2017/4/27.
 * Converted to Kotlin as part of Phase 1 utility migration
 */
object Constant {
    private val SHARED_EXPORT_ELEMENTS = arrayOf(
        "空",
        "例句",
        "加粗的例句",
        "挖空的例句",
        "挖空的例句（全c1模式）",
        "笔记",
        "URL",
        "全部释义",
        "句子翻译"
        //"FBReader跳转链接"
    )

    @JvmStatic
    fun getSharedExportElements(): Array<String> = SHARED_EXPORT_ELEMENTS

    const val INTENT_ANKIHELPER_TARGET_WORD = "com.lmyby.ankihelper.target_word"
    const val INTENT_ANKIHELPER_TARGET_URL = "com.lmyby.ankihelper.url"
    const val INTENT_ANKIHELPER_NOTE_ID = "com.lmyby.ankihelper.note_id"
    const val INTENT_ANKIHELPER_UPDATE_ACTION = "com.lmyby.ankihelper.note_update_action" //replace;append;
    const val INTENT_ANKIHELPER_BASE64 = "com.lmyby.ankihelper.base64"
    const val INTENT_ANKIHELPER_PLAN_NAME = "com.lmyby.ankihelper.plan_name"
    const val INTENT_ANKIHELPER_FBREADER_BOOKMARK_ID = "com.lmyby.ankihelper.fbreader.bookmark.id"
    const val ANKI_PACKAGE_NAME = "com.ichi2.anki"
    const val FBREADER_URL_TMPL = "<a href=\"intent:#Intent;action=android.fbreader.action.VIEW;category=android.intent.category.DEFAULT;type=text/plain;component=org.geometerplus.zlibrary.ui.android/org.geometerplus.android.fbreader.FBReader;S.fbreader.bookmarkid.from.external=%s;end;\">查看原文</a>"
    const val INTENT_ANKIHELPER_NOTE = "com.lmyby.ankihelper.note"
    const val VIBRATE_DURATION = 10

    const val FLOAT_ACTION_BUTTON_ALPHA = 0.3f

    const val STORAGE_DIRECTORY = "ankihelper"
    const val STORAGE_CONTENT_SUBDIRECTORY = "content"
    const val LEFT_BOLD_SUBSTITUDE = "☾"
    const val RIGHT_BOLD_SUBSTITUDE = "☽"

    const val INTENT_ANKIHELPER_CONTENT_INDEX = "com.lmyby.ankihelper.content_index"
    const val UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36"

    const val IMAGE_SUB_DIRECTORY = "ankihelper_image"
    const val AUDIO_SUB_DIRECTORY = "ankihelper_audio"

    // Note: These paths are now managed by StorageManager to always use external storage
    // Legacy paths maintained for compatibility, but StorageManager.getExternalStorageDir() should be used
    @JvmField
    val IMAGE_MEDIA_DIRECTORY = "${Environment.getExternalStorageDirectory()}" +
            "/Android/data/com.lmyby.ankihelper/files/media/ankihelper_image/"

    @JvmField
    val AUDIO_MEDIA_DIRECTORY = "${Environment.getExternalStorageDirectory()}" +
            "/Android/data/com.lmyby.ankihelper/files/media/ankihelper_audio/"

    const val USE_CLIPBOARD_CONTENT_FLAG = "use_clipboard_content_flag"
}
