package com.lmyby.ankihelper.ui.plan

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lmyby.ankihelper.MyApplication
import com.lmyby.ankihelper.R
import com.lmyby.ankihelper.anki.AnkiDroidHelper
import com.lmyby.ankihelper.data.database.AppDatabase
import com.lmyby.ankihelper.data.dict.DictionaryRegister
import com.lmyby.ankihelper.data.dict.IDictionary
import com.lmyby.ankihelper.data.plan.OutputPlanEntity
import com.lmyby.ankihelper.data.plan.OutputPlanPOJO
import com.lmyby.ankihelper.data.plan.OutputPlanRepository
import com.lmyby.ankihelper.data.plan.OutputPlanRepositoryHelper
import com.lmyby.ankihelper.ui.base.BaseEditorActivity
import com.lmyby.ankihelper.util.Constant
import com.lmyby.ankihelper.util.Utils
import java.util.LinkedHashMap

/**
 * Activity for creating and editing output plans
 * Converted to Kotlin as part of Phase 5 activity migration
 */
class PlanEditorActivity : BaseEditorActivity() {

    private var planNameToEdit: String? = null
    private lateinit var mAnkiDroid: AnkiDroidHelper
    private var planForEdit: OutputPlanPOJO? = null
    private var deckList: Map<Long, String>? = null
    private var modelList: Map<Long, String>? = null
    private var dictionaryList: List<IDictionary>? = null
    private var fieldsMapItemList: MutableList<FieldsMapItem> = mutableListOf()
    private var currentDictionary: IDictionary? = null
    private var currentDeckId: Long = 0
    private var currentModelId: Long = 0
    private var planRepositoryHelper: OutputPlanRepositoryHelper? = null

    // Views
    private lateinit var planNameEditText: EditText
    private lateinit var dictionarySpinner: Spinner
    private lateinit var dictionaryIntroductionTextView: TextView
    private lateinit var deckSpinner: Spinner
    private lateinit var modelSpinner: Spinner
    private lateinit var fieldsSpinnersContainer: RecyclerView

    override fun getLayoutResId(): Int = R.layout.activity_plan_editor

    override fun initializeViews() {
        try {
            setViewMember()
            initAnkiApi()

            // Initialize database and load data in background to avoid ANR
            Thread {
                try {
                    // Initialize repository helper off main thread
                    val database = AppDatabase.getInstance(applicationContext)
                    val repository = OutputPlanRepository(database.outputPlanDao())
                    planRepositoryHelper = OutputPlanRepositoryHelper(repository, this@PlanEditorActivity)

                    // Load AnkiDroid data off main thread
                    loadDecksAndModels()

                    // Update UI on main thread
                    runOnUiThread {
                        handleIntent()
                        populateDictionary()
                        populateDecksAndModels()
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(
                            this@PlanEditorActivity,
                            "Failed to initialize: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }.start()
        } catch (e: Exception) {
            Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    override fun setupListeners() {
        // Listeners are set in populate methods
    }

    override fun validateInput(): Boolean {
        val planName = planNameEditText.text.toString().trim()
        if (planName.isEmpty()) {
            Toast.makeText(this, R.string.str_plan_name_should_not_be_blank, Toast.LENGTH_SHORT).show()
            return false
        }

        // Check if all fields are empty
        val allFieldsAreEmpty = fieldsMapItemList.all { item ->
            val v = item.exportedElementNames[item.selectedFieldPos]
            v == Constant.getSharedExportElements()[0]
        }

        if (allFieldsAreEmpty) {
            Toast.makeText(this, R.string.save_plan_error_all_blank, Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    override fun saveData(): Boolean {
        return savePlan()
    }

    private fun savePlan(): Boolean {
        // Check if repository is initialized
        if (planRepositoryHelper == null) {
            Toast.makeText(this, "Please wait, initializing...", Toast.LENGTH_SHORT).show()
            return false
        }

        // Start async save operation - no blocking!
        savePlanAsync()
        // Return false to prevent immediate finish() - we'll finish after save completes
        return false
    }

    private fun savePlanAsync() {
        val planName = planNameEditText.text.toString().trim()

        // Show progress on UI thread
        runOnUiThread {
            // Disable UI to prevent multiple saves
            planNameEditText.isEnabled = false
            dictionarySpinner.isEnabled = false
            deckSpinner.isEnabled = false
            modelSpinner.isEnabled = false
        }

        // Step 1: Check for name conflicts (async, non-blocking)
        planRepositoryHelper?.getPlanByName(planName, object : OutputPlanRepositoryHelper.PlanCallback {
            override fun onSuccess(existingPlan: OutputPlanEntity?) {
                // Check if there's a conflict
                val isConflict = if (planForEdit != null) {
                    // Editing: conflict only if name changed and new name exists
                    planName != planNameToEdit && existingPlan != null
                } else {
                    // Creating: conflict if name already exists
                    existingPlan != null
                }

                if (isConflict) {
                    runOnUiThread {
                        Toast.makeText(
                            this@PlanEditorActivity,
                            R.string.plan_already_exists,
                            Toast.LENGTH_SHORT
                        ).show()
                        // Re-enable UI
                        planNameEditText.isEnabled = true
                        dictionarySpinner.isEnabled = true
                        deckSpinner.isEnabled = true
                        modelSpinner.isEnabled = true
                    }
                } else {
                    // No conflict - proceed with save
                    performSave(planName)
                }
            }

            override fun onError(error: Throwable) {
                // Error checking for conflicts - proceed with save anyway
                performSave(planName)
            }
        })
    }

    private fun performSave(planName: String) {
        // Prepare plan data
        val plan = planForEdit ?: OutputPlanPOJO()

        plan.planName = planName
        plan.dictionaryKey = currentDictionary?.getDictionaryKey() ?: ""
        plan.outputDeckId = currentDeckId
        plan.outputModelId = currentModelId

        val map = LinkedHashMap<String, String>()
        for (item in fieldsMapItemList) {
            val k = item.field
            val v = item.exportedElementNames[item.selectedFieldPos]
            map[k] = v
        }
        plan.fieldsMap = map

        // Convert POJO to Entity
        val entity = OutputPlanEntity(
            planName = plan.planName,
            dictionaryKey = plan.dictionaryKey,
            outputDeckId = plan.outputDeckId,
            outputModelId = plan.outputModelId,
            fieldsMap = plan.getFieldsMapString()
        )

        // Perform save operation (async, non-blocking)
        val callback = object : OutputPlanRepositoryHelper.OperationCallback {
            override fun onSuccess() {
                runOnUiThread {
                    Toast.makeText(
                        this@PlanEditorActivity,
                        "Plan saved successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }

            override fun onError(error: Throwable) {
                runOnUiThread {
                    Toast.makeText(
                        this@PlanEditorActivity,
                        "Failed to save plan: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Re-enable UI on error
                    planNameEditText.isEnabled = true
                    dictionarySpinner.isEnabled = true
                    deckSpinner.isEnabled = true
                    modelSpinner.isEnabled = true
                }
            }
        }

        if (planNameToEdit != null) {
            // Update existing plan
            planRepositoryHelper?.refreshPlan(planNameToEdit!!, entity, callback)
        } else {
            // Insert new plan
            planRepositoryHelper?.savePlan(entity, callback)
        }
    }

    private fun setViewMember() {
        planNameEditText = findViewById(R.id.text_edit_plan_name)
        dictionarySpinner = findViewById(R.id.dictionary_spinner)
        dictionaryIntroductionTextView = findViewById(R.id.text_view_dictionary_introduction)
        deckSpinner = findViewById(R.id.deck_spinner)
        modelSpinner = findViewById(R.id.model_spinner)
        fieldsSpinnersContainer = findViewById(R.id.recycler_view_fields_map)
    }

    private fun initAnkiApi() {
        if (!::mAnkiDroid.isInitialized) {
            mAnkiDroid = AnkiDroidHelper(this)
        }
        if (!AnkiDroidHelper.isApiAvailable(MyApplication.getContext())) {
            Toast.makeText(this, R.string.api_not_available_message, Toast.LENGTH_LONG).show()
        }

        if (mAnkiDroid.shouldRequestPermission()) {
            mAnkiDroid.requestPermission(this, 0)
        }
    }

    private fun handleIntent() {
        intent?.let {
            if (it.action == Intent.ACTION_SEND) {
                val text = it.getStringExtra(Intent.EXTRA_TEXT)
                if (!text.isNullOrEmpty()) {
                    planNameToEdit = text
                    // Load plan asynchronously
                    planRepositoryHelper?.getPlanByName(
                        planNameToEdit!!,
                        object : OutputPlanRepositoryHelper.PlanCallback {
                            override fun onSuccess(entity: OutputPlanEntity?) {
                                entity?.let { plan ->
                                    // Convert Entity to POJO
                                    planForEdit = OutputPlanPOJO().apply {
                                        planName = plan.planName
                                        dictionaryKey = plan.dictionaryKey ?: ""
                                        outputDeckId = plan.outputDeckId
                                        outputModelId = plan.outputModelId
                                        setFieldsMapString(plan.fieldsMap ?: "")
                                    }
                                    // Set plan name unable to edit
                                    planNameEditText.setText(planNameToEdit)
                                    // planNameEditText.isEnabled = false
                                }
                            }

                            override fun onError(error: Throwable) {
                                Toast.makeText(
                                    this@PlanEditorActivity,
                                    "Failed to load plan: ${error.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                }
            }
        }
    }

    private fun loadDecksAndModels() {
        deckList = Utils.hashMap2LinkedHashMap(mAnkiDroid.api.deckList)
        modelList = Utils.hashMap2LinkedHashMap(mAnkiDroid.api.modelList)
    }

    private fun populateDictionary() {
        if (dictionaryList == null) {
            dictionaryList = DictionaryRegister.getDictionaryObjectList()
        }

        val dictionaryNameList = dictionaryList!!.map { it.getDictionaryName() }.toTypedArray()
        val dictionarySpinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            dictionaryNameList
        )
        dictionarySpinner.adapter = dictionarySpinnerAdapter

        planForEdit?.let { plan ->
            val savedKey = plan.dictionaryKey
            var found = false
            for (i in dictionaryList!!.indices) {
                val dict = dictionaryList!![i]
                // Use stable dictionary key for matching
                if (savedKey == dict.getDictionaryKey()) {
                    currentDictionary = dictionaryList!![i]
                    dictionaryIntroductionTextView.text = currentDictionary?.getIntroduction()
                    dictionarySpinner.setSelection(i)
                    found = true
                    Log.d("Editor", "Found dictionary: ${dict.getDictionaryName()}")
                    break
                }
            }
            if (!found) {
                val message = getString(R.string.error_dictionary_not_found, savedKey)
                Utils.showMessage(this@PlanEditorActivity, message)
            }
        } ?: run {
            val pos = dictionarySpinner.selectedItemPosition
            currentDictionary = dictionaryList!![pos]
            dictionaryIntroductionTextView.text = currentDictionary?.getIntroduction()
        }

        dictionarySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentDictionary = dictionaryList!![position]
                dictionaryIntroductionTextView.text = currentDictionary?.getIntroduction()
                refreshFieldSpinners()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun populateDecksAndModels() {
        val deckSpinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            Utils.getMapValueArray(deckList!!)
        )
        deckSpinner.adapter = deckSpinnerAdapter

        val modelSpinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            Utils.getMapValueArray(modelList!!)
        )
        modelSpinner.adapter = modelSpinnerAdapter

        planForEdit?.let { plan ->
            val savedDeckId = plan.outputDeckId
            val savedModelId = plan.outputModelId

            val deckIdList = Utils.getMapKeyArray(deckList!!)
            var deckPos = Utils.getArrayIndex(deckIdList, savedDeckId)
            if (deckPos == -1) {
                deckPos = 0
            }
            currentDeckId = deckIdList[deckPos]
            deckSpinner.setSelection(deckPos)

            val modelIdList = Utils.getMapKeyArray(modelList!!)
            var modelPos = Utils.getArrayIndex(modelIdList, savedModelId)
            if (modelPos == -1) {
                modelPos = 0
            }
            currentModelId = modelIdList[modelPos]
            modelSpinner.setSelection(modelPos)

            refreshFieldSpinners()
        } ?: run {
            currentDeckId = Utils.getMapKeyArray(deckList!!)[0]
            currentModelId = Utils.getMapKeyArray(modelList!!)[0]
            refreshFieldSpinners()
        }

        modelSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentModelId = Utils.getMapKeyArray(modelList!!)[position]
                refreshFieldSpinners()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        deckSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentDeckId = Utils.getMapKeyArray(deckList!!)[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun refreshFieldSpinners() {
        val fields = mAnkiDroid.api.getFieldList(currentModelId)
        val dictionaryElements = currentDictionary?.getExportElementsList() ?: emptyArray()
        val sharedElements = Constant.getSharedExportElements()
        val allElements = Utils.concatenate(sharedElements, dictionaryElements)

        fieldsMapItemList = mutableListOf()

        // If edit, then set spinner initial position
        if (planForEdit != null && currentModelId == planForEdit!!.outputModelId) {
            for (fld in fields) {
                val fldMap = planForEdit!!.fieldsMap
                if (fldMap.containsKey(fld)) {
                    val savedEle = fldMap[fld]
                    var pos = allElements.toList().indexOf(savedEle)
                    if (pos == -1) {
                        pos = 0
                    }
                    fieldsMapItemList.add(FieldsMapItem(fld, allElements, pos))
                }
            }
        } else {
            for (fld in fields) {
                fieldsMapItemList.add(FieldsMapItem(fld, allElements))
            }
        }

        fieldsSpinnersContainer.layoutManager = LinearLayoutManager(this)
        fieldsSpinnersContainer.adapter = FieldMapSpinnerListAdapter(this, fieldsMapItemList)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted
        } else {
            Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_LONG).show()
        }
    }
}
