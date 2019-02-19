package com.wentura.pomodoro;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.wentura.pomodoro.database.Database;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class StatisticsActivity extends AppCompatActivity {
    private static final String TAG = StatisticsActivity.class.getSimpleName();
    private Database database;

    private static void fillDatesWithoutPomodoros(List<StatisticsItem> statisticsItems) {
        int days = 6;
        int statisticItemsSize = statisticsItems.size();
        String currentDate = Utility.getCurrentDate();

        if (!statisticsItems.get(statisticItemsSize - 1).getDate().equals(currentDate)) {
            statisticsItems.add(new StatisticsItem(currentDate,
                    0, 0));
        }

        for (int i = 0; i < statisticItemsSize; i++) {
            Log.d(TAG, "onCreate: " + statisticsItems.get(i).getDate());
            Log.d(TAG, "onCreate: " + Utility.subtractDaysFromCurrentDate(days));
            if (!statisticsItems.get(i).getDate().equals(Utility.subtractDaysFromCurrentDate(days))) {
                statisticsItems.add(new StatisticsItem(Utility.subtractDaysFromCurrentDate(days),
                        0, 0));
                i--;
            }
            days--;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        database = Database.getInstance(this);

        new LoadFromDatabase(this).execute();
    }

    private static class LoadFromDatabase extends AsyncTask<Void, Void, Void> {
        List<StatisticsItem> statisticsItems;

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

            statisticsItems =
                    statisticsActivity.database.pomodoroDao().getAllBetweenDates(Utility.subtractDaysFromCurrentDate(6),
                            Utility.getCurrentDate());

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            StatisticsActivity statisticsActivity = activityWeakReference.get();

            if (statisticsActivity == null || statisticsActivity.isFinishing()) {
                return;
            }

            RecyclerView recyclerView = statisticsActivity.findViewById(R.id.recycler_view);

            Log.d(TAG, "onCreate: " + statisticsItems.size());

            fillDatesWithoutPomodoros(statisticsItems);

            Collections.sort(statisticsItems, Collections.<StatisticsItem>reverseOrder());

            StatisticsAdapter statisticsAdapter = new StatisticsAdapter(statisticsItems);

            recyclerView.setAdapter(statisticsAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(statisticsActivity));
        }
    }
}
