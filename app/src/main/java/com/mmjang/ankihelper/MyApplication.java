package com.mmjang.ankihelper;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import androidx.multidex.MultiDexApplication;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.mmjang.ankihelper.anki.AnkiDroidHelper;
//import com.tencent.bugly.crashreport.CrashReport;

import org.litepal.LitePalApplication;
import org.litepal.LitePal;
import okhttp3.OkHttpClient;
import androidx.work.WorkManager;
import org.litepal.LitePal; // Import for LitePal initialization

/**
 * Created by liao on 2017/4/27.
 */

public class MyApplication extends MultiDexApplication {
    private static Context context;
    private static Application application;
    private static AnkiDroidHelper mAnkiDroid;
    private static OkHttpClient okHttpClient;

    @Override
    public void onCreate() {
        super.onCreate();

        // Assign the application context here
        context = getApplicationContext();
        application = this;
        LitePal.initialize(this);
//        CrashReport.initCrashReport(getApplicationContext(), "398dc6145b", false);

    }

    // No changes needed here as you are returning 'context'
    public static Context getContext() {
        return context;
    }

    // No changes needed here as you are returning 'application'
    public static Application getApplication(){
        return application;
    }

    // Pass the Context as an argument
    public static AnkiDroidHelper getAnkiDroid(Context context) {
      if (mAnkiDroid == null) {
          mAnkiDroid = new AnkiDroidHelper(context);
      }
      return mAnkiDroid;
    }

    private static void getAnkiDroidPermission(Activity activity) {

    }

    public static OkHttpClient getOkHttpClient(){
        if(okHttpClient == null){
            okHttpClient = new OkHttpClient();
        }
        return okHttpClient;
    }
}