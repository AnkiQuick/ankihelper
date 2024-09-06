package com.mmjang.ankihelper.data.content;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mmjang.ankihelper.data.database.DBContract;
import com.mmjang.ankihelper.data.database.DatabaseContext;
import com.mmjang.ankihelper.util.Constant;

import java.io.File;

public class  ContentDatabaseHelper extends SQLiteOpenHelper {

    //private static final String DB_NAME = "ankihelper.db";
    private static final int VERSION = 1;
    private Context mContext;

    public ContentDatabaseHelper(Context context, String dbName) {
        super(new DatabaseContext(context),
                Constant.STORAGE_CONTENT_SUBDIRECTORY + File.separator + dbName, null, VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
