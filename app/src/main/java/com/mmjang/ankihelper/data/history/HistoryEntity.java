package com.mmjang.ankihelper.data.history;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Room entity for History
 * Replaces HistoryPOJO and History (LitePal) models
 *
 * Table: history
 * Primary key: timestamp (unique timestamp in milliseconds)
 */
@Entity(tableName = "history")
public class HistoryEntity {

    @PrimaryKey
    @ColumnInfo(name = "timestamp")
    private long timeStamp;

    @ColumnInfo(name = "type")
    private int type;

    @ColumnInfo(name = "word")
    private String word;

    @ColumnInfo(name = "sentence")
    private String sentence;

    @ColumnInfo(name = "dictionary")
    private String dictionary;

    @ColumnInfo(name = "definition")
    private String definition;

    @ColumnInfo(name = "translation")
    private String translation;

    @ColumnInfo(name = "note")
    private String note;

    @ColumnInfo(name = "tag")
    private String tag;

    // Default constructor with empty strings (matching HistoryPOJO behavior)
    public HistoryEntity() {
        this.word = "";
        this.sentence = "";
        this.dictionary = "";
        this.definition = "";
        this.translation = "";
        this.note = "";
        this.tag = "";
    }

    // Constructor for creating from existing data
    @Ignore
    public HistoryEntity(long timeStamp, int type, String word, String sentence,
                        String dictionary, String definition, String translation,
                        String note, String tag) {
        this.timeStamp = timeStamp;
        this.type = type;
        this.word = word != null ? word : "";
        this.sentence = sentence != null ? sentence : "";
        this.dictionary = dictionary != null ? dictionary : "";
        this.definition = definition != null ? definition : "";
        this.translation = translation != null ? translation : "";
        this.note = note != null ? note : "";
        this.tag = tag != null ? tag : "";
    }

    // Getters and Setters
    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public String getDictionary() {
        return dictionary;
    }

    public void setDictionary(String dictionary) {
        this.dictionary = dictionary;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
