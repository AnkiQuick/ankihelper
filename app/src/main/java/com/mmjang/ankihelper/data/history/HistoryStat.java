package com.mmjang.ankihelper.data.history;

import android.content.Context;

import com.mmjang.ankihelper.data.database.AppDatabase;


import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import kotlinx.coroutines.BuildersKt;

public class HistoryStat {
    private static long MILLIS_OF_DAY = 3600 * 24 * 1000;
    private int lastDays;
    private long startOfToday;
    private long startOfThisMonth;
    private long startOfLastDays;
    private List<HistoryPOJO> dataOfLastDays;

    public HistoryStat(Context context, int days){
        lastDays = days;
        startOfToday = LocalDate.now().atStartOfDay(ZoneOffset.systemDefault()).toInstant().toEpochMilli();
        startOfThisMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay()
                .atZone(ZoneOffset.systemDefault()).toInstant().toEpochMilli();
        startOfLastDays = LocalDate.now().minusDays(days - 1).atStartOfDay()
                .atZone(ZoneOffset.systemDefault()).toInstant().toEpochMilli();

        // Load history using HistoryRepository with blocking pattern
        AppDatabase database = AppDatabase.Companion.getInstance(context.getApplicationContext());
        HistoryRepository repository = new HistoryRepository(database.historyDao());

        try {
            // Use runBlocking to execute suspend function synchronously
            List<HistoryEntity> entities = (List<HistoryEntity>) BuildersKt.runBlocking(
                    kotlin.coroutines.EmptyCoroutineContext.INSTANCE,
                    (scope, continuation) -> repository.getHistoryAfter(startOfLastDays, continuation)
            );

            // Convert entities to POJOs
            dataOfLastDays = convertEntitiesToPOJOs(entities);
        } catch (Exception e) {
            android.util.Log.e("HistoryStat", "Error loading history", e);
            dataOfLastDays = new ArrayList<>();
        }
    }

    /**
     * Convert HistoryEntity list to HistoryPOJO list
     */
    private List<HistoryPOJO> convertEntitiesToPOJOs(List<HistoryEntity> entities) {
        List<HistoryPOJO> pojos = new ArrayList<>();
        for (HistoryEntity entity : entities) {
            HistoryPOJO pojo = new HistoryPOJO();
            pojo.setTimeStamp(entity.getTimeStamp());
            pojo.setType(entity.getType());
            pojo.setWord(entity.getWord());
            pojo.setSentence(entity.getSentence());
            pojo.setDictionary(entity.getDictionary());
            pojo.setDefinition(entity.getDefinition());
            pojo.setTranslation(entity.getTranslation());
            pojo.setNote(entity.getNote());
            pojo.setTag(entity.getTag());
            pojos.add(pojo);
        }
        return pojos;
    }

//    public int getDayCount(int type){
//        return DataSupport.where("timestamp > ? and type = ?",
//                Long.toString(startOfToday), Integer.toString(type)).count(History.class);
//    }
//
//    public int getMonthCount(int type){
//        return DataSupport.where("timestamp > ? and type = ?",
//                Long.toString(startOfToday), Integer.toString(type)).count(History.class);
//    }

    public int[][] getHourStatistics(){
        int[][] result = new int[3][24];
        for(HistoryPOJO history : dataOfLastDays){
            long mills = history.getTimeStamp();
            int type = history.getType();
            int hour = LocalDateTime.ofInstant(Instant.ofEpochMilli(mills), ZoneId.systemDefault()).getHour();
            result[type][hour] += 1;
        }
        return result;
    }

    public int[][] getLastDaysStatistics(){
        int[][] result = new int[3][lastDays];
        for(HistoryPOJO history : dataOfLastDays){
            int pos =(int) ((history.getTimeStamp() - startOfLastDays) / MILLIS_OF_DAY);
            int type = history.getType();
            result[type][pos] += 1;
        }
        return result;
    }

    public static void main(String[] args){
        // HistoryStat historyStat = new HistoryStat(context, 30);
        // Note: This is a test method and requires a Context parameter
    }
}
