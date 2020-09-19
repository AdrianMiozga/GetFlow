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

package com.wentura.focus.statistics.historychart;

import androidx.annotation.NonNull;
import androidx.room.Ignore;

import java.time.LocalDate;

public final class HistoryChartItem implements Comparable<HistoryChartItem> {
    private final String date;
    private final int completedWorkTime;
    private final int incompleteWorkTime;
    private final int activityId;

    public HistoryChartItem(String date, int completedWorkTime,
                            int incompleteWorkTime, int activityId) {
        this.date = date;
        this.completedWorkTime = completedWorkTime;
        this.incompleteWorkTime = incompleteWorkTime;
        this.activityId = activityId;
    }

    @Ignore
    public HistoryChartItem(String Date) {
        this(Date, 0, 0, 0);
    }

    public static HistoryChartItem of(LocalDate LocalDate, int CompletedWorkTime, int IncompleteWorkTime, int activityId) {
        return new HistoryChartItem(LocalDate.toString(), CompletedWorkTime, IncompleteWorkTime, activityId);
    }

    public static HistoryChartItem of(LocalDate LocalDate) {
        return new HistoryChartItem(LocalDate.toString(), 0, 0, 0);
    }

    public LocalDate getDate() {
        return LocalDate.parse(date);
    }

    public int getCompletedWorkTime() {
        return completedWorkTime;
    }

    public int getIncompleteWorkTime() {
        return incompleteWorkTime;
    }

    public int getActivityId() {
        return activityId;
    }

    @Override
    public int compareTo(@NonNull HistoryChartItem historyChartItem) {
        return getDate().compareTo(historyChartItem.getDate());
    }

    @Override
    public String toString() {
        return "HistoryChartItem{" +
                "Date='" + date + '\'' +
                ", CompletedWorkTime=" + completedWorkTime +
                ", IncompleteWorkTime=" + incompleteWorkTime +
                ", ActivityId=" + activityId +
                '}';
    }
}
