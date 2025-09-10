package com.mmjang.ankihelper.ui.popup;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.hotspot2.omadm.PpsMoParser;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import androidx.annotation.RequiresApi;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import androidx.core.text.HtmlCompat;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.text.TextWatcher;
import android.text.Editable;
import android.view.inputmethod.InputMethodManager;

import com.bumptech.glide.Glide;
import com.ichi2.anki.FlashCardsContract;
import com.ichi2.anki.api.NoteInfo;
import com.mmjang.ankihelper.MyApplication;
import com.mmjang.ankihelper.R;
import java.util.Set;
import java.util.HashSet;
import android.content.pm.PackageManager;
import com.mmjang.ankihelper.anki.AnkiDroidHelper;
import com.mmjang.ankihelper.data.Settings;
import com.mmjang.ankihelper.data.database.DatabaseManager;
import com.mmjang.ankihelper.data.dict.Definition;
import com.mmjang.ankihelper.data.dict.DictionaryRegister;
import com.mmjang.ankihelper.data.dict.IDictionary;
import com.mmjang.ankihelper.data.dict.AIDictionary;
import com.mmjang.ankihelper.data.dict.UrbanAutoCompleteAdapter;
import com.mmjang.ankihelper.data.history.HistoryUtil;
import com.mmjang.ankihelper.data.model.UserTag;
import com.mmjang.ankihelper.data.plan.OutputPlan;
import com.mmjang.ankihelper.data.plan.OutputPlanPOJO;
import com.mmjang.ankihelper.domain.CBWatcherService;
import com.mmjang.ankihelper.domain.PlayAudioManager;
import com.mmjang.ankihelper.domain.PronounceManager;
import com.mmjang.ankihelper.ui.LauncherActivity;
import com.mmjang.ankihelper.ui.plan.PlanEditorActivity;
import com.mmjang.ankihelper.ui.widget.BigBangLayout;
import com.mmjang.ankihelper.ui.widget.BigBangLayoutWrapper;
import com.mmjang.ankihelper.util.Constant;
import com.mmjang.ankihelper.util.FieldUtil;
import com.mmjang.ankihelper.util.RegexUtil;
import com.mmjang.ankihelper.util.TextSplitter;
import com.mmjang.ankihelper.util.Translator;
import com.mmjang.ankihelper.util.Utils;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Error;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchConfiguration;
import com.tonyodev.fetch2.FetchListener;
import com.tonyodev.fetch2.NetworkType;
import com.tonyodev.fetch2.Priority;
import com.tonyodev.fetch2.Request;
import com.tonyodev.fetch2core.DownloadBlock;
import com.tonyodev.fetch2core.Func;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.litepal.crud.LitePalSupport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.litepal.LitePal;
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;
import static com.mmjang.ankihelper.util.FieldUtil.getBlankSentence;
import static com.mmjang.ankihelper.util.FieldUtil.getBoldSentence;
import static com.mmjang.ankihelper.util.FieldUtil.getNormalSentence;


public class PopupActivity extends AppCompatActivity implements BigBangLayoutWrapper.ActionListener{

    List<IDictionary> dictionaryList;
    IDictionary currentDicitonary;
    List<OutputPlanPOJO> outputPlanList;
    List<String> languageList;
    OutputPlanPOJO currentOutputPlan;
    Settings settings;
    String mTextToProcess;
    String mPlanNameFromIntent;
    String mCurrentKeyWord;
    TextSplitter mTextSplitter;
    String mNoteEditedByUser = "";
    Set<String> mTagEditedByUser = new HashSet<>();
    //posiblle pre set target word
    String mTargetWord;
    //possible url from dedicated borwser
    String mUrl = "";
    //possible specific note id to update
    Long mUpdateNoteId = 0L;
    //!!!!!!!!!!!important!!! boolean, if the plan spinner is during init, forbid asyncsearch;
    boolean isDuringPlanSpinnerInit = false;
    //update action   replace/append    append is the default action, to prevent data loss;
    String mUpdateAction;
    //possible bookmark id from fbreader
    String mFbReaderBookmarkId;
    //translation
    String mTranslatedResult = "";
    boolean needTranslation = false;
    //views
    AutoCompleteTextView act;
    ImageButton btnSearch;
    ImageButton btnPronounce;
    Spinner planSpinner;
    Spinner pronounceLanguageSpinner;
    //RecyclerView recyclerViewDefinitionList;
    ImageButton mBtnEditNote;
    ImageButton mBtnEditTag;
    ImageButton mBtnTranslation;
    ImageButton mBtnFooterRotateLeft;
    ImageButton mBtnFooterRotateRight;
    ImageButton mBtnFooterScrollup;
    ProgressBar progressBar;
    ProgressBar mAudioProgress;

    // Edit mode views
    EditText mEditTextArea;
    ImageButton mBtnEditMode;
    ImageButton mBtnSaveChanges;
    ImageButton mBtnDiscardChanges;

    CardView mCardViewTranslation;
    EditText mEditTextTranslation;
    //fab
    //FloatingActionButton mFab;
    ScrollView scrollView;
    //plan b
    LinearLayout viewDefinitionList;
    List<Definition> mDefinitionList;
    //media
    MediaPlayer mMediaPlayer;
    //downloader
    Fetch fetch;
    boolean isFetchDownloading = false;
    //async event
    private static final int PROCESS_DEFINITION_LIST = 1;
    private static final int ASYNC_SEARCH_FAILED = 2;
    private static final int TRANSLATION_DONE = 3;
    private static final int TRANSLATIOn_FAILED = 4;

    // Edit mode state
    private enum EditMode {
        SELECT_MODE,
        EDIT_MODE
    }

    private EditMode currentEditMode = EditMode.SELECT_MODE;
    private String originalText; // Backup for discard functionality
    private int lastScrollPosition = 0; // Preserve scroll position between modes

    //view tag
    private static final int TAG_NOTE_ID_LONG = 5;
    
    // Memory-leak-safe Handler implementation
    private static class PopupHandler extends Handler {
        private final WeakReference<PopupActivity> activityRef;
        
        PopupHandler(PopupActivity activity) {
            this.activityRef = new WeakReference<>(activity);
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public void handleMessage(Message msg) {
            PopupActivity activity = activityRef.get();
            if (activity == null) {
                return; // Activity has been garbage collected
            }
            
            switch (msg.what) {
                case PROCESS_DEFINITION_LIST:
                    activity.showSearchButton();
                    activity.mDefinitionList = (List<Definition>) msg.obj;
                    activity.processDefinitionList(activity.mDefinitionList);
                    break;
                case ASYNC_SEARCH_FAILED:
                    activity.showSearchButton();
                    Toast.makeText(activity, (String) msg.obj, Toast.LENGTH_LONG).show();
                    break;
                case TRANSLATION_DONE:
                    String result = (String) msg.obj;
                    String[] splitted = result.split("\n");
                    if(splitted.length > 0 && splitted[0].equals("error")){
                        Toast.makeText(activity, result, Toast.LENGTH_SHORT).show();
                        activity.mBtnTranslation.setEnabled(true); // Re-enable button on error
                        activity.showTranslateNormal(); // Show normal state on error
                        break;
                    }
                    activity.mEditTextTranslation.setText((result));
                    activity.showTranslateDone();
                    activity.showTranslationCardView(true);
                    activity.mBtnTranslation.setEnabled(true); // Re-enable button on success
                    break;
                case TRANSLATIOn_FAILED:
                    activity.showTranslateNormal();
                    Toast.makeText(activity, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    activity.mBtnTranslation.setEnabled(true); // Re-enable button on failure
                    break;
            }
        }
    }
    
    //async
    private final PopupHandler mHandler = new PopupHandler(this);
    private BigBangLayout bigBangLayout;
    private BigBangLayoutWrapper bigBangLayoutWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(Settings.getInstance(this).getPinkThemeQ()){
            setTheme(R.style.TransparentPink);
        }
        super.onCreate(savedInstanceState);
        setStatusBarColor();
        setContentView(R.layout.activity_popup);
//        getActionBar().hide();
        //set animation
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        OverScrollDecoratorHelper.setUpOverScroll(scrollView);
        //
        assignViews();
        initBigBangLayout();
        loadData(); //dictionaryList;
        populatePlanSpinner();
        populateLanguageSpinner();
        setEventListener();
        if (settings.getMoniteClipboardQ()) {
            checkAndRequestClipboardPermissions();
            startCBService();
        }

        handleIntent();

        //async invoke droid
        asyncInvokeDroid();
    }

    private void checkClipboardPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ has stricter clipboard access rules
            // Ensure we have focus or use foreground service where appropriate
            Log.d("Clipboard", "Checking clipboard permissions for Android 10+");
        }
    }
    private void asyncInvokeDroid() {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try{
                            MyApplication.getAnkiDroid(MyApplication.getContext()).getApi().getDeckList();
                        }catch (Exception e){
                        }
                    }
                }
        ).start();
    }

    private void setTargetWord(){
        if (!TextUtils.isEmpty(mTargetWord)) {
            for (BigBangLayout.Line line : bigBangLayout.getLines()) {
                List<BigBangLayout.Item> items = line.getItems();
                for (BigBangLayout.Item item : items) {
                    if (item.getText().equals(mTargetWord)) {
                        item.setSelected(true);
                    }
                }
            }
            act.setText(mTargetWord);
            asyncSearch(mTargetWord);
        }else{
            if(mTextToProcess.matches("[a-zA-Z\\-]*")){
                act.setText(mTextToProcess);
                asyncSearch(mTextToProcess);
            }
        }
    }

    private void setStatusBarColor() {
        int statusBarColor = 0;
        if (Build.VERSION.SDK_INT >= 21) {
            statusBarColor = getWindow().getStatusBarColor();
        }
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(statusBarColor);
        }
    }

    private void assignViews() {
        act = (AutoCompleteTextView) findViewById(R.id.edit_text_hwd);
        btnSearch = (ImageButton) findViewById(R.id.btn_search);
        btnPronounce = ((ImageButton) findViewById(R.id.btn_pronounce));
        planSpinner = (Spinner) findViewById(R.id.plan_spinner);
        pronounceLanguageSpinner = (Spinner) findViewById(R.id.language_spinner);
        //recyclerViewDefinitionList = (RecyclerView) findViewById(R.id.recycler_view_definition_list);
        viewDefinitionList = (LinearLayout) findViewById(R.id.view_definition_list);
        mBtnEditNote = (ImageButton) findViewById(R.id.footer_note);
        mBtnEditTag = (ImageButton) findViewById(R.id.footer_tag);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        bigBangLayout = (BigBangLayout) findViewById(R.id.bigbang);
        bigBangLayoutWrapper = (BigBangLayoutWrapper) findViewById(R.id.bigbang_wrapper);
        mCardViewTranslation = (CardView) findViewById(R.id.cardview_translation);
        mBtnTranslation = (ImageButton) findViewById(R.id.footer_translate);
        mEditTextTranslation = (EditText) findViewById(R.id.edittext_translation);
        //mFab = (FloatingActionButton) findViewById(R.id.fab);
        mBtnFooterRotateLeft = (ImageButton) findViewById(R.id.footer_rotate_left);
        mBtnFooterRotateRight= (ImageButton) findViewById(R.id.footer_rotate_right);
        mBtnFooterScrollup = (ImageButton) findViewById(R.id.footer_scroll_up);
        mAudioProgress = findViewById(R.id.audio_progress);

        // Edit mode views
        mEditTextArea = (EditText) findViewById(R.id.edit_text_area);
        mBtnEditMode = (ImageButton) findViewById(R.id.btn_edit_mode);
        mBtnSaveChanges = (ImageButton) findViewById(R.id.btn_save_changes);
        mBtnDiscardChanges = (ImageButton) findViewById(R.id.btn_discard_changes);
        
        // Icons are set in layout XML using src attribute
        
        // Initialize edit mode views to correct initial state
        bigBangLayoutWrapper.setVisibility(View.VISIBLE);
        mEditTextArea.setVisibility(View.GONE);
        mBtnEditMode.setVisibility(View.VISIBLE);
        mBtnSaveChanges.setVisibility(View.GONE);
        mBtnDiscardChanges.setVisibility(View.GONE);
    }

    private void loadData() {
        dictionaryList = DictionaryRegister.getDictionaryObjectList();
        outputPlanList = DatabaseManager.getInstance().getAllPlan();
        settings = Settings.getInstance(this);
        //load tag
        boolean loadQ = settings.getSetAsDefaultTag();
        if (loadQ) {
            mTagEditedByUser = Utils.fromStringToTagSet(settings.getDefaulTag());
        }
        //check if outputPlanList is empty
        if(outputPlanList.size() == 0){
            //Toast.makeText(this, , Toast.LENGTH_LONG).show();
            Utils.showMessage(this, getResources().getString(R.string.toast_no_available_plan));
        }
    }

    private void populatePlanSpinner() {
        if(outputPlanList.size() == 0){
            return;
        }
        final String[] planNameArr = new String[outputPlanList.size()];
        for (int i = 0; i < outputPlanList.size(); i++) {
            planNameArr[i] = outputPlanList.get(i).getPlanName();
        }
        ArrayAdapter<String> planSpinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, planNameArr);
        planSpinner.setAdapter(planSpinnerAdapter);
        planSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //set plan to last selected plan
        String lastSelectedPlan = settings.getLastSelectedPlan();
        if (lastSelectedPlan.equals("")) //first use, set default plan to first one if any
        {
            if (outputPlanList.size() > 0) {
                settings.setLastSelectedPlan(outputPlanList.get(0).getPlanName());
                currentOutputPlan = outputPlanList.get(0);
                currentDicitonary = getDictionaryFromOutputPlan(currentOutputPlan);
                if(currentDicitonary == null){
                    String message = String.format("方案\"%s\"所选词典\"%s\"不存在，请检查是否需要重新导入自定义词典",
                            currentOutputPlan.getPlanName(),
                            currentOutputPlan.getDictionaryKey());
                    Utils.showMessage(PopupActivity.this, message);
                }else {
                    setActAdapter(currentDicitonary);
                }
            } else {
                return ;
            }
        }

        ///////////////if user add intent parameter to control which plan to use
        mPlanNameFromIntent = getIntent().getStringExtra(Constant.INTENT_ANKIHELPER_PLAN_NAME);
        if(mPlanNameFromIntent != null){
            lastSelectedPlan = mPlanNameFromIntent;
        }
        ///////////////
        int i = 0;
        boolean find = false;
        for (OutputPlanPOJO plan : outputPlanList) {
            if (plan.getPlanName().equals(lastSelectedPlan)) {
                isDuringPlanSpinnerInit = true;
                planSpinner.setSelection(i);
                currentOutputPlan = outputPlanList.get(i);
                currentDicitonary = getDictionaryFromOutputPlan(currentOutputPlan);
                if(currentDicitonary == null) {
                    String message = String.format("方案\"%s\"所选词典\"%s\"不存在，请检查是否需要重新导入自定义词典",
                            currentOutputPlan.getPlanName(),
                            currentOutputPlan.getDictionaryKey());
                    Utils.showMessage(PopupActivity.this, message);
                    break;
                }
                setActAdapter(currentDicitonary);
                find = true;
                break;
            }
            //if not equal, compare next
            i++;
        }
        if (!find) //if the saved last plan no longer in the plan list, reset to first one
        {
            if (outputPlanList.size() > 0) {
                settings.setLastSelectedPlan(outputPlanList.get(0).getPlanName());
                currentOutputPlan = outputPlanList.get(0);
                currentDicitonary = getDictionaryFromOutputPlan(currentOutputPlan);
                if(currentDicitonary == null) {
                    String message = String.format("方案\"%s\"所选词典\"%s\"不存在，请检查是否需要重新导入自定义词典",
                            currentOutputPlan.getPlanName(),
                            currentOutputPlan.getDictionaryKey());
                    Utils.showMessage(PopupActivity.this, message);
                } else {
                    setActAdapter(currentDicitonary);
                }
            }
        } else {
            //if find, then current plan and dictionary must have been set above.
        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            scrollView.setOnScrollChangeListener(
//                    new View.OnScrollChangeListener() {
//                        @Override
//                        public void onScrollChange(View view, int i, int i1, int i2, int i3) {
//                            if(i1 > i3){
//                                mFab.hide();
//                            }else{
//                                mFab.show();
//                                //mFab.setAlpha(Constant.FLOAT_ACTION_BUTTON_ALPHA);
//                            }
//                        }
//                    }
//            );
//        }
    }

    private void populateLanguageSpinner() {

        String[] languages = PronounceManager.getAvailablePronounceLanguage();
        ArrayAdapter<String> languagesSpinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, languages);
        languagesSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pronounceLanguageSpinner.setAdapter(languagesSpinnerAdapter);
        int lastPronounceLanguageIndex = settings.getLastPronounceLanguage();
        pronounceLanguageSpinner.setSelection(lastPronounceLanguageIndex);

    }

    private void initBigBangLayout(){
        bigBangLayout.setShowSymbol(true);
        bigBangLayout.setShowSpace(true);
        bigBangLayout.setShowSection(true);
        bigBangLayout.setItemSpace(4);
        bigBangLayout.setLineSpace(2);
        bigBangLayout.setTextPadding(5);
        bigBangLayout.setTextPaddingPort(5);
        bigBangLayoutWrapper.setStickHeader(true);
        bigBangLayoutWrapper.setActionListener(this);

    }

    private void setEventListener() {

        //auto finish
        Button btnCancelBlank = (Button) findViewById(R.id.btn_cancel_blank);
        Button btnCancelBlankAboveCard = (Button) findViewById(R.id.btn_cancel_blank_above_card);
        btnCancelBlank.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }
        );
        btnCancelBlankAboveCard.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }
        );

        planSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        currentOutputPlan = outputPlanList.get(position);
                        currentDicitonary = getDictionaryFromOutputPlan(currentOutputPlan);
                        if (currentDicitonary != null) {
                            setActAdapter(currentDicitonary);
                        }
                        //memorise last selected plan
                        settings.setLastSelectedPlan(currentOutputPlan.getPlanName());
                        String actContent = act.getText().toString();

                        if(isDuringPlanSpinnerInit){
                            isDuringPlanSpinnerInit = false;
                        }else {
                            if(!actContent.trim().isEmpty()) {
                                asyncSearch(actContent);
                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                }
        );

        pronounceLanguageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                settings.setLastPronounceLanguage(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnSearch.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String word = act.getText().toString();
                        if (!word.isEmpty()) {
                            asyncSearch(word);
                            Utils.hideSoftKeyboard(PopupActivity.this);
                        }
                    }
                }
        );

        btnPronounce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String word = act.getText().toString();
                PlayAudioManager.playPronounceVoice(PopupActivity.this, word);
            }
        });

        mBtnEditNote.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setupEditNoteDialog();
                    }
                }
        );

        mBtnEditTag.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setupTagDialog();
                    }
                }
        );

        act.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Log.d("autocomplete", i + "");
                        act.post(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        btnSearch.callOnClick();
                                    }
                                }
                        );
                    }
                }
        );

        mBtnTranslation.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(mEditTextTranslation.getText().toString().equals("")){
                            mBtnTranslation.setEnabled(false); // Disable button
                            showTranslateLoading(); // Show loading state
                            asyncTranslate(mTextToProcess);
                        }
                    }
                }
        );
        mBtnFooterRotateRight.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int mPlanSize = outputPlanList.size();
                        int currentPos = planSpinner.getSelectedItemPosition();
                        if(mPlanSize > 1){
                            if(currentPos < mPlanSize - 1){
                                planSpinner.setSelection(currentPos + 1);
                            }
                            else if(currentPos == mPlanSize - 1){
                                planSpinner.setSelection(0);
                            }
                            //vibarate(Constant.VIBRATE_DURATION);
                            //scrollView.fullScroll(ScrollView.FOCUS_UP);
                        }else{
                            Toast.makeText(PopupActivity.this, R.string.str_only_one_plan_cant_switch, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        mBtnFooterRotateLeft.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int mPlanSize = outputPlanList.size();
                        int currentPos = planSpinner.getSelectedItemPosition();
                        if(mPlanSize > 1){
                            if(currentPos > 0){
                                planSpinner.setSelection(currentPos - 1);
                            }
                            else if(currentPos == 0){
                                planSpinner.setSelection(mPlanSize - 1);
                            }
                        //    vibarate(Constant.VIBRATE_DURATION);
                            //scrollView.fullScroll(ScrollView.FOCUS_UP);
                        }else{
                            Toast.makeText(PopupActivity.this, R.string.str_only_one_plan_cant_switch, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        mBtnFooterScrollup.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(scrollView.getScrollY() > 0) {
                            scrollView.fullScroll(ScrollView.FOCUS_UP);
                        }
                    }
                }
        );
        
        // Add edit mode event listeners
        setupEditModeListeners();
    }

    private void setupEditModeListeners() {
        // Add TextWatcher to show/hide pronunciation button based on text content
        act.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not needed
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Show pronunciation button if there's text, hide it if empty
                showPronounce(s.length() > 0);
            }
        });

        // Edit mode toggle button
        mBtnEditMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToEditMode();
            }
        });

        // Save changes button
        mBtnSaveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChangesAndReturnToSelectMode();
            }
        });

        // Discard changes button
        mBtnDiscardChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discardChangesAndReturnToSelectMode();
            }
        });
    }

    private void switchToEditMode() {
        // Backup current state
        originalText = getCurrentTextFromBigBangLayout();
        
        // Switch UI components
        bigBangLayoutWrapper.setVisibility(View.GONE);
        mBtnEditMode.setVisibility(View.GONE);
        mEditTextArea.setVisibility(View.VISIBLE);
        mBtnSaveChanges.setVisibility(View.VISIBLE);
        mBtnDiscardChanges.setVisibility(View.VISIBLE);
        
        // Icons are set in layout XML using src attribute
        
        // Populate edit text
        mEditTextArea.setText(originalText);
        mEditTextArea.requestFocus();
        
        // Show keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mEditTextArea, InputMethodManager.SHOW_IMPLICIT);
        
        currentEditMode = EditMode.EDIT_MODE;
    }

    private void saveChangesAndReturnToSelectMode() {
        // Capture modified text from edit_text_area
        String modifiedText = mEditTextArea.getText().toString();
        
        // Update data model
        mTextToProcess = modifiedText;
        
        // Switch UI back to select mode first for better UX
        mEditTextArea.setVisibility(View.GONE);
        mBtnSaveChanges.setVisibility(View.GONE);
        mBtnDiscardChanges.setVisibility(View.GONE);
        bigBangLayoutWrapper.setVisibility(View.VISIBLE);
        mBtnEditMode.setVisibility(View.VISIBLE);
        
        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditTextArea.getWindowToken(), 0);
        
        currentEditMode = EditMode.SELECT_MODE;
        
        // Reprocess text asynchronously
        populateWordSelectBox();
    }

    private void discardChangesAndReturnToSelectMode() {
        // Restore original text state
        mTextToProcess = originalText;
        
        // Switch UI back to select mode first for better UX
        mEditTextArea.setVisibility(View.GONE);
        mBtnSaveChanges.setVisibility(View.GONE);
        mBtnDiscardChanges.setVisibility(View.GONE);
        bigBangLayoutWrapper.setVisibility(View.VISIBLE);
        mBtnEditMode.setVisibility(View.VISIBLE);
        
        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditTextArea.getWindowToken(), 0);
        
        currentEditMode = EditMode.SELECT_MODE;
        
        // Reprocess text with original content asynchronously
        populateWordSelectBox();
    }

    private String getCurrentTextFromBigBangLayout() {
        // Instead of reconstructing from BigBangLayout segments, 
        // we should use the original mTextToProcess which contains the unsegmented text
        return mTextToProcess;
    }

    private IDictionary getDictionaryFromOutputPlan(OutputPlanPOJO outputPlan) {
        String dictionaryKey = outputPlan.getDictionaryKey();
        for (IDictionary dict : dictionaryList) {
            // Handle AI dictionaries differently
            String dictKey;
            if (dict instanceof AIDictionary) {
                dictKey = ((AIDictionary) dict).getDictionaryKey();
            } else {
                dictKey = dict.getDictionaryName();
            }
            
            if (dictKey.equals(dictionaryKey)) {
                return dict;
            }
        }
        return null;
    }

    private void processDefinitionList(List<Definition> definitionList) {
        if (definitionList.isEmpty()) {
            Toast.makeText(this, R.string.definition_not_found, Toast.LENGTH_SHORT).show();
        } else {
//            DefinitionAdapter defAdapter = new DefinitionAdapter(PopupActivity.this, definitionList, mTextSplitter, currentOutputPlan);
//            LinearLayoutManager llm = new LinearLayoutManager(this);
//            //llm.setAutoMeasureEnabled(true);
//            recyclerViewDefinitionList.setLayoutManager(llm);
//            //recyclerViewDefinitionList.getRecycledViewPool().setMaxRecycledViews(0,0);
//            //recyclerViewDefinitionList.setHasFixedSize(true);
//            //recyclerViewDefinitionList.setNestedScrollingEnabled(false);
//            recyclerViewDefinitionList.setAdapter(defAdapter);
            viewDefinitionList.removeAllViewsInLayout();
            for (Definition def : definitionList) {
                viewDefinitionList.addView(getCardFromDefinition(def));
            }
            viewDefinitionList.post(
                    new Runnable() {
                        @Override
                        public void run() {
                            if(scrollView.getScrollY() > 10) {
                                //scrollView.fullScroll(ScrollView.FOCUS_UP);
                            }
                        }
                    }
            );
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(isFromAndroidQClipboard) {
            if (!Settings.getInstance(MyApplication.getContext()).getMoniteClipboardQ()) {
                return;
            }
            
            // Only access clipboard when we have focus (Android 10+ requirement)
            if (hasFocus) {
                try {
                    ClipboardManager cb = this.getSystemService(ClipboardManager.class);
                    if (cb != null && cb.hasPrimaryClip()) {
                        ClipData clipData = cb.getPrimaryClip();
                        if (clipData != null && clipData.getItemCount() > 0) {
                            CharSequence text = clipData.getItemAt(0).getText();
                            if (text != null) {
                                mTextToProcess = text.toString();
                                Log.d("PopupActivity", "Clipboard text retrieved, length: " + mTextToProcess.length());
                                
                                // Reprocess the text now that we have clipboard content
                                populateWordSelectBox();
                                bigBangLayout.post( new Runnable() {
                                    @Override
                                    public void run() {
                                        setTargetWord();
                                        if(Utils.containsTranslationField(currentOutputPlan)){
                                            asyncTranslate(mTextToProcess);
                                        }
                                    }
                                });
                            } else {
                                Log.w("PopupActivity", "Clipboard text is null");
                                Toast.makeText(this, "Clipboard is empty", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Log.w("PopupActivity", "No clipboard content available");
                        Toast.makeText(this, "No clipboard content available", Toast.LENGTH_SHORT).show();
                    }
                } catch (SecurityException e) {
                    Log.e("PopupActivity", "Security exception accessing clipboard", e);
                    Toast.makeText(this, "Cannot access clipboard due to security restrictions", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Log.e("PopupActivity", "Error accessing clipboard", e);
                    Toast.makeText(this, "Error accessing clipboard", Toast.LENGTH_SHORT).show();
                }
                
                // Reset the flag to prevent repeated attempts
                isFromAndroidQClipboard = false;
            } else {
                Log.d("PopupActivity", "No focus, deferring clipboard access");
            }
        }
    }

    boolean isFromAndroidQClipboard = false;

    private void checkAndRequestClipboardPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (checkSelfPermission("android.permission.READ_CLIPBOARD_IN_BACKGROUND") != PackageManager.PERMISSION_GRANTED) {
                // Android 10+ doesn't allow direct clipboard permission requests
                // Instead, we check if foreground service has access
                Log.w("AnkiHelper", "Clipboard background access may be restricted on Android 10+");
            }
        }
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if (intent == null) {
            Log.d("PopupActivity", "Intent is null");
            return;
        }
        String action = intent.getAction();
        String type = intent.getType();
        Log.d("PopupActivity", "Handle intent, action: " + action + ", type: " + type);
        
        if (action == null || type == null) {
            Log.d("PopupActivity", "Action or type is null");
            return;
        }
        //getStringExtra() may return null
        if (Intent.ACTION_SEND.equals(action) && type.equals("text/plain")) {
            Log.d("PopupActivity", "Handling ACTION_SEND");
            String base64 = intent.getStringExtra(Constant.INTENT_ANKIHELPER_BASE64);
            mTextToProcess = intent.getStringExtra(Intent.EXTRA_TEXT);
            Log.d("PopupActivity", "ACTION_SEND text length: " + (mTextToProcess != null ? mTextToProcess.length() : 0));
            
            // Debug: log the actual text content (first 200 chars) to help with debugging
            if (mTextToProcess != null && mTextToProcess.length() > 0) {
                String preview = mTextToProcess.length() > 200 ? mTextToProcess.substring(0, 200) + "..." : mTextToProcess;
                Log.d("PopupActivity", "ACTION_SEND text preview: " + preview.replace("\n", "\\n"));
            }
            
            // IMPORTANT: Only use clipboard fallback if explicitly requested and we're on Android 10+
            // Avoid automatic clipboard access to prevent permission issues
            if(mTextToProcess != null && mTextToProcess.equals(Constant.USE_CLIPBOARD_CONTENT_FLAG)){
                // Only attempt clipboard access if we have focus and proper permissions
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // For Android 10+, check if we have focus before accessing clipboard
                    if (hasWindowFocus()) {
                        Log.d("PopupActivity", "Attempting clipboard access with focus");
                        isFromAndroidQClipboard = true;
                    } else {
                        Log.w("PopupActivity", "Cannot access clipboard - no focus, skipping clipboard fallback");
                        mTextToProcess = "";
                        Toast.makeText(this, "Cannot access clipboard. Please select text directly instead.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    // For older Android versions, proceed with clipboard access
                    isFromAndroidQClipboard = true;
                }
            }
            if(base64 != null && !base64.equals("0") && mTextToProcess != null){
                try {
                    mTextToProcess = new String(Base64.decode(mTextToProcess, Base64.DEFAULT));
                } catch (IllegalArgumentException e) {
                    // Handle base64 decode error
                    Log.e("PopupActivity", "Base64 decode error", e);
                }
            }
            mTargetWord = intent.getStringExtra(Constant.INTENT_ANKIHELPER_TARGET_WORD);
            mUrl = intent.getStringExtra(Constant.INTENT_ANKIHELPER_TARGET_URL);
            //mFbReaderBookmarkId = intent.getStringExtra(Constant.INTENT_ANKIHELPER_FBREADER_BOOKMARK_ID);
            String noteEditedByUser = intent.getStringExtra(Constant.INTENT_ANKIHELPER_NOTE);
            if(noteEditedByUser != null){
                mNoteEditedByUser = noteEditedByUser;
            }
            String updateId = intent.getStringExtra(Constant.INTENT_ANKIHELPER_NOTE_ID);
            mUpdateAction = intent.getStringExtra(Constant.INTENT_ANKIHELPER_UPDATE_ACTION);
            if(updateId != null && !updateId.isEmpty())
            {
                try{
                    mUpdateNoteId = Long.parseLong(updateId);
                    if(mUpdateNoteId > 0){
                        try {
                            NoteInfo note = MyApplication.getAnkiDroid(MyApplication.getContext()).getApi().getNote(mUpdateNoteId);
                            if(note != null) {
                                Set<String> tagsSet = note.getTags();
                                if(tagsSet != null && !tagsSet.isEmpty()) {
                                    // Use a copy to avoid potential modification issues
                                    mTagEditedByUser = new HashSet<>(tagsSet);
                                } else {
                                    mTagEditedByUser = new HashSet<>();
                                }
                            }
                        } catch (SecurityException e) {
                            // Permission denied - handle gracefully
                            Log.w("AnkiHelper", "SecurityException accessing AnkiDroid API: " + e.getMessage());
                            mTagEditedByUser = new HashSet<>();
                        } catch (Exception e) {
                            // Note not found or other issues - handle gracefully
                            Log.w("AnkiHelper", "Error getting note tags: " + e.getMessage());
                            mTagEditedByUser = new HashSet<>();
                        }
                    }
                }
                catch(Exception e){
                    // Handle parsing error
                    Log.w("AnkiHelper", "Error parsing note ID: " + e.getMessage());
                    mUpdateNoteId = -1L;
                    mTagEditedByUser = new HashSet<>();
                }
            }
        }
        if (Intent.ACTION_PROCESS_TEXT.equals(action) && type.equals("text/plain")) {
            Log.d("PopupActivity", "Handling ACTION_PROCESS_TEXT");
            
            // Android 10+ PROCESS_TEXT handling - try multiple methods to extract text
            mTextToProcess = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT);
            
            // Fallback: try alternative extra keys that some apps might use
            if (mTextToProcess == null || mTextToProcess.isEmpty()) {
                mTextToProcess = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT).toString();
            }
            
            // Additional fallback for apps that might use different extra keys
            if (mTextToProcess == null || mTextToProcess.isEmpty()) {
                mTextToProcess = intent.getStringExtra("android.intent.extra.PROCESS_TEXT");
            }
            
            Log.d("PopupActivity", "ACTION_PROCESS_TEXT text length: " + (mTextToProcess != null ? mTextToProcess.length() : 0));
            
            // Debug: log the actual text content (first 200 chars) to help with debugging
            if (mTextToProcess != null && mTextToProcess.length() > 0) {
                String preview = mTextToProcess.length() > 200 ? mTextToProcess.substring(0, 200) + "..." : mTextToProcess;
                Log.d("PopupActivity", "ACTION_PROCESS_TEXT text preview: " + preview.replace("\n", "\\n"));
            } else {
                Log.w("PopupActivity", "ACTION_PROCESS_TEXT received but no text content found in extras");
                // Check what extras are available for debugging
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    Log.d("PopupActivity", "Available extras: " + extras.keySet());
                    for (String key : extras.keySet()) {
                        Object value = extras.get(key);
                        Log.d("PopupActivity", "Extra " + key + " = " + (value != null ? value.toString() : "null"));
                    }
                }
            }
        }
        if (mTextToProcess == null) {
            mTextToProcess = "";
        }
        
        // Trim leading/trailing whitespace and check if text is effectively empty
        mTextToProcess = mTextToProcess.trim();
        if (mTextToProcess.isEmpty()) {
            Log.w("PopupActivity", "Text to process is empty after trimming");
            
            // Provide specific guidance based on the intent action
            if (Intent.ACTION_PROCESS_TEXT.equals(action)) {
                Toast.makeText(this, "No text was received. Try selecting text again or use the Share option instead.", Toast.LENGTH_LONG).show();
            } else if (Intent.ACTION_SEND.equals(action)) {
                Toast.makeText(this, "No text content received from app", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Log successful text reception
            Log.i("PopupActivity", "Successfully received text from intent action: " + action + ", length: " + mTextToProcess.length());
        }
        
        Log.d("PopupActivity", "Final text to process length: " + mTextToProcess.length());

        // Enhanced clipboard permission check for Android 10+
        checkClipboardPermissions();

        populateWordSelectBox();

        HistoryUtil.savePopupOpen(mTextToProcess);

        bigBangLayout.post( new Runnable() {
            @Override
            public void run() {
                setTargetWord();
                if(Utils.containsTranslationField(currentOutputPlan)){
                    asyncTranslate(mTextToProcess);
                }
            }
        });
    }

    private void populateWordSelectBox() {
        populateWordSelectBoxAsync(mTextToProcess);
    }

    private void populateWordSelectBoxAsync(final String textToProcess) {
        // Show loading indicator
        progressBar.setVisibility(View.VISIBLE);
        
        // Validate input text
        if (textToProcess == null || textToProcess.trim().isEmpty()) {
            Log.w("PopupActivity", "populateWordSelectBoxAsync called with empty text");
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "No text content to process", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Process text segmentation in background thread
        Thread textProcessingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("PopupActivity", "Starting text processing, text length: " + textToProcess.length());
                    
                    // Heavy text processing in background
                    final List<String> localSegments = TextSplitter.getLocalSegments(textToProcess);
                    
                    Log.d("PopupActivity", "Text processing complete, segments count: " + localSegments.size());
                    
                    // Update UI on main thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateBigBangLayoutWithSegments(localSegments);
                        }
                    });
                } catch (Exception e) {
                    Log.e("PopupActivity", "Error processing text", e);
                    final String errorMsg = e.getMessage() != null ? e.getMessage() : "Unknown error";
                    // Handle errors on main thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(PopupActivity.this, "Error processing text: " + errorMsg, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
        textProcessingThread.start();
    }

    private void updateBigBangLayoutWithSegments(List<String> localSegments) {
        Log.d("PopupActivity", "Updating BigBangLayout with segments, count: " + localSegments.size());
        
        // Hide loading indicator
        progressBar.setVisibility(View.GONE);
        
        // Update UI with processed segments
        bigBangLayout.removeAllViews();
        
        // Handle empty segments case
        if (localSegments.isEmpty()) {
            Log.w("PopupActivity", "No segments to display");
            Toast.makeText(this, "No text content to display", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Limit the number of segments to prevent performance issues, but increase the limit
        int maxSegments = 500; // Increased from 300 for better multi-line text support
        int segmentCount = 0;
        boolean wasTruncated = false;
        
        for (String localSegment : localSegments) {
            // Stop adding segments if we've reached the limit
            if (segmentCount >= maxSegments) {
                wasTruncated = true;
                // Add a visual indicator that text was truncated
                bigBangLayout.addTextItem("...");
                break;
            }
            bigBangLayout.addTextItem(localSegment);
            segmentCount++;
        }
        
        // Show user feedback if text was truncated
        if (wasTruncated) {
            Toast.makeText(this, "Text was truncated for performance (showing first " + maxSegments + " segments)", Toast.LENGTH_LONG).show();
        }
        
        // Log final state for debugging
        Log.d("PopupActivity", "Displayed " + segmentCount + " segments in BigBangLayout");
    }


    private void asyncSearch(final String word) {
        if (word.length() == 0) {
            showPronounce(false);
            return;
        }
        if(currentDicitonary == null || currentOutputPlan == null){
            return;
        }
        showProgressBar();
        progressBar.invalidate();
        showPronounce(true);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //Your code goes here
                    Log.d("clicked", "yes");
                    List<Definition> d = currentDicitonary.wordLookup(word);
                    Message message = mHandler.obtainMessage();
                    message.obj = d;
                    message.what = PROCESS_DEFINITION_LIST;
                    mHandler.sendMessage(message);
                } catch (Exception e) {
                    String error = e.getMessage();
                    Message message = mHandler.obtainMessage();
                    message.obj = error;
                    message.what = ASYNC_SEARCH_FAILED;
                    mHandler.sendMessage(message);
                }
            }
        });
        thread.start();
        HistoryUtil.saveWordlookup(mTextToProcess, word);
    }

    private void asyncTranslate(final String mTextToProcess){
        if(mTextToProcess == null) return;
        if(mTextToProcess.trim().equals("")) return;
        showTranslateLoading();
        Thread thread = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try{
                            com.mmjang.ankihelper.data.ai.AIManager aiManager = com.mmjang.ankihelper.data.ai.AIManager.getInstance();
                            String result = aiManager.translateTextWithDefaultTranslator(mTextToProcess);
                            Message message = mHandler.obtainMessage();
                            message.obj = result;
                            message.what = TRANSLATION_DONE;
                            mHandler.sendMessage(message);
                        }
                        catch(Exception e){
                            String error = "AI Translation Failed: " + e.getMessage();
                            Message message = mHandler.obtainMessage();
                            message.obj = error;
                            message.what = TRANSLATIOn_FAILED;
                            mHandler.sendMessage(message);
                        }
                    }
                }
        );
        thread.start();
    }

    private void setActAdapter(IDictionary dict) {
        Object adapter = dict.getAutoCompleteAdapter(PopupActivity.this,
                android.R.layout.simple_spinner_dropdown_item);
        if(adapter != null){
            if(adapter instanceof SimpleCursorAdapter){
                act.setAdapter((SimpleCursorAdapter) adapter);
            }
            else if(adapter instanceof UrbanAutoCompleteAdapter){
                act.setAdapter((UrbanAutoCompleteAdapter) adapter);
            }
        }
        act.setOnFocusChangeListener(
                new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if(hasFocus){
                            if(act.getText().toString().trim().isEmpty()){
                                return;
                            }
                            act.showDropDown();
                        }
                    }
                }
        );
    }

    //plan B
    private View getCardFromDefinition(final Definition def) {
        View view;
        if(settings.getLeftHandModeQ()){
            view = LayoutInflater.from(PopupActivity.this)
                    .inflate(R.layout.definition_item_left, null);
        }
        else{
            view = LayoutInflater.from(PopupActivity.this)
                    .inflate(R.layout.definition_item, null);
        }
        //toggle fab with clicks
//        view.setOnClickListener(
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        if(mFab.getVisibility() == View.VISIBLE){
//                            mFab.hide();
//                        }else{
//                            mFab.show();
//                        }
//                    }
//                }
//        );
        final TextView textVeiwDefinition = (TextView) view.findViewById(R.id.textview_definition);
        final ImageButton btnAddDefinition = (ImageButton) view.findViewById(R.id.btn_add_definition);
        final LinearLayout btnAddDefinitionLarge = (LinearLayout) view.findViewById(R.id.btn_add_definition_large);
        final ImageView defImage = view.findViewById(R.id.def_img);
        btnAddDefinitionLarge.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        btnAddDefinition.callOnClick();
                    }
                }
            );
            //final Definition def = mDefinitionList.get(position);
            textVeiwDefinition.setText(HtmlCompat.fromHtml(def.getDisplayHtml(), HtmlCompat.FROM_HTML_MODE_COMPACT));

        if(def.getDisplayHtml().isEmpty()){
            textVeiwDefinition.setVisibility(View.GONE);
        }

        if(def.getImageUrl()!=null && !def.getImageUrl().isEmpty()){
            Glide.with(this).load(def.getImageUrl()).into(defImage);
            defImage.setVisibility(View.VISIBLE);
        }

        if(def.getAudioUrl()!=null && !def.getAudioUrl().isEmpty()){
            textVeiwDefinition.setTextIsSelectable(false);
            textVeiwDefinition.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mMediaPlayer == null) {
                                mMediaPlayer = new MediaPlayer();
                                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                mMediaPlayer.setOnPreparedListener(
                                        new MediaPlayer.OnPreparedListener() {
                                            @Override
                                            public void onPrepared(MediaPlayer mp) {
                                                mMediaPlayer.start();
                                                mAudioProgress.setVisibility(View.GONE);
                                            }
                                        }
                                );
                            }
                            try {
                                if(mMediaPlayer.isPlaying()) {
                                    mMediaPlayer.reset();
                                    //mMediaPlayer.release();
                                }
                            }catch(IllegalStateException e){

                            }
                            try {
                                mMediaPlayer.setDataSource(PopupActivity.this, Uri.parse(def.getAudioUrl()));
                                mAudioProgress.setVisibility(View.VISIBLE);
                                mMediaPlayer.prepareAsync();
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(PopupActivity.this, e.getMessage(), Toast.LENGTH_SHORT);
                            } catch (IllegalStateException e){

                            }
//                            mMediaPlayer.setOnPreparedListener(
//                                    new MediaPlayer.OnPreparedListener() {
//                                        @Override
//                                        public void onPrepared(MediaPlayer mp) {
//                                        }
//                                    }
//                            );

                            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    mp.reset();
                                    mAudioProgress.setVisibility(View.GONE);
                                }
                            });

                            mMediaPlayer.setOnErrorListener(
                                    new MediaPlayer.OnErrorListener() {
                                        @Override
                                        public boolean onError(MediaPlayer mp, int what, int extra) {
                                            mp.reset();
                                            Toast.makeText(PopupActivity.this, "Failed to play audio, check your connection.", Toast.LENGTH_SHORT);
                                            mAudioProgress.setVisibility(View.GONE);
                                            return false;
                                        }
                                    }
                            );
//                            if(mMediaPlayer == null){
//                                mMediaPlayer = new MediaPlayer();
//                            }
//
//                            try {
//                                if(mMediaPlayer.isPlaying()) {
//                                    mMediaPlayer.reset();
//                                    //mMediaPlayer.release();
//                                }
//                            }catch(IllegalStateException e){
//
//                            }
//                            try {
//                                Toast.makeText(PopupActivity.this, "Loading...", Toast.LENGTH_SHORT).show();
//                                mMediaPlayer.setDataSource(PopupActivity.this, Uri.parse(def.getAudioUrl()));
//                                mMediaPlayer.prepare();
//                                mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                                    @Override
//                                    public void onCompletion(MediaPlayer mp) {
//                                        mMediaPlayer.reset();
//                                        //mMediaPlayer.release();
//                                    }
//                                });
//                                mMediaPlayer.start();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                                Toast.makeText(PopupActivity.this, "Failed to play audio", Toast.LENGTH_SHORT).show();
//                            }
                        }
                    }
            );
        }

        //set custom action for the textView
        makeTextViewSelectAndSearch(textVeiwDefinition);
        //holder.itemView.setAnimation(AnimationUtils.loadAnimation(mActivity, android.R.anim.fade_in));
        //holder.textVeiwDefinition.setTextColor(Color.BLACK);
        btnAddDefinition.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //vibarate(Constant.VIBRATE_DURATION);
                        //before add, check if this note is already added by check the attached tag
                        try {
                            Long noteIdAdded = (Long) btnAddDefinition.getTag(R.id.TAG_NOTE_ID);
                            if (noteIdAdded != null) {
                                if (mUpdateNoteId == 0) {
                                    if (Utils.deleteNote(PopupActivity.this, noteIdAdded.longValue())) {
                                        btnAddDefinition.setImageDrawable(ContextCompat.getDrawable(
                                                PopupActivity.this,
                                                Utils.getResIdFromAttribute(PopupActivity.this, R.attr.icon_add)));
                                        btnAddDefinition.setTag(R.id.TAG_NOTE_ID, null);
                                        Toast.makeText(PopupActivity.this, R.string.str_cancel_note_add, Toast.LENGTH_SHORT).show();

                                    } else {
                                        Toast.makeText(PopupActivity.this, R.string.error_note_cancel, Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(PopupActivity.this, R.string.str_not_cancelable_append_mode, Toast.LENGTH_SHORT).show();
                                }
                                return;
                            }

                            //save image
                            if (def.getImageUrl() != null && !def.getImageUrl().isEmpty()) {
                                if (defImage.getDrawable() != null) {
                                    BitmapDrawable drawable = (BitmapDrawable) defImage.getDrawable();
                                    Bitmap bm = drawable.getBitmap();

                                    OutputStream fOut = null;
                                    //Uri outputFileUri;
                                    try {
                                        File root = new File(Constant.IMAGE_MEDIA_DIRECTORY);
                                        if (!root.exists()) {
                                            root.mkdirs();
                                        }
                                        File sdImageMainDirectory = new File(root, def.getImageName());
                                        //outputFileUri = Uri.fromFile(sdImageMainDirectory);
                                        fOut = new FileOutputStream(sdImageMainDirectory);
                                    } catch (Exception e) {

                                    }
                                    try {
                                        bm.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                                        fOut.flush();
                                        fOut.close();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            ///////////////////////////////////
                            AnkiDroidHelper mAnkiDroid = MyApplication.getAnkiDroid(MyApplication.getContext());
                            String[] sharedExportElements = Constant.getSharedExportElements();
                            String[] exportFields = new String[currentOutputPlan.getFieldsMap().size()];
                            int i = 0;
                            Map<String, String> map = currentOutputPlan.getFieldsMap();
                            for (String exportedFieldKey : currentOutputPlan.getFieldsMap().values()) {
                                if (exportedFieldKey.equals(sharedExportElements[0])) {
                                    exportFields[i] = "";
                                    i++;
                                    continue;
                                }

                                if (exportedFieldKey.equals(sharedExportElements[1])) {
                                    exportFields[i] = getNormalSentence(bigBangLayout.getLines());
                                    i++;
                                    continue;
                                }

                                if (exportedFieldKey.equals(sharedExportElements[2])) {
                                    exportFields[i] = getBoldSentence(bigBangLayout.getLines());
                                    i++;
                                    continue;
                                }
                                if (exportedFieldKey.equals(sharedExportElements[3])) {
                                    exportFields[i] = getBlankSentence(bigBangLayout.getLines(), true);
                                    i++;
                                    continue;
                                }
                                if (exportedFieldKey.equals(sharedExportElements[4])) {
                                    exportFields[i] = getBlankSentence(bigBangLayout.getLines(), false);
                                    i++;
                                    continue;
                                }
                                if (exportedFieldKey.equals(sharedExportElements[5])) {
                                    exportFields[i] = mNoteEditedByUser;
                                    i++;
                                    continue;
                                }
                                if (exportedFieldKey.equals(sharedExportElements[6])) {
                                    exportFields[i] = mUrl;
                                    i++;
                                    continue;
                                }
                                if (exportedFieldKey.equals(sharedExportElements[7])) {
                                    exportFields[i] = Utils.getAllHtmlFromDefinitionList(mDefinitionList);
                                    i++;
                                    continue;
                                }
                                if (exportedFieldKey.equals(sharedExportElements[8])) {
                                    exportFields[i] = mEditTextTranslation.getText().toString().replace("\n", "<br/>");
                                    i++;
                                    continue;
                                }
//                            if(exportedFieldKey.equals(sharedExportElements[5])){
//                                if(mFbReaderBookmarkId != null){
//                                    exportFields[i] = String.format(Constant.FBREADER_URL_TMPL, mFbReaderBookmarkId);
//                                }else{
//                                    exportFields[i]="";
//                                }
//                                i++;
//                                continue;
//                            }
                                if (def.hasElement(exportedFieldKey)) {
                                    exportFields[i] = def.getExportElement(exportedFieldKey);
                                    i++;
                                    continue;
                                }

                                exportFields[i] = "";
                                i++;
                            }
                            //handle download; audio or image
                            if (def.getAudioUrl() != null && !def.getAudioUrl().isEmpty()) {
                                if (fetch == null) {
                                    initFetch();
                                }
                                if (map.containsValue("原声例句")) {
                                    final Request request = new Request(def.getAudioUrl(), Constant.AUDIO_MEDIA_DIRECTORY + def.getAudioName());
                                    request.setPriority(Priority.HIGH);
                                    request.setNetworkType(NetworkType.ALL);
                                    isFetchDownloading = true;
                                    fetch.enqueue(request,
                                            new Func<Request>() {
                                                @Override
                                                public void call(@NotNull Request result) {
                                                    mAudioProgress.setVisibility(View.VISIBLE);
                                                    //                                isFetchDownloading = true;
                                                }
                                            }
                                            ,
                                            new Func<Error>() {
                                                @Override
                                                public void call(@NotNull Error result) {
                                                    isFetchDownloading = false;
                                                }
                                            }
                                    );
                                }
                            }

                            if (def.getAudioUrl() != null && !def.getAudioUrl().isEmpty()) {
                                if (fetch == null) {
                                    initFetch();
                                }
                                if (!def.getAudioUrl().isEmpty() && (map.containsValue("音频") || map.containsValue("复合项"))) {
                                    final Request request = new Request(def.getAudioUrl(), Constant.AUDIO_MEDIA_DIRECTORY + def.getAudioName());
                                    request.setPriority(Priority.HIGH);
                                    request.setNetworkType(NetworkType.ALL);
                                    isFetchDownloading = true;
                                    fetch.enqueue(request,
                                            new Func<Request>() {
                                                @Override
                                                public void call(@NotNull Request result) {
                                                    mAudioProgress.setVisibility(View.VISIBLE);
                                                    isFetchDownloading = true;
                                                }
                                            }
                                            ,
                                            new Func<Error>() {
                                                @Override
                                                public void call(@NotNull Error result) {
                                                    isFetchDownloading = false;
                                                }
                                            }
                                    );
                                }
                            }

                            if (def.getAudioUrl() != null && !def.getAudioUrl().isEmpty()) {
                                if (fetch == null) {
                                    initFetch();
                                }
                                if (map.containsValue("离线发音")) {
                                    final Request request = new Request(def.getAudioUrl(), Constant.AUDIO_MEDIA_DIRECTORY + def.getAudioName());
                                    request.setPriority(Priority.HIGH);
                                    request.setNetworkType(NetworkType.ALL);
                                    isFetchDownloading = true;
                                    fetch.enqueue(request,
                                            new Func<Request>() {
                                                @Override
                                                public void call(@NotNull Request result) {
                                                    mAudioProgress.setVisibility(View.VISIBLE);
                                                    isFetchDownloading = true;
                                                }
                                            }
                                            ,
                                            new Func<Error>() {
                                                @Override
                                                public void call(@NotNull Error result) {
                                                    isFetchDownloading = false;
                                                }
                                            }
                                    );
                                }
                            }
                            /////////////////
                            long deckId = currentOutputPlan.getOutputDeckId();
                            long modelId = currentOutputPlan.getOutputModelId();
                            if (mUpdateNoteId == 0) {
                                Long result = mAnkiDroid.getApi().addNote(modelId, deckId, exportFields, mTagEditedByUser);
                                if (result != null) {
                                    Toast.makeText(PopupActivity.this, R.string.str_added, Toast.LENGTH_SHORT).show();
                                    btnAddDefinition.setImageDrawable(ContextCompat.getDrawable(
                                            PopupActivity.this, Utils.getResIdFromAttribute(PopupActivity.this, R.attr.icon_remove)));
                                    clearBigbangSelection();
                                    mNoteEditedByUser = "";
                                    //attach the noteid to the button
                                    btnAddDefinition.setTag(R.id.TAG_NOTE_ID, result);
                                    //if there is a note id field in the model, update the note
                                    int count = 0;
                                    for (String field : currentOutputPlan.getFieldsMap().keySet()) {
                                        if (field.replace(" ", "").toLowerCase().equals("noteid")) {
                                            exportFields[count] = result.toString();
                                            boolean success = mAnkiDroid.getApi().updateNoteFields(
                                                    result.longValue(),
                                                    exportFields
                                            );
                                            if (!success) {
                                                Toast.makeText(PopupActivity.this, R.string.str_error_noteid, Toast.LENGTH_SHORT).show();
                                            }
                                            break;
                                        }
                                        count++;
                                    }
                                    //save note add
                                    HistoryUtil.saveNoteAdd("", getBoldSentence(bigBangLayout.getLines()),
                                            currentDicitonary.getDictionaryName(),
                                            textVeiwDefinition.getText().toString(),
                                            mTranslatedResult,
                                            mNoteEditedByUser,
                                            mTagEditedByUser.toString()
                                    );
                                } else {
                                    Toast.makeText(PopupActivity.this, R.string.str_failed_add, Toast.LENGTH_SHORT).show();
                                }
                            } else {//there's note id, so we need to retrieve note first
                                NoteInfo note = mAnkiDroid.getApi().getNote(mUpdateNoteId);
                                String[] original = note.getFields();
                                Set<String> tags = note.getTags();
                                if (original == null || original.length != exportFields.length) {
                                    Toast.makeText(PopupActivity.this, R.string.str_error_notetype_noncompatible, Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                if (mUpdateAction != null && mUpdateAction.equals("replace")) {
                                    //replace
                                    for (int j = 0; j < original.length; j++) {
                                        if (exportFields[j].isEmpty()) {
                                            exportFields[j] = original[j];
                                        }
                                    }

                                } else {
                                    //append
                                    for (int j = 0; j < original.length; j++) {
                                        if (original[j].trim().isEmpty() || exportFields[j].trim().isEmpty()) {
                                            exportFields[j] = original[j] + exportFields[j];
                                        } else {
                                            exportFields[j] = original[j] + "<br/>" + exportFields[j];
                                        }
                                    }
                                }
                                //we need to check the tag used by user is already in the tags, if not, add it
                                tags.addAll(mTagEditedByUser);
                                boolean success = mAnkiDroid.getApi().updateNoteFields(mUpdateNoteId, exportFields);
                                boolean successTag = mAnkiDroid.getApi().updateNoteTags(mUpdateNoteId, tags);
                                if (success && successTag) {
                                    Toast.makeText(PopupActivity.this, R.string.str_note_updated, Toast.LENGTH_SHORT).show();
                                    btnAddDefinition.setImageDrawable(ContextCompat.getDrawable(
                                            PopupActivity.this, Utils.getResIdFromAttribute(PopupActivity.this, R.attr.icon_remove)));
                                    //btnAddDefinition.setEnabled(false);
                                } else {
                                    Toast.makeText(PopupActivity.this, R.string.str_error_note_update, Toast.LENGTH_SHORT).show();
                                }
                            }
                            if (settings.getAutoCancelPopupQ()) {
                                if (fetch == null) {
                                    finish();
                                } else {
                                    if (!isFetchDownloading) {
                                        finish();
                                    }
                                }
                            }
                        }catch (Exception e){
                            Toast.makeText(PopupActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        return view;
    }

    private void setupEditNoteDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(PopupActivity.this);
        LayoutInflater inflater = PopupActivity.this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_edit_note, null);
        dialogBuilder.setView(dialogView);

        final EditText edt = (EditText) dialogView.findViewById(R.id.edit_note);
        edt.setHorizontallyScrolling(false);
        edt.setMaxLines(4);
        edt.setText(mNoteEditedByUser);
        edt.setSelection(mNoteEditedByUser.length());
        dialogBuilder.setTitle(R.string.dialog_note);
        //dialogBuilder.setMessage("输入笔记");
        dialogBuilder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mNoteEditedByUser = edt.getText().toString();
            }
        });
//                        dialogBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int whichButton) {
//                                //pass
//                            }
//                        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    private void setupTagDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(PopupActivity.this);
        LayoutInflater inflater = PopupActivity.this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_edit_tag, null);
        dialogBuilder.setView(dialogView);
        final AutoCompleteTextView editTag = (AutoCompleteTextView) dialogView.findViewById(R.id.edit_tag);
        final CheckBox checkBoxSetAsDefaultTag = (CheckBox) dialogView.findViewById(R.id.checkbox_as_default_tag);
        final ChipGroup tagChipGroup = (ChipGroup) dialogView.findViewById(R.id.tag_chip_list);
        editTag.setImeOptions(EditorInfo.IME_ACTION_DONE);
        if (mTagEditedByUser.size() > 0) {
            String text = Utils.fromTagSetToString(mTagEditedByUser);
            editTag.setText(text);
            editTag.setSelection(text.length());
        }
        tagChipGroup.setSingleSelection(false);
        final List<UserTag> userTags = LitePal.findAll(UserTag.class);
        for(UserTag userTag : userTags){
            final Chip chip = (Chip) inflater.inflate(R.layout.tag_chip_item, null);
            chip.setText(userTag.getTag());
            chip.setOnCheckedChangeListener(
                    new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if(isChecked){
                                mTagEditedByUser.add(chip.getText().toString());
                            }else{
                                mTagEditedByUser.remove(chip.getText().toString());
                            }
                            //tag1,tag2,tag3
                            String text = Utils.fromTagSetToString(mTagEditedByUser);
                            editTag.setText(text);
                            editTag.setSelection(text.length());
                        }
                    }
            );
            if(mTagEditedByUser.contains(chip.getText().toString())){
                chip.setChecked(true);
            }
            tagChipGroup.addView(chip);
        }
//        String[] arr = new String[userTags.size()];
//        for (int i = 0; i < userTags.size(); i++) {
//            arr[i] = userTags.get(i).getTag();
//        }
//        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(PopupActivity.this,
//                R.layout.support_simple_spinner_dropdown_item, arr);
//        editTag.setAdapter(arrayAdapter);
//        editTag.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (editTag.getText().toString().isEmpty()) {
//                    editTag.showDropDown();
//                }
//                return false;
//            }
//        });
        boolean setDefaultQ = settings.getSetAsDefaultTag();
        checkBoxSetAsDefaultTag.setChecked(setDefaultQ);
        dialogBuilder.setTitle(R.string.dialog_tag);
        //dialogBuilder.setMessage("输入笔记");
        dialogBuilder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String tag = editTag.getText().toString().trim();
                if (tag.isEmpty()) {
                    if (checkBoxSetAsDefaultTag.isChecked()) {
                        mTagEditedByUser.clear();
                        Toast.makeText(PopupActivity.this, R.string.tag_cant_be_blank, Toast.LENGTH_LONG).show();
                    } else {
                        settings.setSetAsDefaultTag(false);
                        mTagEditedByUser.clear();
                    }
                    return;
                } else {
                    mTagEditedByUser = Utils.fromStringToTagSet(editTag.getText().toString());
                    settings.setSetAsDefaultTag(checkBoxSetAsDefaultTag.isChecked());
                    settings.setDefaultTag(editTag.getText().toString());
                    for(String t : mTagEditedByUser){
                        if(!userTags.contains(t)){ //add new tag
                            UserTag userTag = new UserTag(t);
                            userTag.save();
                        }
                    }
                }

            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    private void initFetch(){
        FetchConfiguration fetchConfiguration = new FetchConfiguration.Builder(this)
                .setDownloadConcurrentLimit(3)
                .build();
        fetch = Fetch.Impl.getInstance(fetchConfiguration);
        fetch.addListener(
                new FetchListener() {
                    @Override
                    public void onAdded(@NotNull Download download) {

                    }

                    @Override
                    public void onQueued(@NotNull Download download, boolean b) {

                    }

                    @Override
                    public void onWaitingNetwork(@NotNull Download download) {

                    }

                    @Override
                    public void onCompleted(@NotNull Download download) {
                        Toast.makeText(PopupActivity.this, "Download Completed!", Toast.LENGTH_SHORT).show();
                        mAudioProgress.setVisibility(View.GONE);
                        isFetchDownloading = false;
                        if(settings.getAutoCancelPopupQ()) {
                            finish();
                        }
                    }

                    @Override
                    public void onError(@NotNull Download download, @NotNull Error error, @Nullable Throwable throwable) {
                        Toast.makeText(PopupActivity.this, "Download Failed!", Toast.LENGTH_SHORT).show();
                        mAudioProgress.setVisibility(View.GONE);
                        isFetchDownloading = false;
                        if(settings.getAutoCancelPopupQ()) {
                            finish();
                        }
                    }

                    @Override
                    public void onDownloadBlockUpdated(@NotNull Download download, @NotNull DownloadBlock downloadBlock, int i) {

                    }

                    @Override
                    public void onStarted(@NotNull Download download, @NotNull List<? extends DownloadBlock> list, int i) {

                    }

                    @Override
                    public void onProgress(@NotNull Download download, long l, long l1) {

                    }

                    @Override
                    public void onPaused(@NotNull Download download) {

                    }

                    @Override
                    public void onResumed(@NotNull Download download) {

                    }

                    @Override
                    public void onCancelled(@NotNull Download download) {

                    }

                    @Override
                    public void onRemoved(@NotNull Download download) {

                    }

                    @Override
                    public void onDeleted(@NotNull Download download) {

                    }
                }
        );
    }

    //cancel auto completetextview focus
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        View v = getCurrentFocus();
        boolean ret = super.dispatchTouchEvent(event);

        if (v instanceof AutoCompleteTextView) {
            View currentFocus = getCurrentFocus();
            int screenCoords[] = new int[2];
            currentFocus.getLocationOnScreen(screenCoords);
            float x = event.getRawX() + currentFocus.getLeft() - screenCoords[0];
            float y = event.getRawY() + currentFocus.getTop() - screenCoords[1];

            if (event.getAction() == MotionEvent.ACTION_UP
                    && (x < currentFocus.getLeft() ||
                    x >= currentFocus.getRight() ||
                    y < currentFocus.getTop() ||
                    y > currentFocus.getBottom())) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
                v.clearFocus();
            }
        }
        return ret;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        
        // Clean up handler to prevent memory leaks
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        
        Runtime.getRuntime().gc();
    }

    private void startCBService() {
        Intent intent = new Intent(this, CBWatcherService.class);
        startService(intent);
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        btnSearch.setVisibility(View.GONE);
    }

    private void showSearchButton() {
        progressBar.setVisibility(View.GONE);
        btnSearch.setVisibility(View.VISIBLE);
    }

    private void showPronounce(boolean shouldShow) {
        btnPronounce.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
    }

    private void showTranslateNormal(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mBtnTranslation.setImageResource(Utils.getResIdFromAttribute(this, R.attr.icon_translate_normal));
        }
    }

    private void showTranslateLoading(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mBtnTranslation.setImageResource(Utils.getResIdFromAttribute(this, R.attr.icon_translate_wait));
        }
    }

    private void showTranslateDone(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mBtnTranslation.setImageResource(Utils.getResIdFromAttribute(this, R.attr.icon_translate_done));
        }
    }

    private void showTranslationCardView(boolean show){
        mCardViewTranslation.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onSelected(String text) {
        String currentWord = FieldUtil.getSelectedText(bigBangLayout.getLines());
        if (!currentWord.equals(act.getText().toString())) {
            mCurrentKeyWord = currentWord;
            act.setText(currentWord);
            asyncSearch(currentWord);
        }
    }

    @Override
    public void onSearch(String text) {

    }

    @Override
    public void onShare(String text) {

    }

    @Override
    public void onCopy(String text) {

    }

    @Override
    public void onTrans(String text) {

    }

    @Override
    public void onDrag() {

    }

    @Override
    public void onSwitchType(boolean isLocal) {

    }

    @Override
    public void onSwitchSymbol(boolean isShow) {

    }

    @Override
    public void onSwitchSection(boolean isShow) {

    }

    @Override
    public void onDragSelection() {

    }

    @Override
    public void onCancel() {
        act.setText("");
        asyncSearch("");
    }

    void vibarate(int ms) {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(ms);
        }
    }

    void clearBigbangSelection(){
        for (BigBangLayout.Line line : bigBangLayout.getLines()) {
            List<BigBangLayout.Item> items = line.getItems();
            for (BigBangLayout.Item item : items) {
                if (item.getText().equals(mTargetWord)) {
                    item.setSelected(false);
                }
            }
        }
    }

    private void makeTextViewSelectAndSearch(final TextView textView){
        textView.setCustomSelectionActionModeCallback(new ActionMode.Callback() {

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // Remove the "copy all" option
                menu.removeItem(android.R.id.cut);
                //menu.removeItem(android.R.id.copy);
                return true;
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Called when action mode is first created. The menu supplied
                // will be used to generate action buttons for the action mode

                // Here is an example MenuItem
                menu.add(0, 1, 0, "Definition").setIcon(R.drawable.ic_ali_search);
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // Called when an action mode is about to be exited and
                // destroyed
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case 1:
                        int min = 0;
                        int max = textView.getText().length();
                        if (textView.isFocused()) {
                            final int selStart = textView.getSelectionStart();
                            final int selEnd = textView.getSelectionEnd();

                            min = Math.max(0, Math.min(selStart, selEnd));
                            max = Math.max(0, Math.max(selStart, selEnd));
                        }
                        // Perform your definition lookup with the selected text
                        final String selectedText = textView.getText().subSequence(min, max).toString();
                        // Finish and close the ActionMode
                        mode.finish();
                        act.setText(selectedText);
                        asyncSearch(selectedText);
                        return true;
                    default:
                        break;
                }
                return false;
            }

        });
    }
}
