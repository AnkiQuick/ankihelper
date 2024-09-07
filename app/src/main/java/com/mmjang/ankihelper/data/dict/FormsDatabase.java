     package com.mmjang.ankihelper.data.dict;

     import android.content.Context;

     import androidx.room.Database;
     import androidx.room.Room;
     import androidx.room.RoomDatabase;

     @Database(entities = {Form.class}, version = 1)
     public abstract class FormsDatabase extends RoomDatabase {

         public abstract FormsDao formsDao();

         private static FormsDatabase instance;

         public static synchronized FormsDatabase getInstance(Context context) {
             if (instance == null) {
                 instance = Room.databaseBuilder(context.getApplicationContext(),
                         FormsDatabase.class, "forms.db")
                         .allowMainThreadQueries() // For demonstration, don't use this in production
                         .build();
             }
             return instance;
         }
     }