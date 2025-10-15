package com.mmjang.ankihelper.ui.plan;

import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.core.app.NavUtils;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mmjang.ankihelper.R;
import com.mmjang.ankihelper.anki.AnkiDroidHelper;
import com.mmjang.ankihelper.data.Settings;
import com.mmjang.ankihelper.data.database.AppDatabase;
import com.mmjang.ankihelper.data.plan.OutputPlanEntity;
import com.mmjang.ankihelper.data.plan.OutputPlanPOJO;
import com.mmjang.ankihelper.data.plan.OutputPlanRepository;
import com.mmjang.ankihelper.data.plan.OutputPlanRepositoryHelper;
import com.mmjang.ankihelper.util.Constant;
import com.mmjang.ankihelper.data.dict.DictionaryRegister;
import com.mmjang.ankihelper.data.dict.IDictionary;
import com.mmjang.ankihelper.data.dict.AIDictionary; // Add this import
import com.mmjang.ankihelper.MyApplication;
import com.mmjang.ankihelper.data.plan.OutputPlan;
import com.mmjang.ankihelper.util.Utils;
import com.mmjang.ankihelper.ui.base.BaseEditorActivity;

import org.litepal.crud.LitePalSupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PlanEditorActivity extends BaseEditorActivity {

    private String planNameToEdit;
    private AnkiDroidHelper mAnkiDroid;
    private OutputPlanPOJO planForEdit;
    private Map<Long, String> deckList;
    private Map<Long, String> modelList;
    private List<IDictionary> dictionaryList;
    private List<FieldsMapItem> fieldsMapItemList;
    private IDictionary currentDictionary;
    private long currentDeckId;
    private long currentModelId;
    private OutputPlanRepositoryHelper planRepositoryHelper;
    //views
    private EditText planNameEditText;
    private Spinner dictionarySpinner;
    private TextView dictionaryIntroductionTextView;
    private Spinner deckSpinner;
    private Spinner modelSpinner;
    private RecyclerView fieldsSpinnersContainer;

    @Override
    protected void initializeViews() {
        try {
            setViewMember();
            initAnkiApi();

            // Initialize database and load data in background to avoid ANR
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Initialize repository helper off main thread
                        AppDatabase database = AppDatabase.Companion.getInstance(getApplicationContext());
                        OutputPlanRepository repository = new OutputPlanRepository(database.outputPlanDao());
                        planRepositoryHelper = new OutputPlanRepositoryHelper(repository, PlanEditorActivity.this);

                        // Load AnkiDroid data off main thread
                        loadDecksAndModels();

                        // Update UI on main thread
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                handleIntent();
                                populateDictionary();
                                populateDecksAndModels();
                            }
                        });
                    } catch (Exception e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(PlanEditorActivity.this,
                                    "Failed to initialize: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }).start();
        } catch (Exception e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void setupListeners() {
        // Listeners are set in populate methods
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_plan_editor;
    }

    @Override
    protected boolean validateInput() {
        String planName = planNameEditText.getText().toString().trim();
        if (planName.isEmpty()) {
            Toast.makeText(this, R.string.str_plan_name_should_not_be_blank, Toast.LENGTH_SHORT).show();
            return false;
        }
        
        // Check if all fields are empty
        boolean allFieldsAreEmpty = true;
        for (FieldsMapItem item : fieldsMapItemList) {
            String v = item.getExportedElementNames()[item.getSelectedFieldPos()];
            if (!v.equals(Constant.getSharedExportElements()[0])) {
                allFieldsAreEmpty = false;
                break;
            }
        }
        if (allFieldsAreEmpty) {
            Toast.makeText(this, R.string.save_plan_error_all_blank, Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
    }

    @Override
    protected boolean saveData() {
        return savePlan();
    }

    private boolean savePlan() {
        // Check if repository is initialized
        if (planRepositoryHelper == null) {
            Toast.makeText(this, "Please wait, initializing...", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Start async save operation - no blocking!
        savePlanAsync();
        // Return false to prevent immediate finish() - we'll finish after save completes
        return false;
    }

    private void savePlanAsync() {
        final String planName = planNameEditText.getText().toString().trim();

        // Show progress on UI thread
        runOnUiThread(() -> {
            // Disable UI to prevent multiple saves
            planNameEditText.setEnabled(false);
            dictionarySpinner.setEnabled(false);
            deckSpinner.setEnabled(false);
            modelSpinner.setEnabled(false);
        });

        // Step 1: Check for name conflicts (async, non-blocking)
        planRepositoryHelper.getPlanByName(planName, new OutputPlanRepositoryHelper.PlanCallback() {
            @Override
            public void onSuccess(OutputPlanEntity existingPlan) {
                // Check if there's a conflict
                boolean isConflict = false;
                if (planForEdit != null) {
                    // Editing: conflict only if name changed and new name exists
                    if (!planName.equals(planNameToEdit) && existingPlan != null) {
                        isConflict = true;
                    }
                } else {
                    // Creating: conflict if name already exists
                    if (existingPlan != null) {
                        isConflict = true;
                    }
                }

                if (isConflict) {
                    runOnUiThread(() -> {
                        Toast.makeText(PlanEditorActivity.this, R.string.plan_already_exists, Toast.LENGTH_SHORT).show();
                        // Re-enable UI
                        planNameEditText.setEnabled(true);
                        dictionarySpinner.setEnabled(true);
                        deckSpinner.setEnabled(true);
                        modelSpinner.setEnabled(true);
                    });
                } else {
                    // No conflict - proceed with save
                    performSave(planName);
                }
            }

            @Override
            public void onError(Throwable error) {
                // Error checking for conflicts - proceed with save anyway
                performSave(planName);
            }
        });
    }

    private void performSave(final String planName) {
        // Prepare plan data
        OutputPlanPOJO plan;
        if (planForEdit != null) {
            plan = planForEdit;
        } else {
            plan = new OutputPlanPOJO();
        }

        plan.setPlanName(planName);
        plan.setDictionaryKey(currentDictionary.getDictionaryKey());
        plan.setOutputDeckId(currentDeckId);
        plan.setOutputModelId(currentModelId);

        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        for (FieldsMapItem item : fieldsMapItemList) {
            String k = item.getField();
            String v = item.getExportedElementNames()[item.getSelectedFieldPos()];
            map.put(k, v);
        }
        plan.setFieldsMap(map);

        // Convert POJO to Entity
        OutputPlanEntity entity = new OutputPlanEntity();
        entity.setPlanName(plan.getPlanName());
        entity.setDictionaryKey(plan.getDictionaryKey());
        entity.setOutputDeckId(plan.getOutputDeckId());
        entity.setOutputModelId(plan.getOutputModelId());
        entity.setFieldsMap(plan.getFieldsMapString());

        // Perform save operation (async, non-blocking)
        if (planNameToEdit != null) {
            // Update existing plan
            planRepositoryHelper.refreshPlan(planNameToEdit, entity, new OutputPlanRepositoryHelper.OperationCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        Toast.makeText(PlanEditorActivity.this, "Plan saved successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }

                @Override
                public void onError(Throwable error) {
                    runOnUiThread(() -> {
                        Toast.makeText(PlanEditorActivity.this,
                                "Failed to update plan: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        // Re-enable UI on error
                        planNameEditText.setEnabled(true);
                        dictionarySpinner.setEnabled(true);
                        deckSpinner.setEnabled(true);
                        modelSpinner.setEnabled(true);
                    });
                }
            });
        } else {
            // Insert new plan
            planRepositoryHelper.savePlan(entity, new OutputPlanRepositoryHelper.OperationCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        Toast.makeText(PlanEditorActivity.this, "Plan saved successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }

                @Override
                public void onError(Throwable error) {
                    runOnUiThread(() -> {
                        Toast.makeText(PlanEditorActivity.this,
                                "Failed to save plan: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        // Re-enable UI on error
                        planNameEditText.setEnabled(true);
                        dictionarySpinner.setEnabled(true);
                        deckSpinner.setEnabled(true);
                        modelSpinner.setEnabled(true);
                    });
                }
            });
        }
    }

    private void setViewMember() {
        planNameEditText = (EditText) findViewById(R.id.text_edit_plan_name);
        dictionarySpinner = (Spinner) findViewById(R.id.dictionary_spinner);
        dictionaryIntroductionTextView = (TextView) findViewById(R.id.text_view_dictionary_introduction);
        deckSpinner = (Spinner) findViewById(R.id.deck_spinner);
        modelSpinner = (Spinner) findViewById(R.id.model_spinner);
        fieldsSpinnersContainer = (RecyclerView) findViewById(R.id.recycler_view_fields_map);
    }

    private void initAnkiApi() {
        if (mAnkiDroid == null) {
            mAnkiDroid = new AnkiDroidHelper(this);
        }
        if (!AnkiDroidHelper.isApiAvailable(MyApplication.getContext())) {
            Toast.makeText(this, R.string.api_not_available_message, Toast.LENGTH_LONG).show();
        }

        if (mAnkiDroid.shouldRequestPermission()) {
            mAnkiDroid.requestPermission(this, 0);
        }
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            String action = intent.getAction();
            if (action != null && action.equals(Intent.ACTION_SEND)) {
                String text = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (text != null && !text.isEmpty()) {
                    planNameToEdit = text;
                    // Load plan asynchronously
                    planRepositoryHelper.getPlanByName(planNameToEdit, new OutputPlanRepositoryHelper.PlanCallback() {
                        @Override
                        public void onSuccess(OutputPlanEntity entity) {
                            if (entity != null) {
                                // Convert Entity to POJO
                                planForEdit = new OutputPlanPOJO();
                                planForEdit.setPlanName(entity.getPlanName());
                                planForEdit.setDictionaryKey(entity.getDictionaryKey());
                                planForEdit.setOutputDeckId(entity.getOutputDeckId());
                                planForEdit.setOutputModelId(entity.getOutputModelId());
                                planForEdit.setFieldsMapString(entity.getFieldsMap());
                                //set plan name unable to edit
                                planNameEditText.setText(planNameToEdit);
                                //planNameEditText.setEnabled(false);
                            }
                        }

                        @Override
                        public void onError(Throwable error) {
                            Toast.makeText(PlanEditorActivity.this,
                                    "Failed to load plan: " + error.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }
    }

    private void loadDecksAndModels() {
        deckList = Utils.hashMap2LinkedHashMap(mAnkiDroid.getApi().getDeckList());
        modelList = Utils.hashMap2LinkedHashMap(mAnkiDroid.getApi().getModelList());
    }

    private void populateDictionary() {
        if (dictionaryList == null) {
            dictionaryList = DictionaryRegister.getDictionaryObjectList();
        }

        String[] dictionaryNameList = new String[dictionaryList.size()];
        for (int i = 0; i < dictionaryList.size(); i++) {
            dictionaryNameList[i] = dictionaryList.get(i).getDictionaryName();
        }
        ArrayAdapter<String> dictionarySpinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, dictionaryNameList);
        dictionarySpinner.setAdapter(dictionarySpinnerAdapter);

        if (planForEdit != null) {
            String savedKey = planForEdit.getDictionaryKey();
            boolean found = false;
            for (int i = 0; i < dictionaryList.size(); i++) {
                IDictionary dict = dictionaryList.get(i);
                // Use stable dictionary key for matching
                if (savedKey.equals(dict.getDictionaryKey())) {
                    currentDictionary = dictionaryList.get(i);
                    dictionaryIntroductionTextView.setText(currentDictionary.getIntroduction());
                    dictionarySpinner.setSelection(i);
                    found = true;
                    Log.d("Editor", "Found dictionary: " + dict.getDictionaryName());
                    break;
                }
            }
            if (!found) {
                String message = getString(R.string.error_dictionary_not_found, savedKey);
                Utils.showMessage(PlanEditorActivity.this, message);
            }
        } else {

            int pos = dictionarySpinner.getSelectedItemPosition();
            currentDictionary = dictionaryList.get(pos);
            dictionaryIntroductionTextView.setText(currentDictionary.getIntroduction());

        }

        dictionarySpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        currentDictionary = dictionaryList.get(position);
                        dictionaryIntroductionTextView.setText(currentDictionary.getIntroduction());
                        refreshFieldSpinners();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                }
        );
    }

    private void populateDecksAndModels() {
        ArrayAdapter<String> deckSpinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, Utils.getMapValueArray(deckList));
        deckSpinner.setAdapter(deckSpinnerAdapter);
        ArrayAdapter<String> modelSpinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, Utils.getMapValueArray(modelList));
        modelSpinner.setAdapter(modelSpinnerAdapter);

        if (planForEdit != null) {
            long savedDeckId = planForEdit.getOutputDeckId();
            long savedModelId = planForEdit.getOutputModelId();
            //int i = 0;
            long[] deckIdList = Utils.getMapKeyArray(deckList);
            //int deckPos = Arrays.asList(deckIdList).indexOf(savedDeckId);
            int deckPos = Utils.getArrayIndex(deckIdList, savedDeckId);
            if (deckPos == -1) {
                deckPos = 0;
            }
            currentDeckId = deckIdList[deckPos];
            deckSpinner.setSelection(deckPos);

            long[] modelIdList = Utils.getMapKeyArray(modelList);
            //int modelPos = Arrays.asList(modelIdList).indexOf(savedModelId);
            int modelPos = Utils.getArrayIndex(modelIdList, savedModelId);
            if (modelPos == -1) {
                modelPos = 0;
            }
            currentModelId = modelIdList[modelPos];
            modelSpinner.setSelection(modelPos);

            refreshFieldSpinners();
        } else {
            currentDeckId = Utils.getMapKeyArray(deckList)[0];
            currentModelId = Utils.getMapKeyArray(modelList)[0];
            refreshFieldSpinners();
        }

        modelSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        currentModelId = Utils.getMapKeyArray(modelList)[position];
                        refreshFieldSpinners();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                }
        );

        deckSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        currentDeckId = Utils.getMapKeyArray(deckList)[position];
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                }
        );

    }

    private void refreshFieldSpinners() {
        String[] fields = mAnkiDroid.getApi().getFieldList(currentModelId);
        String[] dictionaryElements = currentDictionary.getExportElementsList();
        String[] sharedElements = Constant.getSharedExportElements();
        String[] allElements = Utils.concatenate(sharedElements, dictionaryElements);
        fieldsMapItemList = new ArrayList<>();
        //if edit, than set spinner initial position
        if (planForEdit != null && currentModelId == planForEdit.getOutputModelId()) {
            for (String fld : fields) {
                Map<String, String> fldMap = planForEdit.getFieldsMap();
                if (fldMap.containsKey(fld)) {
                    String savedEle = fldMap.get(fld);
                    int pos = Arrays.asList(allElements).indexOf(savedEle);
                    if (pos == -1) {
                        pos = 0;
                    }
                    fieldsMapItemList.add(new FieldsMapItem(fld, allElements, pos));
                }
                //Arrays.asList(allElements).indexOf();
            }
        } else {
            for (String fld : fields) {
                fieldsMapItemList.add(new FieldsMapItem(fld, allElements));
            }
        }

        fieldsSpinnersContainer.setLayoutManager(new LinearLayoutManager(this));
        fieldsSpinnersContainer.setAdapter(new FieldMapSpinnerListAdapter(PlanEditorActivity.this, fieldsMapItemList));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(TestActivity.this, R.string.permission_granted, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_LONG).show();
        }
    }

}
