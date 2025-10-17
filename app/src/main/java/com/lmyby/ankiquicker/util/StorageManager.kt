package com.lmyby.ankiquicker.util

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

/**
 * Utility class for managing external storage migration and operations.
 * Handles MANAGE_EXTERNAL_STORAGE permission and provides fallback to internal storage.
 * Converted to Kotlin as part of Phase 1 utility migration
 */
class StorageManager(
    context: Context,
    private val preferences: SharedPreferences
) {
    private val context: Context = context.applicationContext

    companion object {
        private const val TAG = "StorageManager"

        // Preference keys
        const val PREF_MIGRATION_COMPLETED = "migration_completed"
    }

    /**
     * Get the primary storage directory for the app
     * Always uses external storage, no fallback to internal storage
     */
    fun getStorageDir(): File {
        // Use external storage directory with fallback
        val externalDir = Environment.getExternalStorageDirectory()

        if (externalDir != null) {
            val appDir = File(externalDir, "ankihelper")
            if (!appDir.exists()) {
                appDir.mkdirs()
            }
            return appDir
        }

        // Final fallback to internal storage
        val fallbackDir = File(context.filesDir, "ankihelper")
        if (!fallbackDir.exists()) {
            fallbackDir.mkdirs()
        }
        return fallbackDir
    }

    /**
     * Get the app-specific external storage directory
     */
    fun getExternalStorageDir(): File? {
        return context.getExternalFilesDir(null)
    }

    /**
     * Get the internal storage directory
     */
    fun getInternalStorageDir(): File {
        return context.filesDir
    }

    /**
     * Get database directory
     */
    fun getDatabaseDir(): File {
        val storageDir = getStorageDir()
        val dbDir = File(storageDir, "databases")

        if (!dbDir.exists()) {
            dbDir.mkdirs()
        }
        return dbDir
    }

    /**
     * Get media files directory
     */
    fun getMediaDir(): File {
        val storageDir = getStorageDir()
        val mediaDir = File(storageDir, "media")
        if (!mediaDir.exists()) {
            mediaDir.mkdirs()
        }
        return mediaDir
    }

    /**
     * Get cache directory
     */
    fun getCacheDir(): File {
        val storageDir = getStorageDir()
        val cacheDir = File(storageDir, "cache")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        return cacheDir
    }

    /**
     * Get content directory
     */
    fun getContentDir(): File {
        val storageDir = getStorageDir()
        val contentDir = File(storageDir, "content")
        if (!contentDir.exists()) {
            contentDir.mkdirs()
        }
        return contentDir
    }

    /**
     * Get image directory
     */
    fun getImageDir(): File {
        val mediaDir = getMediaDir()
        val imageDir = File(mediaDir, Constant.IMAGE_SUB_DIRECTORY)
        if (!imageDir.exists()) {
            imageDir.mkdirs()
        }
        return imageDir
    }

    /**
     * Get audio directory
     */
    fun getAudioDir(): File {
        val mediaDir = getMediaDir()
        val audioDir = File(mediaDir, Constant.AUDIO_SUB_DIRECTORY)
        if (!audioDir.exists()) {
            audioDir.mkdirs()
        }
        return audioDir
    }

    /**
     * Check if migration has been completed
     */
    fun isMigrationCompleted(): Boolean {
        return preferences.getBoolean(PREF_MIGRATION_COMPLETED, false)
    }

    /**
     * Set migration completed status
     */
    fun setMigrationCompleted(completed: Boolean) {
        preferences.edit()
            .putBoolean(PREF_MIGRATION_COMPLETED, completed)
            .apply()
    }

    /**
     * Copy file from source to destination
     */
    fun copyFile(source: File, destination: File): Boolean {
        return try {
            if (!destination.parentFile!!.exists()) {
                destination.parentFile!!.mkdirs()
            }

            FileInputStream(source).channel.use { sourceChannel ->
                FileOutputStream(destination).channel.use { destChannel ->
                    destChannel.transferFrom(sourceChannel, 0, sourceChannel.size())
                }
            }

            true
        } catch (e: IOException) {
            Log.e(TAG, "Error copying file: ${source.absolutePath} -> ${destination.absolutePath}", e)
            false
        }
    }

    /**
     * Move file from source to destination
     */
    fun moveFile(source: File, destination: File): Boolean {
        return if (copyFile(source, destination)) {
            source.delete()
        } else {
            false
        }
    }

    /**
     * Copy directory recursively
     */
    fun copyDirectory(source: File, destination: File): Boolean {
        if (!source.exists() || !source.isDirectory) {
            return false
        }

        if (!destination.exists()) {
            destination.mkdirs()
        }

        val files = source.listFiles() ?: return false

        for (file in files) {
            val destFile = File(destination, file.name)
            if (file.isDirectory) {
                if (!copyDirectory(file, destFile)) {
                    return false
                }
            } else {
                if (!copyFile(file, destFile)) {
                    return false
                }
            }
        }

        return true
    }

    /**
     * Get available storage space in bytes
     */
    fun getAvailableStorageSpace(): Long {
        val storageDir = getStorageDir()
        return storageDir.usableSpace
    }

    /**
     * Get directory size in bytes
     */
    fun getDirectorySize(directory: File): Long {
        if (!directory.exists()) {
            return 0
        }

        var size = 0L
        val files = directory.listFiles() ?: return 0

        for (file in files) {
            size += if (file.isDirectory) {
                getDirectorySize(file)
            } else {
                file.length()
            }
        }

        return size
    }
}
