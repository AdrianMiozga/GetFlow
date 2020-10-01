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

package org.wentura.getflow.statistics.activitychart;

import androidx.annotation.NonNull;

import org.wentura.getflow.database.Pomodoro;

public final class PieChartItem {

    /** Sum of CompletedWorkTime and IncompleteWorkTime from {@link Pomodoro}. */
    private final long TotalTime;
    private double Percent;
    private final String ActivityName;

    public PieChartItem(long TotalTime, double Percent, String ActivityName) {
        this.TotalTime = TotalTime;
        this.Percent = Percent;
        this.ActivityName = ActivityName;
    }

    public long getTotalTime() {
        return TotalTime;
    }

    public String getActivityName() {
        return ActivityName;
    }

    public void setPercent(double percent) {
        this.Percent = percent;
    }

    public double getPercent() {
        return Percent;
    }

    @NonNull
    @Override
    public String toString() {
        return "PieChartItem{" +
                "TotalTime=" + TotalTime +
                ", Percent=" + Percent +
                ", ActivityName='" + ActivityName + '\'' +
                '}';
    }
}
