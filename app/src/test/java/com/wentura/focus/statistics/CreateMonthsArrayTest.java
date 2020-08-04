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

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class CreateMonthsArrayTest {
    private final List<StatisticsItem> data = new ArrayList<>();
    private MonthData monthData;

    @Test
    public void createMonthsArrayTest() {
        data.add(new StatisticsItem("2020-03-10", 0, 2800, 0, 10000, 0, 0));
        monthData = new MonthData(data);
        monthData.createMonthsArray();

        assertThat(monthData.getGeneratedData().size(), is(1));
        assertThat(monthData.getGeneratedData().get(0).getCompletedWorkTime(), is(2800));
        assertThat(monthData.getGeneratedData().get(0).getIncompleteWorkTime(), is(10000));

        data.add(0, new StatisticsItem("2020-03-02", 0, 3000, 0, 15000, 0, 0));
        monthData = new MonthData(data);
        monthData.createMonthsArray();

        assertThat(monthData.getGeneratedData().size(), is(1));
        assertThat(monthData.getGeneratedData().get(0).getCompletedWorkTime(), is(5800));
        assertThat(monthData.getGeneratedData().get(0).getIncompleteWorkTime(), is(25000));

        data.add(0, new StatisticsItem("2019-02-02", 0, 5000, 0, 8000, 0, 0));
        monthData = new MonthData(data);
        monthData.createMonthsArray();

        assertThat(monthData.getGeneratedData().size(), is(2));
        assertThat(monthData.getGeneratedData().get(1).getCompletedWorkTime(), is(5800));
        assertThat(monthData.getGeneratedData().get(1).getIncompleteWorkTime(), is(25000));
        assertThat(monthData.getGeneratedData().get(0).getCompletedWorkTime(), is(5000));
        assertThat(monthData.getGeneratedData().get(0).getIncompleteWorkTime(), is(8000));
    }

    @Test
    public void createMonthsArrayTest2() {
        data.clear();

        data.add(new StatisticsItem("2019-05-13"));
        monthData = new MonthData(data);
        monthData.createMonthsArray();

        assertThat(monthData.getGeneratedData().size(), is(1));
        assertThat(monthData.getGeneratedData().get(0).getCompletedWorkTime(), is(0));
        assertThat(monthData.getGeneratedData().get(0).getIncompleteWorkTime(), is(0));
    }

    @Test
    public void createMonthsArrayTest3() {
        data.clear();
        monthData = new MonthData(data);
        monthData.createMonthsArray();

        assertThat(monthData.getGeneratedData().size(), is(0));
    }
}
