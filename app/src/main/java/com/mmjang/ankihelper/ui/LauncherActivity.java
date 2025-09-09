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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.mmjang.ankihelper.R;
import com.mmjang.ankihelper.anki.AnkiDroidHelper;
import com.mmjang.ankihelper.data.database.DatabaseManager;
import com.mmjang.ankihelper.data.database.MigrationUtil;
import com.mmjang.ankihelper.data.plan.DefaultPlan;
import com.mmjang.ankihelper.data.plan.OutputPlanPOJO;
import com.mmjang.ankihelper.domain.CBWatcherService;
import com.mmjang.ankihelper.MyApplication;
import com.mmjang.ankihelper.data.Settings;
import com.mmjang.ankihelper.data.AppTheme;
import com.mmjang.ankihelper.data.ThemeManager;


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
    Spinner themeSpinner;
    TextView textViewOpenPlanManager;
    TextView textViewOpenAIConfig;
    TextView textViewAddDefaultPlan;
    TextView textViewAcknowledge;
    TextView textViewOpenStatistics;

    private static final int REQUEST_CODE_ANKI = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settings = Settings.getInstance(LauncherActivity.this);
        ThemeManager.applyTheme(this);
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
        themeSpinner = findViewById(R.id.theme_spinner);
        textViewOpenPlanManager = (TextView) findViewById(R.id.btn_open_plan_manager);
        textViewOpenAIConfig = (TextView) findViewById(R.id.btn_open_ai_config);
        textViewAddDefaultPlan = (TextView) findViewById(R.id.btn_add_default_plan);
        textViewAcknowledge = (TextView) findViewById(R.id.textview_acknowledge);
        textViewOpenStatistics = (TextView) findViewById(R.id.btn_open_statistics);

        switchMoniteClipboard.setChecked(
                settings.getMoniteClipboardQ()
        );

        switchCancelAfterAdd.setChecked(
                settings.getAutoCancelPopupQ()
        );

        switchLeftHandMode.setChecked(
                settings.getLeftHandModeQ()
        );

        // Setup theme spinner
        setupThemeSpinner();

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

        textViewOpenAIConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LauncherActivity.this, com.mmjang.ankihelper.ui.ai.AIConfigActivity.class);
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
        
        textViewOpenStatistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LauncherActivity.this, StatActivity.class);
                startActivity(intent);
            }
        });
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
        return super.onOptionsItemSelected(item);
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

    /**
     * Setup theme selection spinner
     */
    private void setupThemeSpinner() {
        // Create adapter with theme display names
        String[] themeNames = new String[AppTheme.values().length];
        for (int i = 0; i < AppTheme.values().length; i++) {
            themeNames[i] = AppTheme.values()[i].getDisplayName();
        }

        ArrayAdapter<String> themeAdapter = new ArrayAdapter<>(this,
                R.layout.custom_spinner_item, themeNames);
        themeAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        themeSpinner.setAdapter(themeAdapter);

        // Set current selection
        AppTheme currentTheme = settings.getSelectedTheme();
        themeSpinner.setSelection(currentTheme.ordinal());

        // Handle theme selection changes
        themeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AppTheme selectedTheme = AppTheme.values()[position];
                AppTheme currentTheme = settings.getSelectedTheme();

                if (selectedTheme != currentTheme) {
                    settings.setSelectedTheme(selectedTheme);
                    
                    // Show confirmation dialog for theme change
                    showThemeChangeDialog(selectedTheme);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    /**
     * Show theme change confirmation dialog
     */
    private void showThemeChangeDialog(AppTheme newTheme) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.theme_change_title)
                .setMessage(getString(R.string.theme_change_message, newTheme.getDisplayName()))
                .setPositiveButton(R.string.apply, (dialog, which) -> {
                    // Apply theme immediately
                    recreate();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    // Revert spinner selection
                    AppTheme currentTheme = settings.getSelectedTheme();
                    themeSpinner.setSelection(currentTheme.ordinal());
                })
                .show();
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