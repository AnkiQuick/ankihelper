package com.mmjang.ankihelper.data.dict;

import android.content.Context;

public class MaldpeDatabaseHelper extends BaseDatabaseHelper {

    private static final String DATABASE_NAME = "maldpe.db";

    public MaldpeDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, 1);
    }
}