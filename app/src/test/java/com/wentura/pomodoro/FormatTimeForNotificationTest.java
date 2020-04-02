package com.wentura.pomodoro;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
public class FormatTimeForNotificationTest {
    @Parameter
    public long input;
    @Parameter(1)
    public String expected;

    @Parameters(name = "{index}: formatTimeForNotification({0}) = {1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {60_000L, "1m"},
                {70_000L, "1m 10s"},
                {30_000L, "30s"},
                {10_800_000L, "3h"},
                {10_830_000L, "3h"},
                {10_890_000L, "3h 1m"},
                {0L, "0s"}
        });
    }

    @Test
    public void formatTimeForNotification() {
        assertThat(Utility.formatTimeForNotification(input), equalTo(expected));
    }
}
