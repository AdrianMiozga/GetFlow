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

package com.wentura.focus.statistics;

import androidx.annotation.NonNull;
import androidx.room.Ignore;

public class StatisticsItem implements Comparable<StatisticsItem> {
    private String Date;
    private int CompletedWorks;
    private int CompletedWorkTime;
    private int IncompleteWorks;
    private int IncompleteWorkTime;
    private int Breaks;
    private int BreakTime;

    public StatisticsItem(String Date, int CompletedWorks, int CompletedWorkTime,
                          int IncompleteWorks, int IncompleteWorkTime, int Breaks, int BreakTime) {
        this.Date = Date;
        this.CompletedWorks = CompletedWorks;
        this.CompletedWorkTime = CompletedWorkTime;
        this.IncompleteWorks = IncompleteWorks;
        this.IncompleteWorkTime = IncompleteWorkTime;
        this.Breaks = Breaks;
        this.BreakTime = BreakTime;
    }

    @Ignore
    public StatisticsItem(String Date) {
        this(Date, 0, 0, 0, 0, 0, 0);
    }

    String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    int getCompletedWorks() {
        return CompletedWorks;
    }

    int getCompletedWorkTime() {
        return CompletedWorkTime;
    }

    int getIncompleteWorks() {
        return IncompleteWorks;
    }

    int getIncompleteWorkTime() {
        return IncompleteWorkTime;
    }

    int getBreaks() {
        return Breaks;
    }

    int getBreakTime() {
        return BreakTime;
    }

    @Override
    public int compareTo(@NonNull StatisticsItem statisticsItem) {
        return getDate().compareTo(statisticsItem.getDate());
    }
}
