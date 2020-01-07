package com.wentura.pomodoro.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.wentura.pomodoro.statistics.StatisticsItem;

import java.util.List;

@Dao
public interface PomodoroDao {
    @Insert
    void insertPomodoro(Pomodoro pomodoro);

    @Query("SELECT Date FROM Pomodoro ORDER BY Date DESC LIMIT 1")
    String getLatestDate();

    @Query("SELECT * FROM Pomodoro WHERE Date BETWEEN :startDate AND :endDate ORDER BY Date DESC")
    List<StatisticsItem> getAllDatesBetween(String startDate, String endDate);

    @Query("UPDATE Pomodoro SET CompletedWorks = :completedWorks WHERE Date = :date")
    void updateCompletedWorks(int completedWorks, String date);

    @Query("UPDATE Pomodoro SET CompletedWorkTime = :completedWorkTime WHERE Date = :date")
    void updateCompletedWorkTime(int completedWorkTime, String date);

    @Query("UPDATE Pomodoro SET IncompleteWorks = :incompleteWorks WHERE Date = :date")
    void updateIncompleteWorks(int incompleteWorks, String date);

    @Query("UPDATE Pomodoro SET IncompleteWorkTime = :incompleteWorkTime WHERE Date = :date")
    void updateIncompleteWorkTime(int incompleteWorkTime, String date);

    @Query("UPDATE Pomodoro SET Breaks = :breaks WHERE Date = :date")
    void updateBreaks(int breaks, String date);

    @Query("UPDATE Pomodoro SET BreakTime = :BreakTime WHERE Date = :date")
    void updateBreakTime(int BreakTime, String date);

    @Query("SELECT CompletedWorks FROM Pomodoro WHERE Date = :date")
    int getCompletedWorks(String date);

    @Query("SELECT CompletedWorkTime FROM Pomodoro WHERE Date = :date")
    int getCompletedWorkTime(String date);

    @Query("SELECT IncompleteWorks FROM Pomodoro WHERE Date = :date")
    int getIncompleteWorks(String date);

    @Query("SELECT IncompleteWorkTime FROM Pomodoro WHERE Date = :date")
    int getIncompleteWorkTime(String date);

    @Query("SELECT Breaks FROM Pomodoro WHERE Date = :date")
    int getBreaks(String date);

    @Query("SELECT BreakTime FROM Pomodoro WHERE Date = :date")
    int getBreakTime(String date);
}
