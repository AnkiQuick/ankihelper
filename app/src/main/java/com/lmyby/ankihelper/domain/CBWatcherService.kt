package com.lmyby.ankihelper.domain

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.lmyby.ankihelper.MyApplication
import com.lmyby.ankihelper.R
import com.lmyby.ankihelper.data.Settings
import com.lmyby.ankihelper.ui.popup.PopupActivity
import com.lmyby.ankihelper.util.Constant

/**
 * Service for monitoring clipboard changes
 * Converted to Kotlin as part of Phase 3 domain logic migration
 * Modernized to use stopForeground(int) in Phase 14.6
 */
class CBWatcherService : Service() {
    private val listener = ClipboardManager.OnPrimaryClipChangedListener {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !hasClipboardPermission()) {
            Log.w(TAG, "Clipboard access denied on Android 10+")
            return@OnPrimaryClipChangedListener
        }
        performClipboardCheck()
    }

    private var pm: ClipboardManager? = null

    override fun onCreate() {
        pm = getSystemService(CLIPBOARD_SERVICE) as? ClipboardManager
        pm?.addPrimaryClipChangedListener(listener)
    }

    override fun onDestroy() {
        super.onDestroy()
        pm?.removePrimaryClipChangedListener(listener)
        // Use modern stopForeground API (API 24+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        Log.d(TAG, "onDestroy")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        pm?.addPrimaryClipChangedListener(listener)

        // Create notification channel
        val channelId = "com.lmyby.ankihelper"
        val channelName = "CBService"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableLights(false)
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(notificationChannel)
        }

        // Create pending intent for notification
        val intentStart = Intent(applicationContext, PopupActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            putExtra(Intent.EXTRA_TEXT, Constant.USE_CLIPBOARD_CONTENT_FLAG)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intentStart,
            PendingIntent.FLAG_MUTABLE
        )

        // Build notification
        val contentText = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            getString(R.string.str_clipboard_service_running_version_q)
        } else {
            getString(R.string.str_clipboard_service_running)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.icon_light)
            .setContentTitle(resources.getText(R.string.app_name))
            .setContentText(contentText)
            .setContentIntent(pendingIntent)
            .setWhen(System.currentTimeMillis())

        val notification = builder.build()
        notification.flags = notification.flags or Notification.FLAG_FOREGROUND_SERVICE

        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun performClipboardCheck() {
        Log.d(TAG, "clip_changed")
        if (!Settings.getInstance(MyApplication.getContext()).getMoniteClipboardQ()) {
            return
        }

        val cb = getSystemService(CLIPBOARD_SERVICE) as? ClipboardManager
        if (cb == null || !hasClipboardPermission()) {
            Log.w(TAG, "Clipboard access not available or permission denied")
            return
        }

        try {
            if (cb.hasPrimaryClip()) {
                cb.primaryClip?.let { clipData ->
                    if (clipData.itemCount > 0) {
                        clipData.getItemAt(0).text?.let { text ->
                            if (isEnglish(text.toString())) {
                                val intent = Intent(applicationContext, PopupActivity::class.java).apply {
                                    action = Intent.ACTION_SEND
                                    type = "text/plain"
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                    putExtra(Intent.EXTRA_TEXT, text.toString())
                                }
                                startActivity(intent)
                            }
                        }
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException accessing clipboard: ${e.message}")
        }
    }

    private fun hasClipboardPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ requires special focus rules for clipboard access
            try {
                val cm = getSystemService(CLIPBOARD_SERVICE) as? ClipboardManager ?: return false

                // Check clipboard service availability without causing permission denial
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // Android 11+ has stricter focus requirements
                    cm.hasPrimaryClip() && cm.primaryClipDescription != null
                } else {
                    // Android 10 (API 29) and below
                    cm.hasPrimaryClip()
                }
            } catch (e: SecurityException) {
                Log.w(TAG, "SecurityException checking clipboard access: ${e.message}")
                return false
            }
        }
        return true
    }

    companion object {
        private const val TAG = "CBWatcherService"
        private const val REQUEST_CODE_CBW = 0
        private const val NOTIFICATION_ID = 2333

        @JvmStatic
        fun isEnglish(content: String): Boolean {
            val nonEnglishCharThreshold = 0.2

            // Check if it's a URL
            val urlHeads = arrayOf("http://", "https://", "ftp://")
            for (urlHead in urlHeads) {
                if (content.startsWith(urlHead)) {
                    return false
                }
            }

            val len = content.length
            if (len == 0) {
                return false
            }

            var notEnglishCount = 0
            for (c in content) {
                if (!((c in 'a'..'z') || (c in 'A'..'Z') || (c in '0'..'9') || isPunctuationOrBlank(c))) {
                    notEnglishCount++
                }
            }

            val ratio = notEnglishCount.toDouble() / len.toDouble()
            return ratio <= nonEnglishCharThreshold
        }

        @JvmStatic
        fun isPunctuationOrBlank(c: Char): Boolean {
            return c == ' ' ||
                    c == ',' ||
                    c == '.' ||
                    c == '!' ||
                    c == '?' ||
                    c == ':' ||
                    c == ';' ||
                    c == '(' ||
                    c == ')' ||
                    c == '~' ||
                    c == '"' ||
                    c == '"' ||
                    c == '"'
        }
    }
}
