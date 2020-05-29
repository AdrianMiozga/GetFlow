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

import com.wentura.focus.Utility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.wentura.focus.Constants.datePattern;
import static com.wentura.focus.Utility.stringToDate;

class DayData extends ChartData {
    private List<StatisticsItem> days;

    DayData(List<StatisticsItem> data) {
        super(data);
    }

    public void generate() {
        prepareDays(Utility.getCurrentDate());
        createEntries(days);
    }

    @Override
    List<StatisticsItem> getGeneratedData() {
        return new ArrayList<>(days);
    }

    void prepareDays(String currentDate) {
        days = getData();
        SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern, Locale.US);

        if (days.size() == 1) {
            if (!days.get(0).getDate().equals(currentDate)) {
                days.add(new StatisticsItem(currentDate));
            }
        }

        for (int i = 0; i < days.size() - 1; i++) {
            Date todayDate = stringToDate(days.get(i).getDate());
            Date nextDate = stringToDate(days.get(i + 1).getDate());

            if (nextDate == null || todayDate == null) {
                continue;
            }

            Calendar calendarNext = Calendar.getInstance();
            calendarNext.setTime(nextDate);

            Calendar todayCalendar = Calendar.getInstance();
            todayCalendar.setTime(todayDate);
            todayCalendar.add(Calendar.DATE, 1);

            String insertDate;

            if (calendarNext.compareTo(todayCalendar) != 0) {
                insertDate = dateFormat.format(todayCalendar.getTime());
                days.add(i + 1, new StatisticsItem(insertDate));
                continue;
            }

            if (i + 1 == days.size() - 1 && !days.get(i + 1).getDate().equals(currentDate)) {
                todayCalendar.add(Calendar.DATE, 1);
                insertDate = dateFormat.format(todayCalendar.getTime());

                days.add(new StatisticsItem(insertDate));
            }
        }

        if (days.size() == 0) {
            days.add(new StatisticsItem(currentDate));
        }

        if (days.size() < 12) {
            Date firstDay = stringToDate(days.get(0).getDate());

            if (firstDay == null) {
                return;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(firstDay);

            for (int i = 12 - days.size(); i > 0; i--) {
                calendar.add(Calendar.DATE, -1);

                days.add(0, new StatisticsItem(dateFormat.format(calendar.getTime())));
            }
        }
    }
}
