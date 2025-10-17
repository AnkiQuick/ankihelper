package com.lmyby.ankiquicker.ui.plan

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.lmyby.ankiquicker.MyApplication
import com.lmyby.ankiquicker.R
import com.lmyby.ankiquicker.data.Settings
import com.lmyby.ankiquicker.data.database.AppDatabase
import com.lmyby.ankiquicker.data.plan.OutputPlanEntity
import com.lmyby.ankiquicker.data.plan.OutputPlanPOJO
import com.lmyby.ankiquicker.data.plan.OutputPlanRepository
import com.lmyby.ankiquicker.data.plan.OutputPlanRepositoryHelper
import com.lmyby.ankiquicker.ui.plan.helper.SimpleItemTouchHelperCallback
import com.lmyby.ankiquicker.util.DialogUtil

class PlansManagerActivity : AppCompatActivity() {

    private lateinit var mPlanList: MutableList<OutputPlanPOJO>
    private lateinit var planListView: RecyclerView
    private lateinit var mPlansAdapter: PlansAdapter
    private lateinit var planRepositoryHelper: OutputPlanRepositoryHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Settings.getInstance(this).getPinkThemeQ()) {
            setTheme(R.style.AppThemePink)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plans_manager)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize repository helper
        val database = AppDatabase.getInstance(applicationContext)
        val repository = OutputPlanRepository(database.outputPlanDao())
        planRepositoryHelper = OutputPlanRepositoryHelper(repository, this)

        val fab = findViewById<FloatingActionButton>(R.id.add_plan)
        fab.setOnClickListener {
            if (MyApplication.getAnkiDroid(applicationContext).isAnkiDroidRunning) {
                val intent = Intent(this@PlansManagerActivity, PlanEditorActivity::class.java)
                startActivity(intent)
            } else {
                DialogUtil.showStartAnkiDialog(this@PlansManagerActivity)
            }
        }

        initPlanList()

        Thread {
            try {
                MyApplication.getAnkiDroid(applicationContext).api.getDeckList()
            } catch (e: Exception) {
                // Silently fail
            }
        }.start()
    }

    override fun onResume() {
        super.onResume()
        // Load plans using repository helper
        planRepositoryHelper.getAllPlans(object : OutputPlanRepositoryHelper.PlansCallback {
            override fun onSuccess(plans: MutableList<OutputPlanEntity>) {
                // Convert entities to POJOs
                val newList = convertEntitiesToPOJOs(plans)
                mPlanList.clear()
                mPlanList.addAll(newList)
                mPlansAdapter.notifyDataSetChanged()
            }

            override fun onError(error: Throwable) {
                Toast.makeText(
                    this@PlansManagerActivity,
                    "Failed to load plans: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun initPlanList() {
        mPlanList = ArrayList()
        planListView = findViewById(R.id.plan_list)
        val llm = LinearLayoutManager(this)
        planListView.layoutManager = llm
        mPlansAdapter = PlansAdapter(this@PlansManagerActivity, mPlanList)
        planListView.adapter = mPlansAdapter

        val callback: ItemTouchHelper.Callback = SimpleItemTouchHelperCallback(mPlansAdapter)
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(planListView)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.activity_plans_manager_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_export_plan -> exportPlans()
            R.id.menu_item_import_plan -> importPlans()
            android.R.id.home -> NavUtils.navigateUpFromSameTask(this)
        }
        return true
    }

    private fun importPlans() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (clipboard.hasPrimaryClip()) {
            clipboard.text?.let { text ->
                val plansString = text.toString()
                processPlanString(plansString)
            }
        } else {
            Toast.makeText(this, "剪贴板为空！", Toast.LENGTH_SHORT).show()
        }
    }

    private fun processPlanString(plansString: String) {
        val lines = plansString.split("\n")
        if (lines.isEmpty()) {
            Toast.makeText(this, "格式错误！", Toast.LENGTH_SHORT).show()
            return
        }

        for (line in lines) {
            if (line.replace(" ", "").replace("\t", "").isEmpty()) {
                continue // blank line
            }

            val items = line.split("\\|\\|\\|".toRegex())
            if (items.size != 5) {
                val errorMessage = "$line\n格式错误，每行项目数应为5"
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                continue
            }

            try {
                var planName = items[0].trim()
                val deckId = items[1].toLong()
                val modelId = items[2].toLong()
                val dictKey = items[3].trim()
                val fieldMapString = items[4]

                // Check for duplicate plan names
                for (outputPlan in mPlanList) {
                    if (outputPlan.planName == planName) {
                        planName = "${planName}_copy"
                        break
                    }
                }

                val outputPlanEntity = OutputPlanEntity(
                    planName = planName,
                    dictionaryKey = dictKey,
                    outputDeckId = deckId,
                    outputModelId = modelId,
                    fieldsMap = fieldMapString
                )

                planRepositoryHelper.savePlan(outputPlanEntity, object : OutputPlanRepositoryHelper.OperationCallback {
                    override fun onSuccess() {
                        // Plan saved successfully
                    }

                    override fun onError(error: Throwable) {
                        Toast.makeText(
                            this@PlansManagerActivity,
                            "Failed to save plan: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            } catch (e: Exception) {
                Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
        onResume()
    }

    private fun convertEntitiesToPOJOs(entities: List<OutputPlanEntity>): List<OutputPlanPOJO> {
        val pojos = ArrayList<OutputPlanPOJO>()
        for (entity in entities) {
            val pojo = OutputPlanPOJO().apply {
                planName = entity.planName ?: ""
                dictionaryKey = entity.dictionaryKey ?: ""
                outputDeckId = entity.outputDeckId
                outputModelId = entity.outputModelId
                setFieldsMapString(entity.fieldsMap ?: "")
            }
            pojos.add(pojo)
        }
        return pojos
    }

    private fun convertPOJOToEntity(pojo: OutputPlanPOJO): OutputPlanEntity {
        return OutputPlanEntity(
            planName = pojo.planName,
            dictionaryKey = pojo.dictionaryKey,
            outputDeckId = pojo.outputDeckId,
            outputModelId = pojo.outputModelId,
            fieldsMap = pojo.getFieldsMapString()
        )
    }

    private fun exportPlans() {
        val sb = StringBuilder()
        for (plan in mPlanList) {
            sb.append(plan.planName)
            sb.append(PLAN_SEP)
            sb.append(plan.outputDeckId)
            sb.append(PLAN_SEP)
            sb.append(plan.outputModelId)
            sb.append(PLAN_SEP)
            sb.append(plan.dictionaryKey)
            sb.append(PLAN_SEP)
            sb.append(plan.getFieldsMapString())
            sb.append("\n")
        }
        val exportedString = sb.toString()
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("plans string", exportedString)
        clipboard.setPrimaryClip(clip)
    }

    companion object {
        private const val PLAN_SEP = "|||"
        private const val ERROR_FORMAT = 1
    }
}
