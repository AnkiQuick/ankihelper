package com.lmyby.ankihelper.ui.storage

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.lmyby.ankihelper.R
import com.lmyby.ankihelper.util.Constant
import com.lmyby.ankihelper.util.StorageManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.DecimalFormat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.log10
import kotlin.math.pow

/**
 * Activity for managing external storage migration.
 * Handles permission requests and performs data migration from internal to external storage.
 */
class StorageMigrationActivity : AppCompatActivity() {

    private lateinit var storageManager: StorageManager
    private lateinit var preferences: SharedPreferences
    private lateinit var executorService: ExecutorService

    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView
    private lateinit var sizeText: TextView
    private lateinit var migrateButton: Button
    private lateinit var requestPermissionButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_storage_migration)

        initializeViews()
        initializeComponents()
        checkStorageStatus()
    }

    private fun initializeViews() {
        progressBar = findViewById(R.id.progress_bar)
        statusText = findViewById(R.id.status_text)
        sizeText = findViewById(R.id.size_text)
        migrateButton = findViewById(R.id.migrate_button)
        requestPermissionButton = findViewById(R.id.request_permission_button)

        migrateButton.setOnClickListener { startMigration() }
        requestPermissionButton.setOnClickListener { requestStoragePermissions() }
    }

    private fun initializeComponents() {
        preferences = getSharedPreferences("ankihelper_prefs", MODE_PRIVATE)
        storageManager = StorageManager(this, preferences)
        executorService = Executors.newSingleThreadExecutor()
    }

    private fun checkStorageStatus() {
        if (storageManager.isMigrationCompleted()) {
            showMigrationCompleted()
            return
        }

        if (hasRequiredPermissions()) {
            showMigrationReady()
        } else {
            showPermissionRequired()
        }

        updateStorageInfo()
    }

    private fun hasRequiredPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            val readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            val writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            readPermission == PackageManager.PERMISSION_GRANTED &&
                    writePermission == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = android.net.Uri.parse("package:$packageName")
                startActivity(intent)
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                REQUEST_EXTERNAL_STORAGE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                checkStorageStatus()
            } else {
                Toast.makeText(this, R.string.storage_permission_denied, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkStorageStatus()
    }

    private fun updateStorageInfo() {
        val internalSize = getDirectorySize(filesDir)
        val availableSpace = storageManager.getAvailableStorageSpace()

        val internalSizeStr = formatFileSize(internalSize)
        val availableSpaceStr = formatFileSize(availableSpace)

        sizeText.text = getString(R.string.storage_info_format, internalSizeStr, availableSpaceStr)
    }

    private fun showPermissionRequired() {
        statusText.setText(R.string.storage_permission_required)
        migrateButton.visibility = View.GONE
        requestPermissionButton.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
    }

    private fun showMigrationReady() {
        statusText.setText(R.string.storage_migration_ready)
        migrateButton.visibility = View.VISIBLE
        requestPermissionButton.visibility = View.GONE
        progressBar.visibility = View.GONE
    }

    private fun showMigrationInProgress() {
        statusText.setText(R.string.storage_migration_in_progress)
        migrateButton.visibility = View.GONE
        requestPermissionButton.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        progressBar.isIndeterminate = true
    }

    private fun showMigrationCompleted() {
        statusText.setText(R.string.storage_migration_completed)
        migrateButton.visibility = View.GONE
        requestPermissionButton.visibility = View.GONE
        progressBar.visibility = View.GONE
    }

    private fun showMigrationFailed(error: String?) {
        statusText.text = getString(R.string.storage_migration_failed, error ?: "Unknown error")
        migrateButton.visibility = View.VISIBLE
        requestPermissionButton.visibility = View.GONE
        progressBar.visibility = View.GONE
    }

    private fun startMigration() {
        if (!hasRequiredPermissions()) {
            Toast.makeText(this, R.string.storage_permission_required, Toast.LENGTH_SHORT).show()
            return
        }

        showMigrationInProgress()

        executorService.execute {
            try {
                val success = performMigration()

                runOnUiThread {
                    if (success) {
                        storageManager.setMigrationCompleted(true)
                        showMigrationCompleted()
                        Toast.makeText(this, R.string.storage_migration_success, Toast.LENGTH_LONG).show()
                    } else {
                        showMigrationFailed(getString(R.string.storage_migration_error_unknown))
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Migration failed", e)
                runOnUiThread { showMigrationFailed(e.message) }
            }
        }
    }

    private fun performMigration(): Boolean {
        // Migrate databases
        if (!migrateDatabases()) {
            return false
        }

        // Migrate media files
        if (!migrateMediaFiles()) {
            return false
        }

        // Migrate cache
        if (!migrateCache()) {
            return false
        }

        return true
    }

    private fun migrateDatabases(): Boolean {
        // Check multiple possible locations for databases
        val possibleDbDirs = arrayOf(
            File(filesDir, "databases"),
            File(applicationInfo.dataDir, "databases"),
            File("/data/data/$packageName/databases")
        )

        val externalDbDir = storageManager.getDatabaseDir()
        var foundDatabases = false

        // Migrate existing databases
        for (internalDbDir in possibleDbDirs) {
            if (internalDbDir.exists() && internalDbDir.isDirectory) {
                val dbFiles = internalDbDir.listFiles { _, name -> name.endsWith(".db") }
                if (dbFiles != null && dbFiles.isNotEmpty()) {
                    Log.i(TAG, "Found databases in: ${internalDbDir.absolutePath}")
                    foundDatabases = true

                    // Copy each database file individually
                    for (dbFile in dbFiles) {
                        val targetFile = File(externalDbDir, dbFile.name)
                        if (!storageManager.copyFile(dbFile, targetFile)) {
                            Log.e(TAG, "Failed to copy database: ${dbFile.name}")
                            return false
                        }
                        Log.i(TAG, "Successfully copied database: ${dbFile.name}")
                    }
                }
            }
        }

        // Copy built-in dictionary databases from assets
        if (!copyBuiltInDictionaries(externalDbDir)) {
            Log.e(TAG, "Failed to copy built-in dictionaries")
            return false
        }

        if (!foundDatabases) {
            Log.i(TAG, "No existing databases found to migrate, but built-in dictionaries will be copied")
        }

        return true
    }

    private fun copyBuiltInDictionaries(externalDbDir: File): Boolean {
        // Built-in dictionary files
        val builtInDictionaries = arrayOf(
            "cdepe4.db",
            "collins_v2.db",
            "forms.db",
            "maldpe.db",
            "oaldpe10.db",
            "ode2_v2.db",
            "wb_headwords.db"
        )

        var allCopied = true

        for (dictFile in builtInDictionaries) {
            val targetFile = File(externalDbDir, dictFile)

            // Skip if file already exists and has content
            if (targetFile.exists() && targetFile.length() > 1000) {
                Log.i(TAG, "Dictionary already exists: $dictFile")
                continue
            }

            // Copy from assets
            try {
                assets.open("databases/$dictFile").use { input ->
                    FileOutputStream(targetFile).use { output ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        var totalBytes = 0L

                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            totalBytes += bytesRead
                        }

                        Log.i(TAG, "Successfully copied built-in dictionary: $dictFile (${totalBytes / 1024}KB)")
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Failed to copy built-in dictionary: $dictFile - ${e.message}")
                allCopied = false
            }
        }

        return allCopied
    }

    private fun migrateMediaFiles(): Boolean {
        // Migrate image files
        val internalImageDir = File(filesDir, "media/${Constant.IMAGE_SUB_DIRECTORY}")
        val externalImageDir = storageManager.getImageDir()

        if (internalImageDir.exists()) {
            if (!storageManager.copyDirectory(internalImageDir, externalImageDir)) {
                return false
            }
        }

        // Migrate audio files
        val internalAudioDir = File(filesDir, "media/${Constant.AUDIO_SUB_DIRECTORY}")
        val externalAudioDir = storageManager.getAudioDir()

        if (internalAudioDir.exists()) {
            if (!storageManager.copyDirectory(internalAudioDir, externalAudioDir)) {
                return false
            }
        }

        return true
    }

    private fun migrateCache(): Boolean {
        val internalCacheDir = cacheDir
        val externalCacheDir = storageManager.getCacheDir()

        if (internalCacheDir.exists()) {
            return storageManager.copyDirectory(internalCacheDir, externalCacheDir)
        }

        return true
    }

    private fun getDirectorySize(directory: File): Long {
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

    private fun formatFileSize(size: Long): String {
        if (size == 0L) {
            return "0 B"
        }

        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val unitIndex = (log10(size.toDouble()) / log10(1024.0)).toInt()
        val unitValue = size / 1024.0.pow(unitIndex)

        return String.format("%.2f %s", unitValue, units[unitIndex])
    }

    override fun onDestroy() {
        super.onDestroy()
        executorService.shutdown()
    }

    companion object {
        private const val TAG = "StorageMigrationActivity"
        private const val REQUEST_MANAGE_EXTERNAL_STORAGE = 1001
        private const val REQUEST_EXTERNAL_STORAGE = 1002
    }
}
