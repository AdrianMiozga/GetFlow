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

    @ColumnInfo(name = "Breaks")
    private int Breaks;

    @ColumnInfo(name = "CompletedWorksTime")
    private int completedWorksTime;

    @ColumnInfo(name = "CompletedBreaksTime")
    private int completedBreaksTime;

    public Pomodoro(@NonNull String date, int completedWorks, int Breaks,
                    int completedWorksTime, int completedBreaksTime) {
        this.date = date;
        this.completedWorks = completedWorks;
        this.Breaks = Breaks;
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

    int getBreaks() {
        return Breaks;
    }

    int getCompletedWorksTime() {
        return completedWorksTime;
    }

    int getCompletedBreaksTime() {
        return completedBreaksTime;
    }
}
