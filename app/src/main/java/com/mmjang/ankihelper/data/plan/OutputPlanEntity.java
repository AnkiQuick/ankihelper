package com.mmjang.ankihelper.data.plan;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.mmjang.ankihelper.util.Utils;

import java.util.Map;

/**
 * Room entity for OutputPlan
 * Replaces OutputPlanPOJO and OutputPlan (LitePal) models
 *
 * Table: plan
 * Primary key: planname (unique plan identifier)
 */
@Entity(tableName = "plan")
public class OutputPlanEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "planname")
    private String planName = "";

    @ColumnInfo(name = "dictionarykey")
    private String dictionaryKey;

    @ColumnInfo(name = "outputdeckid")
    private long outputDeckId;

    @ColumnInfo(name = "outputmodelid")
    private long outputModelId;

    @ColumnInfo(name = "fieldsmap")
    private String fieldsMap;

    // Default constructor required by Room
    public OutputPlanEntity() {
    }

    // Constructor for creating from existing data
    @Ignore
    public OutputPlanEntity(@NonNull String planName, String dictionaryKey,
                           long outputDeckId, long outputModelId, String fieldsMap) {
        this.planName = planName;
        this.dictionaryKey = dictionaryKey;
        this.outputDeckId = outputDeckId;
        this.outputModelId = outputModelId;
        this.fieldsMap = fieldsMap;
    }

    // Getters and Setters
    @NonNull
    public String getPlanName() {
        return planName;
    }

    public void setPlanName(@NonNull String planName) {
        this.planName = planName;
    }

    public String getDictionaryKey() {
        return dictionaryKey;
    }

    public void setDictionaryKey(String dictionaryKey) {
        this.dictionaryKey = dictionaryKey;
    }

    public long getOutputDeckId() {
        return outputDeckId;
    }

    public void setOutputDeckId(long outputDeckId) {
        this.outputDeckId = outputDeckId;
    }

    public long getOutputModelId() {
        return outputModelId;
    }

    public void setOutputModelId(long outputModelId) {
        this.outputModelId = outputModelId;
    }

    // Room-required getter/setter for the fieldsMap field
    public String getFieldsMap() {
        return fieldsMap;
    }

    public void setFieldsMap(String fieldsMap) {
        this.fieldsMap = fieldsMap;
    }

    // Helper methods for String/Map conversion (keeps existing functionality from POJO)
    public String getFieldsMapString() {
        return fieldsMap;
    }

    public void setFieldsMapString(String fieldsMap) {
        this.fieldsMap = fieldsMap;
    }

    public Map<String, String> getFieldsMapAsMap() {
        return Utils.fieldsStr2Map(fieldsMap);
    }

    public void setFieldsMapFromMap(Map<String, String> fieldsMapObj) {
        this.fieldsMap = Utils.fieldsMap2Str(fieldsMapObj);
    }
}
