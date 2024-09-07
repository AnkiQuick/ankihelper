package com.mmjang.ankihelper.data.dict;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "hwds")
public class WebsterLearnersEntry {
    @PrimaryKey
    public String hwd;
}