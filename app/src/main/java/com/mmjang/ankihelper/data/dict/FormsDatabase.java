     package com.mmjang.ankihelper.data.dict;

     import android.content.Context;

     import androidx.room.Database;
     import androidx.room.Room;
     import androidx.room.RoomDatabase;

     @Database(entities = {DummyEntity.class}, version = 1, exportSchema = false)
     public abstract class FormsDatabase extends RoomDatabase {

         public abstract FormsDao formsDao();

         private static FormsDatabase instance;

         public static synchronized FormsDatabase getInstance(Context context) {
             if (instance == null) {
                 instance = Room.databaseBuilder(context.getApplicationContext(),
                         FormsDatabase.class, "forms.db")
                         // Removed .allowMainThreadQueries() - use coroutines for async operations
                         .build();
             }
             return instance;
         }
     }