package com.mmjang.ankihelper.data.history;

import com.mmjang.ankihelper.data.database.DatabaseManager;

import org.litepal.crud.LitePalSupport;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import java.util.List;

public class HistoryStat {
    private static long MILLIS_OF_DAY = 3600 * 24 * 1000;
    private int lastDays;
    private long startOfToday;
    private long startOfThisMonth;
    private long startOfLastDays;
    private List<HistoryPOJO> dataOfLastDays;

    public HistoryStat(int days){
        lastDays = days;
        startOfToday = LocalDate.now().atStartOfDay(ZoneOffset.systemDefault()).toInstant().toEpochMilli();
        startOfThisMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay()
                .atZone(ZoneOffset.systemDefault()).toInstant().toEpochMilli();
        startOfLastDays = LocalDate.now().minusDays(days - 1).atStartOfDay()
                .atZone(ZoneOffset.systemDefault()).toInstant().toEpochMilli();
//        dataOfLastDays = DataSupport.where("timestamp > ?", Long.toString(startOfLastDays))
//                .find(History.class);
        dataOfLastDays = DatabaseManager.getInstance().getHistoryAfter(startOfLastDays);
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
        HistoryStat historyStat = new HistoryStat(30);
    }
}
