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
