package com.mmjang.ankihelper.data.dict;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.FilterQueryProvider;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollinsEnEn implements IDictionary {
    private static final String DATABASE_NAME = "collins_v2.db";
    private static final String TABLE_DICT = "dict";
    private static final String FIELD_HWD = "hwd";
    private static final String FIELD_DISPLAYED_HWD = "display_hwd";
    private static final String FIELD_PHRASE = "phrase";
    private static final String FIELD_PHONETICS = "phonetics";
    private static final String FIELD_SENSE = "sense";
    private static final String FIELD_EXT = "ext";
    private static final String FIELD_DEF_EN = "def_en";
    private static final String FIELD_DEF_CN = "def_cn";

    private final Context mContext;
    private final SQLiteDatabase db;

    private static final String[] EXP_ELE_LIST = new String[]{
            "单词",
            "音标",
            "释义",
            "有道美式发音",
            "有道英式发音",
            "复合项"
    };

    private static final String DICT_NAME = "柯林斯英英";
    private static final String DICT_INTRO = "数据来自柯林斯COBUILD高级词典，释义权威地道";

    public CollinsEnEn(Context context) {
        mContext = context;
        CollinsEnEnDatabaseHelper dbHelper = new CollinsEnEnDatabaseHelper(context);
        db = dbHelper.getReadableDatabase();
    }

    public String getDictionaryName() {
        return DICT_NAME;
    }

    public String getIntroduction() {
        return DICT_INTRO;
    }

    public String[] getExportElementsList() {
        return EXP_ELE_LIST;
    }

    public List<Definition> wordLookup(String key) {
        key = keyCleanup(key);
        List<Definition> definitions = new ArrayList<>();

        if (key.isEmpty()) {
            return definitions;
        }

        Cursor cursor = db.query(
                TABLE_DICT,
                new String[]{FIELD_HWD, FIELD_DISPLAYED_HWD, FIELD_PHRASE,
                           FIELD_PHONETICS, FIELD_SENSE, FIELD_EXT, FIELD_DEF_EN, FIELD_DEF_CN},
                FIELD_HWD + "=? COLLATE NOCASE",
                new String[]{key},
                null,
                null,
                null
        );

        try {
            while (cursor.moveToNext()) {
                Definition definition = getDefFromCursor(cursor);
                definitions.add(definition);
            }
        } finally {
            cursor.close();
        }

        return definitions;
    }

    public ListAdapter getAutoCompleteAdapter(Context context, int layout) {
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
            context,
            layout,
            null,
            new String[]{FIELD_HWD},
            new int[]{android.R.id.text1},
            0
        );

        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence constraint) {
                return getFilterCursor(constraint.toString());
            }
        });

        adapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
            public CharSequence convertToString(Cursor cursor) {
                return cursor.getString(cursor.getColumnIndexOrThrow("hwd"));
            }
        });

        return adapter;
    }

    private Definition getDefFromCursor(Cursor cursor) {
        Map<String, String> elementMap = new HashMap<>();

        String hwd = cursor.getString(0);
        String phrase = cursor.getString(2).trim();
        String phonetics = cursor.getString(3).trim();
        String sense = cursor.getString(4).trim();
        String ext = cursor.getString(5).trim();
        String defEn = cursor.getString(6).trim();
        String defCn = cursor.getString(7).trim();

        elementMap.put(EXP_ELE_LIST[0], phrase.isEmpty() ? hwd : phrase);
        elementMap.put(EXP_ELE_LIST[1], phonetics);
        elementMap.put(EXP_ELE_LIST[2], "<i>" + sense + "</i><br/>" + ext + "<br/>" + defEn);
        elementMap.put(EXP_ELE_LIST[3], getYoudaoAudioTag(hwd, 2));
        elementMap.put(EXP_ELE_LIST[4], getYoudaoAudioTag(hwd, 1));

        String combined = getCombinedElement(elementMap);
        elementMap.put(EXP_ELE_LIST[5], combined);

        String displayHtml = buildDisplayHtml(phrase, hwd, defEn, defCn);

        return new Definition(elementMap, displayHtml);
    }

    private Cursor getFilterCursor(String query) {
        try {
            return db.query(
                "hwds",
                new String[]{"rowid as _id", "hwd"},
                "hwd LIKE ?",
                new String[]{query + "%"},
                null,
                null,
                null
            );
        } catch (Exception e) {
            Log.e("CollinsEnEn", "Filter query error: " + e.getMessage());
            return null;
        }
    }

    private String getCombinedElement(Map<String, String> elements) {
        return "<div class='div_collins'>" +
               "<div class='collins_hwd'>" + elements.get(EXP_ELE_LIST[0]) + "</div> " +
               "<div class='collins_ipa'>" + elements.get(EXP_ELE_LIST[1]) + elements.get(EXP_ELE_LIST[3]) + "</div>" +
               "<div class='collins_def'>" + elements.get(EXP_ELE_LIST[2]).replace("<br/>", " ") + "</div></div>";
    }

    private String buildDisplayHtml(String phrase, String hwd, String defEn, String defCn) {
        StringBuilder displayHtml = new StringBuilder();
        if (phrase.isEmpty()) {
            displayHtml.append("<b>").append(hwd).append("</b><br/>");
            displayHtml.append(defEn);
        } else {
            displayHtml.append("<b>").append(phrase).append("</b><br/>");
            displayHtml.append(defEn).append(" ").append(defCn);
        }
        return displayHtml.toString();
    }

    private String keyCleanup(String key) {
        return key.trim().replaceAll("[,.!?()\"'“”’？]", "").toLowerCase();
    }

    private String getYoudaoAudioTag(String word, int voiceType) {
        return "[sound:https://dict.youdao.com/dictvoice?audio=" + word + "&type=" + voiceType + "]";
    }
}