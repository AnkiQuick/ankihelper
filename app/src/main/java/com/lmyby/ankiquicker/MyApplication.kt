package com.lmyby.ankiquicker

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDexApplication
import androidx.work.WorkManager
import com.lmyby.ankiquicker.anki.AnkiDroidHelper
import com.lmyby.ankiquicker.data.history.HistoryUtil
import okhttp3.OkHttpClient

/**
 * Main Application class
 * Manages app-level singletons and initialization
 * Converted to Kotlin as part of Phase 13 final conversion
 */
class MyApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        // Assign the application context here
        context = applicationContext
        application = this

        // Initialize HistoryUtil for Room database in background to avoid blocking main thread
        Thread {
            HistoryUtil.initialize(applicationContext)
        }.start()

        // Language is now handled automatically by AndroidX AppCompatDelegate
        // No need for custom attachBaseContext or onConfigurationChanged

        // CrashReport.initCrashReport(getApplicationContext(), "398dc6145b", false)
    }

    companion object {
        private var context: Context? = null
        private var application: Application? = null
        private var mAnkiDroid: AnkiDroidHelper? = null
        private var okHttpClient: OkHttpClient? = null

        @JvmStatic
        fun getContext(): Context {
            return context ?: throw IllegalStateException("Application context is not initialized")
        }

        @JvmStatic
        fun getApplication(): Application {
            return application ?: throw IllegalStateException("Application is not initialized")
        }

        /**
         * Get AnkiDroidHelper instance (lazy initialized)
         * @param context Context to initialize helper with
         */
        @JvmStatic
        fun getAnkiDroid(context: Context): AnkiDroidHelper {
            if (mAnkiDroid == null) {
                mAnkiDroid = AnkiDroidHelper(context)
            }
            return mAnkiDroid!!
        }

        @JvmStatic
        fun getOkHttpClient(): OkHttpClient {
            if (okHttpClient == null) {
                okHttpClient = OkHttpClient()
            }
            return okHttpClient!!
        }
    }
}
