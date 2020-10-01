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

package org.wentura.getflow;

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
