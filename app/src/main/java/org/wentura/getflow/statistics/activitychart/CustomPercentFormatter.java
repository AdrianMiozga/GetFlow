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

import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.DecimalFormat;

public final class CustomPercentFormatter extends ValueFormatter {

    private final DecimalFormat decimalFormat;

    public CustomPercentFormatter() {
        decimalFormat = new DecimalFormat("#0");
    }

    @Override
    public String getFormattedValue(float value) {
        return decimalFormat.format(value) + "%";
    }

    @Override
    public String getPieLabel(float value, PieEntry pieEntry) {
        return getFormattedValue(value);
    }
}
