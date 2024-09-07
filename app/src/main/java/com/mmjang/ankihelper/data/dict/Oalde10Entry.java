package com.mmjang.ankihelper.data.dict;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "dict")
public class Oalde10Entry {
    @PrimaryKey
    public String hwd;

    public String phrase;

    public String sense;

    public String phonetics;

    public String defEn;

    public String defCn;
}