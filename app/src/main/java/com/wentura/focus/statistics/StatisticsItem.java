package com.wentura.focus.statistics;

import androidx.annotation.NonNull;
import androidx.room.Ignore;

public class StatisticsItem implements Comparable<StatisticsItem> {
    private String Date;
    private int CompletedWorks;
    private int CompletedWorkTime;
    private int IncompleteWorks;
    private int IncompleteWorkTime;
    private int Breaks;
    private int BreakTime;

    public StatisticsItem(String Date, int CompletedWorks, int CompletedWorkTime,
                          int IncompleteWorks, int IncompleteWorkTime, int Breaks, int BreakTime) {
        this.Date = Date;
        this.CompletedWorks = CompletedWorks;
        this.CompletedWorkTime = CompletedWorkTime;
        this.IncompleteWorks = IncompleteWorks;
        this.IncompleteWorkTime = IncompleteWorkTime;
        this.Breaks = Breaks;
        this.BreakTime = BreakTime;
    }

    @Ignore
    public StatisticsItem(String Date) {
        this(Date, 0, 0, 0, 0, 0, 0);
    }

    String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    int getCompletedWorks() {
        return CompletedWorks;
    }

    int getCompletedWorkTime() {
        return CompletedWorkTime;
    }

    int getIncompleteWorks() {
        return IncompleteWorks;
    }

    int getIncompleteWorkTime() {
        return IncompleteWorkTime;
    }

    int getBreaks() {
        return Breaks;
    }

    int getBreakTime() {
        return BreakTime;
    }

    @Override
    public int compareTo(@NonNull StatisticsItem statisticsItem) {
        return getDate().compareTo(statisticsItem.getDate());
    }
}
