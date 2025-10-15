package com.lmyby.ankihelper.data.plan;

import android.content.Context;

import com.lmyby.ankihelper.MyApplication;
import com.lmyby.ankihelper.R;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by liao on 2017/3/19.
 */

public class VocabularyCardModel {
    private final String MODEL_FILE = "vocabulary_card_model.html";
    private final String MODEL_SPLITTER = "@@@";
    private final String CODING = "UTF-8";
    private final int NUMBER_OF_MODEL_STRING = 5;

    private String[] front = new String[2];
    private String css = "";
    private String[] back = new String[2];

    String[] QFMT = new String[2];
    String[] AFMT = new String[2];
    String[] Cards = {"recite", "type"};
    String CSS;

    // Resource IDs for internationalization
    private static final int[] FIELD_RES_IDS = {
            R.string.vocab_field_word,
            R.string.vocab_field_phonetic,
            R.string.vocab_field_definition,
            R.string.vocab_field_notes,
            R.string.vocab_field_example,
            0,  // "url" - no translation needed
            R.string.vocab_field_pronunciation
    };

    /**
     * Get localized vocabulary card field names
     * @return Array of localized field names
     */
    public static String[] getFields() {
        Context context = MyApplication.getContext();
        String[] fields = new String[FIELD_RES_IDS.length];
        for (int i = 0; i < FIELD_RES_IDS.length; i++) {
            if (FIELD_RES_IDS[i] == 0) {
                // Handle special case for "url" which doesn't need translation
                fields[i] = "url";
            } else {
                fields[i] = context.getString(FIELD_RES_IDS[i]);
            }
        }
        return fields;
    }

    /**
     * @deprecated Use {@link #getFields()} instead for localized field names
     */
    @Deprecated
    public static final String [] FILEDS = {
            "单词",
            "音标",
            "释义",
            "笔记",
            "例句",
            "url",
            "发音"
    };


    VocabularyCardModel(Context ct){

        try {
            InputStream ips = ct.getResources().getAssets().open(MODEL_FILE);
            byte[] data = new byte[ips.available()];
            ips.read(data);
            String defaultModelStr = new String(data, CODING);
            String[] defaultModelSplitted = defaultModelStr.split(MODEL_SPLITTER);
            if(defaultModelSplitted.length == NUMBER_OF_MODEL_STRING) {
                front[0] = defaultModelSplitted[0];
                back[0] = defaultModelSplitted[1];
                front[1] = defaultModelSplitted[2];
                back[1] = defaultModelSplitted[3];
                css = defaultModelSplitted[4];
            }
            else{
                ;
            }
            QFMT[0] = front[0];
            QFMT[1] = front[1];
            AFMT[0] = back[0];
            AFMT[1] = back[1];
            CSS = css;

        }
        catch(IOException e) {
            e.printStackTrace();
            return;
        }
    }
}

