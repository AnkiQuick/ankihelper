package com.mmjang.ankihelper.data.dict;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.FilterQueryProvider;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
/**
 * Created by liao on 2017/3/15.
 */

public class Cdepe4 extends SQLiteAssetHelper implements IDictionary {
    //private static final String DATABASE_NAME = ".db";
    private static final String DATABASE_NAME = "cdepe4.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_DICT = "dict";
    private static final String FIELD_HWD = "hwd";
    private static final String FIELD_PHRASE = "phrase";
    private static final String FIELD_SENSE = "sense";
    private static final String FIELD_PHONETICS = "phonetics";
    private static final String FIELD_DEF_EN = "def_en";
    private static final String FIELD_DEF_CN = "def_cn";

    private static final String DICT_NAME = "剑桥在线英汉双解词典完美版";

    private SQLiteDatabase db;

    private Context mContext;

    public Cdepe4(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        db = getReadableDatabase();
        mContext = context;
    }

    private static final String[] EXP_ELE_LIST = new String[]{
            "单词",
            "词性",
            "音标",
            "英文释义",
            "中文释义",
            "有道美式发音",
            "有道英式发音"
    };

    public String getDictionaryName() {
        return DICT_NAME;
    }

    public String getIntroduction() {
        return "剑桥在线英汉双解词典完美版,来自https://forum.freemdict.com/t/topic/25795";
    }

    public String[] getExportElementsList() {
        return EXP_ELE_LIST;
    }

    public List<Definition> wordLookup(String key) {
        //db = getReadableDatabase(); // according to stackoverflow, it's alright to let the database open
        key = keyCleanup(key);
        List<Definition> re = queryDefinition(key);
        Log.d("", "单词需要查找变形表");
        String[] deflectResult = getForms(key);
        for (String s : deflectResult) {
            Log.d("", "已变形单词" + s);
        }
        if (deflectResult.length == 0) {
            //
        } else {
            for (String deflectedWord : deflectResult) {
                re.addAll(queryDefinition(deflectedWord));
            }
        }

        if(re.isEmpty()){
            try{
                re.add(toDefinition(YoudaoOnline.getDefinition(key)));
            }
            catch (IOException e){
                //Toast.makeText(mContext, "本地词典未查到，有道词典在线查询失败，请检查网络连接", Toast.LENGTH_SHORT).show();
            }
        }

        // db.close();
        return re;
    }

    /**
     * @param context this
     * @param layout  support_simple_spinner_dropdown_item
     * @return
     */
    public ListAdapter getAutoCompleteAdapter(Context context, int layout) {
        SimpleCursorAdapter adapter =
                new SimpleCursorAdapter(context, layout,
                        null,
                        new String[]{FIELD_HWD},
                        new int[]{android.R.id.text1},
                        0
                );
        adapter.setFilterQueryProvider(
                new FilterQueryProvider() {
                    @Override
                    public Cursor runQuery(CharSequence constraint) {
                        return getFilterCursor(constraint.toString());
                    }
                }
        );
        adapter.setCursorToStringConverter(
                new SimpleCursorAdapter.CursorToStringConverter() {
                    @Override
                    public CharSequence convertToString(Cursor cursor) {
                        return cursor.getString(1);
                    }
                }
        );

        return adapter;
    }

    /**
     * @param q word to lookup
     * @return a array of definitions, retrun ArrayList<>() if none was found
     */
    private ArrayList<Definition> queryDefinition(String q) {
        //SQLiteDatabase db = getReadableDatabase();
        ArrayList<Definition> re = new ArrayList<>();
        if(q.isEmpty()){
            return re;
        }
        Cursor cursor = db.query(TABLE_DICT,
                new String[]{FIELD_HWD, FIELD_PHRASE,FIELD_SENSE, FIELD_PHONETICS, FIELD_DEF_EN, FIELD_DEF_CN},
                FIELD_HWD + "=? COLLATE NOCASE", new String[]{q}, null, null, null, null);
        while (cursor.moveToNext()) {
            Definition def = getDefFromCursor(cursor);
            re.add(def);
        }
        return re;
    }

    private Definition getDefFromCursor(Cursor cursor) {
        HashMap<String, String> eleMap = new HashMap<>();
        String hwd = cursor.getString(0);
        // df.setDisplayedHeadWord(cursor.getString(1).trim());
        String phrase = cursor.getString(1).trim();
        String sense = cursor.getString(2).trim();
        String phonetics = cursor.getString(3).trim();
        String defEn = cursor.getString(4).trim();
        String defCn = cursor.getString(5).trim();


        eleMap.put(EXP_ELE_LIST[0], hwd);
        eleMap.put(EXP_ELE_LIST[1], "<span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#0d47a1; border-radius:3px;'>" + sense + "</span>");
        eleMap.put(EXP_ELE_LIST[2], "<span >"+phonetics + "</span>");
        if (phrase.equals("")) {
          eleMap.put(EXP_ELE_LIST[3],  "<span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#0d47a1; border-radius:3px;'>" + sense + "</span>  <span style=margin-right:3px; padding:0;margin:0; padding:0;>" +  defEn+"</span>");

          eleMap.put(EXP_ELE_LIST[4],  "<span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#0d47a1; border-radius:3px;'>" +  sense + "</span>  <span style=margin-right:3px; padding:0;margin:0; padding:0;>" +  defCn+"</span>");
        } else {
          eleMap.put(EXP_ELE_LIST[3],  "<span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#0d47a1; border-radius:3px;'>"+phrase+" </span> <span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#0d47a1; border-radius:3px;'>" + sense + "</span>  <span style=margin-right:3px; padding:0;margin:0; padding:0;>" +  defEn+"</span>");

          eleMap.put(EXP_ELE_LIST[4],  "<span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#0d47a1; border-radius:3px;'>"+phrase+" </span> <span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#0d47a1; border-radius:3px;'>" +  sense + "</span>  <span style=margin-right:3px; padding:0;margin:0; padding:0;>" +  defCn+"</span>");
        }



        eleMap.put(EXP_ELE_LIST[5], getYoudaoAudioTag(hwd, 2));
        eleMap.put(EXP_ELE_LIST[6], getYoudaoAudioTag(hwd, 1));
        String displayHtml;
        StringBuilder sb = new StringBuilder();

        if (phrase.equals("")) {
              sb.append("<span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#0d47a1; border-radius:3px;'>"+sense+" </span> <span> "+phonetics+"</span> <span style=margin-right:3px; padding:0;margin:0; padding:0;>" + defEn + "</span> <span sytle=margin-right:3px; padding:0;margin:0; padding:0;>" + defCn + "</span>");
        } else {
              sb.append("<span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#0d47a1; border-radius:3px;'>"+phrase+" </span> </span> <span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#0d47a1; border-radius:3px;'>"+sense+" </span> <span> "+phonetics+"</span> <span style=margin-right:3px; padding:0;margin:0; padding:0;>" + defEn + "</span> <span sytle=margin-right:3px; padding:0;margin:0; padding:0;>" + defCn + "</span>");
        }


        displayHtml = sb.toString();
        return new Definition(eleMap, displayHtml);
    }

    private String[] getForms(String q) {
        //SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("forms", new String[]{"bases"}, "hwd=? ", new String[]{q.toLowerCase()}, null, null, null);
        String bases = "";
        while (cursor.moveToNext()) {
            bases = cursor.getString(0);
        }
        return bases.split("@@@");
    }


    private Cursor getFilterCursor(String q) {
        Log.d("databse", "getFilterCursor" + q);
        Cursor cursor = db.query("hwds", new String[]{"rowid _id", "hwd"}, "hwd LIKE ?", new String[]{q + "%"}, null, null, null);
        return cursor;
    }

    /**
     * 去除左右两边空格，标点
     *
     * @param key
     * @return
     */
    private String keyCleanup(String key) {
        return key.trim().replaceAll("[,.!?()\"'“”’？]", "").toLowerCase();
    }

    private Definition toDefinition(YoudaoResult youdaoResult){
        String notiString = "<font color='gray'>本地词典未查到，以下是有道在线释义</font><br/>";
        String definition = "<b>" + youdaoResult.returnPhrase + "</b><br/>";
        for(String def : youdaoResult.translation){
            definition += def + "<br/>";
        }

        definition += "<font color='gray'>网络释义</font><br/>";
        for(String key : youdaoResult.webTranslation.keySet()){
            String joined = "";
            for(String value : youdaoResult.webTranslation.get(key)){
                joined += value + "; ";
            }
            definition += "<b>" + key + "</b>: " + joined + "<br/>";
        }

        Map<String, String> exp = new HashMap<>();
        exp.put(EXP_ELE_LIST[0], youdaoResult.returnPhrase);
        exp.put(EXP_ELE_LIST[1], youdaoResult.phonetic);
        exp.put(EXP_ELE_LIST[2], definition);
        exp.put(EXP_ELE_LIST[3], getYoudaoAudioTag(youdaoResult.returnPhrase, 2));
        exp.put(EXP_ELE_LIST[4], getYoudaoAudioTag(youdaoResult.returnPhrase, 1));
        return new Definition(exp, notiString + definition);
    }

    String getYoudaoAudioTag(String word, int voiceType){
        return "[sound:https://dict.youdao.com/dictvoice?audio=" + word + "&type=" + voiceType +"]";
    }
    String decodeHtmlContent(String encodedText) {

        String decodedText = null;
        try {
            decodedText = URLDecoder.decode(encodedText, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return decodedText;

    }
}
