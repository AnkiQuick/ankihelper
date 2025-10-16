package com.lmyby.ankihelper.util

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.lmyby.ankihelper.MyApplication
import com.lmyby.ankihelper.R

/**
 * Created by Gao on 2017/6/27.
 * Converted to Kotlin as part of Phase 1 utility migration
 */
object DialogUtil {

    @JvmStatic
    fun showStartAnkiDialog(activityContext: Context) {
        AlertDialog.Builder(activityContext)
            .setMessage(activityContext.getString(R.string.plan_anki_not_started))
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                // Empty click handler
            }
            .setPositiveButton(android.R.string.ok) { _, _ ->
                MyApplication.getAnkiDroid(MyApplication.getContext()).startAnkiDroid()
            }
            .show()
    }
}
