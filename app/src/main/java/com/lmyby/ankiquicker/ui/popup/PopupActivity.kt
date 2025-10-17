package com.lmyby.ankiquicker.ui.popup

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.ActionMode
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.bumptech.glide.Glide
import com.ichi2.anki.api.NoteInfo
import com.lmyby.ankiquicker.MyApplication
import com.lmyby.ankiquicker.R
import com.lmyby.ankiquicker.anki.AnkiDroidHelper
import com.lmyby.ankiquicker.data.Settings
import com.lmyby.ankiquicker.data.ai.AIManager
import com.lmyby.ankiquicker.data.database.AppDatabase
import com.lmyby.ankiquicker.data.dict.Definition
import com.lmyby.ankiquicker.data.dict.DictionaryRegister
import com.lmyby.ankiquicker.data.dict.IDictionary
import com.lmyby.ankiquicker.data.dict.UrbanAutoCompleteAdapter
import com.lmyby.ankiquicker.data.history.HistoryUtil
import com.lmyby.ankiquicker.data.plan.OutputPlanEntity
import com.lmyby.ankiquicker.data.plan.OutputPlanPOJO
import com.lmyby.ankiquicker.data.plan.OutputPlanRepository
import com.lmyby.ankiquicker.data.plan.OutputPlanRepositoryHelper
import com.lmyby.ankiquicker.domain.CBWatcherService
import com.lmyby.ankiquicker.domain.PlayAudioManager
import com.lmyby.ankiquicker.domain.PronounceManager
import com.lmyby.ankiquicker.ui.widget.BigBangLayout
import com.lmyby.ankiquicker.ui.widget.BigBangLayoutWrapper
import com.lmyby.ankiquicker.util.*
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2.Error
import com.tonyodev.fetch2core.DownloadBlock
import com.tonyodev.fetch2core.Func
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.ref.WeakReference
import android.widget.SimpleCursorAdapter

class PopupActivity : AppCompatActivity(), BigBangLayoutWrapper.ActionListener {

    private lateinit var dictionaryList: List<IDictionary>
    private var currentDicitonary: IDictionary? = null
    private lateinit var outputPlanList: List<OutputPlanPOJO>
    private var currentOutputPlan: OutputPlanPOJO? = null
    private lateinit var settings: Settings
    private var mTextToProcess: String = ""
    private var mPlanNameFromIntent: String? = null
    private var mCurrentKeyWord: String? = null
    private var mNoteEditedByUser: String = ""
    private var mTagEditedByUser: MutableSet<String> = HashSet()
    private var mTargetWord: String? = null
    private var mUrl: String = ""
    private var mUpdateNoteId: Long = 0L
    private var isDuringPlanSpinnerInit: Boolean = false
    private var mUpdateAction: String? = null
    private var mTranslatedResult: String = ""
    private var needTranslation: Boolean = false
    private var isFromAndroidQClipboard: Boolean = false

    // Views
    private lateinit var act: AutoCompleteTextView
    private lateinit var btnSearch: ImageButton
    private lateinit var btnPronounce: ImageButton
    private lateinit var planSpinner: Spinner
    private lateinit var pronounceLanguageSpinner: Spinner
    private lateinit var mBtnEditNote: ImageButton
    private lateinit var mBtnEditTag: ImageButton
    private lateinit var mBtnTranslation: ImageButton
    private lateinit var mBtnFooterRotateLeft: ImageButton
    private lateinit var mBtnFooterRotateRight: ImageButton
    private lateinit var mBtnFooterScrollup: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var mAudioProgress: ProgressBar
    private lateinit var mEditTextArea: EditText
    private lateinit var mBtnEditMode: ImageButton
    private lateinit var mBtnSaveChanges: ImageButton
    private lateinit var mBtnDiscardChanges: ImageButton
    private lateinit var mCardViewTranslation: CardView
    private lateinit var mEditTextTranslation: EditText
    private lateinit var scrollView: ScrollView
    private lateinit var viewDefinitionList: LinearLayout
    private lateinit var bigBangLayout: BigBangLayout
    private lateinit var bigBangLayoutWrapper: BigBangLayoutWrapper

    private var mDefinitionList: List<Definition>? = null
    private var mMediaPlayer: MediaPlayer? = null
    private var fetch: Fetch? = null
    private var isFetchDownloading: Boolean = false

    // Edit mode
    private enum class EditMode {
        SELECT_MODE,
        EDIT_MODE
    }

    private var currentEditMode: EditMode = EditMode.SELECT_MODE
    private var originalText: String? = null

    // Managers
    private lateinit var intentHandler: PopupIntentHandler
    private lateinit var searchManager: PopupSearchManager
    private lateinit var translationManager: PopupTranslationManager
    private lateinit var dialogManager: PopupDialogManager

    // Handler
    private val mHandler = PopupHandler(this)

    // Memory-leak-safe Handler implementation
    private class PopupHandler(activity: PopupActivity) : Handler(Looper.getMainLooper()) {
        private val activityRef: WeakReference<PopupActivity> = WeakReference(activity)

        override fun handleMessage(msg: Message) {
            val activity = activityRef.get() ?: return

            when (msg.what) {
                PROCESS_DEFINITION_LIST -> {
                    activity.searchManager.showSearchButton()
                    @Suppress("UNCHECKED_CAST")
                    activity.mDefinitionList = msg.obj as? List<Definition>
                    activity.mDefinitionList?.let {
                        activity.searchManager.processDefinitionList(it)
                    }
                }
                ASYNC_SEARCH_FAILED -> {
                    activity.searchManager.showSearchButton()
                    Toast.makeText(activity, msg.obj as String, Toast.LENGTH_LONG).show()
                }
                TRANSLATION_DONE -> {
                    val result = msg.obj as String
                    val splitted = result.split("\n")
                    if (splitted.isNotEmpty() && splitted[0] == "error") {
                        Toast.makeText(activity, result, Toast.LENGTH_SHORT).show()
                        activity.translationManager.setTranslationButtonEnabled(true)
                        activity.translationManager.showTranslateNormal()
                        return
                    }
                    activity.translationManager.setTranslationText(result)
                    activity.translationManager.showTranslateDone()
                    activity.translationManager.showTranslationCardView(true)
                    activity.translationManager.setTranslationButtonEnabled(true)
                }
                TRANSLATIOn_FAILED -> {
                    activity.translationManager.showTranslateNormal()
                    Toast.makeText(activity, msg.obj as String, Toast.LENGTH_SHORT).show()
                    activity.translationManager.setTranslationButtonEnabled(true)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Settings.getInstance(this).getPinkThemeQ()) {
            setTheme(R.style.TransparentPink)
        }
        super.onCreate(savedInstanceState)
        setStatusBarColor()
        setContentView(R.layout.activity_popup)
        // Use modern transition API for API 34+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, R.anim.slide_in, R.anim.slide_out)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        }

        scrollView = findViewById(R.id.scrollView)
        OverScrollDecoratorHelper.setUpOverScroll(scrollView)

        assignViews()
        loadData() // Must be called before managers that depend on 'settings'
        initSearchManager()
        initTranslationManager()
        initDialogManager()
        initBigBangLayout()
        populatePlanSpinner()
        populateLanguageSpinner()
        setEventListener()

        if (settings.getMoniteClipboardQ()) {
            checkAndRequestClipboardPermissions()
            startCBService()
        }

        handleIntent()
        asyncInvokeDroid()
    }

    private fun asyncInvokeDroid() {
        Thread {
            try {
                MyApplication.getAnkiDroid(MyApplication.getContext()).api.getDeckList()
            } catch (e: Exception) {
                // Silently fail
            }
        }.start()
    }

    private fun setTargetWord() {
        if (!mTargetWord.isNullOrEmpty()) {
            for (line in bigBangLayout.getLines()) {
                line.getItems()?.forEach { item ->
                    if (item.getText() == mTargetWord) {
                        item.setSelected(true)
                    }
                }
            }
            act.setText(mTargetWord)
            searchManager.asyncSearch(mTargetWord!!, mTextToProcess, currentDicitonary, currentOutputPlan)
        } else {
            if (mTextToProcess.matches(Regex("[a-zA-Z\\-]*"))) {
                act.setText(mTextToProcess)
                searchManager.asyncSearch(mTextToProcess, mTextToProcess, currentDicitonary, currentOutputPlan)
            }
        }
    }

    private fun setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= 21) {
            val statusBarColor = window.statusBarColor
            window.statusBarColor = statusBarColor
        }
    }

    private fun assignViews() {
        act = findViewById(R.id.edit_text_hwd)
        btnSearch = findViewById(R.id.btn_search)
        btnPronounce = findViewById(R.id.btn_pronounce)
        planSpinner = findViewById(R.id.plan_spinner)
        pronounceLanguageSpinner = findViewById(R.id.language_spinner)
        viewDefinitionList = findViewById(R.id.view_definition_list)
        mBtnEditNote = findViewById(R.id.footer_note)
        mBtnEditTag = findViewById(R.id.footer_tag)
        progressBar = findViewById(R.id.progress_bar)
        bigBangLayout = findViewById(R.id.bigbang)
        bigBangLayoutWrapper = findViewById(R.id.bigbang_wrapper)
        mCardViewTranslation = findViewById(R.id.cardview_translation)
        mBtnTranslation = findViewById(R.id.footer_translate)
        mEditTextTranslation = findViewById(R.id.edittext_translation)
        mBtnFooterRotateLeft = findViewById(R.id.footer_rotate_left)
        mBtnFooterRotateRight = findViewById(R.id.footer_rotate_right)
        mBtnFooterScrollup = findViewById(R.id.footer_scroll_up)
        mAudioProgress = findViewById(R.id.audio_progress)

        // Edit mode views
        mEditTextArea = findViewById(R.id.edit_text_area)
        mBtnEditMode = findViewById(R.id.btn_edit_mode)
        mBtnSaveChanges = findViewById(R.id.btn_save_changes)
        mBtnDiscardChanges = findViewById(R.id.btn_discard_changes)

        // Initialize edit mode views to correct initial state
        bigBangLayoutWrapper.visibility = View.VISIBLE
        mEditTextArea.visibility = View.GONE
        mBtnEditMode.visibility = View.VISIBLE
        mBtnSaveChanges.visibility = View.GONE
        mBtnDiscardChanges.visibility = View.GONE
    }

    private fun loadData() {
        dictionaryList = DictionaryRegister.getDictionaryObjectList()

        // Load output plans using blocking method
        try {
            val database = AppDatabase.getInstance(applicationContext)
            val repository = OutputPlanRepository(database.outputPlanDao())
            val plans = OutputPlanRepositoryHelper.getAllPlansBlocking(repository)
            outputPlanList = convertEntitiesToPOJOs(plans)
        } catch (e: Exception) {
            Log.e("PopupActivity", "Error loading plans", e)
            outputPlanList = ArrayList()
        }

        settings = Settings.getInstance(this)

        // Load tag
        val loadQ = settings.getSetAsDefaultTag()
        if (loadQ) {
            mTagEditedByUser = Utils.fromStringToTagSet(settings.getDefaulTag()).toMutableSet()
        }

        // Check if outputPlanList is empty
        if (outputPlanList.isEmpty()) {
            Utils.showMessage(this, resources.getString(R.string.toast_no_available_plan))
        }
    }

    private fun convertEntitiesToPOJOs(entities: List<OutputPlanEntity>): List<OutputPlanPOJO> {
        return entities.map { entity ->
            OutputPlanPOJO().apply {
                planName = entity.planName ?: ""
                dictionaryKey = entity.dictionaryKey ?: ""
                outputDeckId = entity.outputDeckId
                outputModelId = entity.outputModelId
                setFieldsMapString(entity.fieldsMap ?: "")
            }
        }
    }

    private fun populatePlanSpinner() {
        if (outputPlanList.isEmpty()) {
            return
        }

        val planNameArr = outputPlanList.map { it.planName }.toTypedArray()
        val planSpinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            planNameArr
        )
        planSpinner.adapter = planSpinnerAdapter
        planSpinnerAdapter.setDropDownViewResource(R.layout.centered_spinner_dropdown_item)

        // Set plan to last selected plan
        var lastSelectedPlan = settings.getLastSelectedPlan()
        if (lastSelectedPlan.isEmpty()) {
            if (outputPlanList.isNotEmpty()) {
                settings.setLastSelectedPlan(outputPlanList[0].planName)
                currentOutputPlan = outputPlanList[0]
                currentDicitonary = getDictionaryFromOutputPlan(currentOutputPlan!!)
                if (currentDicitonary == null) {
                    val message = String.format(
                        "方案\"%s\"所选词典\"%s\"不存在，请检查是否需要重新导入自定义词典",
                        currentOutputPlan!!.planName,
                        currentOutputPlan!!.dictionaryKey
                    )
                    Utils.showMessage(this, message)
                } else {
                    setActAdapter(currentDicitonary!!)
                }
            } else {
                return
            }
        }

        // If user added intent parameter to control which plan to use
        mPlanNameFromIntent = intent.getStringExtra(Constant.INTENT_ANKIHELPER_PLAN_NAME)
        if (mPlanNameFromIntent != null) {
            lastSelectedPlan = mPlanNameFromIntent!!
        }

        var find = false
        outputPlanList.forEachIndexed { i, plan ->
            if (plan.planName == lastSelectedPlan) {
                isDuringPlanSpinnerInit = true
                planSpinner.setSelection(i)
                currentOutputPlan = outputPlanList[i]
                currentDicitonary = getDictionaryFromOutputPlan(currentOutputPlan!!)
                if (currentDicitonary == null) {
                    val message = String.format(
                        "方案\"%s\"所选词典\"%s\"不存在，请检查是否需要重新导入自定义词典",
                        currentOutputPlan!!.planName,
                        currentOutputPlan!!.dictionaryKey
                    )
                    Utils.showMessage(this, message)
                    return
                }
                setActAdapter(currentDicitonary!!)
                find = true
                return@forEachIndexed
            }
        }

        if (!find) {
            if (outputPlanList.isNotEmpty()) {
                settings.setLastSelectedPlan(outputPlanList[0].planName)
                currentOutputPlan = outputPlanList[0]
                currentDicitonary = getDictionaryFromOutputPlan(currentOutputPlan!!)
                if (currentDicitonary == null) {
                    val message = String.format(
                        "方案\"%s\"所选词典\"%s\"不存在，请检查是否需要重新导入自定义词典",
                        currentOutputPlan!!.planName,
                        currentOutputPlan!!.dictionaryKey
                    )
                    Utils.showMessage(this, message)
                } else {
                    setActAdapter(currentDicitonary!!)
                }
            }
        }
    }

    private fun populateLanguageSpinner() {
        val languages = PronounceManager.getAvailablePronounceLanguage(this)
        val languagesSpinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            languages
        )
        languagesSpinnerAdapter.setDropDownViewResource(R.layout.centered_spinner_dropdown_item)
        pronounceLanguageSpinner.adapter = languagesSpinnerAdapter
        val lastPronounceLanguageIndex = settings.getLastPronounceLanguage()
        pronounceLanguageSpinner.setSelection(lastPronounceLanguageIndex)
    }

    private fun initSearchManager() {
        searchManager = PopupSearchManager(
            this, mHandler, progressBar,
            btnSearch, btnPronounce, viewDefinitionList
        )
        searchManager.setDefinitionProcessor(object : PopupSearchManager.DefinitionProcessor {
            override fun getCardFromDefinition(def: Definition): View {
                return this@PopupActivity.getCardFromDefinition(def)
            }
        })
    }

    private fun initTranslationManager() {
        translationManager = PopupTranslationManager(
            this, mHandler,
            mBtnTranslation, mCardViewTranslation, mEditTextTranslation
        )
        translationManager.setTranslationCallback(object : PopupTranslationManager.TranslationCallback {
            override fun performTranslation(text: String): String {
                return AIManager.getInstance().translateTextWithDefaultTranslator(text)
            }
        })
    }

    private fun initDialogManager() {
        dialogManager = PopupDialogManager(this, settings)
        dialogManager.setNoteEditCallback(object : PopupDialogManager.NoteEditCallback {
            override fun getCurrentNote(): String = mNoteEditedByUser

            override fun onNoteEdited(newNote: String) {
                mNoteEditedByUser = newNote
            }
        })
        dialogManager.setTagEditCallback(object : PopupDialogManager.TagEditCallback {
            override fun getCurrentTags(): Set<String> = mTagEditedByUser

            override fun onTagsEdited(newTags: Set<String>) {
                mTagEditedByUser = newTags.toMutableSet()
            }
        })
    }

    private fun initBigBangLayout() {
        bigBangLayout.setShowSymbol(true)
        bigBangLayout.setShowSpace(true)
        bigBangLayout.setShowSection(true)
        bigBangLayout.setItemSpace(4)
        bigBangLayout.setLineSpace(2)
        bigBangLayout.setTextPadding(5)
        bigBangLayout.setTextPaddingPort(5)
        bigBangLayoutWrapper.setStickHeader(true)
        bigBangLayoutWrapper.setActionListener(this)
    }

    private fun setEventListener() {
        val btnCancelBlank = findViewById<Button>(R.id.btn_cancel_blank)
        val btnCancelBlankAboveCard = findViewById<Button>(R.id.btn_cancel_blank_above_card)

        btnCancelBlank.setOnClickListener { finish() }
        btnCancelBlankAboveCard.setOnClickListener { finish() }

        planSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentOutputPlan = outputPlanList[position]
                currentDicitonary = getDictionaryFromOutputPlan(currentOutputPlan!!)
                if (currentDicitonary != null) {
                    setActAdapter(currentDicitonary!!)
                }
                settings.setLastSelectedPlan(currentOutputPlan!!.planName)
                val actContent = act.text.toString()

                if (isDuringPlanSpinnerInit) {
                    isDuringPlanSpinnerInit = false
                } else {
                    if (actContent.trim().isNotEmpty()) {
                        searchManager.asyncSearch(actContent, mTextToProcess, currentDicitonary, currentOutputPlan)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        pronounceLanguageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                settings.setLastPronounceLanguage(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnSearch.setOnClickListener {
            val word = act.text.toString()
            if (word.isNotEmpty()) {
                searchManager.asyncSearch(word, mTextToProcess, currentDicitonary, currentOutputPlan)
                Utils.hideSoftKeyboard(this)
            }
        }

        btnPronounce.setOnClickListener {
            val word = act.text.toString()
            PlayAudioManager.playPronounceVoice(this, word)
        }

        mBtnEditNote.setOnClickListener {
            dialogManager.showEditNoteDialog()
        }

        mBtnEditTag.setOnClickListener {
            dialogManager.showTagDialog()
        }

        act.onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
            Log.d("autocomplete", "item clicked")
            act.post { btnSearch.callOnClick() }
        }

        mBtnTranslation.setOnClickListener {
            if (translationManager.translationText.isEmpty()) {
                translationManager.setTranslationButtonEnabled(false)
                translationManager.showTranslateLoading()
                translationManager.asyncTranslate(mTextToProcess)
            }
        }

        mBtnFooterRotateRight.setOnClickListener {
            val mPlanSize = outputPlanList.size
            val currentPos = planSpinner.selectedItemPosition
            if (mPlanSize > 1) {
                if (currentPos < mPlanSize - 1) {
                    planSpinner.setSelection(currentPos + 1)
                } else if (currentPos == mPlanSize - 1) {
                    planSpinner.setSelection(0)
                }
            } else {
                Toast.makeText(this, R.string.str_only_one_plan_cant_switch, Toast.LENGTH_SHORT).show()
            }
        }

        mBtnFooterRotateLeft.setOnClickListener {
            val mPlanSize = outputPlanList.size
            val currentPos = planSpinner.selectedItemPosition
            if (mPlanSize > 1) {
                if (currentPos > 0) {
                    planSpinner.setSelection(currentPos - 1)
                } else if (currentPos == 0) {
                    planSpinner.setSelection(mPlanSize - 1)
                }
            } else {
                Toast.makeText(this, R.string.str_only_one_plan_cant_switch, Toast.LENGTH_SHORT).show()
            }
        }

        mBtnFooterScrollup.setOnClickListener {
            if (scrollView.scrollY > 0) {
                scrollView.fullScroll(ScrollView.FOCUS_UP)
            }
        }

        setupEditModeListeners()
    }

    private fun setupEditModeListeners() {
        act.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchManager.showPronounce(s?.length ?: 0 > 0)
            }
        })

        mBtnEditMode.setOnClickListener { switchToEditMode() }
        mBtnSaveChanges.setOnClickListener { saveChangesAndReturnToSelectMode() }
        mBtnDiscardChanges.setOnClickListener { discardChangesAndReturnToSelectMode() }
    }

    private fun switchToEditMode() {
        originalText = getCurrentTextFromBigBangLayout()

        bigBangLayoutWrapper.visibility = View.GONE
        mBtnEditMode.visibility = View.GONE
        mEditTextArea.visibility = View.VISIBLE
        mBtnSaveChanges.visibility = View.VISIBLE
        mBtnDiscardChanges.visibility = View.VISIBLE

        mEditTextArea.setText(originalText)
        mEditTextArea.requestFocus()

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(mEditTextArea, InputMethodManager.SHOW_IMPLICIT)

        currentEditMode = EditMode.EDIT_MODE
    }

    private fun saveChangesAndReturnToSelectMode() {
        val modifiedText = mEditTextArea.text.toString()
        mTextToProcess = modifiedText

        mEditTextArea.visibility = View.GONE
        mBtnSaveChanges.visibility = View.GONE
        mBtnDiscardChanges.visibility = View.GONE
        bigBangLayoutWrapper.visibility = View.VISIBLE
        mBtnEditMode.visibility = View.VISIBLE

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(mEditTextArea.windowToken, 0)

        currentEditMode = EditMode.SELECT_MODE
        populateWordSelectBox()
    }

    private fun discardChangesAndReturnToSelectMode() {
        mTextToProcess = originalText ?: ""

        mEditTextArea.visibility = View.GONE
        mBtnSaveChanges.visibility = View.GONE
        mBtnDiscardChanges.visibility = View.GONE
        bigBangLayoutWrapper.visibility = View.VISIBLE
        mBtnEditMode.visibility = View.VISIBLE

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(mEditTextArea.windowToken, 0)

        currentEditMode = EditMode.SELECT_MODE
        populateWordSelectBox()
    }

    private fun getCurrentTextFromBigBangLayout(): String {
        return mTextToProcess
    }

    private fun getDictionaryFromOutputPlan(outputPlan: OutputPlanPOJO): IDictionary? {
        val dictionaryKey = outputPlan.dictionaryKey
        return dictionaryList.firstOrNull { it.getDictionaryKey() == dictionaryKey }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (isFromAndroidQClipboard) {
            if (!Settings.getInstance(MyApplication.getContext()).getMoniteClipboardQ()) {
                return
            }

            if (hasFocus) {
                try {
                    val cb = getSystemService(ClipboardManager::class.java)
                    if (cb?.hasPrimaryClip() == true) {
                        val clipData = cb.primaryClip
                        if (clipData != null && clipData.itemCount > 0) {
                            val text = clipData.getItemAt(0).text
                            if (text != null) {
                                mTextToProcess = text.toString()
                                Log.d("PopupActivity", "Clipboard text retrieved, length: ${mTextToProcess.length}")

                                populateWordSelectBox()
                                bigBangLayout.post {
                                    setTargetWord()
                                    if (Utils.containsTranslationField(currentOutputPlan)) {
                                        translationManager.asyncTranslate(mTextToProcess)
                                    }
                                }
                            } else {
                                Log.w("PopupActivity", "Clipboard text is null")
                                Toast.makeText(this, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Log.w("PopupActivity", "No clipboard content available")
                        Toast.makeText(this, "No clipboard content available", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: SecurityException) {
                    Log.e("PopupActivity", "Security exception accessing clipboard", e)
                    Toast.makeText(
                        this,
                        "Cannot access clipboard due to security restrictions",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (e: Exception) {
                    Log.e("PopupActivity", "Error accessing clipboard", e)
                    Toast.makeText(this, "Error accessing clipboard", Toast.LENGTH_SHORT).show()
                }

                isFromAndroidQClipboard = false
            } else {
                Log.d("PopupActivity", "No focus, deferring clipboard access")
            }
        }
    }

    private fun checkAndRequestClipboardPermissions() {
        intentHandler.checkClipboardPermissions()
    }

    private fun handleIntent() {
        intentHandler = PopupIntentHandler(this)

        val data = intentHandler.processIntent(intent)

        mTextToProcess = data.textToProcess
        mTargetWord = data.targetWord
        mUrl = data.url
        mNoteEditedByUser = data.noteEditedByUser
        mTagEditedByUser = data.tagEditedByUser.toMutableSet()
        mUpdateNoteId = data.updateNoteId
        mUpdateAction = data.updateAction
        mPlanNameFromIntent = data.planNameFromIntent
        isFromAndroidQClipboard = data.isFromAndroidQClipboard

        if (mTextToProcess.isEmpty()) {
            val action = intent?.action
            if (Intent.ACTION_PROCESS_TEXT == action) {
                Toast.makeText(
                    this,
                    "No text was received. Try selecting text again or use the Share option instead.",
                    Toast.LENGTH_LONG
                ).show()
            } else if (Intent.ACTION_SEND == action) {
                Toast.makeText(this, "No text content received from app", Toast.LENGTH_SHORT).show()
            }
        }

        checkAndRequestClipboardPermissions()
        populateWordSelectBox()
        HistoryUtil.savePopupOpen(mTextToProcess)

        bigBangLayout.post {
            setTargetWord()
            if (Utils.containsTranslationField(currentOutputPlan)) {
                translationManager.asyncTranslate(mTextToProcess)
            }
        }
    }

    private fun populateWordSelectBox() {
        populateWordSelectBoxAsync(mTextToProcess)
    }

    private fun populateWordSelectBoxAsync(textToProcess: String) {
        progressBar.visibility = View.VISIBLE

        if (textToProcess.trim().isEmpty()) {
            Log.w("PopupActivity", "populateWordSelectBoxAsync called with empty text")
            progressBar.visibility = View.GONE
            Toast.makeText(this, "No text content to process", Toast.LENGTH_SHORT).show()
            return
        }

        Thread {
            try {
                Log.d("PopupActivity", "Starting text processing, text length: ${textToProcess.length}")

                val localSegments = TextSplitter.getLocalSegments(textToProcess)

                Log.d("PopupActivity", "Text processing complete, segments count: ${localSegments.size}")

                runOnUiThread {
                    updateBigBangLayoutWithSegments(localSegments)
                }
            } catch (e: Exception) {
                Log.e("PopupActivity", "Error processing text", e)
                val errorMsg = e.message ?: "Unknown error"
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this,
                        "Error processing text: $errorMsg",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }.start()
    }

    private fun updateBigBangLayoutWithSegments(localSegments: List<String>) {
        Log.d("PopupActivity", "Updating BigBangLayout with segments, count: ${localSegments.size}")

        progressBar.visibility = View.GONE
        bigBangLayout.removeAllViews()

        if (localSegments.isEmpty()) {
            Log.w("PopupActivity", "No segments to display")
            Toast.makeText(this, "No text content to display", Toast.LENGTH_SHORT).show()
            return
        }

        val maxSegments = 500
        var segmentCount = 0
        var wasTruncated = false

        for (localSegment in localSegments) {
            if (segmentCount >= maxSegments) {
                wasTruncated = true
                bigBangLayout.addTextItem("...")
                break
            }
            bigBangLayout.addTextItem(localSegment)
            segmentCount++
        }

        if (wasTruncated) {
            Toast.makeText(
                this,
                "Text was truncated for performance (showing first $maxSegments segments)",
                Toast.LENGTH_LONG
            ).show()
        }

        Log.d("PopupActivity", "Displayed $segmentCount segments in BigBangLayout")
    }

    private fun setActAdapter(dict: IDictionary) {
        val adapter = dict.getAutoCompleteAdapter(this, android.R.layout.simple_spinner_dropdown_item)
        when (adapter) {
            is SimpleCursorAdapter -> act.setAdapter(adapter)
            is UrbanAutoCompleteAdapter -> act.setAdapter(adapter)
        }

        act.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                if (act.text.toString().trim().isNotEmpty()) {
                    act.showDropDown()
                }
            }
        }
    }

    private fun getCardFromDefinition(def: Definition): View {
        val view = if (settings.getLeftHandModeQ()) {
            LayoutInflater.from(this).inflate(R.layout.definition_item_left, null)
        } else {
            LayoutInflater.from(this).inflate(R.layout.definition_item, null)
        }

        val textVeiwDefinition = view.findViewById<TextView>(R.id.textview_definition)
        val btnAddDefinition = view.findViewById<ImageButton>(R.id.btn_add_definition)
        val btnAddDefinitionLarge = view.findViewById<LinearLayout>(R.id.btn_add_definition_large)
        val defImage = view.findViewById<ImageView>(R.id.def_img)

        btnAddDefinitionLarge.setOnClickListener { btnAddDefinition.callOnClick() }

        textVeiwDefinition.text = HtmlCompat.fromHtml(def.displayHtml, HtmlCompat.FROM_HTML_MODE_COMPACT)

        if (def.displayHtml.isEmpty()) {
            textVeiwDefinition.visibility = View.GONE
        }

        if (!def.imageUrl.isNullOrEmpty()) {
            Glide.with(this).load(def.imageUrl).into(defImage)
            defImage.visibility = View.VISIBLE
        }

        if (!def.audioUrl.isNullOrEmpty()) {
            textVeiwDefinition.setTextIsSelectable(false)
            textVeiwDefinition.setOnClickListener {
                if (mMediaPlayer == null) {
                    mMediaPlayer = MediaPlayer().apply {
                        // Use modern AudioAttributes API (API 21+)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            setAudioAttributes(
                                AudioAttributes.Builder()
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .build()
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            setAudioStreamType(AudioManager.STREAM_MUSIC)
                        }
                        setOnPreparedListener {
                            it.start()
                            mAudioProgress.visibility = View.GONE
                        }
                    }
                }
                try {
                    if (mMediaPlayer?.isPlaying == true) {
                        mMediaPlayer?.reset()
                    }
                } catch (e: IllegalStateException) {
                    // Ignore
                }
                try {
                    mMediaPlayer?.setDataSource(this, Uri.parse(def.audioUrl))
                    mAudioProgress.visibility = View.VISIBLE
                    mMediaPlayer?.prepareAsync()
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                } catch (e: IllegalStateException) {
                    // Ignore
                }

                mMediaPlayer?.setOnCompletionListener {
                    it.reset()
                    mAudioProgress.visibility = View.GONE
                }

                mMediaPlayer?.setOnErrorListener { mp, _, _ ->
                    mp.reset()
                    Toast.makeText(this, "Failed to play audio, check your connection.", Toast.LENGTH_SHORT)
                        .show()
                    mAudioProgress.visibility = View.GONE
                    false
                }
            }
        }

        makeTextViewSelectAndSearch(textVeiwDefinition)

        btnAddDefinition.setOnClickListener {
            try {
                val noteIdAdded = btnAddDefinition.getTag(R.id.TAG_NOTE_ID) as? Long
                if (noteIdAdded != null) {
                    if (mUpdateNoteId == 0L) {
                        if (Utils.deleteNote(this, noteIdAdded)) {
                            btnAddDefinition.setImageDrawable(
                                ContextCompat.getDrawable(
                                    this,
                                    Utils.getResIdFromAttribute(this, R.attr.icon_add)
                                )
                            )
                            btnAddDefinition.setTag(R.id.TAG_NOTE_ID, null)
                            Toast.makeText(this, R.string.str_cancel_note_add, Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, R.string.error_note_cancel, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, R.string.str_not_cancelable_append_mode, Toast.LENGTH_SHORT).show()
                    }
                    return@setOnClickListener
                }

                // Save image
                if (!def.imageUrl.isNullOrEmpty()) {
                    if (defImage.drawable != null) {
                        val drawable = defImage.drawable as BitmapDrawable
                        val bm = drawable.bitmap

                        try {
                            val root = File(Constant.IMAGE_MEDIA_DIRECTORY)
                            if (!root.exists()) {
                                root.mkdirs()
                            }
                            val sdImageMainDirectory = File(root, def.imageName ?: "image.png")
                            val fOut = FileOutputStream(sdImageMainDirectory)
                            bm.compress(Bitmap.CompressFormat.PNG, 100, fOut)
                            fOut.flush()
                            fOut.close()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                val mAnkiDroid = MyApplication.getAnkiDroid(MyApplication.getContext())
                val sharedExportElements = Constant.getSharedExportElements()
                val exportFields = Array(currentOutputPlan!!.fieldsMap.size) { "" }
                var i = 0
                val map = currentOutputPlan!!.fieldsMap

                for (exportedFieldKey in currentOutputPlan!!.fieldsMap.values) {
                    when (exportedFieldKey) {
                        sharedExportElements[0] -> {
                            exportFields[i] = ""
                        }
                        sharedExportElements[1] -> {
                            exportFields[i] = FieldUtil.getNormalSentence(bigBangLayout.getLines())
                        }
                        sharedExportElements[2] -> {
                            exportFields[i] = FieldUtil.getBoldSentence(bigBangLayout.getLines())
                        }
                        sharedExportElements[3] -> {
                            exportFields[i] = FieldUtil.getBlankSentence(bigBangLayout.getLines(), true)
                        }
                        sharedExportElements[4] -> {
                            exportFields[i] = FieldUtil.getBlankSentence(bigBangLayout.getLines(), false)
                        }
                        sharedExportElements[5] -> {
                            exportFields[i] = mNoteEditedByUser
                        }
                        sharedExportElements[6] -> {
                            exportFields[i] = mUrl
                        }
                        sharedExportElements[7] -> {
                            exportFields[i] = Utils.getAllHtmlFromDefinitionList(mDefinitionList ?: emptyList())
                        }
                        sharedExportElements[8] -> {
                            exportFields[i] = mEditTextTranslation.text.toString().replace("\n", "<br/>")
                        }
                        else -> {
                            if (def.hasElement(exportedFieldKey)) {
                                exportFields[i] = def.getExportElement(exportedFieldKey) ?: ""
                            } else {
                                exportFields[i] = ""
                            }
                        }
                    }
                    i++
                }

                // Handle download; audio or image
                if (!def.audioUrl.isNullOrEmpty()) {
                    if (fetch == null) {
                        initFetch()
                    }
                    if (map.containsValue("原声例句")) {
                        val request = Request(def.audioUrl, Constant.AUDIO_MEDIA_DIRECTORY + def.audioName)
                        request.priority = Priority.HIGH
                        request.networkType = NetworkType.ALL
                        isFetchDownloading = true
                        fetch?.enqueue(
                            request,
                            Func {
                                mAudioProgress.visibility = View.VISIBLE
                            },
                            Func {
                                isFetchDownloading = false
                            }
                        )
                    }
                }

                if (!def.audioUrl.isNullOrEmpty()) {
                    if (fetch == null) {
                        initFetch()
                    }
                    if (map.containsValue("音频") || map.containsValue("复合项")) {
                        val request = Request(def.audioUrl, Constant.AUDIO_MEDIA_DIRECTORY + def.audioName)
                        request.priority = Priority.HIGH
                        request.networkType = NetworkType.ALL
                        isFetchDownloading = true
                        fetch?.enqueue(
                            request,
                            Func {
                                mAudioProgress.visibility = View.VISIBLE
                                isFetchDownloading = true
                            },
                            Func {
                                isFetchDownloading = false
                            }
                        )
                    }
                }

                if (!def.audioUrl.isNullOrEmpty()) {
                    if (fetch == null) {
                        initFetch()
                    }
                    if (map.containsValue("离线发音")) {
                        val request = Request(def.audioUrl, Constant.AUDIO_MEDIA_DIRECTORY + def.audioName)
                        request.priority = Priority.HIGH
                        request.networkType = NetworkType.ALL
                        isFetchDownloading = true
                        fetch?.enqueue(
                            request,
                            Func {
                                mAudioProgress.visibility = View.VISIBLE
                                isFetchDownloading = true
                            },
                            Func {
                                isFetchDownloading = false
                            }
                        )
                    }
                }

                val deckId = currentOutputPlan!!.outputDeckId
                val modelId = currentOutputPlan!!.outputModelId

                if (mUpdateNoteId == 0L) {
                    val result = mAnkiDroid.api.addNote(modelId, deckId, exportFields, mTagEditedByUser)
                    if (result != null) {
                        Toast.makeText(this, R.string.str_added, Toast.LENGTH_SHORT).show()
                        btnAddDefinition.setImageDrawable(
                            ContextCompat.getDrawable(
                                this,
                                Utils.getResIdFromAttribute(this, R.attr.icon_remove)
                            )
                        )
                        clearBigbangSelection()
                        mNoteEditedByUser = ""
                        btnAddDefinition.setTag(R.id.TAG_NOTE_ID, result)

                        var count = 0
                        for (field in currentOutputPlan!!.fieldsMap.keys) {
                            if (field.replace(" ", "").lowercase() == "noteid") {
                                exportFields[count] = result.toString()
                                val updateSuccess = mAnkiDroid.api.updateNoteFields(result, exportFields)
                                if (!updateSuccess) {
                                    Toast.makeText(this, R.string.str_error_noteid, Toast.LENGTH_SHORT).show()
                                }
                                break
                            }
                            count++
                        }

                        HistoryUtil.saveNoteAdd(
                            "",
                            FieldUtil.getBoldSentence(bigBangLayout.getLines()),
                            currentDicitonary?.getDictionaryName() ?: "",
                            textVeiwDefinition.text.toString(),
                            mTranslatedResult,
                            mNoteEditedByUser,
                            mTagEditedByUser.toString()
                        )
                    } else {
                        Toast.makeText(this, R.string.str_failed_add, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val note = mAnkiDroid.api.getNote(mUpdateNoteId)
                    if (note == null) {
                        Toast.makeText(this, R.string.str_error_notetype_noncompatible, Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    val original = note.fields
                    val tags = note.tags.toMutableSet()

                    if (original.size != exportFields.size) {
                        Toast.makeText(this, R.string.str_error_notetype_noncompatible, Toast.LENGTH_SHORT)
                            .show()
                        return@setOnClickListener
                    }

                    if (mUpdateAction == "replace") {
                        for (j in original.indices) {
                            if (exportFields[j].isEmpty()) {
                                exportFields[j] = original[j]
                            }
                        }
                    } else {
                        for (j in original.indices) {
                            exportFields[j] = if (original[j].trim().isEmpty() || exportFields[j].trim()
                                    .isEmpty()
                            ) {
                                original[j] + exportFields[j]
                            } else {
                                original[j] + "<br/>" + exportFields[j]
                            }
                        }
                    }

                    tags.addAll(mTagEditedByUser)
                    val success = mAnkiDroid.api.updateNoteFields(mUpdateNoteId, exportFields)
                    val successTag = mAnkiDroid.api.updateNoteTags(mUpdateNoteId, tags)

                    if (success && successTag) {
                        Toast.makeText(this, R.string.str_note_updated, Toast.LENGTH_SHORT).show()
                        btnAddDefinition.setImageDrawable(
                            ContextCompat.getDrawable(
                                this,
                                Utils.getResIdFromAttribute(this, R.attr.icon_remove)
                            )
                        )
                    } else {
                        Toast.makeText(this, R.string.str_error_note_update, Toast.LENGTH_SHORT).show()
                    }
                }

                if (settings.getAutoCancelPopupQ()) {
                    if (fetch == null) {
                        finish()
                    } else {
                        if (!isFetchDownloading) {
                            finish()
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }

    private fun initFetch() {
        val fetchConfiguration = FetchConfiguration.Builder(this)
            .setDownloadConcurrentLimit(3)
            .build()
        fetch = Fetch.Impl.getInstance(fetchConfiguration)
        fetch?.addListener(object : FetchListener {
            override fun onAdded(download: Download) {}
            override fun onQueued(download: Download, b: Boolean) {}
            override fun onWaitingNetwork(download: Download) {}

            override fun onCompleted(download: Download) {
                Toast.makeText(this@PopupActivity, "Download Completed!", Toast.LENGTH_SHORT).show()
                mAudioProgress.visibility = View.GONE
                isFetchDownloading = false
                if (settings.getAutoCancelPopupQ()) {
                    finish()
                }
            }

            override fun onError(download: Download, error: Error, throwable: Throwable?) {
                Toast.makeText(this@PopupActivity, "Download Failed!", Toast.LENGTH_SHORT).show()
                mAudioProgress.visibility = View.GONE
                isFetchDownloading = false
                if (settings.getAutoCancelPopupQ()) {
                    finish()
                }
            }

            override fun onDownloadBlockUpdated(download: Download, downloadBlock: DownloadBlock, i: Int) {}
            override fun onStarted(download: Download, list: List<DownloadBlock>, i: Int) {}
            override fun onProgress(download: Download, l: Long, l1: Long) {}
            override fun onPaused(download: Download) {}
            override fun onResumed(download: Download) {}
            override fun onCancelled(download: Download) {}
            override fun onRemoved(download: Download) {}
            override fun onDeleted(download: Download) {}
        })
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val v = currentFocus
        val ret = super.dispatchTouchEvent(event)

        if (v is AutoCompleteTextView) {
            val currentFocus = currentFocus ?: return ret
            val screenCoords = IntArray(2)
            currentFocus.getLocationOnScreen(screenCoords)
            val x = event.rawX + currentFocus.left - screenCoords[0]
            val y = event.rawY + currentFocus.top - screenCoords[1]

            if (event.action == MotionEvent.ACTION_UP &&
                (x < currentFocus.left ||
                        x >= currentFocus.right ||
                        y < currentFocus.top ||
                        y > currentFocus.bottom)
            ) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                window.currentFocus?.let {
                    imm.hideSoftInputFromWindow(it.windowToken, 0)
                }
                v.clearFocus()
            }
        }
        return ret
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacksAndMessages(null)
        Runtime.getRuntime().gc()
    }

    private fun startCBService() {
        val intent = Intent(this, CBWatcherService::class.java)
        startService(intent)
    }

    override fun onSelected(text: String) {
        val currentWord = FieldUtil.getSelectedText(bigBangLayout.getLines())
        if (currentWord != act.text.toString()) {
            mCurrentKeyWord = currentWord
            act.setText(currentWord)
            searchManager.asyncSearch(currentWord, mTextToProcess, currentDicitonary, currentOutputPlan)
        }
    }

    override fun onSearch(text: String) {}
    override fun onShare(text: String) {}
    override fun onCopy(text: String) {}
    override fun onTrans(text: String) {}
    override fun onDrag() {}
    override fun onSwitchType(isLocal: Boolean) {}
    override fun onSwitchSymbol(isShow: Boolean) {}
    override fun onSwitchSection(isShow: Boolean) {}
    override fun onDragSelection() {}

    override fun onCancel() {
        act.setText("")
        searchManager.asyncSearch("", mTextToProcess, currentDicitonary, currentOutputPlan)
    }

    private fun vibarate(ms: Int) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(Vibrator::class.java)
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        if (vibrator.hasVibrator()) {
            // Use modern VibrationEffect API (API 26+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(ms.toLong(), VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(ms.toLong())
            }
        }
    }

    private fun clearBigbangSelection() {
        for (line in bigBangLayout.getLines()) {
            line.getItems()?.forEach { item ->
                if (item.getText() == mTargetWord) {
                    item.setSelected(false)
                }
            }
        }
    }

    private fun makeTextViewSelectAndSearch(textView: TextView) {
        textView.customSelectionActionModeCallback = object : ActionMode.Callback {
            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                menu.removeItem(android.R.id.cut)
                return true
            }

            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                menu.add(0, 1, 0, "Definition").setIcon(R.drawable.ic_ali_search)
                return true
            }

            override fun onDestroyActionMode(mode: ActionMode) {}

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                return when (item.itemId) {
                    1 -> {
                        var min = 0
                        var max = textView.text.length
                        if (textView.isFocused) {
                            val selStart = textView.selectionStart
                            val selEnd = textView.selectionEnd
                            min = maxOf(0, minOf(selStart, selEnd))
                            max = maxOf(0, maxOf(selStart, selEnd))
                        }
                        val selectedText = textView.text.subSequence(min, max).toString()
                        mode.finish()
                        act.setText(selectedText)
                        searchManager.asyncSearch(selectedText, mTextToProcess, currentDicitonary, currentOutputPlan)
                        true
                    }
                    else -> false
                }
            }
        }
    }

    companion object {
        private const val TAG_NOTE_ID_LONG = 5
        private const val PROCESS_DEFINITION_LIST = 1
        private const val ASYNC_SEARCH_FAILED = 2
        private const val TRANSLATION_DONE = 3
        private const val TRANSLATIOn_FAILED = 4
    }
}
