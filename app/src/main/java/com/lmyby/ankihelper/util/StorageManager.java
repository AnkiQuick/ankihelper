package com.lmyby.ankihelper.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Utility class for managing external storage migration and operations.
 * Handles MANAGE_EXTERNAL_STORAGE permission and provides fallback to internal storage.
 */
public class StorageManager {
    private static final String TAG = "StorageManager";
    
    // Preference keys
    public static final String PREF_MIGRATION_COMPLETED = "migration_completed";
    
    
    private final Context context;
    private final SharedPreferences preferences;
    
    public StorageManager(@NonNull Context context, @NonNull SharedPreferences preferences) {
        this.context = context.getApplicationContext();
        this.preferences = preferences;
    }
    
    /**
     * Get the primary storage directory for the app
     * Always uses external storage, no fallback to internal storage
     */
    public File getStorageDir() {
        // Use external storage directory with fallback
        File externalDir = Environment.getExternalStorageDirectory();
        
        if (externalDir != null) {
            File appDir = new File(externalDir, "ankihelper");
            if (!appDir.exists()) {
                appDir.mkdirs();
            }
            return appDir;
        }
        
        // Final fallback to internal storage
        File fallbackDir = new File(context.getFilesDir(), "ankihelper");
        if (!fallbackDir.exists()) {
            fallbackDir.mkdirs();
        }
        return fallbackDir;
    }
    
    /**
     * Get the app-specific external storage directory
     */
    public File getExternalStorageDir() {
        return context.getExternalFilesDir(null);
    }
    
    /**
     * Get the internal storage directory
     */
    public File getInternalStorageDir() {
        return context.getFilesDir();
    }
    
    
    /**
     * Get database directory
     */
    public File getDatabaseDir() {
        File storageDir = getStorageDir();
        File dbDir = new File(storageDir, "databases");
        
        if (!dbDir.exists()) {
            dbDir.mkdirs();
        }
        return dbDir;
    }
    
    /**
     * Get media files directory
     */
    public File getMediaDir() {
        File storageDir = getStorageDir();
        File mediaDir = new File(storageDir, "media");
        if (!mediaDir.exists()) {
            mediaDir.mkdirs();
        }
        return mediaDir;
    }
    
    /**
     * Get cache directory
     */
    public File getCacheDir() {
        File storageDir = getStorageDir();
        File cacheDir = new File(storageDir, "cache");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        return cacheDir;
    }
    
    /**
     * Get content directory
     */
    public File getContentDir() {
        File storageDir = getStorageDir();
        File contentDir = new File(storageDir, "content");
        if (!contentDir.exists()) {
            contentDir.mkdirs();
        }
        return contentDir;
    }
    
    /**
     * Get image directory
     */
    public File getImageDir() {
        File mediaDir = getMediaDir();
        File imageDir = new File(mediaDir, Constant.IMAGE_SUB_DIRECTORY);
        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }
        return imageDir;
    }
    
    /**
     * Get audio directory
     */
    public File getAudioDir() {
        File mediaDir = getMediaDir();
        File audioDir = new File(mediaDir, Constant.AUDIO_SUB_DIRECTORY);
        if (!audioDir.exists()) {
            audioDir.mkdirs();
        }
        return audioDir;
    }
    
        
    
    
    /**
     * Check if migration has been completed
     */
    public boolean isMigrationCompleted() {
        return preferences.getBoolean(PREF_MIGRATION_COMPLETED, false);
    }
    
    /**
     * Set migration completed status
     */
    public void setMigrationCompleted(boolean completed) {
        preferences.edit()
                .putBoolean(PREF_MIGRATION_COMPLETED, completed)
                .apply();
    }
    
    /**
     * Copy file from source to destination
     */
    public boolean copyFile(File source, File destination) {
        try {
            if (!destination.getParentFile().exists()) {
                destination.getParentFile().mkdirs();
            }
            
            FileChannel sourceChannel = new FileInputStream(source).getChannel();
            FileChannel destChannel = new FileOutputStream(destination).getChannel();
            
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
            
            sourceChannel.close();
            destChannel.close();
            
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error copying file: " + source.getAbsolutePath() + " -> " + destination.getAbsolutePath(), e);
            return false;
        }
    }
    
    /**
     * Move file from source to destination
     */
    public boolean moveFile(File source, File destination) {
        if (copyFile(source, destination)) {
            return source.delete();
        }
        return false;
    }
    
    /**
     * Copy directory recursively
     */
    public boolean copyDirectory(File source, File destination) {
        if (!source.exists() || !source.isDirectory()) {
            return false;
        }
        
        if (!destination.exists()) {
            destination.mkdirs();
        }
        
        File[] files = source.listFiles();
        if (files == null) {
            return false;
        }
        
        for (File file : files) {
            File destFile = new File(destination, file.getName());
            if (file.isDirectory()) {
                if (!copyDirectory(file, destFile)) {
                    return false;
                }
            } else {
                if (!copyFile(file, destFile)) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Get available storage space in bytes
     */
    public long getAvailableStorageSpace() {
        File storageDir = getStorageDir();
        return storageDir.getUsableSpace();
    }
    
    
    /**
     * Get directory size in bytes
     */
    public long getDirectorySize(File directory) {
        if (!directory.exists()) {
            return 0;
        }
        
        long size = 0;
        File[] files = directory.listFiles();
        if (files == null) {
            return 0;
        }
        
        for (File file : files) {
            if (file.isDirectory()) {
                size += getDirectorySize(file);
            } else {
                size += file.length();
            }
        }
        
        return size;
    }
    
    
}