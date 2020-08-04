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

import java.time.LocalDate;

public final class StatisticsItem implements Comparable<StatisticsItem> {
    private final String Date;
    private final int CompletedWorks;
    private final int CompletedWorkTime;
    private final int IncompleteWorks;
    private final int IncompleteWorkTime;
    private final int Breaks;
    private final int BreakTime;

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

    public static StatisticsItem of(LocalDate LocalDate, int CompletedWorks, int CompletedWorkTime,
                                    int IncompleteWorks, int IncompleteWorkTime, int Breaks, int BreakTime) {
        return new StatisticsItem(LocalDate.toString(), CompletedWorks, CompletedWorkTime, IncompleteWorks,
                IncompleteWorkTime, Breaks,
                BreakTime);
    }

    public static StatisticsItem of(LocalDate LocalDate) {
        return new StatisticsItem(LocalDate.toString(), 0, 0, 0, 0, 0, 0);
    }

    LocalDate getDate() {
        return LocalDate.parse(Date);
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
