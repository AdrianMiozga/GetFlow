package com.wentura.pomodoro;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.wentura.pomodoro.database.Database;
import com.wentura.pomodoro.database.Pomodoro;

public class UpdateDatabaseIncompleteWorks extends AsyncTask<Void, Void, Void> {
    private Database database;
    private int time;

    UpdateDatabaseIncompleteWorks(Context context, int time) {
        this.database = Database.getInstance(context);
        this.time = time;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        String currentDate = Utility.getCurrentDate();

        Log.d("Pomodoro", "doInBackground: Update database incomplete works");

        if (database.pomodoroDao().getLatestDate() != null && database.pomodoroDao().getLatestDate().equals(currentDate)) {
            database.pomodoroDao().updateIncompleteWorks(database.pomodoroDao().getIncompleteWorks(currentDate) + 1,
                    currentDate);
            database.pomodoroDao().updateIncompleteWorkTime(database.pomodoroDao().getIncompleteWorkTime(currentDate) + time, currentDate);
        } else {
            database.pomodoroDao().insertPomodoro(new Pomodoro(currentDate, 0, 0,
                    1, time, 0, 0));
        }
        return null;
    }
}