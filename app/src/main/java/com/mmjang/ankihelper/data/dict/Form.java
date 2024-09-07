     package com.mmjang.ankihelper.data.dict;

     import androidx.room.Entity;
     import androidx.room.PrimaryKey;

     @Entity(tableName = "forms")
     public class Form {
         @PrimaryKey
         public String hwd;

         public String bases;
     }