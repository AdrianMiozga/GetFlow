package com.wentura.pomodoro;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.wentura.pomodoro.database.Database;
import com.wentura.pomodoro.database.Pomodoro;

class UpdateDatabaseCompletedWorks extends AsyncTask<Void, Void, Void> {
    private Database database;
    private int time;

    UpdateDatabaseCompletedWorks(Context context, int time) {
        this.database = Database.getInstance(context);
        this.time = time;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        String currentDate = Utility.getCurrentDate();

        Log.d("Pomodoro", "doInBackground: Update database complete works");

        if (database.pomodoroDao().getLatestDate() != null && database.pomodoroDao().getLatestDate().equals(currentDate)) {
            database.pomodoroDao().updateCompletedWorks(database.pomodoroDao().getCompletedWorks(currentDate) + 1, currentDate);
            database.pomodoroDao().updateCompletedWorkTime(database.pomodoroDao().getCompletedWorkTime(currentDate) + time, currentDate);
        } else {
            database.pomodoroDao().insertPomodoro(new Pomodoro(currentDate, 1, time,
                    0, 0, 0, 0));
        }
        return null;
    }
}
