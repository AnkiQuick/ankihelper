package com.lmyby.ankihelper.data.ai

import android.content.Context
import android.net.ConnectivityManager

/**
 * Converted to Kotlin as part of Phase 2 data model migration
 */
object NetworkUtil {
    @JvmStatic
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (connectivityManager != null) {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
        return false
    }
}
