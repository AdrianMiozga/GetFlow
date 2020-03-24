package com.wentura.pomodoro.statistics;

import android.util.Log;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;


public class CustomYAxisFormatter extends ValueFormatter {
    private static final String TAG = "Hello";

    CustomYAxisFormatter() {
    }

    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        String result;

        if (value <= 0) {
            return "";
        }

        Log.d(TAG, "getAxisLabel: = " + value / 3_600_000);

        if (axis.mAxisMaximum > 3_600_000) {
            result = Math.round(value / 3_600_000) + "h";
        } else {
            result = (int) Math.ceil(value / 60_000) + "m";
        }

        return result;
    }
}
