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

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public final class Pomodoro {
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "Date")
    private final String Date;

    @ColumnInfo(name = "CompletedWorks")
    private final int CompletedWorks;

    @ColumnInfo(name = "CompletedWorkTime")
    private final int CompletedWorkTime;

    @ColumnInfo(name = "IncompleteWorks")
    private final int IncompleteWorks;

    @ColumnInfo(name = "IncompleteWorkTime")
    private final int IncompleteWorkTime;

    @ColumnInfo(name = "Breaks")
    private final int Breaks;

    @ColumnInfo(name = "BreakTime")
    private final int BreakTime;

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
