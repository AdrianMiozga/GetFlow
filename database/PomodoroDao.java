package com.wentura.pomodoro.database;

import com.wentura.pomodoro.StatisticsItem;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface PomodoroDao {
    @Insert
    void insertPomodoro(Pomodoro pomodoro);

    @Query("UPDATE Pomodoro SET CompletedWorks = :completedWorks WHERE Date = :date")
    void updateCompletedWorks(int completedWorks, String date);

    @Query("UPDATE Pomodoro SET CompletedBreaks = :completedBreaks WHERE Date = :date")
    void updateCompletedBreaks(int completedBreaks, String date);

    @Query("SELECT Date FROM Pomodoro ORDER BY Date DESC LIMIT 1")
    String getLatestDate();

    @Query("SELECT CompletedWorks FROM Pomodoro WHERE Date = :date")
    int getCompletedWorks(String date);

    @Query("SELECT CompletedBreaks FROM Pomodoro WHERE Date = :date")
    int getCompletedBreaks(String date);

    @Query("SELECT * FROM Pomodoro WHERE Date BETWEEN :startDate AND :endDate ORDER BY Date DESC")
    List<StatisticsItem> getAllDatesBetween(String startDate, String endDate);
}
