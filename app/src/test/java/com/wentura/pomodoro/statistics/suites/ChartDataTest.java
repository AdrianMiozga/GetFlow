package com.wentura.pomodoro.statistics.suites;

import com.wentura.pomodoro.statistics.CreateMonthsArrayTest;
import com.wentura.pomodoro.statistics.PrepareDaysTest;
import com.wentura.pomodoro.statistics.PrepareMonthsTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({CreateMonthsArrayTest.class, PrepareDaysTest.class, PrepareMonthsTest.class})
public class ChartDataTest {
}
