package com.lmyby.ankihelper.data.dict;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.FilterQueryProvider;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.lmyby.ankihelper.MyApplication;
import com.lmyby.ankihelper.R;
import com.lmyby.ankihelper.data.ai.AIDictionaryConfig;
import com.lmyby.ankihelper.data.ai.AIConfigRepository;
import com.lmyby.ankihelper.data.dict.AIDictionary;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liao on 2017/3/15.
 */

public class Cdepe4 implements IDictionary {
  // private static final String DATABASE_NAME = ".db";
  private static final String DATABASE_NAME = "cdepe4.db";
  private static final int DATABASE_VERSION = 1;
  private static final String TABLE_DICT = "dict";
  private static final String FIELD_HWD = "hwd";
  private static final String FIELD_PHRASE = "phrase";
  private static final String FIELD_SENSE = "sense";
  private static final String FIELD_PHONETICS = "phonetics";
  private static final String FIELD_DEF_EN = "def_en";
  private static final String FIELD_DEF_CN = "def_cn";

  // Resource IDs for internationalization
  private static final int DICT_NAME_RES_ID = R.string.dict_name_cambridge;
  private static final int DICT_DESC_RES_ID = R.string.dict_desc_cambridge;
  private static final int[] DICT_FIELD_RES_IDS = {
      R.string.dict_field_word,
      R.string.dict_field_pos,
      R.string.dict_field_phonetic,
      R.string.dict_field_en_definition,
      R.string.dict_field_zh_definition,
      R.string.dict_field_us_pronunciation,
      R.string.dict_field_uk_pronunciation
  };

  private Cdepe4Database roomDb;
  private Cdepe4Dao dao;

  private Context mContext;

  public Cdepe4(Context context) {
    mContext = context;
    // Initialize the Room database
    roomDb = Cdepe4Database.getInstance(context);
    dao = roomDb.cdepe4Dao();
  }

  @Override
  public String getDictionaryKey() {
    return "cdepe4";
  }

  /**
   * Get export elements list with localized field names
   * @return Array of localized field names
   */
  public String[] getExportElementsList() {
    Context context = mContext != null ? mContext : MyApplication.getContext();
    String[] fields = new String[DICT_FIELD_RES_IDS.length];
    for (int i = 0; i < DICT_FIELD_RES_IDS.length; i++) {
      fields[i] = context.getString(DICT_FIELD_RES_IDS[i]);
    }
    return fields;
  }

  public String getDictionaryName() {
    Context context = mContext != null ? mContext : MyApplication.getContext();
    return context.getString(DICT_NAME_RES_ID);
  }

  public String getIntroduction() {
    Context context = mContext != null ? mContext : MyApplication.getContext();
    return context.getString(DICT_DESC_RES_ID);
  }

  public List<Definition> wordLookup(String key) {
    // db = getReadableDatabase(); // according to stackoverflow, it's alright to
    // let the database open
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

    if (re.isEmpty()) {
      // Try to use AI dictionary as fallback
      try {
        List<AIDictionaryConfig> aiConfigs = AIConfigRepository.getAllAIDictionaryConfigs();
        if (!aiConfigs.isEmpty()) {
          AIDictionary aiDict = new AIDictionary(aiConfigs.get(0)); // Use the first AI dictionary config
          re.addAll(aiDict.wordLookup(key));
        }
      } catch (Exception e) {
        // If AI lookup fails, continue with empty result
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
    SimpleCursorAdapter adapter = new SimpleCursorAdapter(context, layout,
        null,
        new String[] { FIELD_HWD },
        new int[] { android.R.id.text1 },
        0);
    adapter.setFilterQueryProvider(
        new FilterQueryProvider() {
          @Override
          public Cursor runQuery(CharSequence constraint) {
            return getFilterCursor(constraint.toString());
          }
        });
    adapter.setCursorToStringConverter(
        new SimpleCursorAdapter.CursorToStringConverter() {
          @Override
          public CharSequence convertToString(Cursor cursor) {
            return cursor.getString(1);
          }
        });

    return adapter;
  }

  /**
   * @param q word to lookup
   * @return a array of definitions, retrun ArrayList<>() if none was found
   */
  private ArrayList<Definition> queryDefinition(String q) {
    ArrayList<Definition> re = new ArrayList<>();
    if (q.isEmpty()) {
      return re;
    }
    // Use Room DAO (blocking call)
    Cursor cursor = dao.queryDefinition(q);
    while (cursor.moveToNext()) {
      Definition def = getDefFromCursor(cursor);
      re.add(def);
    }
    cursor.close();
    return re;
  }

  private Definition getDefFromCursor(Cursor cursor) {
    HashMap<String, String> eleMap = new HashMap<>();
    String[] fieldNames = getExportElementsList(); // Get localized field names

    String hwd = cursor.getString(0);
    // df.setDisplayedHeadWord(cursor.getString(1).trim());
    String phrase = cursor.getString(1).trim();
    String sense = cursor.getString(2).trim();
    String phonetics = cursor.getString(3).trim();
    String defEn = cursor.getString(4).trim();
    String defCn = cursor.getString(5).trim();

    eleMap.put(fieldNames[0], hwd);
    eleMap.put(fieldNames[1],
        "<span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#42A5F5; border-radius:3px;'>"
            + sense + "</span>");
    eleMap.put(fieldNames[2], "<span >" + phonetics + "</span>");
    if (phrase.equals("")) {
      eleMap.put(fieldNames[3],
          "<span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#42A5F5; border-radius:3px;'>"
              + sense + "</span>  <span style=margin-right:3px; padding:0;margin:0; padding:0;>" + defEn + "</span>");

      eleMap.put(fieldNames[4],
          "<span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#42A5F5; border-radius:3px;'>"
              + sense + "</span>  <span style=margin-right:3px; padding:0;margin:0; padding:0;>" + defCn + "</span>");
    } else {
      eleMap.put(fieldNames[3],
          "<span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#42A5F5; border-radius:3px;'>"
              + phrase
              + " </span> <span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#42A5F5; border-radius:3px;'>"
              + sense + "</span>  <span style=margin-right:3px; padding:0;margin:0; padding:0;>" + defEn + "</span>");

      eleMap.put(fieldNames[4],
          "<span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#42A5F5; border-radius:3px;'>"
              + phrase
              + " </span> <span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#42A5F5; border-radius:3px;'>"
              + sense + "</span>  <span style=margin-right:3px; padding:0;margin:0; padding:0;>" + defCn + "</span>");
    }

    eleMap.put(fieldNames[5], getYoudaoAudioTag(hwd, 2));
    eleMap.put(fieldNames[6], getYoudaoAudioTag(hwd, 1));
    String displayHtml;
    StringBuilder sb = new StringBuilder();

    if (phrase.equals("")) {
      sb.append(
          "<span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#42A5F5; border-radius:3px;'>"
              + sense + " </span> &nbsp;<span> " + phonetics
              + "</span> <span style=margin-right:3px; padding:0;margin:0; padding:0;>" + defEn
              + "</span> <span sytle=margin-right:3px; padding:0;margin:0; padding:0;>" + defCn + "</span>");
    } else {
      sb.append(
          "<span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#42A5F5; border-radius:3px;'>"
              + phrase
              + " </span>&nbsp; </span> <span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#42A5F5; border-radius:3px;'>"
              + sense + " </span>&nbsp; <span> " + phonetics
              + "</span> <span style=margin-right:3px; padding:0;margin:0; padding:0;>" + defEn
              + "</span> <span sytle=margin-right:3px; padding:0;margin:0; padding:0;>" + defCn + "</span>");
    }

    displayHtml = sb.toString();
    return new Definition(eleMap, displayHtml);
  }

  private String[] getForms(String q) {
    // Use Room DAO (blocking call)
    Cursor cursor = dao.getForms(q.toLowerCase());
    String bases = "";
    while (cursor.moveToNext()) {
      bases = cursor.getString(0);
    }
    cursor.close();
    return bases.split("@@@");
  }

  private Cursor getFilterCursor(String q) {
    Log.d("databse", "getFilterCursor" + q);
    // Use Room DAO (blocking call)
    return dao.getFilterCursor(q + "%");
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

  private Definition toDefinition(String word, String phonetic, String definitionHtml) {
    String[] fieldNames = getExportElementsList(); // Get localized field names
    Map<String, String> exp = new HashMap<>();
    exp.put(fieldNames[0], word);
    exp.put(fieldNames[1], phonetic);
    exp.put(fieldNames[2], definitionHtml);
    exp.put(fieldNames[3], getYoudaoAudioTag(word, 2));
    exp.put(fieldNames[4], getYoudaoAudioTag(word, 1));
    return new Definition(exp, definitionHtml);
  }

  String getYoudaoAudioTag(String word, int voiceType) {
    return "[sound:https://dict.youdao.com/dictvoice?audio=" + word + "&type=" + voiceType + "]";
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