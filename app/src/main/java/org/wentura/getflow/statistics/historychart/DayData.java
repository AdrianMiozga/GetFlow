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

package org.wentura.getflow.statistics.historychart;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class DayData extends ChartData {

    private List<HistoryChartItem> days;

    public DayData(List<HistoryChartItem> data) {
        super(data);
    }

    public void generate() {
        prepareDays(LocalDate.now());
        createEntries(days);
    }

    @Override
    public List<HistoryChartItem> getGeneratedData() {
        return new ArrayList<>(days);
    }

    /**
     * Prepares day data by making sure that there is always at least 12 entries.
     * Also, fills any gaps between dates.
     *
     * @param currentDate current date
     */
    public void prepareDays(LocalDate currentDate) {
        days = getData();

        if (days.size() == 1) {
            if (!days.get(0).getDate().equals(currentDate)) {
                days.add(HistoryChartItem.of(currentDate));
            }
        }

        for (int i = 0; i < days.size() - 1; i++) {
            LocalDate todayDate = days.get(i).getDate().plusDays(1);
            LocalDate nextDate = days.get(i + 1).getDate();

            if (!todayDate.equals(nextDate)) {
                days.add(i + 1, HistoryChartItem.of(todayDate));
                continue;
            }

            if (i + 1 == days.size() - 1 && !days.get(i + 1).getDate().equals(currentDate)) {
                days.add(HistoryChartItem.of(todayDate.plusDays(1)));
            }
        }

        if (days.size() == 0) {
            days.add(HistoryChartItem.of(currentDate));
        }

        if (days.size() < 12) {
            LocalDate firstDay = days.get(0).getDate();

            for (int i = 12 - days.size(); i > 0; i--) {
                firstDay = firstDay.minusDays(1);
                days.add(0, HistoryChartItem.of(firstDay));
            }
        }
    }
}
