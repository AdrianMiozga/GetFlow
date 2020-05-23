package com.wentura.focus.statistics;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.wentura.focus.R;

import static com.wentura.focus.statistics.SpinnerOption.DAYS;
import static com.wentura.focus.statistics.SpinnerOption.MONTHS;

class HistoryChart {
    private LineChart chart;
    private StatisticsActivity statisticsActivity;
    private XAxis xAxis;
    private YAxis yAxis;

    HistoryChart(StatisticsActivity statisticsActivity) {
        this.statisticsActivity = statisticsActivity;
        chart = statisticsActivity.findViewById(R.id.history_chart);

        xAxis = chart.getXAxis();
        yAxis = chart.getAxisLeft();
    }

    void setupChart() {
        setupXAxis();
        setupYAxis();
        setupChartLook();
    }

    private void setupXAxis() {
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(11);
        xAxis.setTextColor(statisticsActivity.getResources().getColor(R.color.white));
        xAxis.setSpaceMax(0.1f);
    }

    private void setupYAxis() {
        yAxis.setDrawAxisLine(false);
        yAxis.setValueFormatter(new CustomYAxisFormatter());
        yAxis.setLabelCount(7, true);
        yAxis.setGridColor(statisticsActivity.getResources().getColor(R.color.grey));
        yAxis.setTextColor(statisticsActivity.getResources().getColor(R.color.white));
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

    void displayData(ChartData chartData) {
        chart.setData(new LineData(setupDataSet(chartData)));

        setYAxisRange(chartData);
        setXAxisFormatter(chartData);

        chart.setVisibleXRange(11f, 11f);
        chart.moveViewToX(chartData.getSize());
    }

    private LineDataSet setupDataSet(ChartData chartData) {
        LineDataSet dataSet = new LineDataSet(chartData.getEntries(), null);
        dataSet.setColors(statisticsActivity.getResources().getColor(R.color.colorPrimary));
        dataSet.setCircleColor(statisticsActivity.getResources().getColor(R.color.colorPrimary));
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
        if (chartData instanceof DayData) {
            xAxis.setValueFormatter(new CustomXAxisFormatter(chartData.getGeneratedData(), DAYS));
        } else {
            xAxis.setValueFormatter(new CustomXAxisFormatter(chartData.getGeneratedData(), MONTHS));
        }
    }
}
