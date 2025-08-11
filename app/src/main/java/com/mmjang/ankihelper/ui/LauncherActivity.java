package com.mmjang.ankihelper.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import android.widget.TextView;
import android.widget.Toast;

import com.mmjang.ankihelper.R;
import com.mmjang.ankihelper.anki.AnkiDroidHelper;
import com.mmjang.ankihelper.data.database.DatabaseManager;
import com.mmjang.ankihelper.data.database.MigrationUtil;
import com.mmjang.ankihelper.data.plan.DefaultPlan;
import com.mmjang.ankihelper.data.plan.OutputPlanPOJO;
import com.mmjang.ankihelper.domain.CBWatcherService;
import com.mmjang.ankihelper.MyApplication;
import com.mmjang.ankihelper.data.Settings;


import com.mmjang.ankihelper.ui.plan.PlansManagerActivity;
import com.mmjang.ankihelper.ui.stat.StatActivity;


import java.util.List;
import java.util.ArrayList;

public class LauncherActivity extends AppCompatActivity {

    AnkiDroidHelper mAnkiDroid;
    Settings settings;
    DatabaseManager databaseManager;

    //views
    MaterialSwitch switchMoniteClipboard;
    MaterialSwitch switchCancelAfterAdd;
    MaterialSwitch switchLeftHandMode;
    MaterialSwitch switchPinkTheme;
    TextView textViewOpenPlanManager;
    TextView textViewAddDefaultPlan;
    TextView textViewAcknowledge;

    private static final int REQUEST_CODE_ANKI = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settings = Settings.getInstance(LauncherActivity.this);
        if (settings.getPinkThemeQ()) {
            setTheme(R.style.Theme_AnkiHelperPink);
        } else {
            setTheme(R.style.Theme_AnkiHelper);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher); // Set the layout first
        setVersion();

        // Initialize AnkiDroidHelper in onCreate
        mAnkiDroid = MyApplication.getAnkiDroid(this);

        // Call checkAndRequestPermissions after AnkiDroidHelper is initialized
        checkAndRequestPermissions();

        // Calculate the database path

        // Initialize DatabaseManager after setting the layout
        databaseManager = databaseManager.getInstance();

        switchMoniteClipboard = findViewById(R.id.switch_monite_clipboard);
        switchCancelAfterAdd = findViewById(R.id.switch_cancel_after_add);
        switchLeftHandMode = findViewById(R.id.left_hand_mode);
        switchPinkTheme = findViewById(R.id.pink_theme_switch);
        textViewOpenPlanManager = (TextView) findViewById(R.id.btn_open_plan_manager);
        textViewAddDefaultPlan = (TextView) findViewById(R.id.btn_add_default_plan);
        textViewAcknowledge = (TextView) findViewById(R.id.textview_acknowledge);

        switchMoniteClipboard.setChecked(
                settings.getMoniteClipboardQ()
        );

        switchCancelAfterAdd.setChecked(
                settings.getAutoCancelPopupQ()
        );

        switchLeftHandMode.setChecked(
                settings.getLeftHandModeQ()
        );

        switchPinkTheme.setChecked(
                settings.getPinkThemeQ()
        );

        switchMoniteClipboard.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        settings.setMoniteClipboardQ(isChecked);
                        if (isChecked) {
                            startCBService();
                        } else {
                            stopCBService();
                        }
                    }
                }
        );

        switchLeftHandMode.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        settings.setLeftHandModeQ(isChecked);
                    }
                }
        );

        switchCancelAfterAdd.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        settings.setAutoCancelPopupQ(isChecked);
                    }
                }
        );

        switchPinkTheme.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        settings.setPinkThemeQ(b);
                        recreate();
                    }
                }
        );

        textViewOpenPlanManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mAnkiDroid.isAnkiDroidRunning()) {
                    Toast.makeText(LauncherActivity.this, R.string.api_not_available_message, Toast.LENGTH_LONG).show();
                    return;
                }

                if (mAnkiDroid.shouldRequestPermission()) {
                    mAnkiDroid.requestPermission(LauncherActivity.this, 0);
                    return;
                }

                Intent intent = new Intent(LauncherActivity.this, PlansManagerActivity.class);
                startActivity(intent);
            }
        });

        textViewAddDefaultPlan.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!AnkiDroidHelper.isApiAvailable(MyApplication.getContext())) {
                            Toast.makeText(LauncherActivity.this, R.string.api_not_available_message, Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (mAnkiDroid.shouldRequestPermission()) {
                            mAnkiDroid.requestPermission(LauncherActivity.this, 0);
                            return;
                        } else {

                        }
                        askIfAddDefaultPlan();
                    }
                }
        );
        //debug new feature
//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    YoudaoOnline.getDefinition("dedicate");
//                }
//                catch (IOException e){
//
//                }
//            }
//        });
//        thread.start();
    }
    private void checkAndRequestPermissions() {
      if (mAnkiDroid == null) {
          mAnkiDroid = new AnkiDroidHelper(this);
      }
      if (mAnkiDroid.shouldRequestPermission()) {
          mAnkiDroid.requestPermission(this, REQUEST_CODE_ANKI);
      }
        // Only check notification permission (for internal storage)
        if (Build.VERSION.SDK_INT >= 23 &&
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_ANKI);
            return;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

      if (item.getItemId() == R.id.menu_item_stat) {
          Intent intent2 = new Intent(this, StatActivity.class);
          startActivity(intent2);
      }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Call super.onRequestPermissionsResult() first
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length == 0) {
            return;
        }

        if (requestCode == REQUEST_CODE_ANKI ) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ensureExternalDbDirectoryAndMigrate();
            } else {
                //Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_LONG).show();
                new AlertDialog.Builder(LauncherActivity.this)
                        .setMessage(R.string.permission_denied)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                openSettingsPage();
                            }
                        }).show();
            }
        }
        // The REQUEST_CODE_STORAGE is no longer used for internal storage
        // ...
    }

    private void ensureExternalDbDirectoryAndMigrate() {
        // Check for storage permission
        //if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        //    // Request permission
        //    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE);
        //    return; // Exit the function to wait for permission result
        //}
        // ... the rest of the function is no longer needed, as we are using internal storage now
    }

    private void startCBService() {
        Intent intent = new Intent(this, CBWatcherService.class);
        startService(intent);
    }

    private void stopCBService() {
        Intent intent = new Intent(this, CBWatcherService.class);
        stopService(intent);
    }

    void askIfAddDefaultPlan() {
        List<OutputPlanPOJO> plans;
        plans = databaseManager.getAllPlan(); // Access internal database by default

        for (OutputPlanPOJO plan : plans) {
            if (plan.getPlanName().equals(DefaultPlan.DEFAULT_PLAN_NAME)) {
                new AlertDialog.Builder(LauncherActivity.this)
                        .setMessage(R.string.duplicate_plan_name_complain)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                return;
                            }
                        }).show();
                return;
            }
        }
        if (plans.size() == 0) {
            new AlertDialog.Builder(LauncherActivity.this)
                    .setTitle(R.string.confirm_add_default_plan)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            DefaultPlan plan = new DefaultPlan(LauncherActivity.this);
                            plan.addDefaultPlan();
                            Toast.makeText(LauncherActivity.this, R.string.default_plan_added, Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null).show();
        } else {
            new AlertDialog.Builder(LauncherActivity.this)
                    .setMessage(R.string.confirm_add_default_plan_when_exists_already)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            try {
                                DefaultPlan plan = new DefaultPlan(LauncherActivity.this);
                                plan.addDefaultPlan();
                                Toast.makeText(LauncherActivity.this, R.string.default_plan_added, Toast.LENGTH_SHORT).show();
                            }catch (Exception e){
                                Toast.makeText(LauncherActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.no, null).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void openSettingsPage() {
        Intent intent = new Intent(this, com.mmjang.ankihelper.ui.settings.SettingsActivity.class);
        startActivity(intent);
    }



    public void setVersion() {
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            TextView versionTextView = (TextView) findViewById(R.id.textview_version);
            versionTextView.setText(
                    "Ver: " + versionName
            );
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

}