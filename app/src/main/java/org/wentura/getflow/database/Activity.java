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

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import org.wentura.getflow.Constants;

@Entity
public final class Activity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    private int id;

    @ColumnInfo(name = "Name")
    private final String name;

    /** Work duration, in minutes. */
    @ColumnInfo(name = "WorkDuration")
    private final int workDuration;

    /** Break duration, in minutes. */
    @ColumnInfo(name = "BreakDuration")
    private final int breakDuration;

    @ColumnInfo(name = "LongBreaks")
    private final boolean longBreaks;

    /** Long break duration, in minutes. */
    @ColumnInfo(name = "LongBreakDuration")
    private final int longBreakDuration;

    @ColumnInfo(name = "SessionsBeforeLongBreak")
    private final int sessionsBeforeLongBreak;

    @ColumnInfo(name = "DND")
    private final boolean DND;

    @ColumnInfo(name = "KeepDNDOnBreaks")
    private final boolean keepDNDOnBreaks;

    @ColumnInfo(name = "WiFi")
    private final boolean WiFi;

    @ColumnInfo(name = "showInStatistics")
    private final boolean showInStatistics;

    public Activity(String name, int workDuration, int breakDuration, boolean longBreaks,
                    int longBreakDuration, int sessionsBeforeLongBreak, boolean DND, boolean keepDNDOnBreaks,
                    boolean WiFi, boolean showInStatistics) {
        this.name = name;
        this.workDuration = workDuration;
        this.breakDuration = breakDuration;
        this.longBreaks = longBreaks;
        this.longBreakDuration = longBreakDuration;
        this.sessionsBeforeLongBreak = sessionsBeforeLongBreak;
        this.DND = DND;
        this.keepDNDOnBreaks = keepDNDOnBreaks;
        this.WiFi = WiFi;
        this.showInStatistics = showInStatistics;
    }

    @Ignore
    public Activity(String name) {
        this(name, Constants.DEFAULT_WORK_TIME, Constants.DEFAULT_BREAK_TIME, true, Constants.DEFAULT_LONG_BREAK_TIME,
                Constants.DEFAULT_SESSIONS_BEFORE_LONG_BREAK, false, false, false, true);
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getWorkDuration() {
        return workDuration;
    }

    public int getBreakDuration() {
        return breakDuration;
    }

    public boolean isLongBreaks() {
        return longBreaks;
    }

    public int getLongBreakDuration() {
        return longBreakDuration;
    }

    public int getSessionsBeforeLongBreak() {
        return sessionsBeforeLongBreak;
    }

    public boolean isDND() {
        return DND;
    }

    public boolean isKeepDNDOnBreaks() {
        return keepDNDOnBreaks;
    }

    public boolean isWiFi() {
        return WiFi;
    }

    public boolean isShowInStatistics() {
        return showInStatistics;
    }
}
