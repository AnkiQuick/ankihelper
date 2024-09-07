package com.mmjang.ankihelper.data.dict;

import android.content.Context;

import com.mmjang.ankihelper.data.dict.FormsDatabase; // Import FormsDatabase
import com.mmjang.ankihelper.data.dict.Form; // Import Form
import com.mmjang.ankihelper.data.dict.FormsDao; // Import FormsDao

public class FormsUtil {

    private static FormsUtil instance = null;
    private FormsDatabase db; // Holds the Room database instance

    protected FormsUtil(Context context){
        // Initialize Room database
        db = FormsDatabase.getInstance(context);
    }

    public static FormsUtil getInstance(Context context){
        if(instance == null){
            instance = new FormsUtil(context);
        }
        return instance;
    }

    public String[] getForms(String q) {
        // Get forms from Room database
        String bases = db.formsDao().getForms(q.toLowerCase());

        if(bases == null || bases.isEmpty()){
            return new String[0];
        }
        return bases.split("@@@");
    }
}