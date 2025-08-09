package com.mmjang.ankihelper.data.dict;

import android.content.Context;

public class CollinsEnEnDatabaseHelper extends BaseDatabaseHelper {

    private static final String DATABASE_NAME = "collins_v2.db";

    public CollinsEnEnDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, 1);
    }
}