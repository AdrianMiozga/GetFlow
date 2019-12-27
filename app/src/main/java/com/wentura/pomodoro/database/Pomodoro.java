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

    @ColumnInfo(name = "CompletedWorkTime")
    private int CompletedWorkTime;

    @ColumnInfo(name = "IncompleteWorks")
    private int IncompleteWorks;

    @ColumnInfo(name = "IncompleteWorkTime")
    private int IncompleteWorkTime;

    @ColumnInfo(name = "Breaks")
    private int Breaks;

    @ColumnInfo(name = "BreakTime")
    private int BreakTime;

    public Pomodoro(@NonNull String Date, int CompletedWorks, int CompletedWorkTime,
                    int IncompleteWorks, int IncompleteWorkTime, int Breaks, int BreakTime) {
        this.Date = Date;
        this.CompletedWorks = CompletedWorks;
        this.CompletedWorkTime = CompletedWorkTime;
        this.IncompleteWorks = IncompleteWorks;
        this.IncompleteWorkTime = IncompleteWorkTime;
        this.Breaks = Breaks;
        this.BreakTime = BreakTime;
    }

    @NonNull
    String getDate() {
        return Date;
    }

    int getCompletedWorks() {
        return CompletedWorks;
    }

    int getCompletedWorkTime() {
        return CompletedWorkTime;
    }

    public int getIncompleteWorks() {
        return IncompleteWorks;
    }

    public int getIncompleteWorkTime() {
        return IncompleteWorkTime;
    }

    int getBreaks() {
        return Breaks;
    }

    int getBreakTime() {
        return BreakTime;
    }
}
