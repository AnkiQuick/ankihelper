package com.mmjang.ankihelper.data.database;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

import com.mmjang.ankihelper.util.StorageManager;

import java.io.File;

public class DatabaseContext extends ContextWrapper {

    private static final String DEBUG_CONTEXT = "DatabaseContext";
    private final StorageManager storageManager;

    public DatabaseContext(Context base) {
        super(base);
        SharedPreferences preferences = base.getSharedPreferences("ankihelper_prefs", Context.MODE_PRIVATE);
        this.storageManager = new StorageManager(base, preferences);
    }

    @Override
    public File getDatabasePath(String name)  {
        // Always use external storage - no fallback to internal storage
        File databaseDir = storageManager.getDatabaseDir();
        if (databaseDir == null) {
            Log.e(DEBUG_CONTEXT, "Database directory is null!");
            return null;
        }

        // Don't modify the database name, just use it as-is
        File result = new File(databaseDir, name);

        if (!result.getParentFile().exists()) {
            result.getParentFile().mkdirs();
        }

        return result;
    }

    /* this version is called for android devices >= api-11. thank to @damccull for fixing this. */
    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler errorHandler) {
        return openOrCreateDatabase(name,mode, factory);
    }

    /* this version is called for android devices < api-11 */
    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory) {
        File dbPath = getDatabasePath(name);
        SQLiteDatabase result = SQLiteDatabase.openOrCreateDatabase(dbPath.getAbsolutePath(), null);

        Log.w(DEBUG_CONTEXT, "openOrCreateDatabase(" + name + ",,) = " + result.getPath());
        return result;
    }
}
