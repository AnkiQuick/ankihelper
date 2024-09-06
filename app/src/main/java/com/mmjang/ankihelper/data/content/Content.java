package com.mmjang.ankihelper.data.content;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.mmjang.ankihelper.data.database.DatabaseManager;
import com.mmjang.ankihelper.util.Constant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Content {

    private static final String suffix = ".db";
    private List<File> dbFileList;
    private SQLiteDatabase.OpenParams.Builder mParametersBuilder;
    private Context mContext;
    private ContentDatabaseHelper[] helperList;

    public Content(Context context){
        mContext = context;
        File contentFolder = new File(Environment.getDataDirectory().getAbsolutePath() + File.separator
                + Constant.STORAGE_DIRECTORY + File.separator +
                Constant.STORAGE_CONTENT_SUBDIRECTORY);
        File[] listOfFiles = contentFolder.listFiles();
        dbFileList = new ArrayList<>();
        for(File f : listOfFiles){
            String fileName = f.getName();
            if(fileName.endsWith(suffix)){
                dbFileList.add(f);
            }
        }
        helperList = new ContentDatabaseHelper[dbFileList.size()];
    }

    public List<String> getContentDBList(){
        List<String> list = new ArrayList<>();
        for(File f : dbFileList){
            list.add(
                    f.getName().replace(suffix, "")
            );
        }
        return list;
    }

    //first one is total count, second one is read count
    public List<Long> getCountAt(int index){
        if(helperList[index] == null){
            String name = dbFileList.get(index).getName();
            helperList[index] = new ContentDatabaseHelper(mContext, name);
        }
        SQLiteDatabase database;
        try {
            database = helperList[index].getWritableDatabase();
        }
        catch (Exception e){
            return null;
        }
        if(database == null){
            return null;
        }

        List<Long> countList = new ArrayList<>();
        countList.add(DatabaseUtils.queryNumEntries(
                database,
                "content",
                null,
                null
        ));
        countList.add(
                DatabaseUtils.queryNumEntries(
                        database,
                        "content",
                        "is_read=1",
                        null
                )
        );
        return countList;
    }

    public ContentEntity getRandomContentAt(int index, boolean filterRead){
        if(helperList[index] == null){
            String name = dbFileList.get(index).getName();
            helperList[index] = new ContentDatabaseHelper(mContext, name);
        }
        SQLiteDatabase database;
        try {
            database = helperList[index].getWritableDatabase();
        }
        catch (Exception e){
            return null;
        }
        if(database == null){
            return null;
        }
        Cursor cursor;
        if(filterRead) {
            cursor = database.rawQuery(
                    "select id, txt, note, is_read from content " +
                            "where id in (select id from content where is_read=0 order by random() limit 1)"
                    , null
            );
        }else{
            cursor = database.rawQuery(
                    "select id, txt, note, is_read from content where " +
                            "id in (select id from content order by random() limit 1)"
                    , null);
        }
        cursor.getCount();
        if(cursor.getCount() == 0){
            return null;
        }else{
            cursor.moveToNext();
            int id = cursor.getInt(0);
            ContentEntity contentEntity = new ContentEntity(cursor.getString(1), cursor.getString(2));
            boolean isRead = cursor.getInt(3) == 1;
            if(!isRead){
                ContentValues contentValues = new ContentValues();
                contentValues.put("id", id);
                contentValues.put("txt", cursor.getString(1));
                contentValues.put("note", cursor.getString(2));
                contentValues.put("is_read", 1);
                database.update("content", contentValues, "id=" + id, null);
            }
            return contentEntity;
        }
    }


}
