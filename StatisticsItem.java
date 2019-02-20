package com.wentura.pomodoro;

import androidx.annotation.NonNull;

public class StatisticsItem implements Comparable<StatisticsItem> {
    private String Date;
    private int CompletedWorks;
    private int CompletedBreaks;

    public StatisticsItem(String Date, int CompletedWorks, int CompletedBreaks) {
        this.Date = Date;
        this.CompletedWorks = CompletedWorks;
        this.CompletedBreaks = CompletedBreaks;
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

    public void setDate(String date) {
        Date = date;
    }

    @Override
    public int compareTo(@NonNull StatisticsItem statisticsItem) {
        return getDate().compareTo(statisticsItem.getDate());
    }
}
