package com.wentura.pomodoro.statistics;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.wentura.pomodoro.Constants;
import com.wentura.pomodoro.R;
import com.wentura.pomodoro.Utility;
import com.wentura.pomodoro.database.Database;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.wentura.pomodoro.statistics.SpinnerOption.DAYS;
import static com.wentura.pomodoro.statistics.SpinnerOption.MONTHS;

public class StatisticsActivity extends AppCompatActivity {
    public static final String datePattern = "yyyy-MM-dd";
    private Database database;
    private static final String TAG = "Help";
    private static List<StatisticsItem> days = new ArrayList<>();
    private static List<StatisticsItem> months = new ArrayList<>();
    private static List<Entry> entries = new ArrayList<>();
    private TextView numberTodayTextView;
    private TextView numberWeekTextView;
    private TextView numberMonthTextView;
    private TextView numberTotalTextView;
    private LineChart chart;
    private static int currentSelectedIndex;

    private static void updateChartData(LineChart lineChart, Context context, int position) {
        entries.clear();

        XAxis xAxis = lineChart.getXAxis();

        long maxValue = 0;
        if (position == 1) {
            for (int i = 0; i < months.size(); i++) {
                long totalTime = months.get(i).getCompletedWorkTime() + months.get(i).getIncompleteWorkTime();

                entries.add(new Entry(i, totalTime));

                if (totalTime > maxValue) {
                    maxValue = totalTime;
                }
            }

            xAxis.setValueFormatter(new CustomXAxisFormatter(months, MONTHS));
        } else {
            for (int i = 0; i < days.size(); i++) {
                long totalTime = days.get(i).getCompletedWorkTime() + days.get(i).getIncompleteWorkTime();

                entries.add(new Entry(i, totalTime));

                if (totalTime > maxValue) {
                    maxValue = totalTime;
                }
            }

            xAxis.setValueFormatter(new CustomXAxisFormatter(days, DAYS));
        }

        LineDataSet lineDataSet = new LineDataSet(entries, null);
        lineDataSet.setColors(context.getResources().getColor(R.color.colorPrimary));
        lineDataSet.setCircleColor(context.getResources().getColor(R.color.colorPrimary));
        lineDataSet.setLineWidth(2f);
        lineDataSet.setCircleRadius(3.5f);
        lineDataSet.setDrawValues(false);
        lineDataSet.setDrawCircleHole(false);

        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);

        YAxis yAxis = lineChart.getAxisLeft();

        if (maxValue <= 3_600_000) {
            yAxis.setAxisMaximum(3_600_000);
            yAxis.setAxisMinimum(-50000f);
        } else {
            double toHours = maxValue / 3_600_000d;
            double result =
                    Math.ceil(toHours / 6) * 6;
            double toMilliseconds = result * 3_600_000;
            yAxis.setAxisMaximum((float) toMilliseconds);
            yAxis.setAxisMinimum(-100000f);
        }

        lineChart.setVisibleXRange(11f, 11f);
        lineChart.moveViewToX(entries.size());
    }

    private static void setupChart(StatisticsActivity statisticsActivity) {
        LineChart chart = statisticsActivity.findViewById(R.id.history_chart);

        YAxis yAxis = chart.getAxisLeft();
        yAxis.setDrawAxisLine(false);
        yAxis.setValueFormatter(new CustomYAxisFormatter());
        yAxis.setLabelCount(7, true);
        yAxis.setGridColor(statisticsActivity.getResources().getColor(R.color.grey));
        yAxis.setTextColor(statisticsActivity.getResources().getColor(R.color.white));

        XAxis xAxis = chart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelCount(11);
        xAxis.setTextColor(statisticsActivity.getResources().getColor(R.color.white));
        xAxis.setSpaceMax(0.1f);

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

        updateChartData(chart, statisticsActivity.getApplicationContext(), currentSelectedIndex);
    }

    private static void prepareMonthsForChart() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern, Locale.US);

        // The loop below this condition works only when the months array has two entries. I'm
        // using this block of code to generate the second entry if it doesn't exist.
        if (months.size() == 1) {
            Date thisMonth = stringToDate(months.get(0).getDate());
            Date currentMonth = stringToDate(Utility.getCurrentDate());

            if (thisMonth != null && currentMonth != null) {
                Calendar thisCalendar = Calendar.getInstance();
                thisCalendar.setTime(thisMonth);

                Calendar currentCalendar = Calendar.getInstance();
                currentCalendar.setTime(currentMonth);

                if (thisCalendar.get(Calendar.MONTH) != currentCalendar.get(Calendar.MONTH) || thisCalendar.get(Calendar.YEAR) != currentCalendar.get(Calendar.YEAR)) {
                    String date = calendarToString(currentCalendar);

                    months.add(new StatisticsItem(date, 0, 0, 0, 0, 0, 0));
                }
            }
        }

        for (int i = 0; i < months.size() - 1; i++) {
            if (i == 200) {
                break;
            }
            Date thisMonth = stringToDate(months.get(i).getDate());
            Date nextMonth = stringToDate(months.get(i + 1).getDate());

            if (nextMonth == null || thisMonth == null) {
                continue;
            }

            Calendar nextCalendar = Calendar.getInstance();
            nextCalendar.setTime(nextMonth);

            Calendar thisCalendar = Calendar.getInstance();
            thisCalendar.setTime(thisMonth);
            thisCalendar.add(Calendar.MONTH, 1);

            String insertDate;

            if (nextCalendar.get(Calendar.MONTH) != thisCalendar.get(Calendar.MONTH) ||
                    nextCalendar.get(Calendar.YEAR) != thisCalendar.get(Calendar.YEAR)) {
                insertDate = dateFormat.format(thisCalendar.getTime());
                months.add(i + 1, new StatisticsItem(insertDate,
                        0, 0, 0, 0, 0, 0));
                continue;
            }

            if (i + 1 == months.size() - 1 &&
                    !new SimpleDateFormat("MMMM", Locale.US).format(nextCalendar.getTime()).equals(Utility.getCurrentMonth())) {
                thisCalendar.add(Calendar.MONTH, 1);
                insertDate = dateFormat.format(thisCalendar.getTime());

                months.add(new StatisticsItem(insertDate,
                        0, 0, 0, 0, 0, 0));
            }
        }

        if (months.size() == 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, -11);

            for (int i = 0; i < 12; i++) {
                months.add(new StatisticsItem(dateFormat.format(calendar.getTime()), 0, 0, 0, 0, 0,
                        0));

                calendar.add(Calendar.MONTH, 1);
            }
            return;
        }

        if (months.size() < 12) {
            Date firstMonth = stringToDate(months.get(0).getDate());

            if (firstMonth == null) {
                return;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(firstMonth);

            for (int i = 12 - months.size(); i > 0; i--) {
                calendar.add(Calendar.MONTH, -1);

                months.add(0, new StatisticsItem(dateFormat.format(calendar.getTime()), 0, 0, 0, 0, 0, 0));
            }
        }
    }

    private static void createMonthsArray() {
        int totalCompletedTime = 0;
        int totalIncompleteTime = 0;

        for (int i = 0; i < days.size() - 1; i++) {
            Date todayDate = stringToDate(days.get(i).getDate());
            Date nextDate = stringToDate(days.get(i + 1).getDate());

            if (nextDate == null || todayDate == null) {
                continue;
            }

            Calendar today = Calendar.getInstance();
            today.setTime(todayDate);

            Calendar nextDay = Calendar.getInstance();
            nextDay.setTime(nextDate);

            totalCompletedTime += days.get(i).getCompletedWorkTime();
            totalIncompleteTime += days.get(i).getIncompleteWorkTime();

            if (today.get(Calendar.MONTH) != nextDay.get(Calendar.MONTH) ||
                    today.get(Calendar.YEAR) != nextDay.get(Calendar.YEAR)) {
                String stringDate = calendarToString(today);

                months.add(new StatisticsItem(stringDate, 0, totalCompletedTime, 0,
                        totalIncompleteTime, 0, 0));

                totalCompletedTime = 0;
                totalIncompleteTime = 0;
            }

            if (today.get(Calendar.MONTH) == nextDay.get(Calendar.MONTH) &&
                    today.get(Calendar.YEAR) == nextDay.get(Calendar.YEAR) &&
                    i == days.size() - 2) {

                totalCompletedTime += days.get(i + 1).getCompletedWorkTime();
                totalIncompleteTime += days.get(i + 1).getIncompleteWorkTime();

                String stringDate = calendarToString(nextDay);

                months.add(new StatisticsItem(stringDate, 0, totalCompletedTime, 0,
                        totalIncompleteTime, 0, 0));
            }
        }

        if (days.size() == 1) {
            Date todayDate = stringToDate(days.get(0).getDate());

            if (todayDate == null) {
                return;
            }

            Calendar todayCalendar = Calendar.getInstance();
            todayCalendar.setTime(todayDate);

            String stringDate = calendarToString(todayCalendar);

            months.add(new StatisticsItem(stringDate, 0, days.get(0).getCompletedWorkTime(), 0,
                    days.get(0).getIncompleteWorkTime(), 0, 0));
        }
    }

    private static String calendarToString(Calendar calendar) {
        StringBuilder result = new StringBuilder();

        result.append(calendar.get(Calendar.YEAR));
        result.append("-");

        if (calendar.get(Calendar.MONTH) + 1 < 10) {
            result.append("0");
        }

        result.append((calendar.get(Calendar.MONTH) + 1));
        result.append("-01");
        return result.toString();
    }

    private static Date stringToDate(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(StatisticsActivity.datePattern, Locale.US);

        try {
            return dateFormat.parse(date);
        } catch (ParseException parseException) {
            parseException.printStackTrace();
            return null;
        }
    }

    private static void prepareDaysForChart() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern, Locale.US);

        if (days.size() == 1) {
            if (!days.get(0).getDate().equals(Utility.getCurrentDate())) {
                days.add(new StatisticsItem(Utility.getCurrentDate(), 0,
                        0, 0, 0, 0, 0));
            }
        }

        for (int i = 0; i < days.size() - 1; i++) {
            Date todayDate = stringToDate(days.get(i).getDate());
            Date nextDate = stringToDate(days.get(i + 1).getDate());

            if (nextDate == null || todayDate == null) {
                continue;
            }

            Calendar calendarNext = Calendar.getInstance();
            calendarNext.setTime(nextDate);

            Calendar todayCalendar = Calendar.getInstance();
            todayCalendar.setTime(todayDate);
            todayCalendar.add(Calendar.DATE, 1);

            String insertDate;

            if (calendarNext.compareTo(todayCalendar) != 0) {
                insertDate = dateFormat.format(todayCalendar.getTime());
                days.add(i + 1, new StatisticsItem(insertDate,
                        0, 0, 0, 0, 0, 0));
                continue;
            }

            if (i + 1 == days.size() - 1 && !days.get(i + 1).getDate().equals(Utility.getCurrentDate())) {
                todayCalendar.add(Calendar.DATE, 1);
                insertDate = dateFormat.format(todayCalendar.getTime());

                days.add(new StatisticsItem(insertDate,
                        0, 0, 0, 0, 0, 0));
            }
        }

        if (days.size() == 0) {
            days.add(new StatisticsItem(Utility.getCurrentDate(), 0,
                    0, 0, 0, 0, 0));
        }

        if (days.size() < 12) {
            Date firstDay = stringToDate(days.get(0).getDate());

            if (firstDay == null) {
                return;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(firstDay);

            for (int i = 12 - days.size(); i > 0; i--) {
                calendar.add(Calendar.DATE, -1);

                days.add(0, new StatisticsItem(dateFormat.format(calendar.getTime()), 0, 0, 0, 0, 0, 0));
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        Spinner spinner = findViewById(R.id.spinner);
        numberTodayTextView = findViewById(R.id.numberTodayTextView);
        numberWeekTextView = findViewById(R.id.numberWeekTextView);
        numberMonthTextView = findViewById(R.id.numberMonthTextView);
        numberTotalTextView = findViewById(R.id.numberTotalTextView);
        chart = findViewById(R.id.history_chart);

        Paint paint = chart.getPaint(Chart.PAINT_INFO);
        paint.setColor(getResources().getColor(R.color.white));
        chart.setNoDataText(getString(R.string.loading_chart));
        chart.invalidate();

        database = Database.getInstance(this);

        days.clear();
        months.clear();
        entries.clear();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.history_spinner, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);

        currentSelectedIndex = sharedPreferences.getInt(Constants.SPINNER_SETTING, 1);

        spinner.setAdapter(adapter);
        spinner.setSelection(currentSelectedIndex);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (currentSelectedIndex == position) {
                    return;
                }

                currentSelectedIndex = position;

                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.putInt(Constants.SPINNER_SETTING, position).apply();

                updateChartData(chart, getApplicationContext(), position);

                Log.d(TAG, "onItemSelected: " + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        new LoadFromDatabase(this).execute();
    }

    private static class LoadFromDatabase extends AsyncTask<Void, Void, Void> {
        StatisticsItem statisticsItemToday;
        List<StatisticsItem> statisticsItemsWeek;
        List<StatisticsItem> statisticsItemsMonth;
        List<StatisticsItem> statisticsItemsTotal;

        private WeakReference<StatisticsActivity> activityWeakReference;

        LoadFromDatabase(StatisticsActivity context) {
            this.activityWeakReference = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            StatisticsActivity statisticsActivity = activityWeakReference.get();

            if (statisticsActivity == null || statisticsActivity.isFinishing()) {
                return null;
            }

            statisticsItemToday = statisticsActivity.database.pomodoroDao().getAll(Utility.getCurrentDate());

            statisticsItemsWeek =
                    statisticsActivity.database.pomodoroDao().getAllDatesBetween(Utility.subtractDaysFromCurrentDate(6),
                            Utility.subtractDaysFromCurrentDate(1));

            statisticsItemsMonth =
                    statisticsActivity.database.pomodoroDao().getAllDatesBetween(Utility.subtractDaysFromCurrentDate(29),
                            Utility.subtractDaysFromCurrentDate(7));

            statisticsItemsTotal =
                    statisticsActivity.database.pomodoroDao().getAllDateLess(Utility.subtractDaysFromCurrentDate(29));

            days = statisticsActivity.database.pomodoroDao().getAll();

            // 50m
//            days.add(new StatisticsItem(Utility.getCurrentDate(), 3, 3000000, 0, 0, 0, 0));

            // 8h
//            days.add(new StatisticsItem(Utility.getCurrentDate(), 3, 28800000, 0, 0, 0, 0));

            // 70h
//            days.add(new StatisticsItem(Utility.getCurrentDate(), 3, 252000000, 0, 0, 0, 0));

            // 160h
//            days.add(new StatisticsItem(Utility.getCurrentDate(), 3, 576000000, 0, 0, 0, 0));

            // 2h
//            days.add(new StatisticsItem(Utility.getCurrentDate(), 3, 7200000, 0, 0, 0, 0));

            // 1h
//            days.add(new StatisticsItem(Utility.getCurrentDate(), 3, 3600000, 0, 0, 0, 0));

            // Yesterday
//            days.add(new StatisticsItem("2020-03-18", 3, 3000000, 0, 0, 0, 0));

            // year, two months old
//            days.add(new StatisticsItem("2019-01-19", 3, 3000000, 0, 0, 0, 0));

            // Month old
//            days.add(new StatisticsItem("2020-02-19", 3, 3000000, 0, 0, 0, 0));

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
//            Log.d(TAG, "=============days:");
//            for (StatisticsItem statisticsItem : days) {
//                Log.d(TAG, statisticsItem.getDate());
//            }
//            Log.d(TAG, "onPostExecute: ");

            StatisticsActivity statisticsActivity = activityWeakReference.get();

            if (statisticsActivity == null || statisticsActivity.isFinishing()) {
                return;
            }

            int timeToday = 0;

            if (statisticsItemToday != null) {
                timeToday =
                        statisticsItemToday.getCompletedWorkTime() + statisticsItemToday.getIncompleteWorkTime();
            }

            int timeWeek = timeToday;

            for (StatisticsItem statisticsItem : statisticsItemsWeek) {
                timeWeek += statisticsItem.getCompletedWorkTime();
                timeWeek += statisticsItem.getIncompleteWorkTime();
            }

            int timeMonth = timeWeek;

            for (StatisticsItem statisticsItem : statisticsItemsMonth) {
                timeMonth += statisticsItem.getCompletedWorkTime();
                timeMonth += statisticsItem.getIncompleteWorkTime();
            }

            int timeTotal = timeMonth;

            for (StatisticsItem statisticsItem : statisticsItemsTotal) {
                timeTotal += statisticsItem.getCompletedWorkTime();
                timeTotal += statisticsItem.getIncompleteWorkTime();
            }

            statisticsActivity.numberTodayTextView.setText(Utility.formatStatisticsTime(timeToday));
            statisticsActivity.numberWeekTextView.setText(Utility.formatStatisticsTime(timeWeek));
            statisticsActivity.numberMonthTextView.setText(Utility.formatStatisticsTime(timeMonth));
            statisticsActivity.numberTotalTextView.setText(Utility.formatStatisticsTime(timeTotal));

            createMonthsArray();

//            for (StatisticsItem statisticsItem : months) {
//                Log.d(TAG, "After createMonthsArray(): " + statisticsItem.getDate());
//            }

            prepareMonthsForChart();

//            for (StatisticsItem statisticsItem : months) {
//                Log.d(TAG, "After prepareMonthsForChart(): " + statisticsItem.getDate());
//            }

            prepareDaysForChart();

//            for (StatisticsItem statisticsItem : days) {
//                Log.d(TAG, "onPostExecute: " + statisticsItem.getDate());
//            }

            setupChart(statisticsActivity);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.background_down, R.anim.foreground_down);
    }
}