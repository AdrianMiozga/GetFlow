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

import android.content.Context;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.wentura.getflow.R;

public class HistoryChart {

    private final LineChart chart;
    private final Context context;
    private final XAxis xAxis;
    private final YAxis yAxis;

    public HistoryChart(Context context, LineChart lineChart) {
        this.chart = lineChart;
        this.context = context;

        xAxis = chart.getXAxis();
        yAxis = chart.getAxisLeft();
    }

    public void setupChart() {
        setupXAxis();
        setupYAxis();
        setupChartLook();
    }

    private void setupXAxis() {
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(11);
        xAxis.setTextColor(context.getResources().getColor(R.color.white));
        xAxis.setSpaceMax(0.1f);
    }

    private void setupYAxis() {
        yAxis.setDrawAxisLine(false);
        yAxis.setValueFormatter(new CustomYAxisFormatter());
        yAxis.setLabelCount(7, true);
        yAxis.setGridColor(context.getResources().getColor(R.color.grey));
        yAxis.setTextColor(context.getResources().getColor(R.color.white));
    }

    private void setupChartLook() {
        chart.setExtraBottomOffset(20f);
        chart.setXAxisRenderer(new CustomXAxisRenderer(chart.getViewPortHandler(), xAxis,
                chart.getTransformer(YAxis.AxisDependency.LEFT)));
        chart.getAxisRight().setEnabled(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setScaleEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setHighlightPerTapEnabled(false);
        chart.setHighlightPerDragEnabled(false);
        chart.getDescription().setEnabled(false);
    }

    public void displayData(ChartData chartData) {
        chart.setData(new LineData(setupDataSet(chartData)));

        setYAxisRange(chartData);
        setXAxisFormatter(chartData);

        chart.setVisibleXRange(11f, 11f);
        chart.moveViewToX(chartData.getSize());
    }

    private LineDataSet setupDataSet(ChartData chartData) {
        LineDataSet dataSet = new LineDataSet(chartData.getEntries(), null);
        dataSet.setColors(context.getResources().getColor(R.color.colorPrimary));
        dataSet.setCircleColor(context.getResources().getColor(R.color.colorPrimary));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(3.5f);
        dataSet.setDrawValues(false);
        dataSet.setDrawCircleHole(false);
        return dataSet;
    }

    private void setYAxisRange(ChartData chartData) {
        long maxValue = chartData.getMaxValue();

        if (maxValue <= 3_600_000f) {
            yAxis.setAxisMaximum(3_600_000f);
            yAxis.setAxisMinimum(-50_000f);
        } else {
            yAxis.setAxisMaximum((float) calculateYMax(maxValue));
            yAxis.setAxisMinimum(-100_000f);
        }
    }

    private double calculateYMax(long maxValue) {
        double toHours = maxValue / 3_600_000d;
        double result = Math.ceil(toHours / 6) * 6;
        return result * 3_600_000;
    }

    private void setXAxisFormatter(ChartData chartData) {
        // TODO: 05.09.2020 Improve
        if (chartData instanceof DayData) {
            xAxis.setValueFormatter(new CustomXAxisFormatter(chartData.getGeneratedData(), SpinnerOption.DAYS));
        } else if (chartData instanceof WeekData) {
            xAxis.setValueFormatter(new CustomXAxisFormatter(chartData.getGeneratedData(), SpinnerOption.WEEKS));
        } else {
            xAxis.setValueFormatter(new CustomXAxisFormatter(chartData.getGeneratedData(), SpinnerOption.MONTHS));
        }
    }
}
