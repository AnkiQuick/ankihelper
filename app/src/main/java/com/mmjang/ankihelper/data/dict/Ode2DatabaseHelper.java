package com.mmjang.ankihelper.data.dict;

import android.content.Context;

public class Ode2DatabaseHelper extends BaseDatabaseHelper {

    private static final String DATABASE_NAME = "ode2_v2.db";

    public Ode2DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, 1);
    }
}