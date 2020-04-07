package com.wentura.pomodoro.statistics;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assume.assumeThat;

@RunWith(Parameterized.class)
public class PrepareDaysTest {
    private static String currentDate = "2020-03-10";
    @Parameter
    public String date;
    private List<StatisticsItem> data;
    private DayData dayData;

    @Parameters(name = "{index}: {0}")
    public static Iterable<? extends Object> dates() {
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
        data.add(new StatisticsItem(date, 0, 0, 0, 0, 0, 0));
        dayData = new DayData(data);
    }

    @Test
    public void prepareDaysTest() {
        dayData.prepareDays(currentDate);
        countDays();
    }

    @Test
    public void prepareDaysWithCurrentDateTest() {
        data.add(new StatisticsItem(currentDate, 0, 0, 0, 0, 0, 0));
        assumeThat(data.get(0).getDate(), is(not(data.get(1).getDate())));

        dayData.prepareDays(currentDate);
        countDays();
    }

    private void countDays() {
        LocalDate localDate = LocalDate.parse(currentDate);
        for (int i = data.size() - 1; i >= 0; i--) {
            assertThat("At i = " + i, data.get(i).getDate(), equalTo(localDate.toString()));
            localDate = localDate.minusDays(1);
        }
    }
}