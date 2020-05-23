package com.wentura.focus.statistics;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

class CustomYAxisFormatter extends ValueFormatter {

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
