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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

final class CustomXAxisFormatter extends ValueFormatter {

    private final List<HistoryChartItem> historyChartItems;
    private final SpinnerOption spinnerOption;

    CustomXAxisFormatter(List<HistoryChartItem> historyChartItems, SpinnerOption spinnerOption) {
        this.historyChartItems = historyChartItems;
        this.spinnerOption = spinnerOption;
    }

    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        if (value >= historyChartItems.size()) {
            return "";
        }

        LocalDate date = historyChartItems.get((int) value).getDate();

        String result = "";

        switch (spinnerOption) {
            case DAYS: {
                if (date.getMonthValue() == 1 && date.getDayOfMonth() == 1 || value == axis.mEntries[0]) {
                    result = DateTimeFormatter.ofPattern("MMM").format(date) + "\n" + date.getYear();
                    break;
                }

                if (date.getDayOfMonth() == 1) {
                    result = DateTimeFormatter.ofPattern("MMM").format(date);
                    break;
                }

                result = String.valueOf(date.getDayOfMonth());
                break;
            }
            case WEEKS: {
                if (value == axis.mEntries[0]) {
                    result = DateTimeFormatter.ofPattern("MMM").format(date) + "\n" + date.getYear();
                    break;
                }

                if (date.getDayOfMonth() >= 1 && date.getDayOfMonth() <= 7) {
                    result = DateTimeFormatter.ofPattern("MMM").format(date);
                    break;
                }

                result = String.valueOf(date.getDayOfMonth());
                break;
            }
            case MONTHS: {
                result = DateTimeFormatter.ofPattern("MMM").format(date);

                if (date.getMonthValue() == 1 || value == axis.mEntries[0]) {
                    result += "\n" + date.getYear();
                }
                break;
            }
        }
        return result;
    }
}
