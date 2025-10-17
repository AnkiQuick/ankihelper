package com.lmyby.ankiquicker.domain

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.lmyby.ankiquicker.data.Settings

/**
 * BroadcastReceiver for screen lock/unlock events
 * Converted to Kotlin as part of Phase 3 domain logic migration
 */
class OnlockReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val newIntent = Intent(context, CBWatcherService::class.java)
        val settings = Settings.getInstance(context)
        if (settings.getMoniteClipboardQ()) {
            Toast.makeText(context, "debug", Toast.LENGTH_SHORT).show()
            context.startService(newIntent)
        }
    }
}
