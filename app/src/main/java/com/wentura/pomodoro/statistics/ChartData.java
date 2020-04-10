package com.wentura.pomodoro.statistics;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;

abstract class ChartData {
    private List<Entry> entries;
    private List<StatisticsItem> data;
    private long maxValue = 0;

    ChartData(List<StatisticsItem> data) {
        this.data = data;
        entries = new ArrayList<>();
    }

    abstract void generate();

    abstract List<StatisticsItem> getGeneratedData();

    void createEntries(List<StatisticsItem> statisticsItems) {
        entries.clear();

        maxValue = 0;

        for (int i = 0; i < statisticsItems.size(); i++) {
            long totalTime = statisticsItems.get(i).getCompletedWorkTime() + statisticsItems.get(i).getIncompleteWorkTime();

            entries.add(new Entry(i, totalTime));

            if (totalTime > maxValue) {
                maxValue = totalTime;
            }
        }
    }

    List<Entry> getEntries() {
        return new ArrayList<>(entries);
    }

    long getSize() {
        return entries.size();
    }

    long getMaxValue() {
        return maxValue;
    }

    List<StatisticsItem> getData() {
        return new ArrayList<>(data);
    }
}
