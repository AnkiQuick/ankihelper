package com.mmjang.ankihelper.data.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Room entity for UserTag
 * Replaces UserTag (LitePal) model
 *
 * Table: usertag
 * Primary key: tag (unique tag identifier)
 */
@Entity(tableName = "usertag")
public class UserTagEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "tag")
    private String tag = "";

    // Default constructor required by Room
    public UserTagEntity() {
    }

    // Constructor for creating with tag
    @Ignore
    public UserTagEntity(@NonNull String tag) {
        this.tag = tag;
    }

    // Getters and Setters
    @NonNull
    public String getTag() {
        return tag;
    }

    public void setTag(@NonNull String tag) {
        this.tag = tag;
    }
}
