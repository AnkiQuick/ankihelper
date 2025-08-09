package com.mmjang.ankihelper.data.dict;

import android.content.Context;

public class Cdepe4DatabaseHelper extends BaseDatabaseHelper {

    private static final String DATABASE_NAME = "cdepe4.db";

    public Cdepe4DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, 1);
    }
}