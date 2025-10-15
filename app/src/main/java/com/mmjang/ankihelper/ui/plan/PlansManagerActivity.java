package com.mmjang.ankihelper.ui.plan;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.app.NavUtils;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.mmjang.ankihelper.MyApplication;
import com.mmjang.ankihelper.R;
import com.mmjang.ankihelper.data.Settings;
import com.mmjang.ankihelper.data.database.AppDatabase;
import com.mmjang.ankihelper.data.plan.OutputPlanEntity;
import com.mmjang.ankihelper.data.plan.OutputPlanPOJO;
import com.mmjang.ankihelper.data.plan.OutputPlanRepository;
import com.mmjang.ankihelper.data.plan.OutputPlanRepositoryHelper;
import com.mmjang.ankihelper.ui.plan.helper.SimpleItemTouchHelperCallback;
import com.mmjang.ankihelper.util.DialogUtil;
import com.mmjang.ankihelper.util.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PlansManagerActivity extends AppCompatActivity {

    private List<OutputPlanPOJO> mPlanList;
    RecyclerView planListView;
    PlansAdapter mPlansAdapter;
    private static final String PLAN_SEP = "|||";
    private static final int ERROR_FORMAT = 1;
    private OutputPlanRepositoryHelper planRepositoryHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(Settings.getInstance(this).getPinkThemeQ()){
            setTheme(R.style.AppThemePink);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plans_manager);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize repository helper
        AppDatabase database = AppDatabase.Companion.getInstance(getApplicationContext());
        OutputPlanRepository repository = new OutputPlanRepository(database.outputPlanDao());
        planRepositoryHelper = new OutputPlanRepositoryHelper(repository, this);

        // Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_plan);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MyApplication.getAnkiDroid(getApplicationContext()).isAnkiDroidRunning()) {
                    Intent intent = new Intent(PlansManagerActivity.this, PlanEditorActivity.class);
                    startActivity(intent);
                } else {
                    DialogUtil.showStartAnkiDialog(PlansManagerActivity.this);
                }
            }
        });
        initPlanList();

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try{
                            MyApplication.getAnkiDroid(getApplicationContext()).getApi().getDeckList();
                        }catch (Exception e){
                        }
                    }
                }
        ).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Load plans using repository helper
        planRepositoryHelper.getAllPlans(new OutputPlanRepositoryHelper.PlansCallback() {
            @Override
            public void onSuccess(List<OutputPlanEntity> entities) {
                // Convert entities to POJOs
                List<OutputPlanPOJO> newList = convertEntitiesToPOJOs(entities);
                mPlanList.clear();
                mPlanList.addAll(newList);
                mPlansAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Throwable error) {
                Toast.makeText(PlansManagerActivity.this,
                        "Failed to load plans: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initPlanList() {
        mPlanList = new ArrayList<>();
        //Log.d("PlansManager:", plans.size() + "ge");
        planListView = (RecyclerView) findViewById(R.id.plan_list);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        planListView.setLayoutManager(llm);
        mPlansAdapter = new PlansAdapter(PlansManagerActivity.this, mPlanList);
        //planList.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        planListView.setAdapter(mPlansAdapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mPlansAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(planListView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_plans_manager_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

      if (item.getItemId() ==R.id.menu_item_export_plan) {
        exportPlans();
      }else if (item.getItemId() == R.id.menu_item_import_plan) {
        importPlans();
      } else if (item.getItemId() ==android.R.id.home) {
        NavUtils.navigateUpFromSameTask(this);
      }

        return true;
    }

    private void importPlans() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if(clipboard.hasPrimaryClip()){
            if(clipboard.getText()!=null){
                String plansString = clipboard.getText().toString();
                processPlanString(plansString);
            }
        }else{
            Toast.makeText(this, "剪贴板为空！", Toast.LENGTH_SHORT).show();
        }
    }

    private void processPlanString(String plansString) {
        String[] lines = plansString.split("\n");
        if(lines.length == 0){
            Toast.makeText(this, "格式错误！", Toast.LENGTH_SHORT).show();
            return ;
        }

        for(String line : lines){
            if(line.replace(" ","").replace("\t", "").equals("")){
                continue;//blank line
            }
            String[] items = line.split("\\|\\|\\|");
            if(items.length != 5){
                String errorMessage = line;
                errorMessage += "\n格式错误，每行项目数应为5";
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                continue;
            }
            try {
                String planName = items[0].trim();
                long deckId = Long.parseLong(items[1]);
                long modeld = Long.parseLong(items[2]);
                String dictKey = items[3].trim();
                String fieldMapString = items[4];
                for(OutputPlanPOJO outputPlan : mPlanList){
                    if(outputPlan.getPlanName().equals(planName)){
                        planName = planName + "_copy";
                        break;
                    }
                }
                OutputPlanEntity outputPlanEntity = new OutputPlanEntity();
                outputPlanEntity.setPlanName(planName);
                outputPlanEntity.setOutputDeckId(deckId);
                outputPlanEntity.setOutputModelId(modeld);
                outputPlanEntity.setDictionaryKey(dictKey);
                outputPlanEntity.setFieldsMap(fieldMapString);

                planRepositoryHelper.savePlan(outputPlanEntity, new OutputPlanRepositoryHelper.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        // Plan saved successfully
                    }

                    @Override
                    public void onError(Throwable error) {
                        Toast.makeText(PlansManagerActivity.this,
                                "Failed to save plan: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
            catch (Exception e){
                Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        onResume();
    }

    private List<OutputPlanPOJO> convertEntitiesToPOJOs(List<OutputPlanEntity> entities) {
        List<OutputPlanPOJO> pojos = new ArrayList<>();
        for (OutputPlanEntity entity : entities) {
            OutputPlanPOJO pojo = new OutputPlanPOJO();
            pojo.setPlanName(entity.getPlanName());
            pojo.setDictionaryKey(entity.getDictionaryKey());
            pojo.setOutputDeckId(entity.getOutputDeckId());
            pojo.setOutputModelId(entity.getOutputModelId());
            pojo.setFieldsMapString(entity.getFieldsMap());
            pojos.add(pojo);
        }
        return pojos;
    }

    private OutputPlanEntity convertPOJOToEntity(OutputPlanPOJO pojo) {
        OutputPlanEntity entity = new OutputPlanEntity();
        entity.setPlanName(pojo.getPlanName());
        entity.setDictionaryKey(pojo.getDictionaryKey());
        entity.setOutputDeckId(pojo.getOutputDeckId());
        entity.setOutputModelId(pojo.getOutputModelId());
        entity.setFieldsMap(pojo.getFieldsMapString());
        return entity;
    }

    private void exportPlans() {
        StringBuilder sb = new StringBuilder();
        for(OutputPlanPOJO plan : mPlanList){
            sb.append(plan.getPlanName());
            sb.append(PLAN_SEP);
            sb.append(plan.getOutputDeckId());
            sb.append(PLAN_SEP);
            sb.append(plan.getOutputModelId());
            sb.append(PLAN_SEP);
            sb.append(plan.getDictionaryKey());
            sb.append(PLAN_SEP);
            sb.append(plan.getFieldsMapString());
            sb.append("\n");
        }
        String exportedString = sb.toString();
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("plans string", exportedString);
        clipboard.setPrimaryClip(clip);
    }

}
