package com.wentura.pomodoro.statistics;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.wentura.pomodoro.R;
import com.wentura.pomodoro.Utility;
import com.wentura.pomodoro.database.Database;

import java.lang.ref.WeakReference;
import java.util.List;

public class StatisticsActivity extends AppCompatActivity {
    private Database database;
    TextView numberTodayTextView;
    TextView numberWeekTextView;
    TextView numberMonthTextView;
    TextView numberTotalTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        numberTodayTextView = findViewById(R.id.numberTodayTextView);
        numberWeekTextView = findViewById(R.id.numberWeekTextView);
        numberMonthTextView = findViewById(R.id.numberMonthTextView);
        numberTotalTextView = findViewById(R.id.numberTotalTextView);

        database = Database.getInstance(this);

        new LoadFromDatabase(this).execute();
    }

    private static class LoadFromDatabase extends AsyncTask<Void, Void, Void> {
        private static final String TAG = "Help";
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

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            StatisticsActivity statisticsActivity = activityWeakReference.get();

            if (statisticsActivity == null || statisticsActivity.isFinishing()) {
                return;
            }

            int timeToday = statisticsItemToday.getCompletedWorkTime() + statisticsItemToday.getIncompleteWorkTime();

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
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.background_down, R.anim.foreground_down);
    }
}
