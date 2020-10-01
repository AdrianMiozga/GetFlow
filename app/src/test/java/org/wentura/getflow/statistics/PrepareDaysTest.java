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

import org.wentura.getflow.statistics.historychart.DayData;
import org.wentura.getflow.statistics.historychart.HistoryChartItem;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assume.assumeThat;
import static org.junit.runners.Parameterized.Parameter;
import static org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class PrepareDaysTest {
    private static final String currentDate = "2020-03-10";
    @Parameter
    public String date;
    private List<HistoryChartItem> data;
    private DayData dayData;

    @Parameters(name = "{index}: {0}")
    public static Iterable<String> dates() {
        return Arrays.asList(
                currentDate,
                "2020-03-09",
                "2020-02-28",
                "2020-03-01",
                "2020-02-10",
                "2019-03-10");
    }

    @Before
    public void setUp() {
        data = new ArrayList<>();
        data.add(new HistoryChartItem(date, 0, 0));
        dayData = new DayData(data);
    }

    @Test
    public void prepareDaysTest() {
        dayData.prepareDays(LocalDate.parse(currentDate));
        countDays();
    }

    @Test
    public void prepareDaysWithCurrentDateTest() {
        data.add(new HistoryChartItem(currentDate, 0, 0));
        assumeThat(data.get(0).getDate(), is(not(data.get(1).getDate())));

        dayData.prepareDays(LocalDate.parse(currentDate));
        countDays();
    }

    private void countDays() {
        LocalDate localDate = LocalDate.parse(currentDate);
        for (int i = dayData.getGeneratedData().size() - 1; i >= 0; i--) {
            assertThat("At i = " + i, dayData.getGeneratedData().get(i).getDate().toString(),
                    equalTo(localDate.toString()));
            localDate = localDate.minusDays(1);
        }
    }
}