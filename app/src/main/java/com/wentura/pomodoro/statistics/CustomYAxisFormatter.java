package com.wentura.pomodoro.statistics;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

public class CustomYAxisFormatter extends ValueFormatter {
    CustomYAxisFormatter() {
    }

    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        String result;

        if (value <= 0) {
            return "";
        }

        if (axis.mAxisMaximum > 3_600_000) {
            result = (int) Math.ceil(value / 3_600_000) + "h";
        } else {
            result = (int) value / 60_000 + "m";
        }

        return result;
    }
}
