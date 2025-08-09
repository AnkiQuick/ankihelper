package com.mmjang.ankihelper.data.dict;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.mmjang.ankihelper.data.database.DatabaseContext;
import com.mmjang.ankihelper.util.Constant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BaseDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "BaseDatabaseHelper";
    private final String databaseName;
    private final File databaseFile;

    public BaseDatabaseHelper(Context context, String databaseName, int version) {
        super(new DatabaseContext(context), databaseName, null, version);
        this.databaseName = databaseName;
        this.databaseFile = new File(context.getFilesDir(), Constant.STORAGE_DIRECTORY + "/databases/" + databaseName);

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

        databaseFile.getParentFile().mkdirs();

        try (InputStream input = context.getAssets().open("databases/" + databaseName);
             FileOutputStream output = new FileOutputStream(databaseFile)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytes = 0;

            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }

            Log.i("DatabaseHelper", "Successfully copied " + databaseName + " (" + (totalBytes/1024) + "KB)");

        } catch (IOException e) {
            Log.e("DatabaseHelper", "Failed to copy " + databaseName + " from assets: " + e.getMessage());
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
            Log.e("DatabaseHelper", "Database missing for " + databaseName + ", attempting rebuild");
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(databaseFile, null);
            createFallbackDatabase();
            db.close();
        }
        return super.getReadableDatabase();
    }
}