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

package org.wentura.getflow.database;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;

import org.wentura.getflow.Constants;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@androidx.room.Database(entities = {Pomodoro.class, Activity.class}, version = 1)
public abstract class Database extends RoomDatabase {

    private static volatile Database database;
    public static final ExecutorService databaseExecutor =
            Executors.newSingleThreadExecutor();

    private static Database buildDatabaseInstance(Context context) {
        if (database != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of " +
                    "this class.");
        }
        return Room.databaseBuilder(context, Database.class, Constants.DATABASE_NAME).build();
    }

    public static Database getInstance(Context context) {
        if (database == null) {
            synchronized (Database.class) {
                if (database == null) {
                    database = buildDatabaseInstance(context);
                }
            }
        }
        return database;
    }

    public abstract PomodoroDao pomodoroDao();

    public abstract ActivityDao activityDao();
}
