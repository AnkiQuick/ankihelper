package com.mmjang.ankihelper.data.dict;

import android.content.Context;

public class WebsterLearnersDatabaseHelper extends BaseDatabaseHelper {

    private static final String DATABASE_NAME = "wb_headwords.db";

    public WebsterLearnersDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, 1);
    }
}