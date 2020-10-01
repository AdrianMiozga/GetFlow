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

package org.wentura.getflow.statistics;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.wentura.getflow.Constants;
import org.wentura.getflow.R;
import org.wentura.getflow.Utility;
import org.wentura.getflow.database.Database;
import org.wentura.getflow.statistics.activitychart.ActivityPieChartRenderer;
import org.wentura.getflow.statistics.activitychart.CustomPercentFormatter;
import org.wentura.getflow.statistics.activitychart.LabelElement;
import org.wentura.getflow.statistics.activitychart.LegendAdapter;
import org.wentura.getflow.statistics.activitychart.LegendItem;
import org.wentura.getflow.statistics.activitychart.PieChartItem;
import org.wentura.getflow.statistics.historychart.ChartData;
import org.wentura.getflow.statistics.historychart.DayData;
import org.wentura.getflow.statistics.historychart.HistoryChart;
import org.wentura.getflow.statistics.historychart.HistoryChartItem;
import org.wentura.getflow.statistics.historychart.MonthData;
import org.wentura.getflow.statistics.historychart.WeekData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class StatisticsActivity extends AppCompatActivity {

    private static int historySpinnerCurrentSelectedIndex;
    private static int activitiesSpinnerCurrentSelectedIndex;
    private static List<HistoryChartItem> data = new ArrayList<>();
    private static PieChart pieChart;
    private static ChartData monthData;
    private static ChartData dayData;
    private static ChartData weekData;
    private boolean shouldScrollDown;
    private HistoryChart historyChart;
    private Database database;
    private PieData pieData;
    private List<LegendItem> legendItems;
    boolean isActivitySpinnerSelectionFromTouch = false;

    // Views
    private LineChart historyChartView;
    private TextView numberTodayTextView;
    private TextView numberWeekTextView;
    private TextView numberMonthTextView;
    private TextView numberTotalTextView;
    private Spinner historySpinner;
    private Spinner activitiesSpinner;
    private RecyclerView legendRecyclerView;
    private TextView othersTextView;
    private ScrollView scrollView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        historySpinner = findViewById(R.id.history_spinner);
        activitiesSpinner = findViewById(R.id.activities_spinner);
        numberTodayTextView = findViewById(R.id.number_today_text_view);
        numberWeekTextView = findViewById(R.id.number_week_text_view);
        numberMonthTextView = findViewById(R.id.number_month_text_view);
        numberTotalTextView = findViewById(R.id.number_total_text_view);
        historyChartView = findViewById(R.id.history_chart);

        scrollView = findViewById(R.id.statistics_scroll_view);
        scrollView.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

        legendRecyclerView = findViewById(R.id.legend_recycler_view);
        legendRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        legendRecyclerView.setHasFixedSize(true);

        othersTextView = findViewById(R.id.others_text_view);

        pieChart = findViewById(R.id.pie_chart);

        database = Database.getInstance(this);

        setupPieChartLook();

        setHistoryChartNoDataText();
        setupHistorySpinner();
        setupActivitiesSpinner();

        loadFromDatabase();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.statistics_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.select_activities) {
            Future<List<LabelElement>> labelElementsFuture =
                    Database.databaseExecutor.submit(() -> database.activityDao().getAllActivityNames());

            Future<boolean[]> checkedItemsFuture =
                    Database.databaseExecutor.submit(() -> database.activityDao().showInStatisticsAll());

            try {
                List<LabelElement> labelElements = labelElementsFuture.get();
                boolean[] checkedItems = checkedItemsFuture.get();

                String[] activities = new String[labelElements.size()];

                for (int i = 0; i < labelElements.size(); i++) {
                    activities[i] = labelElements.get(i).getName();
                }

                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);

                Set<Integer> values = new HashSet<>();

                builder.setTitle(R.string.select_activities_dialog_title)
                        .setMultiChoiceItems(activities, checkedItems,
                                (dialog, which, isChecked) -> checkedItems[which] = isChecked)

                        .setPositiveButton(R.string.select_activities_positive_button, (dialog, id) -> {
                            for (int i = 0; i < checkedItems.length; i++) {
                                if (checkedItems[i]) {
                                    values.add(labelElements.get(i).getID());
                                }
                            }
                            Database.databaseExecutor.execute(() ->
                                    database.activityDao().updateShowInStatistics(values));
                            loadFromDatabase();
                        })
                        .setNegativeButton(R.string.select_activities_negative_button, (dialog, id) -> {

                        });
                builder.show();
                return true;
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void setupPieChartLook() {
        pieData = createActivityPieData(new ArrayList<>());

        pieChart.setRenderer(new ActivityPieChartRenderer(pieChart));

        // Hole
        pieChart.setHoleColor(Color.BLACK);
        pieChart.setTransparentCircleRadius(0f);

        // Offsets
        pieChart.setExtraTopOffset(35f);
        pieChart.setExtraBottomOffset(45f);

        // Text Formatting
        pieChart.setEntryLabelTextSize(14f);

        // No Data Text
        pieChart.setNoDataText(getString(R.string.activity_pie_chart_no_data));
        pieChart.setNoDataTextColor(Color.WHITE);

        // Miscellaneous
        pieChart.setRotationEnabled(false);
        pieChart.setTouchEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setMinAngleForSlices(1f);
        pieChart.setUsePercentValues(true);
        pieChart.setCenterTextColor(Color.WHITE);
        pieChart.setData(pieData);
    }

    private PieData createActivityPieData(List<PieEntry> entries) {
        PieDataSet dataSet = new PieDataSet(entries, null);

        // Colors
        List<Integer> colors = new ArrayList<>();
        colors.add(getResources().getColor(R.color.pie_chart_1));
        colors.add(getResources().getColor(R.color.pie_chart_2));
        colors.add(getResources().getColor(R.color.pie_chart_3));
        colors.add(getResources().getColor(R.color.pie_chart_4));
        colors.add(getResources().getColor(R.color.pie_chart_5));
        colors.add(getResources().getColor(R.color.pie_chart_6));

        dataSet.setColors(colors);
        dataSet.setValueTextColors(colors);
        dataSet.setValueLineColor(Color.WHITE);

        // Activity Name Position
        dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        // Line Lengths
        dataSet.setValueLineWidth(2f);
        dataSet.setValueLinePart1Length(0.8f);

        PieData pieData = new PieData(dataSet);

        // Text Formatting
        pieData.setValueTextSize(12f);
        pieData.setValueFormatter(new CustomPercentFormatter());

        return pieData;
    }

    /**
     * Put activities below {@link Constants#CLUMP_ACTIVITIES_BELOW_THIS_PERCENT} into one activity for chart
     * readability. Also, set percentages on {@link PieChartItem}. Activities are sorted from highest to lowest
     * percent with the Others activity put in the middle.
     *
     * @param activities to prepare
     * @return prepared activities
     */
    private List<PieChartItem> prepareActivities(List<PieChartItem> activities) {
        if (activities.size() == 0) {
            return activities;
        }

        double totalTime = 0;
        double othersTime = 0;
        List<PieChartItem> result = new ArrayList<>(activities.size());

        for (PieChartItem pieChartItem : activities) {
            totalTime += pieChartItem.getTotalTime();
        }

        for (PieChartItem pieChartItem : activities) {
            double percent = pieChartItem.getTotalTime() / totalTime * 100;
            pieChartItem.setPercent(percent);
        }

        Collections.sort(activities, Comparator.comparing(PieChartItem::getPercent).reversed());

        int fullActivitiesAdded = 0;
        for (PieChartItem pieChartItem : activities) {
            if (activities.size() <= Constants.MAX_ITEMS_TO_SHOW_ON_ACTIVITY_CHART) {
                if (pieChartItem.getPercent() > Constants.CLUMP_ACTIVITIES_BELOW_THIS_PERCENT &&
                        ++fullActivitiesAdded <= Constants.MAX_ITEMS_TO_SHOW_ON_ACTIVITY_CHART) {
                    result.add(pieChartItem);
                } else {
                    othersTime += pieChartItem.getTotalTime();
                }
            } else {
                if (pieChartItem.getPercent() > Constants.CLUMP_ACTIVITIES_BELOW_THIS_PERCENT &&
                        ++fullActivitiesAdded < Constants.MAX_ITEMS_TO_SHOW_ON_ACTIVITY_CHART) {
                    result.add(pieChartItem);
                } else {
                    othersTime += pieChartItem.getTotalTime();
                }
            }
        }

        if (othersTime > 0) {
            if (result.size() > 1) {
                result.add(result.size() / 2, new PieChartItem((int) othersTime, othersTime / totalTime * 100, ""));
            } else {
                result.add(new PieChartItem((int) othersTime, othersTime / totalTime * 100, ""));
            }
        }
        return result;
    }

    private void setHistoryChartNoDataText() {
        Paint paint = historyChartView.getPaint(Chart.PAINT_INFO);
        paint.setColor(Color.WHITE);
        historyChartView.setNoDataText(getString(R.string.loading_chart));
        historyChartView.invalidate();
    }

    private void setupHistorySpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.history_spinner, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);

        historySpinnerCurrentSelectedIndex = sharedPreferences.getInt(Constants.HISTORY_SPINNER_SETTING, 1);

        historySpinner.setAdapter(adapter);
        historySpinner.setSelection(historySpinnerCurrentSelectedIndex);
        historySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (historySpinnerCurrentSelectedIndex == position) {
                    return;
                }

                historySpinnerCurrentSelectedIndex = position;

                sharedPreferences.edit().putInt(Constants.HISTORY_SPINNER_SETTING, position).apply();

                if (position == 0) {
                    historyChart.displayData(dayData);
                } else if (position == 1) {
                    historyChart.displayData(weekData);
                } else {
                    historyChart.displayData(monthData);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public class ActivitySpinnerInteractionListener
            implements AdapterView.OnItemSelectedListener, View.OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            isActivitySpinnerSelectionFromTouch = true;
            view.performClick();
            return false;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            final SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(StatisticsActivity.this);

            activitiesSpinnerCurrentSelectedIndex = position;

            sharedPreferences.edit().putInt(Constants.ACTIVITIES_SPINNER_SETTING, position).apply();

            Database.databaseExecutor.execute(() -> {
                List<PieChartItem> pieChartItems;

                switch (position) {
                    case 0:
                        pieChartItems = database.pomodoroDao().getPieChartItems(LocalDate.now().toString());
                        break;
                    case 1:
                        pieChartItems =
                                database.pomodoroDao().getPieChartItems(LocalDate.now().minusDays(6).toString(),
                                        LocalDate.now().toString());
                        break;
                    case 2:
                        pieChartItems =
                                database.pomodoroDao().getPieChartItems(LocalDate.now().minusMonths(1).toString(),
                                        LocalDate.now().toString());
                        break;
                    default:
                        pieChartItems = database.pomodoroDao().getAllPieChartItems();
                        break;
                }

                runOnUiThread(() -> {
                    loadPieChart(pieChartItems);

                    if (isActivitySpinnerSelectionFromTouch) {
                        scrollToBottom();
                        isActivitySpinnerSelectionFromTouch = false;
                    }
                });
            });
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    private void setupActivitiesSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.activities_spinner, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);

        activitiesSpinnerCurrentSelectedIndex = sharedPreferences.getInt(Constants.ACTIVITIES_SPINNER_SETTING, 1);

        activitiesSpinner.setAdapter(adapter);
        activitiesSpinner.setSelection(activitiesSpinnerCurrentSelectedIndex);

        activitiesSpinner.setOnItemSelectedListener(new ActivitySpinnerInteractionListener());
        activitiesSpinner.setOnTouchListener(new ActivitySpinnerInteractionListener());
    }

    private void scrollToBottom() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (sharedPreferences.getBoolean(Constants.SCROLL_PIE_CHART_AUTOMATICALLY, true)) {
            if (isActivitySpinnerSelectionFromTouch) {
                TextView activityTextView = findViewById(R.id.activities_text_view);

                if (shouldScrollDown) {
                    ObjectAnimator scrollToBottom =
                            ObjectAnimator.ofInt(scrollView, "scrollY", activityTextView.getTop());
                    scrollToBottom.setDuration(500);
                    scrollToBottom.start();
                }
            }
        }
    }

    private void loadPieChart(List<PieChartItem> activities) {
        List<PieEntry> entries = new ArrayList<>();

        List<PieChartItem> preparedPieChartItems = prepareActivities(activities);

        for (PieChartItem pieChartItem : preparedPieChartItems) {
            entries.add(new PieEntry(pieChartItem.getTotalTime(), pieChartItem.getActivityName(),
                    Utility.formatStatisticsTime(pieChartItem.getTotalTime())));
        }

        pieData = createActivityPieData(entries);

        // Center Text
        pieChart.setCenterText(Utility.formatStatisticsTime((long) pieData.getYValueSum()));

        // Hole
        pieChart.setHoleRadius(50f);

        if (preparedPieChartItems.size() == 0) {
            pieChart.setData(null);
        } else {
            pieChart.setData(pieData);
        }

        pieChart.invalidate();

        int previousLegendSize;

        if (legendItems == null) {
            previousLegendSize = 0;
        } else {
            previousLegendSize = legendItems.size();
        }

        legendItems = new ArrayList<>();

        List<PieChartItem> others = new ArrayList<>(activities);
        others.removeAll(preparedPieChartItems);

        Collections.sort(others, Comparator.comparingDouble(PieChartItem::getPercent).reversed());

        for (PieChartItem pieChartItem : others) {
            legendItems.add(new LegendItem(pieChartItem.getActivityName(), pieChartItem.getPercent(),
                    Utility.formatStatisticsTime(pieChartItem.getTotalTime())));
        }

        shouldScrollDown = previousLegendSize <= legendItems.size();

        if (legendItems.size() > 0) {
            for (PieChartItem pieChartItem : preparedPieChartItems) {
                if (pieChartItem.getActivityName().isEmpty()) {
                    othersTextView.setVisibility(View.VISIBLE);
                    othersTextView.setText(getResources().getString(R.string.others_text,
                            pieChartItem.getPercent()));

                    for (int i = 0; i < pieData.getDataSet().getEntryCount(); i++) {
                        PieEntry pieEntry = pieData.getDataSet().getEntryForIndex(i);

                        if (pieEntry.getLabel().isEmpty()) {
                            int[] colors = pieData.getColors();

                            othersTextView.setTextColor(colors[i]);

                            break;
                        }
                    }
                }
            }
        } else {
            othersTextView.setVisibility(View.GONE);
        }

        if (legendItems.size() == 0) {
            legendRecyclerView.setVisibility(View.GONE);
        } else {
            legendRecyclerView.setVisibility(View.VISIBLE);
            legendRecyclerView.setAdapter(new LegendAdapter(legendItems));
        }
    }

    private void loadFromDatabase() {
        HistoryChartItem historyChartItemToday = null;
        List<HistoryChartItem> historyChartItemsWeek = new ArrayList<>();
        List<HistoryChartItem> historyChartItemsMonth = new ArrayList<>();
        List<HistoryChartItem> historyChartItemsTotal = new ArrayList<>();
        List<HistoryChartItem> historyChartItemsWeekData = new ArrayList<>();

        try {
            int[] idsOfActivitiesToShow =
                    Database.databaseExecutor.submit(() -> database.activityDao().getIdsToShow()).get();

            LocalDate now = LocalDate.now();

            historyChartItemToday =
                    Database.databaseExecutor.submit(() ->
                            database.pomodoroDao().getAllGroupByDate(now.toString(), idsOfActivitiesToShow)).get();

            historyChartItemsWeek =
                    Database.databaseExecutor.submit(() ->
                            database.pomodoroDao().getAllDatesBetweenGroupByDate(now.minusDays(6).toString(),
                                    now.minusDays(1).toString(), idsOfActivitiesToShow)).get();

            historyChartItemsMonth =
                    Database.databaseExecutor.submit(() ->
                            database.pomodoroDao().getAllDatesBetweenGroupByDate(now.minusDays(29).toString(),
                                    now.minusDays(7).toString(), idsOfActivitiesToShow)).get();

            historyChartItemsTotal =
                    Database.databaseExecutor.submit(() ->
                            database.pomodoroDao().getAllDateLessGroupByDate(now.minusDays(29).toString(),
                                    idsOfActivitiesToShow)).get();

            data = Database.databaseExecutor.submit(() ->
                    database.pomodoroDao().getAllGroupByDate(idsOfActivitiesToShow)).get();

            historyChartItemsWeekData =
                    Database.databaseExecutor.submit(() ->
                            database.pomodoroDao().getAllGroupByWeek(idsOfActivitiesToShow)).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        long timeToday = 0;

        if (historyChartItemToday != null) {
            timeToday = historyChartItemToday.getTime();
        }

        long timeWeek = timeToday;

        for (HistoryChartItem historyChartItem : historyChartItemsWeek) {
            timeWeek += historyChartItem.getTime();
        }

        long timeMonth = timeWeek;

        for (HistoryChartItem historyChartItem : historyChartItemsMonth) {
            timeMonth += historyChartItem.getTime();
        }

        long timeTotal = timeMonth;

        for (HistoryChartItem historyChartItem : historyChartItemsTotal) {
            timeTotal += historyChartItem.getTime();
        }

        numberTodayTextView.setText(Utility.formatStatisticsTime(timeToday));
        numberWeekTextView.setText(Utility.formatStatisticsTime(timeWeek));
        numberMonthTextView.setText(Utility.formatStatisticsTime(timeMonth));
        numberTotalTextView.setText(Utility.formatStatisticsTime(timeTotal));

        monthData = new MonthData(data);
        monthData.generate();

        dayData = new DayData(data);
        dayData.generate();

        weekData = new WeekData(historyChartItemsWeekData);
        weekData.generate();

        historyChart = new HistoryChart(this, historyChartView);
        historyChart.setupChart();

        if (historySpinnerCurrentSelectedIndex == 0) {
            historyChart.displayData(dayData);
        } else if (historySpinnerCurrentSelectedIndex == 1) {
            historyChart.displayData(weekData);
        } else {
            historyChart.displayData(monthData);
        }
    }
}
