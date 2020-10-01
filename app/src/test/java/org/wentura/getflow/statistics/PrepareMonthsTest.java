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

package org.wentura.getflow.statistics;

import org.wentura.getflow.statistics.historychart.HistoryChartItem;
import org.wentura.getflow.statistics.historychart.MonthData;

import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PrepareMonthsTest {
    private static final String currentDate = "2020-03-10";
    private final List<HistoryChartItem> data = new ArrayList<>();
    private MonthData monthData;

    @Test
    public void prepareMonthsTest() {
        monthData = new MonthData(data);
        monthData.createMonthsArray();
        monthData.prepareMonths(currentDate);

        assertThat(monthData.getGeneratedData().size(), is(12));

        for (int i = 0; i < monthData.getGeneratedData().size(); i++) {
            assertThat(monthData.getGeneratedData().get(i).getTime(), is(0L));
        }
        countMonths();

        data.add(0, new HistoryChartItem(currentDate, 20000, 0));
        monthData = new MonthData(data);
        monthData.createMonthsArray();
        monthData.prepareMonths(currentDate);

        assertThat(monthData.getGeneratedData().size(), is(12));
        assertThat(monthData.getGeneratedData().get(11).getTime(), is(20000L));
        countMonths();
    }

    @Test
    public void prepareMonthsTest2() {
        data.clear();
        data.add(new HistoryChartItem("2020-01-08", 8300, 0));
        monthData = new MonthData(data);
        monthData.createMonthsArray();
        monthData.prepareMonths(currentDate);

        assertThat(monthData.getGeneratedData().size(), is(12));

        for (int i = 0; i < monthData.getGeneratedData().size(); i++) {
            if (i == 9) {
                assertThat(monthData.getGeneratedData().get(9).getTime(), is(8300L));
            } else {
                assertThat(monthData.getGeneratedData().get(i).getTime(), is(0L));
            }
        }
        countMonths();

        data.add(new HistoryChartItem(currentDate));
        monthData = new MonthData(data);
        monthData.createMonthsArray();
        monthData.prepareMonths(currentDate);

        assertThat(monthData.getGeneratedData().size(), is(12));
        for (int i = 0; i < monthData.getGeneratedData().size(); i++) {
            if (i == 9) {
                assertThat(monthData.getGeneratedData().get(i).getTime(), is(8300L));
            } else {
                assertThat(monthData.getGeneratedData().get(i).getTime(), is(0L));
            }
        }
        countMonths();

        data.add(0, new HistoryChartItem("2019-01-08", 1200, 0));
        monthData = new MonthData(data);
        monthData.createMonthsArray();
        monthData.prepareMonths(currentDate);

        assertThat(monthData.getGeneratedData().size(), is(15));

        for (int i = 0; i < monthData.getGeneratedData().size(); i++) {
            if (i == 0) {
                assertThat(monthData.getGeneratedData().get(i).getTime(), is(1200L));
            } else if (i == 12) {
                assertThat(monthData.getGeneratedData().get(i).getTime(), is(8300L));
            } else {
                assertThat("At i = " + i,
                        monthData.getGeneratedData().get(i).getTime(), is(0L));
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