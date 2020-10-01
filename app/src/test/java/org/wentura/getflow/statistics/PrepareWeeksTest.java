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
import org.wentura.getflow.statistics.historychart.WeekData;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class PrepareWeeksTest {
    private static final String currentDate = "2020-03-10";
    private static final String dateFromDB = "2020-03-09";
    private List<HistoryChartItem> data;
    private WeekData weekData;

    @Before
    public void setUp() {
        data = new ArrayList<>();
    }

    /** Check if 12 entries are created with proper dates when there's no data from database. */
    @Test
    public void prepareWeeksTest() {
        weekData = new WeekData(new ArrayList<>());
        weekData.prepareWeeks(LocalDate.parse(currentDate));
        assertThat(weekData.getGeneratedData().size(), equalTo(12));
        countWeeks();
    }

    /** Check if 12 entries are created with proper dates when there's only one entry. */
    @Test
    public void prepareWeeksTest2() {
        data.add(new HistoryChartItem(dateFromDB));

        weekData = new WeekData(data);
        weekData.prepareWeeks(LocalDate.parse(currentDate));
        assertThat(weekData.getGeneratedData().size(), equalTo(12));
        countWeeks();
    }

    /** Check if 12 entries are created with proper dates when there's one entry from 11 weeks ago. */
    @Test
    public void prepareWeeksTest3() {
        data.add(new HistoryChartItem("2019-12-23"));

        weekData = new WeekData(data);
        weekData.prepareWeeks(LocalDate.parse(currentDate));
        assertThat(weekData.getGeneratedData().size(), equalTo(12));
        countWeeks();
    }

    /** Check if 13 entries are created with proper dates when there's one entry from 12 weeks ago. */
    @Test
    public void prepareWeeksTest4() {
        data.add(new HistoryChartItem("2019-12-16"));

        weekData = new WeekData(data);
        weekData.prepareWeeks(LocalDate.parse(currentDate));
        assertThat(weekData.getGeneratedData().size(), equalTo(13));
        countWeeks();
    }

    /** Check if 109 entries are created with proper dates when there's one entry from 108 weeks/756 days ago. */
    @Test
    public void prepareWeeksTest5() {
        data.add(new HistoryChartItem("2018-02-12"));

        weekData = new WeekData(data);
        weekData.prepareWeeks(LocalDate.parse(currentDate));
        assertThat(weekData.getGeneratedData().size(), equalTo(109));
        countWeeks();
    }

    /** Check if no entries are created when there's already 12 existing from the last 12 weeks. */
    @Test
    public void prepareWeeksTest6() {
        data.add(new HistoryChartItem("2019-12-23"));
        data.add(new HistoryChartItem("2019-12-30"));
        data.add(new HistoryChartItem("2020-01-06"));
        data.add(new HistoryChartItem("2020-01-13"));
        data.add(new HistoryChartItem("2020-01-20"));
        data.add(new HistoryChartItem("2020-01-27"));
        data.add(new HistoryChartItem("2020-02-03"));
        data.add(new HistoryChartItem("2020-02-10"));
        data.add(new HistoryChartItem("2020-02-17"));
        data.add(new HistoryChartItem("2020-02-24"));
        data.add(new HistoryChartItem("2020-03-02"));
        data.add(new HistoryChartItem("2020-03-09"));

        weekData = new WeekData(data);
        weekData.prepareWeeks(LocalDate.parse(currentDate));
        assertThat(weekData.getGeneratedData().size(), equalTo(12));
        countWeeks();
    }

    /** Checks if each entry is 7 days apart. */
    private void countWeeks() {
        LocalDate localDate = LocalDate.parse(dateFromDB);

        for (int i = weekData.getGeneratedData().size() - 1; i >= 0; i--) {
            assertThat("At i = " + i, weekData.getGeneratedData().get(i).getDate().toString(),
                    equalTo(localDate.toString()));
            localDate = localDate.minusWeeks(1);
        }
    }
}
