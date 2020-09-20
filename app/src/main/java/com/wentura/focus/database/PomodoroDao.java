/*
 * Copyright (C) 2020 Adrian Miozga <AdrianMiozga@outlook.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.wentura.focus.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.wentura.focus.statistics.activitychart.PieChartItem;
import com.wentura.focus.statistics.historychart.HistoryChartItem;

import java.util.List;

@Dao
public interface PomodoroDao {
    @Insert
    void insertPomodoro(Pomodoro pomodoro);

    @Query("SELECT " +
            "SUM(Pomodoro.CompletedWorkTime) + SUM(Pomodoro.IncompleteWorkTime) AS TotalTime, " +
            "0 AS Percent, " +
            "Activity.Name AS ActivityName " +
            "FROM Pomodoro INNER JOIN Activity ON Pomodoro.ActivityId = Activity.ID GROUP BY ActivityId")
    List<PieChartItem> getAllPieChartItems();

    @Query("SELECT " +
            "SUM(Pomodoro.CompletedWorkTime) + SUM(Pomodoro.IncompleteWorkTime) AS TotalTime, " +
            "0 AS Percent, " +
            "Activity.Name AS ActivityName " +
            "FROM Pomodoro INNER JOIN Activity ON Pomodoro.ActivityId = Activity.ID " +
            "WHERE Pomodoro.Date BETWEEN :startDate AND :endDate " +
            "GROUP BY ActivityId")
    List<PieChartItem> getPieChartItems(String startDate, String endDate);

    @Query("SELECT " +
            "SUM(Pomodoro.CompletedWorkTime) + SUM(Pomodoro.IncompleteWorkTime) AS TotalTime, " +
            "0 AS Percent, " +
            "Activity.Name AS ActivityName " +
            "FROM Pomodoro INNER JOIN Activity ON Pomodoro.ActivityId = Activity.ID " +
            "WHERE Pomodoro.Date = :date " +
            "GROUP BY ActivityId")
    List<PieChartItem> getPieChartItems(String date);

    @Query("SELECT 1 FROM Pomodoro WHERE ActivityId = :activityId")
    boolean isDataWrittenWithActivity(int activityId);

    @Query("DELETE FROM Pomodoro WHERE ActivityId = :activityId")
    void deleteAllDataWithActivityId(int activityId);

    @Query("SELECT ID FROM Pomodoro WHERE Date = :date AND ActivityId = :activityId")
    int getId(String date, int activityId);

    @Query("SELECT Date AS date, " +
            "SUM(CompletedWorkTime) AS completedWorkTime, " +
            "SUM(IncompleteWorkTime) AS incompleteWorkTime, " +
            "ActivityId AS activityId " +
            "FROM Pomodoro WHERE Date BETWEEN :startDate AND :endDate AND ActivityId IN(:activityId) GROUP BY Date ORDER BY Date")
    List<HistoryChartItem> getAllDatesBetweenGroupByDate(String startDate, String endDate, int[] activityId);

    @Query("SELECT Date AS date, " +
            "SUM(CompletedWorkTime) AS completedWorkTime, " +
            "SUM(IncompleteWorkTime) AS incompleteWorkTime, " +
            "ActivityId AS activityId " +
            "FROM Pomodoro WHERE Date = :date AND ActivityId IN(:activityId) GROUP BY Date")
    HistoryChartItem getAllGroupByDate(String date, int[] activityId);

    @Query("SELECT Date AS date, " +
            "SUM(CompletedWorkTime) AS completedWorkTime, " +
            "SUM(IncompleteWorkTime) AS incompleteWorkTime, " +
            "ActivityId AS activityId " +
            "FROM Pomodoro WHERE ActivityId IN(:activityId) GROUP BY Date ORDER BY Date")
    List<HistoryChartItem> getAllGroupByDate(int[] activityId);

    @Query("SELECT Date AS date, " +
            "SUM(CompletedWorkTime) AS completedWorkTime, " +
            "SUM(IncompleteWorkTime) AS incompleteWorkTime, " +
            "ActivityId AS activityId " +
            "FROM Pomodoro WHERE Date < :date GROUP BY Date ORDER BY Date")
    List<HistoryChartItem> getAllDateLessGroupByDate(String date);

    @Query("UPDATE Pomodoro SET CompletedWorks = :completedWorks WHERE ID = :id")
    void updateCompletedWorks(int completedWorks, int id);

    @Query("UPDATE Pomodoro SET CompletedWorkTime = :completedWorkTime WHERE ID = :id")
    void updateCompletedWorkTime(int completedWorkTime, int id);

    @Query("UPDATE Pomodoro SET IncompleteWorks = :incompleteWorks WHERE ID = :id")
    void updateIncompleteWorks(int incompleteWorks, int id);

    @Query("UPDATE Pomodoro SET IncompleteWorkTime = :incompleteWorkTime WHERE ID = :id")
    void updateIncompleteWorkTime(int incompleteWorkTime, int id);

    @Query("UPDATE Pomodoro SET Breaks = :breaks WHERE ID = :id")
    void updateBreaks(int breaks, int id);

    @Query("UPDATE Pomodoro SET BreakTime = :BreakTime WHERE ID = :id")
    void updateBreakTime(int BreakTime, int id);

    @Query("SELECT CompletedWorks FROM Pomodoro WHERE ID = :id")
    int getCompletedWorks(int id);

    @Query("SELECT CompletedWorkTime FROM Pomodoro WHERE ID = :id")
    int getCompletedWorkTime(int id);

    @Query("SELECT IncompleteWorks FROM Pomodoro WHERE ID = :id")
    int getIncompleteWorks(int id);

    @Query("SELECT IncompleteWorkTime FROM Pomodoro WHERE ID = :id")
    int getIncompleteWorkTime(int id);

    @Query("SELECT Breaks FROM Pomodoro WHERE ID = :id")
    int getBreaks(int id);

    @Query("SELECT BreakTime FROM Pomodoro WHERE ID = :id")
    int getBreakTime(int id);
}
