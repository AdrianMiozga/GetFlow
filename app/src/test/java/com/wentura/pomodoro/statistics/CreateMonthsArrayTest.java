package com.wentura.pomodoro.statistics;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class CreateMonthsArrayTest {
    private List<StatisticsItem> data = new ArrayList<>();
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
