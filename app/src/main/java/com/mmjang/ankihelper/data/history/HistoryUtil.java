package com.mmjang.ankihelper.data.history;

import android.content.Context;

import com.mmjang.ankihelper.data.database.AppDatabase;

/**
 * Utility class for saving history entries using Room database
 */
public class HistoryUtil {

    private static HistoryRepositoryHelper repositoryHelper;

    /**
     * Initialize the utility with application context
     * Must be called before any save methods
     */
    public static void initialize(Context context) {
        if (repositoryHelper == null) {
            AppDatabase database = AppDatabase.Companion.getInstance(context.getApplicationContext());
            HistoryRepository repository = new HistoryRepository(database.historyDao());
            repositoryHelper = new HistoryRepositoryHelper(repository);
        }
    }

    /**
     * Convert HistoryPOJO to HistoryEntity
     */
    private static HistoryEntity convertToEntity(HistoryPOJO pojo) {
        HistoryEntity entity = new HistoryEntity();
        entity.setTimeStamp(pojo.getTimeStamp());
        entity.setType(pojo.getType());
        entity.setWord(pojo.getWord());
        entity.setSentence(pojo.getSentence());
        entity.setDictionary(pojo.getDictionary());
        entity.setDefinition(pojo.getDefinition());
        entity.setTranslation(pojo.getTranslation());
        entity.setNote(pojo.getNote());
        entity.setTag(pojo.getTag());
        return entity;
    }

    public static void savePopupOpen(String sentence){
        if (repositoryHelper == null) {
            android.util.Log.e("HistoryUtil", "HistoryUtil not initialized! Call initialize() first.");
            return;
        }
        HistoryPOJO history = new HistoryPOJO();
        history.setType(HistoryType.POPUP_OPEN);
        history.setTimeStamp(System.currentTimeMillis());
        history.setSentence(sentence);
        repositoryHelper.insertHistory(convertToEntity(history));
    }

    public static void saveWordlookup(String sentence, String word){
        if (repositoryHelper == null) {
            android.util.Log.e("HistoryUtil", "HistoryUtil not initialized! Call initialize() first.");
            return;
        }
        HistoryPOJO history = new HistoryPOJO();
        history.setType(HistoryType.WORD_LOOK_UP);
        history.setTimeStamp(System.currentTimeMillis());
        history.setSentence(sentence);
        history.setWord(word);
        repositoryHelper.insertHistory(convertToEntity(history));
    }

    public static void saveNoteAdd(String sentence, String word,
                                   String dictionary, String definition,
                                   String translation, String note, String tag){
        if (repositoryHelper == null) {
            android.util.Log.e("HistoryUtil", "HistoryUtil not initialized! Call initialize() first.");
            return;
        }
        HistoryPOJO history = new HistoryPOJO();
        history.setType(HistoryType.NOTE_ADD);
        history.setTimeStamp(System.currentTimeMillis());
        history.setSentence(sentence);
        history.setWord(word);
        history.setDictionary(dictionary);
        history.setDefinition(definition);
        history.setTranslation(translation);
        history.setNote(note);
        history.setTag(tag);
        repositoryHelper.insertHistory(convertToEntity(history));
    }
}
