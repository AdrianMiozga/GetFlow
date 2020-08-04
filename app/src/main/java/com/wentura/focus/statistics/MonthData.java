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
import static com.wentura.focus.Utility.calendarToString;
import static com.wentura.focus.Utility.stringToDate;

final class MonthData extends ChartData {
    private final List<StatisticsItem> months = new ArrayList<>();

    MonthData(List<StatisticsItem> data) {
        super(data);
    }

    public void generate() {
        createMonthsArray();
        prepareMonths(Utility.getCurrentDate());
        createEntries(months);
    }

    @Override
    List<StatisticsItem> getGeneratedData() {
        return new ArrayList<>(months);
    }

    void prepareMonths(String currentDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern, Locale.US);

        // The loop below this condition works only when the months array has two entries. I'm
        // using this block of code to generate the second entry if it doesn't exist.
        if (months.size() == 1) {
            Date thisMonth = stringToDate(months.get(0).getDate());
            Date currentMonth = stringToDate(currentDate);

            if (thisMonth != null && currentMonth != null) {
                Calendar thisCalendar = Calendar.getInstance();
                thisCalendar.setTime(thisMonth);

                Calendar currentCalendar = Calendar.getInstance();
                currentCalendar.setTime(currentMonth);

                if (thisCalendar.get(Calendar.MONTH) != currentCalendar.get(Calendar.MONTH) ||
                        thisCalendar.get(Calendar.YEAR) != currentCalendar.get(Calendar.YEAR)) {
                    String date = calendarToString(currentCalendar);

                    months.add(new StatisticsItem(date));
                }
            }
        }

        for (int i = 0; i < months.size() - 1; i++) {
            Date thisMonth = stringToDate(months.get(i).getDate());
            Date nextMonth = stringToDate(months.get(i + 1).getDate());

            if (nextMonth == null || thisMonth == null) {
                continue;
            }

            Calendar nextCalendar = Calendar.getInstance();
            nextCalendar.setTime(nextMonth);

            Calendar thisCalendar = Calendar.getInstance();
            thisCalendar.setTime(thisMonth);
            thisCalendar.add(Calendar.MONTH, 1);

            String insertDate;

            if (nextCalendar.get(Calendar.MONTH) != thisCalendar.get(Calendar.MONTH) ||
                    nextCalendar.get(Calendar.YEAR) != thisCalendar.get(Calendar.YEAR)) {
                insertDate = dateFormat.format(thisCalendar.getTime());
                months.add(i + 1, new StatisticsItem(insertDate));
                continue;
            }

            if (i + 1 == months.size() - 1 &&
                    !new SimpleDateFormat("MMMM", Locale.US).format(nextCalendar.getTime()).equals(Utility.getMonth(currentDate))) {
                thisCalendar.add(Calendar.MONTH, 1);
                insertDate = dateFormat.format(thisCalendar.getTime());

                months.add(new StatisticsItem(insertDate));
            }
        }

        if (months.size() == 0) {
            Calendar calendar = Calendar.getInstance();
            Date date = stringToDate(currentDate);
            if (date != null) {
                calendar.setTime(date);
                calendar.add(Calendar.MONTH, -11);

                for (int i = 0; i < 12; i++) {
                    months.add(new StatisticsItem(dateFormat.format(calendar.getTime())));

                    calendar.add(Calendar.MONTH, 1);
                }
            }
        }

        if (months.size() < 12) {
            Date firstMonth = stringToDate(months.get(0).getDate());

            if (firstMonth != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(firstMonth);

                for (int i = 12 - months.size(); i > 0; i--) {
                    calendar.add(Calendar.MONTH, -1);

                    months.add(0, new StatisticsItem(dateFormat.format(calendar.getTime())));
                }
            }
        }
    }

    void createMonthsArray() {
        int totalCompletedTime = 0;
        int totalIncompleteTime = 0;
        List<StatisticsItem> days = getData();

        for (int i = 0; i < days.size() - 1; i++) {
            Date todayDate = stringToDate(days.get(i).getDate());
            Date nextDate = stringToDate(days.get(i + 1).getDate());

            if (nextDate == null || todayDate == null) {
                continue;
            }

            Calendar today = Calendar.getInstance();
            today.setTime(todayDate);

            Calendar nextDay = Calendar.getInstance();
            nextDay.setTime(nextDate);

            totalCompletedTime += days.get(i).getCompletedWorkTime();
            totalIncompleteTime += days.get(i).getIncompleteWorkTime();

            if (today.get(Calendar.MONTH) != nextDay.get(Calendar.MONTH) ||
                    today.get(Calendar.YEAR) != nextDay.get(Calendar.YEAR)) {
                String stringDate = calendarToString(today);

                months.add(new StatisticsItem(stringDate, 0, totalCompletedTime, 0,
                        totalIncompleteTime, 0, 0));

                totalCompletedTime = 0;
                totalIncompleteTime = 0;
            }

            if (i == days.size() - 2) {
                if (today.get(Calendar.MONTH) == nextDay.get(Calendar.MONTH) &&
                        today.get(Calendar.YEAR) == nextDay.get(Calendar.YEAR) &&
                        i == days.size() - 2) {

                    totalCompletedTime += days.get(i + 1).getCompletedWorkTime();
                    totalIncompleteTime += days.get(i + 1).getIncompleteWorkTime();

                    String stringDate = calendarToString(nextDay);

                    months.add(new StatisticsItem(stringDate, 0, totalCompletedTime, 0,
                            totalIncompleteTime, 0, 0));
                }

                if (today.get(Calendar.MONTH) != nextDay.get(Calendar.MONTH) ||
                        today.get(Calendar.YEAR) != nextDay.get(Calendar.YEAR)) {

                    months.add(days.get(i + 1));
                }
            }
        }

        if (days.size() == 1) {
            Date todayDate = stringToDate(days.get(0).getDate());

            if (todayDate != null) {
                Calendar todayCalendar = Calendar.getInstance();
                todayCalendar.setTime(todayDate);

                String stringDate = calendarToString(todayCalendar);

                months.add(new StatisticsItem(stringDate, 0, days.get(0).getCompletedWorkTime(), 0,
                        days.get(0).getIncompleteWorkTime(), 0, 0));
            }
        }
    }
}
