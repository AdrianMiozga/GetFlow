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

package org.wentura.getflow.statistics.activitychart;

public final class LegendItem {

    private final String name;
    private final String time;
    private final double percent;

    public LegendItem(String name, double percent, String time) {
        this.name = name;
        this.time = time;
        this.percent = percent;
    }

    public double getPercent() {
        return percent;
    }

    public String getName() {
        return name;
    }

    public String getTime() {
        return time;
    }

    public int getTimeLength() {
        return time.length();
    }
}
