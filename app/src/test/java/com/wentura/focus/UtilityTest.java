package com.wentura.focus;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class UtilityTest {
    @Test
    public void formatStatisticsTime_ThreeHours_ReturnsFormatted() {
        assertThat(Utility.formatStatisticsTime(10_800_000L), equalTo("3h"));
    }

    @Test
    public void formatStatisticsTime_ThreeHoursTwoMinutes_ReturnsFormatted() {
        assertThat("Should be 3h 2m as I'm always rounding up minutes in this method",
                Utility.formatStatisticsTime(10_890_000L), equalTo("3h 2m"));
    }

    @Test
    public void formatStatisticsTime_FifteenSeconds_ReturnsFormatted() {
        assertThat("Anything <= 60s should display as 1m",
                Utility.formatStatisticsTime(15_000L), equalTo("1m"));
    }
}
