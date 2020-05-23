package com.wentura.focus.statistics;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.wentura.focus.Utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

class CustomXAxisFormatter extends ValueFormatter {
    private List<StatisticsItem> statisticsItems;
    private SpinnerOption spinnerOption;

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
