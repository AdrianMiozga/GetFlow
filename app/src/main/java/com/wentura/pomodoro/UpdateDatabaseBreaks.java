package com.wentura.pomodoro;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.wentura.pomodoro.database.Database;
import com.wentura.pomodoro.database.Pomodoro;

import java.lang.ref.WeakReference;

public class UpdateDatabaseBreaks extends AsyncTask<Void, Void, Void> {
    private WeakReference<MainActivity> weakReference;
    private Database database;

    UpdateDatabaseBreaks(MainActivity context) {
        this.weakReference = new WeakReference<>(context);
        this.database = Database.getInstance(weakReference.get());
    }

    private static int getLastSessionDuration(WeakReference<MainActivity> weakReference) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(weakReference.get());

        int lastWorkSessionDuration = sharedPreferences.getInt(Constants.LAST_SESSION_DURATION, 0);

        if (lastWorkSessionDuration == 0) {
            return Integer.parseInt(sharedPreferences.getString(Constants.WORK_DURATION_SETTING,
                    Constants.DEFAULT_WORK_TIME));
        } else {
            return lastWorkSessionDuration;
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        String currentDate = Utility.getCurrentDate();

        if (database.pomodoroDao().getLatestDate().equals(currentDate)) {
            database.pomodoroDao().updateBreaks(database.pomodoroDao().getBreaks(currentDate) + 1, currentDate);
            database.pomodoroDao().updateCompletedBreaksTime(database.pomodoroDao().getCompletedBreaksTime(currentDate) + getLastSessionDuration(weakReference), currentDate);
        } else {
            database.pomodoroDao().insertPomodoro(new Pomodoro(currentDate, 0, 1, 0, getLastSessionDuration(weakReference)));
        }
        return null;
    }
}