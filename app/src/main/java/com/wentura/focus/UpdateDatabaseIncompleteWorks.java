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

package com.wentura.focus;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.wentura.focus.database.Database;
import com.wentura.focus.database.Pomodoro;

class UpdateDatabaseIncompleteWorks extends AsyncTask<Void, Void, Void> {
    private static final String TAG = UpdateDatabaseIncompleteWorks.class.getSimpleName();
    private Database database;
    private int time;

    UpdateDatabaseIncompleteWorks(Context context, int time) {
        this.database = Database.getInstance(context);
        this.time = time;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        String currentDate = Utility.getCurrentDate();

        Log.d(TAG, "doInBackground: Update database incomplete works");

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
