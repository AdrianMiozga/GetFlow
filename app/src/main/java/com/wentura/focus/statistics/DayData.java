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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

final class DayData extends ChartData {
    private List<StatisticsItem> days;

    DayData(List<StatisticsItem> data) {
        super(data);
    }

    public void generate() {
        prepareDays(LocalDate.now());
        createEntries(days);
    }

    @Override
    List<StatisticsItem> getGeneratedData() {
        return new ArrayList<>(days);
    }

    void prepareDays(LocalDate currentDate) {
        days = getData();

        if (days.size() == 1) {
            if (!days.get(0).getDate().equals(currentDate)) {
                days.add(StatisticsItem.of(currentDate));
            }
        }

        for (int i = 0; i < days.size() - 1; i++) {
            LocalDate todayDate = days.get(i).getDate().plusDays(1);
            LocalDate nextDate = days.get(i + 1).getDate();

            if (!todayDate.equals(nextDate)) {
                days.add(i + 1, StatisticsItem.of(todayDate));
                continue;
            }

            if (i + 1 == days.size() - 1 && !days.get(i + 1).getDate().equals(currentDate)) {
                days.add(StatisticsItem.of(todayDate.plusDays(1)));
            }
        }

        if (days.size() == 0) {
            days.add(StatisticsItem.of(currentDate));
        }

        if (days.size() < 12) {
            LocalDate firstDay = days.get(0).getDate();

            for (int i = 12 - days.size(); i > 0; i--) {
                firstDay = firstDay.minusDays(1);
                days.add(0, StatisticsItem.of(firstDay));
            }
        }
    }
}
