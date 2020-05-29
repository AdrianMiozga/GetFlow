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

import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.wentura.focus.Constants;
import com.wentura.focus.R;
import com.wentura.focus.Utility;
import com.wentura.focus.database.Database;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class StatisticsActivity extends AppCompatActivity {
    private static final String TAG = StatisticsActivity.class.getSimpleName();
    private Database database;
    private static List<StatisticsItem> data = new ArrayList<>();
    private TextView numberTodayTextView;
    private TextView numberWeekTextView;
    private TextView numberMonthTextView;
    private TextView numberTotalTextView;
    private static HistoryChart historyChart;
    private static int currentSelectedIndex;
    private static ChartData monthData;
    private static ChartData dayData;
    private Spinner spinner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        spinner = findViewById(R.id.spinner);
        numberTodayTextView = findViewById(R.id.numberTodayTextView);
        numberWeekTextView = findViewById(R.id.numberWeekTextView);
        numberMonthTextView = findViewById(R.id.numberMonthTextView);
        numberTotalTextView = findViewById(R.id.numberTotalTextView);

        database = Database.getInstance(this);

        setChartNoDataText();
        setupSpinner();

        new LoadFromDatabase(this).execute();
    }

    private void setChartNoDataText() {
        LineChart lineChart = findViewById(R.id.history_chart);
        Paint paint = lineChart.getPaint(Chart.PAINT_INFO);
        paint.setColor(getResources().getColor(R.color.white));
        lineChart.setNoDataText(getString(R.string.loading_chart));
        lineChart.invalidate();
    }

    private void setupSpinner() {
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

                if (position == 1) {
                    historyChart.displayData(monthData);
                } else {
                    historyChart.displayData(dayData);
                }

                Log.d(TAG, "onItemSelected: " + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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

            data = statisticsActivity.database.pomodoroDao().getAll();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
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

            monthData = new MonthData(data);
            monthData.generate();

            dayData = new DayData(data);
            dayData.generate();

            historyChart = new HistoryChart(statisticsActivity);
            historyChart.setupChart();

            if (currentSelectedIndex == 1) {
                historyChart.displayData(monthData);
            } else {
                historyChart.displayData(dayData);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.background_down, R.anim.foreground_down);
    }
}