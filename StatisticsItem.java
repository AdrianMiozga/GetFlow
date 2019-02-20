package com.wentura.pomodoro;

import androidx.annotation.NonNull;

public class StatisticsItem implements Comparable<StatisticsItem> {
    private String Date;
    private int CompletedWorks;
    private int CompletedBreaks;
    private int CompletedWorksTime;
    private int CompletedBreaksTime;

    public StatisticsItem(String Date, int CompletedWorks, int CompletedBreaks,
                          int CompletedWorksTime, int CompletedBreaksTime) {
        this.Date = Date;
        this.CompletedWorks = CompletedWorks;
        this.CompletedBreaks = CompletedBreaks;
        this.CompletedBreaksTime = CompletedBreaksTime;
        this.CompletedWorksTime = CompletedWorksTime;
    }

    int getCompletedWorks() {
        return CompletedWorks;
    }

    int getCompletedBreaks() {
        return CompletedBreaks;
    }

    String getDate() {
        return Date;
    }

    int getCompletedWorksTime() {
        return CompletedWorksTime;
    }

    int getCompletedBreaksTime() {
        return CompletedBreaksTime;
    }

    public void setDate(String date) {
        Date = date;
    }

    @Override
    public int compareTo(@NonNull StatisticsItem statisticsItem) {
        return getDate().compareTo(statisticsItem.getDate());
    }
}
