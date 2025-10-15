package com.lmyby.ankihelper.data.dict;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.lmyby.ankihelper.data.database.DatabaseContext;
import com.lmyby.ankihelper.util.StorageManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BaseDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "BaseDatabaseHelper";
    private final String databaseName;
    private final File databaseFile;
    private final StorageManager storageManager;
    private final Context context;

    public BaseDatabaseHelper(Context context, String databaseName, int version) {
        super(new DatabaseContext(context), databaseName, null, version);
        this.context = context;
        this.databaseName = databaseName;
        SharedPreferences preferences = context.getSharedPreferences("ankihelper_prefs", Context.MODE_PRIVATE);
        this.storageManager = new StorageManager(context, preferences);
        this.databaseFile = new File(storageManager.getDatabaseDir(), databaseName);

        Log.d(TAG, "Initializing dictionary database: " + databaseName);
        Log.d(TAG, "Dictionary database path: " + databaseFile.getAbsolutePath());

        // Force copy from assets if database doesn't exist or is empty
        copyDatabaseFromAssets(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Database should be copied from assets
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle upgrades if necessary
    }

    private void copyDatabaseFromAssets(Context context) {
        if (databaseFile.exists() && databaseFile.length() > 1000) {
            return; // Database already exists and has content
        }

        if (!databaseFile.getParentFile().exists()) {
            databaseFile.getParentFile().mkdirs();
        }

        try {
            InputStream input = context.getAssets().open("databases/" + databaseName);
            FileOutputStream output = new FileOutputStream(databaseFile);

            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytes = 0;

            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }

            input.close();
            output.close();

        } catch (IOException e) {
            Log.e(TAG, "Failed to copy " + databaseName + " from assets: " + e.getMessage());
            // Create minimal tables as fallback
            createFallbackDatabase();
        }
    }

    private void createFallbackDatabase() {
        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(databaseFile, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS dict (hwd TEXT PRIMARY KEY, phrase TEXT, sense TEXT, phonetics TEXT, def_en TEXT, def_cn TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS hwds (rowid INTEGER PRIMARY KEY, hwd TEXT)");
        db.close();
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        if (!databaseFile.exists() || databaseFile.length() < 1000) {
            // Database missing or corrupted, attempt rebuild
            if (!databaseFile.getParentFile().exists()) {
                databaseFile.getParentFile().mkdirs();
            }
            
            copyDatabaseFromAssets(context);
            
            if (!databaseFile.exists() || databaseFile.length() < 1000) {
                // Still no database, create fallback
                SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(databaseFile, null);
                createFallbackDatabase();
                db.close();
            }
        }
        
        return super.getReadableDatabase();
    }
}