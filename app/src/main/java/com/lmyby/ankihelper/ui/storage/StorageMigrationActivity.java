package com.lmyby.ankihelper.ui.storage;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.lmyby.ankihelper.R;
import com.lmyby.ankihelper.util.Constant;
import com.lmyby.ankihelper.util.StorageManager;

import java.io.File;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity for managing external storage migration.
 * Handles permission requests and performs data migration from internal to external storage.
 */
public class StorageMigrationActivity extends AppCompatActivity {
    private static final String TAG = "StorageMigrationActivity";
    private static final int REQUEST_MANAGE_EXTERNAL_STORAGE = 1001;
    private static final int REQUEST_EXTERNAL_STORAGE = 1002;
    
    private StorageManager storageManager;
    private SharedPreferences preferences;
    private ExecutorService executorService;
    
    private ProgressBar progressBar;
    private TextView statusText;
    private TextView sizeText;
    private Button migrateButton;
    private Button requestPermissionButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_migration);
        
        initializeViews();
        initializeComponents();
        checkStorageStatus();
    }
    
    private void initializeViews() {
        progressBar = findViewById(R.id.progress_bar);
        statusText = findViewById(R.id.status_text);
        sizeText = findViewById(R.id.size_text);
        migrateButton = findViewById(R.id.migrate_button);
        requestPermissionButton = findViewById(R.id.request_permission_button);
        
        migrateButton.setOnClickListener(v -> startMigration());
        requestPermissionButton.setOnClickListener(v -> requestStoragePermissions());
    }
    
    private void initializeComponents() {
        preferences = getSharedPreferences("ankihelper_prefs", MODE_PRIVATE);
        storageManager = new StorageManager(this, preferences);
        executorService = Executors.newSingleThreadExecutor();
    }
    
    private void checkStorageStatus() {
        if (storageManager.isMigrationCompleted()) {
            showMigrationCompleted();
            return;
        }
        
        if (hasRequiredPermissions()) {
            showMigrationReady();
        } else {
            showPermissionRequired();
        }
        
        updateStorageInfo();
    }
    
    private boolean hasRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            int writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return readPermission == PackageManager.PERMISSION_GRANTED &&
                   writePermission == PackageManager.PERMISSION_GRANTED;
        }
    }
    
    private void requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            } catch (Exception e) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        } else {
            ActivityCompat.requestPermissions(this, 
                new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, 
                REQUEST_EXTERNAL_STORAGE);
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && 
                grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                checkStorageStatus();
            } else {
                Toast.makeText(this, R.string.storage_permission_denied, Toast.LENGTH_LONG).show();
            }
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        checkStorageStatus();
    }
    
    private void updateStorageInfo() {
        long internalSize = getDirectorySize(getFilesDir());
        long availableSpace = storageManager.getAvailableStorageSpace();
        
        DecimalFormat df = new DecimalFormat("#.##");
        String internalSizeStr = formatFileSize(internalSize);
        String availableSpaceStr = formatFileSize(availableSpace);
        
        sizeText.setText(getString(R.string.storage_info_format, internalSizeStr, availableSpaceStr));
    }
    
    private void showPermissionRequired() {
        statusText.setText(R.string.storage_permission_required);
        migrateButton.setVisibility(View.GONE);
        requestPermissionButton.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }
    
    private void showMigrationReady() {
        statusText.setText(R.string.storage_migration_ready);
        migrateButton.setVisibility(View.VISIBLE);
        requestPermissionButton.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }
    
    private void showMigrationInProgress() {
        statusText.setText(R.string.storage_migration_in_progress);
        migrateButton.setVisibility(View.GONE);
        requestPermissionButton.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);
    }
    
    private void showMigrationCompleted() {
        statusText.setText(R.string.storage_migration_completed);
        migrateButton.setVisibility(View.GONE);
        requestPermissionButton.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }
    
    private void showMigrationFailed(String error) {
        statusText.setText(getString(R.string.storage_migration_failed, error));
        migrateButton.setVisibility(View.VISIBLE);
        requestPermissionButton.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }
    
    private void startMigration() {
        if (!hasRequiredPermissions()) {
            Toast.makeText(this, R.string.storage_permission_required, Toast.LENGTH_SHORT).show();
            return;
        }
        
        showMigrationInProgress();
        
        executorService.execute(() -> {
            try {
                boolean success = performMigration();
                
                runOnUiThread(() -> {
                    if (success) {
                        storageManager.setMigrationCompleted(true);
                        showMigrationCompleted();
                        Toast.makeText(this, R.string.storage_migration_success, Toast.LENGTH_LONG).show();
                    } else {
                        showMigrationFailed(getString(R.string.storage_migration_error_unknown));
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Migration failed", e);
                runOnUiThread(() -> showMigrationFailed(e.getMessage()));
            }
        });
    }
    
    private boolean performMigration() {
        // Migrate databases
        if (!migrateDatabases()) {
            return false;
        }
        
        
        // Migrate media files
        if (!migrateMediaFiles()) {
            return false;
        }
        
        // Migrate cache
        if (!migrateCache()) {
            return false;
        }
        
        return true;
    }
    
    private boolean migrateDatabases() {
        // Check multiple possible locations for databases
        File[] possibleDbDirs = {
            new File(getFilesDir(), "databases"),
            new File(getApplicationInfo().dataDir, "databases"),
            new File("/data/data/" + getPackageName() + "/databases")
        };
        
        File externalDbDir = storageManager.getDatabaseDir();
        boolean foundDatabases = false;
        
        // Migrate existing databases
        for (File internalDbDir : possibleDbDirs) {
            if (internalDbDir.exists() && internalDbDir.isDirectory()) {
                File[] dbFiles = internalDbDir.listFiles((dir, name) -> name.endsWith(".db"));
                if (dbFiles != null && dbFiles.length > 0) {
                    Log.i(TAG, "Found databases in: " + internalDbDir.getAbsolutePath());
                    foundDatabases = true;
                    
                    // Copy each database file individually
                    for (File dbFile : dbFiles) {
                        File targetFile = new File(externalDbDir, dbFile.getName());
                        if (!storageManager.copyFile(dbFile, targetFile)) {
                            Log.e(TAG, "Failed to copy database: " + dbFile.getName());
                            return false;
                        }
                        Log.i(TAG, "Successfully copied database: " + dbFile.getName());
                    }
                }
            }
        }
        
        // Copy built-in dictionary databases from assets
        if (!copyBuiltInDictionaries(externalDbDir)) {
            Log.e(TAG, "Failed to copy built-in dictionaries");
            return false;
        }
        
        if (!foundDatabases) {
            Log.i(TAG, "No existing databases found to migrate, but built-in dictionaries will be copied");
        }
        
        return true;
    }
    
    private boolean copyBuiltInDictionaries(File externalDbDir) {
        // Built-in dictionary files
        String[] builtInDictionaries = {
            "cdepe4.db",
            "collins_v2.db", 
            "forms.db",
            "maldpe.db",
            "oaldpe10.db",
            "ode2_v2.db",
            "wb_headwords.db"
        };
        
        boolean allCopied = true;
        
        for (String dictFile : builtInDictionaries) {
            File targetFile = new File(externalDbDir, dictFile);
            
            // Skip if file already exists and has content
            if (targetFile.exists() && targetFile.length() > 1000) {
                Log.i(TAG, "Dictionary already exists: " + dictFile);
                continue;
            }
            
            // Copy from assets
            try (InputStream input = getAssets().open("databases/" + dictFile);
                 FileOutputStream output = new FileOutputStream(targetFile)) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalBytes = 0;
                
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;
                }
                
                Log.i(TAG, "Successfully copied built-in dictionary: " + dictFile + " (" + (totalBytes/1024) + "KB)");
                
            } catch (IOException e) {
                Log.e(TAG, "Failed to copy built-in dictionary: " + dictFile + " - " + e.getMessage());
                allCopied = false;
            }
        }
        
        return allCopied;
    }
    
    
    private boolean migrateMediaFiles() {
        // Migrate image files
        File internalImageDir = new File(getFilesDir(), "media/" + Constant.IMAGE_SUB_DIRECTORY);
        File externalImageDir = storageManager.getImageDir();
        
        if (internalImageDir.exists()) {
            if (!storageManager.copyDirectory(internalImageDir, externalImageDir)) {
                return false;
            }
        }
        
        // Migrate audio files
        File internalAudioDir = new File(getFilesDir(), "media/" + Constant.AUDIO_SUB_DIRECTORY);
        File externalAudioDir = storageManager.getAudioDir();
        
        if (internalAudioDir.exists()) {
            if (!storageManager.copyDirectory(internalAudioDir, externalAudioDir)) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean migrateCache() {
        File internalCacheDir = getCacheDir();
        File externalCacheDir = storageManager.getCacheDir();
        
        if (internalCacheDir.exists()) {
            return storageManager.copyDirectory(internalCacheDir, externalCacheDir);
        }
        
        return true;
    }
    
    private long getDirectorySize(File directory) {
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
    
    private String formatFileSize(long size) {
        if (size == 0) {
            return "0 B";
        }
        
        String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int unitIndex = (int) (Math.log10(size) / Math.log10(1024));
        double unitValue = size / Math.pow(1024, unitIndex);
        
        return String.format("%.2f %s", unitValue, units[unitIndex]);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}