package com.mmjang.ankihelper.data.dict;

import android.content.Context;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ArrayAdapter;

import com.mmjang.ankihelper.MyApplication;
import com.mmjang.ankihelper.R;
import com.mmjang.ankihelper.data.ai.AIDictionaryConfig;
import com.mmjang.ankihelper.data.ai.LLMConfig;
import com.mmjang.ankihelper.data.ai.AIConfigRepository;
import com.mmjang.ankihelper.data.ai.service.AIDictionaryService;
import com.mmjang.ankihelper.data.ai.AIException;
import com.mmjang.ankihelper.data.ai.cache.AIDictionaryCache;
import com.mmjang.ankihelper.data.dict.Definition;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AIDictionary implements IDictionary {
    private AIDictionaryConfig config;
    private AIDictionaryService service;

    // Resource IDs for internationalization
    private static final int[] DICT_FIELD_RES_IDS = {
            R.string.dict_field_word,
            R.string.dict_field_pos,
            R.string.dict_field_phonetic,
            R.string.dict_field_en_definition,
            R.string.dict_field_zh_definition,
            R.string.dict_field_us_pronunciation,
            R.string.dict_field_uk_pronunciation
    };

    public AIDictionary(AIDictionaryConfig config) {
        this.config = config;
        this.service = new AIDictionaryService();
    }

    @Override
    public String getDictionaryName() {
        return config.getDictionaryName();
    }

    public String getDictionaryKey() {
        return "AI_" + config.getId();
    }

    @Override
    public String getIntroduction() {
        LLMConfig llmConfig = AIConfigRepository.getLLMConfigById(config.getLlmId());
        if (llmConfig != null) {
            return "AI Dictionary using " + llmConfig.getName() + " model";
        }
        return "AI Dictionary";
    }

    /**
     * Get export elements list with localized field names
     * @return Array of localized field names
     */
    @Override
    public String[] getExportElementsList() {
        Context context = MyApplication.getContext();
        String[] fields = new String[DICT_FIELD_RES_IDS.length];
        for (int i = 0; i < DICT_FIELD_RES_IDS.length; i++) {
            fields[i] = context.getString(DICT_FIELD_RES_IDS[i]);
        }
        return fields;
    }

    @Override
    public List<Definition> wordLookup(String key) {
        List<Definition> definitions = new ArrayList<>();

        try {
            Log.d("AIDictionary", "Starting word lookup for: " + key);

            // Get the LLM config
            LLMConfig llmConfig = AIConfigRepository.getLLMConfigById(config.getLlmId());
            if (llmConfig == null) {
                Log.w("AIDictionary", "LLM config not found for config ID: " + config.getLlmId());
                return definitions;
            }

            Log.d("AIDictionary", "LLM config found: " + llmConfig.getName());

            // Get the word definition from the AI service
            List<AIDictionaryCache> cacheResults = service.getWordDefinition(key, config, llmConfig);

            Log.d("AIDictionary", "Received " + cacheResults.size() + " cache results");

            // Get localized field names
            String[] fieldNames = getExportElementsList();

            // Convert AIDictionaryCache results to Definition objects
            for (AIDictionaryCache cache : cacheResults) {
                // Create export elements map
                Map<String, String> exportElements = new HashMap<>();
                String determinedWord = cache.getHwd() != null ? cache.getHwd() : key;
                String sense = cache.getSense() != null ? cache.getSense() : "";
                String phrase = cache.getPhrase() != null ? cache.getPhrase() : "";
                String phonetics = cache.getPhonetics() != null ? cache.getPhonetics() : "";
                String defEn = cache.getDefEn() != null ? cache.getDefEn() : "";
                String defCn = cache.getDefCn() != null ? cache.getDefCn() : "";

                exportElements.put(fieldNames[0], determinedWord);
                exportElements.put(fieldNames[1], "<span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#42A5F5; border-radius:3px;'>" + sense + "</span>");
                exportElements.put(fieldNames[2], "<span >"+phonetics + "</span>");
                if (phrase.equals("")) {
                  exportElements.put(fieldNames[3],
                      "<span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#42A5F5; border-radius:3px;'>"
                          + sense + "</span>  <span style=margin-right:3px; padding:0;margin:0; padding:0;>" + defEn + "</span>");

                  exportElements.put(fieldNames[4],
                      "<span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#42A5F5; border-radius:3px;'>"
                          + sense + "</span>  <span style=margin-right:3px; padding:0;margin:0; padding:0;>" + defCn + "</span>");
                } else {
                  exportElements.put(fieldNames[3],
                      "<span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#42A5F5; border-radius:3px;'>"
                          + phrase
                          + " </span> <span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#42A5F5; border-radius:3px;'>"
                          + sense + "</span>  <span style=margin-right:3px; padding:0;margin:0; padding:0;>" + defEn + "</span>");

                  exportElements.put(fieldNames[4],
                      "<span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#42A5F5; border-radius:3px;'>"
                          + phrase
                          + " </span> <span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#42A5F5; border-radius:3px;'>"
                          + sense + "</span>  <span style=margin-right:3px; padding:0;margin:0; padding:0;>" + defCn + "</span>");
                }
                exportElements.put(fieldNames[5], getYoudaoAudioTag(determinedWord, 2));
                exportElements.put(fieldNames[6], getYoudaoAudioTag(determinedWord, 1));

                // Create display HTML
                StringBuilder displayHtml = new StringBuilder();
                if (phrase.equals("")) {
                  displayHtml.append(
                      "<span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#42A5F5; border-radius:3px;'>"
                          + sense + " </span> &nbsp;<span> " + phonetics
                          + "</span> <span style=margin-right:3px; padding:0;margin:0; padding:0;>" + defEn
                          + "</span> <span sytle=margin-right:3px; padding:0;margin:0; padding:0;>" + defCn + "</span>");
                } else {
                  displayHtml.append(
                      "<span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#42A5F5; border-radius:3px;'>"
                          + phrase
                          + " </span>&nbsp; </span> <span style='text-transform:lowercase; font-size:0.9em; margin-right:5px; padding:2px 4px; color:white; background-color:#42A5F5; border-radius:3px;'>"
                          + sense + " </span> &nbsp; <span> " + phonetics
                          + "</span> <span style=margin-right:3px; padding:0;margin:0; padding:0;>" + defEn
                          + "</span> <span sytle=margin-right:3px; padding:0;margin:0; padding:0;>" + defCn + "</span>");
                }
                // Create Definition object
                Definition def = new Definition(exportElements, displayHtml.toString());
                definitions.add(def);
            }

            Log.d("AIDictionary", "Returning " + definitions.size() + " definitions");
        } catch (AIException e) {
            Log.e("AIDictionary", "AI Error during dictionary lookup for word: " + key, e);
            // Create a Definition object to show the error to the user
            Map<String, String> exportElements = new HashMap<>();
            exportElements.put("Error", "AI Dictionary Error: " + e.getMessage());
            Definition errorDef = new Definition(exportElements,
                "<b>AI Dictionary Error</b><br/>" + e.getMessage() +
                "<br/><br/>Please check your LLM configuration and ensure the API is accessible.");
            definitions.add(errorDef);
        } catch (Exception e) {
            Log.e("AIDictionary", "Error during dictionary lookup for word: " + key, e);
            e.printStackTrace();
            // Create a Definition object to show the error to the user
            Map<String, String> exportElements = new HashMap<>();
            exportElements.put("Error", "Error: " + e.getMessage());
            Definition errorDef = new Definition(exportElements,
                "<b>Error</b><br/>" + e.getMessage() +
                "<br/><br/>Please check the logs for more details.");
            definitions.add(errorDef);
        }

        return definitions;
    }

    private String getYoudaoAudioTag(String word, int type) {
        return String.format("[sound:https://dict.youdao.com/dictvoice?audio=%s&type=%d]", word, type);
    }

    @Override
    public ListAdapter getAutoCompleteAdapter(Context context, int layout) {
        // Return a simple adapter with no autocomplete for AI dictionaries
        return new ArrayAdapter<>(context, layout, new String[]{});
    }

    public long getConfigId() {
        return config.getId();
    }
}
