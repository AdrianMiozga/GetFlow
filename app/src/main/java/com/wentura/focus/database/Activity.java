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

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.wentura.focus.Constants;

@Entity
public final class Activity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    private int id;

    @ColumnInfo(name = "Name")
    private final String name;

    @ColumnInfo(name = "WorkDuration")
    private final int workDuration;

    @ColumnInfo(name = "BreakDuration")
    private final int breakDuration;

    @ColumnInfo(name = "LongBreaks")
    private final boolean longBreaks;

    @ColumnInfo(name = "LongBreakDuration")
    private final int longBreakDuration;

    @ColumnInfo(name = "DND")
    private final boolean DND;

    @ColumnInfo(name = "KeepDNDOnBreaks")
    private final boolean keepDNDOnBreaks;

    @ColumnInfo(name = "WiFi")
    private final boolean WiFi;

    public Activity(String name, int workDuration, int breakDuration, boolean longBreaks, int longBreakDuration,
                    boolean DND, boolean keepDNDOnBreaks, boolean WiFi) {
        this.name = name;
        this.workDuration = workDuration;
        this.breakDuration = breakDuration;
        this.longBreaks = longBreaks;
        this.longBreakDuration = longBreakDuration;
        this.DND = DND;
        this.keepDNDOnBreaks = keepDNDOnBreaks;
        this.WiFi = WiFi;
    }

    @Ignore
    public Activity(String name) {
        this(name, Constants.DEFAULT_WORK_TIME, Constants.DEFAULT_BREAK_TIME, true, Constants.DEFAULT_LONG_BREAK_TIME, false, false, false);
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

    public boolean isDND() {
        return DND;
    }

    public boolean isKeepDNDOnBreaks() {
        return keepDNDOnBreaks;
    }

    public boolean isWiFi() {
        return WiFi;
    }
}
