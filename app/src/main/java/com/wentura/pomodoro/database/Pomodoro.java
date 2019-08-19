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

    @ColumnInfo(name = "CompletedWorksTime")
    private int completedWorksTime;

    @ColumnInfo(name = "CompletedBreaksTime")
    private int completedBreaksTime;

    public Pomodoro(@NonNull String date, int completedWorks, int completedBreaks,
                    int completedWorksTime, int completedBreaksTime) {
        this.date = date;
        this.completedWorks = completedWorks;
        this.completedBreaks = completedBreaks;
        this.completedWorksTime = completedWorksTime;
        this.completedBreaksTime = completedBreaksTime;
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

    int getCompletedWorksTime() {
        return completedWorksTime;
    }

    int getCompletedBreaksTime() {
        return completedBreaksTime;
    }
}
