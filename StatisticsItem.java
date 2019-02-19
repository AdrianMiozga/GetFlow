package com.wentura.pomodoro;

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

    @Override
    public int compareTo(StatisticsItem statisticsItem) {
        return getDate().compareTo(statisticsItem.getDate());
    }
}
