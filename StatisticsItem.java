package com.wentura.pomodoroapp;

public class StatisticsItem {
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
}
