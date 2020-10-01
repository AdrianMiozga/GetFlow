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

package org.wentura.getflow.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import org.wentura.getflow.statistics.activitychart.LabelElement;

import java.util.List;
import java.util.Set;

@Dao
public interface ActivityDao {

    @Insert
    void insertActivity(Activity activity);

    @Query("SELECT ID FROM Activity WHERE showInStatistics = 1")
    int[] getIdsToShow();

    @Query("UPDATE Activity SET showInStatistics = CASE WHEN ID IN(:activityIds) THEN 1 ELSE 0 END")
    void updateShowInStatistics(Set<Integer> activityIds);

    @Query("SELECT showInStatistics FROM Activity")
    boolean[] showInStatisticsAll();

    @Query("SELECT ID, Name FROM Activity")
    List<LabelElement> getAllActivityNames();

    @Query("SELECT * FROM Activity")
    List<Activity> getAll();

    @Query("SELECT 1 FROM Activity WHERE Name = :name")
    boolean isNameOccupied(String name);

    @Query("SELECT ID FROM Activity LIMIT 1")
    int getFirstActivityID();

    @Query("SELECT COUNT(*) FROM Activity")
    int getNumberOfActivities();

    @Query("DELETE FROM Activity WHERE id = :id")
    void deleteActivity(int id);

    @Query("UPDATE Activity SET Name = :name WHERE id = :id")
    void updateActivityName(int id, String name);

    @Query("SELECT * FROM Activity WHERE id = :id")
    Activity getActivity(int id);

    @Query("UPDATE Activity SET WorkDuration = :duration WHERE id = :id")
    void updateWorkDuration(int id, int duration);

    @Query("UPDATE Activity SET BreakDuration = :duration WHERE id = :id")
    void updateBreakDuration(int id, int duration);

    @Query("UPDATE Activity SET LongBreakDuration = :duration WHERE id = :id")
    void updateLongBreakDuration(int id, int duration);

    @Query("UPDATE Activity SET SessionsBeforeLongBreak = :sessions WHERE id = :id")
    void updateSessionsBeforeLongBreak(int id, int sessions);

    @Query("SELECT WorkDuration FROM Activity WHERE id = :id")
    int getWorkDuration(int id);

    @Query("SELECT BreakDuration FROM Activity WHERE id = :id")
    int getBreakDuration(int id);

    @Query("SELECT LongBreakDuration FROM Activity WHERE id = :id")
    int getLongBreakDuration(int id);

    @Query("SELECT SessionsBeforeLongBreak FROM Activity WHERE id = :id")
    int getSessionsBeforeLongBreak(int id);

    @Query("UPDATE Activity SET LongBreaks = :areEnabled WHERE id = :id")
    void setLongBreaksEnabled(int id, boolean areEnabled);

    @Query("SELECT LongBreaks FROM Activity WHERE id = :id")
    boolean areLongBreaksEnabled(int id);

    @Query("UPDATE Activity SET DND = :isEnabled WHERE id = :id")
    void setDNDEnabled(int id, boolean isEnabled);

    @Query("SELECT DND FROM Activity WHERE id = :id")
    boolean isDNDEnabled(int id);

    @Query("UPDATE Activity SET KeepDNDOnBreaks = :isEnabled WHERE id = :id")
    void setKeepDNDOnBreaks(int id, boolean isEnabled);

    @Query("SELECT KeepDNDOnBreaks FROM Activity WHERE id = :id")
    boolean isDNDKeptOnBreaks(int id);

    @Query("UPDATE Activity SET WiFi = :isEnabled WHERE id = :id")
    void setDisableWifi(int id, boolean isEnabled);

    @Query("SELECT WiFi FROM Activity WHERE id = :id")
    boolean isWifiDisabledDuringWorkSession(int id);

    @Query("SELECT Name FROM Activity WHERE id = :id")
    String getName(int id);
}
