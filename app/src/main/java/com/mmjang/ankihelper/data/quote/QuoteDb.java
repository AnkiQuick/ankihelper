package com.mmjang.ankihelper.data.quote;

import android.content.Context;
import android.database.Cursor;

import java.util.Random;

/**
 * Created by liao on 2017/8/13.
 */

public class QuoteDb {
    private static final String DATABASE_NAME = "quote.db";
    private static final int DATABASE_VERSIOn = 1;
    private static final int ID_MAX = 15972;

    private static QuoteDb instance = null;
    private QuoteDatabaseHelper db; // Holds the database helper instance
    private Context mContext;

    protected QuoteDb(Context context){
        // Initialize the database helper
        db = new QuoteDatabaseHelper(context);
        mContext = context;
    }

    public static QuoteDb getInstance(Context context){
        if(instance == null){
            instance = new QuoteDb(context);
        }
        return instance;
    }

    public String getQuote() {
        int randomKey = randInt(0, ID_MAX);
        // Get the quote from the database
        Cursor cursor = db.getReadableDatabase().query("quote", new String[]{"content"}, "id=?", new String[]{Integer.toString(randomKey)}, null, null, null);
        String content = "";
        if (cursor.moveToNext()) {
            content = cursor.getString(0);
        }
        return content;
    }

    public static int randInt(int min, int max) {

        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }
}