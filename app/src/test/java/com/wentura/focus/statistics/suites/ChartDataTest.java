package com.wentura.focus.statistics.suites;

import com.wentura.focus.statistics.CreateMonthsArrayTest;
import com.wentura.focus.statistics.PrepareDaysTest;
import com.wentura.focus.statistics.PrepareMonthsTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({CreateMonthsArrayTest.class, PrepareDaysTest.class, PrepareMonthsTest.class})
public class ChartDataTest {
}
