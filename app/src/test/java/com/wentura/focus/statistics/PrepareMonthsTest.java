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

import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PrepareMonthsTest {
    private static final String currentDate = "2020-03-10";
    private final List<StatisticsItem> data = new ArrayList<>();
    private MonthData monthData;

    @Test
    public void prepareMonthsTest() {
        monthData = new MonthData(data);
        monthData.createMonthsArray();
        monthData.prepareMonths(currentDate);

        assertThat(monthData.getGeneratedData().size(), is(12));

        for (int i = 0; i < monthData.getGeneratedData().size(); i++) {
            assertThat(monthData.getGeneratedData().get(i).getIncompleteWorkTime(), is(0));
            assertThat(monthData.getGeneratedData().get(i).getCompletedWorkTime(), is(0));
        }
        countMonths();

        data.add(0, new StatisticsItem(currentDate, 0, 20000, 0, 5000, 0, 0));
        monthData = new MonthData(data);
        monthData.createMonthsArray();
        monthData.prepareMonths(currentDate);

        assertThat(monthData.getGeneratedData().size(), is(12));
        assertThat(monthData.getGeneratedData().get(11).getCompletedWorkTime(), is(20000));
        assertThat(monthData.getGeneratedData().get(11).getIncompleteWorkTime(), is(5000));
        countMonths();
    }

    @Test
    public void prepareMonthsTest2() {
        data.clear();
        data.add(new StatisticsItem("2020-01-08", 0, 8300, 0, 2008, 0, 0));
        monthData = new MonthData(data);
        monthData.createMonthsArray();
        monthData.prepareMonths(currentDate);

        assertThat(monthData.getGeneratedData().size(), is(12));

        for (int i = 0; i < monthData.getGeneratedData().size(); i++) {
            if (i == 9) {
                assertThat(monthData.getGeneratedData().get(9).getCompletedWorkTime(), is(8300));
                assertThat(monthData.getGeneratedData().get(9).getIncompleteWorkTime(), is(2008));
            } else {
                assertThat(monthData.getGeneratedData().get(i).getCompletedWorkTime(), is(0));
                assertThat(monthData.getGeneratedData().get(i).getIncompleteWorkTime(), is(0));
            }
        }
        countMonths();

        data.add(new StatisticsItem(currentDate));
        monthData = new MonthData(data);
        monthData.createMonthsArray();
        monthData.prepareMonths(currentDate);

        assertThat(monthData.getGeneratedData().size(), is(12));
        for (int i = 0; i < monthData.getGeneratedData().size(); i++) {
            if (i == 9) {
                assertThat(monthData.getGeneratedData().get(i).getCompletedWorkTime(), is(8300));
                assertThat(monthData.getGeneratedData().get(i).getIncompleteWorkTime(), is(2008));
            } else {
                assertThat(monthData.getGeneratedData().get(i).getCompletedWorkTime(), is(0));
                assertThat(monthData.getGeneratedData().get(i).getIncompleteWorkTime(), is(0));
            }
        }
        countMonths();

        data.add(0, new StatisticsItem("2019-01-08", 0, 1200, 0, 5000, 0, 0));
        monthData = new MonthData(data);
        monthData.createMonthsArray();
        monthData.prepareMonths(currentDate);

        assertThat(monthData.getGeneratedData().size(), is(15));

        for (int i = 0; i < monthData.getGeneratedData().size(); i++) {
            if (i == 0) {
                assertThat(monthData.getGeneratedData().get(i).getCompletedWorkTime(), is(1200));
                assertThat(monthData.getGeneratedData().get(i).getIncompleteWorkTime(), is(5000));
            } else if (i == 12) {
                assertThat(monthData.getGeneratedData().get(i).getCompletedWorkTime(), is(8300));
                assertThat(monthData.getGeneratedData().get(i).getIncompleteWorkTime(), is(2008));
            } else {
                assertThat("At i = " + i,
                        monthData.getGeneratedData().get(i).getCompletedWorkTime(), is(0));
                assertThat("At i = " + i, monthData.getGeneratedData().get(i).getIncompleteWorkTime(), is(0));
            }
        }

        countMonths();
    }

    private void countMonths() {
        LocalDate localDate = LocalDate.parse(currentDate);
        for (int i = monthData.getGeneratedData().size() - 1; i >= 0; i--) {
            assertThat("At i = " + i, monthData.getGeneratedData().get(i).getDate().toString().substring(0, 7),
                    equalTo(localDate.toString().substring(0, 7)));
            localDate = localDate.minusMonths(1);
        }
    }
}