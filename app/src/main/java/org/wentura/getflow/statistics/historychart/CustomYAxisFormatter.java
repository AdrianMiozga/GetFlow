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

package org.wentura.getflow.statistics.historychart;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

final class CustomYAxisFormatter extends ValueFormatter {

    CustomYAxisFormatter() {
    }

    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        String result;

        if (value <= 0) {
            return "";
        }

        if (axis.mAxisMaximum > 3_600_000) {
            result = Math.round(value / 3_600_000) + "h";
        } else {
            result = (int) Math.ceil(value / 60_000) + "m";
        }

        return result;
    }
}
