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

package com.wentura.focus.statistics;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.wentura.focus.Utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

final class CustomXAxisFormatter extends ValueFormatter {
    private final List<StatisticsItem> statisticsItems;
    private final SpinnerOption spinnerOption;

    CustomXAxisFormatter(List<StatisticsItem> statisticsItems, SpinnerOption spinnerOption) {
        this.statisticsItems = statisticsItems;
        this.spinnerOption = spinnerOption;
    }

    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        if (value >= statisticsItems.size()) {
            return "";
        }

        String date = statisticsItems.get((int) value).getDate();
        SimpleDateFormat fromPattern = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        String result = "";

        switch (spinnerOption) {
            case DAYS: {
                SimpleDateFormat toPattern = new SimpleDateFormat("d", Locale.US);
                SimpleDateFormat month = new SimpleDateFormat("MMM", Locale.getDefault());
                SimpleDateFormat year = new SimpleDateFormat("yyyy", Locale.US);

                Date parse = null;

                try {
                    parse = fromPattern.parse(date);
                } catch (ParseException parseException) {
                    parseException.printStackTrace();
                }

                if (parse == null) {
                    return result;
                }

                result = toPattern.format(parse);

                if (result.equals("1")) {
                    result = month.format(parse);
                }

                if (result.equals(Utility.getFirstMonthOfTheYear(0)) || value == axis.mEntries[0]) {
                    result = month.format(parse) + "\n" + year.format(parse);
                }

                break;
            }
            case MONTHS: {
                SimpleDateFormat toPattern = new SimpleDateFormat("MMM", Locale.getDefault());
                SimpleDateFormat year = new SimpleDateFormat("yyyy", Locale.US);

                Date parse = null;

                try {
                    parse = fromPattern.parse(date);
                } catch (ParseException parseException) {
                    parseException.printStackTrace();
                }

                if (parse == null) {
                    return result;
                }

                result = toPattern.format(parse);

                if (result.equals(Utility.getFirstMonthOfTheYear(0)) || value == axis.mEntries[0]) {
                    result += "\n" + year.format(parse);
                }
                break;
            }
        }
        return result;
    }
}
