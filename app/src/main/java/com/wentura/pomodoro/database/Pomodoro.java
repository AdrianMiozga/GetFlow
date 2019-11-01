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
    private String Date;

    @ColumnInfo(name = "CompletedWorks")
    private int CompletedWorks;

    @ColumnInfo(name = "Breaks")
    private int Breaks;

    @ColumnInfo(name = "CompletedWorkTime")
    private int CompletedWorkTime;

    @ColumnInfo(name = "BreakTime")
    private int BreakTime;

    public Pomodoro(@NonNull String Date, int CompletedWorks, int Breaks,
                    int CompletedWorkTime, int BreakTime) {
        this.Date = Date;
        this.CompletedWorks = CompletedWorks;
        this.Breaks = Breaks;
        this.CompletedWorkTime = CompletedWorkTime;
        this.BreakTime = BreakTime;
    }

    @NonNull
    String getDate() {
        return Date;
    }

    int getCompletedWorks() {
        return CompletedWorks;
    }

    int getBreaks() {
        return Breaks;
    }

    int getCompletedWorkTime() {
        return CompletedWorkTime;
    }

    int getBreakTime() {
        return BreakTime;
    }
}
