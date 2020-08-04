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

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;

abstract class ChartData {
    private final List<Entry> entries;
    private final List<StatisticsItem> data;
    private long maxValue = 0;

    ChartData(List<StatisticsItem> data) {
        this.data = data;
        entries = new ArrayList<>();
    }

    abstract void generate();

    abstract List<StatisticsItem> getGeneratedData();

    void createEntries(List<StatisticsItem> statisticsItems) {
        entries.clear();

        maxValue = 0;

        for (int i = 0; i < statisticsItems.size(); i++) {
            long totalTime = statisticsItems.get(i).getCompletedWorkTime() + statisticsItems.get(i).getIncompleteWorkTime();

            entries.add(new Entry(i, totalTime));

            if (totalTime > maxValue) {
                maxValue = totalTime;
            }
        }
    }

    List<Entry> getEntries() {
        return new ArrayList<>(entries);
    }

    long getSize() {
        return entries.size();
    }

    long getMaxValue() {
        return maxValue;
    }

    List<StatisticsItem> getData() {
        return new ArrayList<>(data);
    }
}
