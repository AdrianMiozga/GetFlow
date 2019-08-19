package com.wentura.pomodoro.database;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.wentura.pomodoro.Constants;

@androidx.room.Database(entities = {Pomodoro.class}, version = 1)
public abstract class Database extends RoomDatabase {
    private static volatile Database database;

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
}
