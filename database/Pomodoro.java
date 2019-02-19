package com.wentura.pomodoro.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Pomodoro {
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "Date")
    private String date;

    @ColumnInfo(name = "CompletedWorks")
    private int completedWorks;

    @ColumnInfo(name = "CompletedBreaks")
    private int completedBreaks;

    public Pomodoro(@NonNull String date, int completedWorks, int completedBreaks) {
        this.date = date;
        this.completedWorks = completedWorks;
        this.completedBreaks = completedBreaks;
    }

    @NonNull
    String getDate() {
        return date;
    }

    int getCompletedWorks() {
        return completedWorks;
    }

    int getCompletedBreaks() {
        return completedBreaks;
    }
}
