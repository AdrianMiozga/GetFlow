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

package org.wentura.getflow;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.preference.PreferenceManager;

import org.wentura.getflow.database.Database;
import org.wentura.getflow.database.Pomodoro;
import org.wentura.getflow.database.PomodoroDao;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

public final class Utility {

    // Suppress default constructor for noninstantiability
    private Utility() {
        throw new AssertionError();
    }

    static void setWifiEnabled(Context context, boolean enable, int activityId) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            return;
        }

        Database database = Database.getInstance(context);

        Database.databaseExecutor.execute(() -> {
            if (database.activityDao().isWifiDisabledDuringWorkSession(activityId)) {
                WifiManager wifiManager =
                        (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

                if (wifiManager != null) {
                    wifiManager.setWifiEnabled(enable);
                }
            }
        });
    }

    static void setDoNotDisturb(Context context, int mode, int activityId) {
        Database database = Database.getInstance(context);

        Database.databaseExecutor.execute(() -> {
            if (database.activityDao().isDNDEnabled(activityId)) {
                setRingerMode(context, mode);
            }
        });
    }

    private static void setRingerMode(Context context, int mode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager == null) {
                return;
            }

            if (!notificationManager.isNotificationPolicyAccessGranted()) {
                return;
            }

            AudioManager audioManager = context.getSystemService(AudioManager.class);

            if (audioManager == null) {
                return;
            }
            audioManager.setRingerMode(mode);
        } else {
            AudioManager audioManager =
                    (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

            if (audioManager == null) {
                return;
            }
            audioManager.setRingerMode(mode);
        }
    }

    @SuppressLint("DefaultLocale")
    static String formatTime(long milliseconds) {
        // I'm adding 999 milliseconds so that the timer doesn't end one second after 00:00, but
        // exactly when 00:00 strikes. Adding exactly 1000 milliseconds makes the timer show one
        // second more when it's not started.
        milliseconds += 999;
        return String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds % 60000));
    }

    @SuppressLint("DefaultLocale")
    public static String formatStatisticsTime(long milliseconds) {
        long hours = TimeUnit.MILLISECONDS.toHours(milliseconds);

        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) -
                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliseconds));

        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds));

        if (milliseconds == 0) {
            return "0h";
        }

        if (minutes == 0 && hours == 0) {
            return String.format("%ds", seconds);
        }

        if (seconds > 0) {
            minutes++;
        }

        if (minutes == 60) {
            minutes = 0;
            hours++;
        }

        if (hours > 0 && minutes == 0 || hours > 9) {
            return String.format("%dh", hours);
        }

        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        }

        return String.format("%dm", minutes);
    }

    @SuppressLint("DefaultLocale")
    static String formatTimeForNotification(long milliseconds) {
        milliseconds += 999;

        long hours = TimeUnit.MILLISECONDS.toHours(milliseconds);

        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) -
                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliseconds));

        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds));

        if (hours > 0 && minutes != 0) {
            return String.format("%dh %dm", hours, minutes);
        }

        if (hours > 0) {
            return String.format("%dh", hours);
        }

        if (minutes > 0 && seconds > 0) {
            return String.format("%dm %ds", minutes, seconds);
        }

        if (minutes > 0 && seconds == 0) {
            return String.format("%dm", minutes);
        }
        return String.format("%ds", seconds);
    }

    static void toggleKeepScreenOn(Context context) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(Constants.KEEP_SCREEN_ON_SETTING, false)) {
            ((Activity) context).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            ((Activity) context).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    public static float convertDpToPixel(float dp, Context context) {
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static void updateDatabaseBreaks(Context context, int time, int activityId) {
        PomodoroDao pomodoroDao = Database.getInstance(context).pomodoroDao();

        String currentDate = LocalDate.now().toString();

        Database.databaseExecutor.execute(() -> {
            int pomodoroId = pomodoroDao.getId(currentDate, activityId);

            if (pomodoroId == 0) {
                pomodoroDao.insertPomodoro(new Pomodoro(currentDate, 0, 0, 0, 0, 1, time, activityId));
            } else {
                pomodoroDao.updateBreaks(pomodoroDao.getBreaks(pomodoroId) + 1, pomodoroId);
                pomodoroDao.updateBreakTime(pomodoroDao.getBreakTime(pomodoroId) + time, pomodoroId);
            }
        });
    }

    public static void updateDatabaseCompletedWorks(Context context, int time, int activityId) {
        PomodoroDao pomodoroDao = Database.getInstance(context).pomodoroDao();

        String currentDate = LocalDate.now().toString();

        Database.databaseExecutor.execute(() -> {
            int pomodoroId = pomodoroDao.getId(currentDate, activityId);

            if (pomodoroId == 0) {
                pomodoroDao.insertPomodoro(new Pomodoro(currentDate, 1, time, 0, 0, 0, 0, activityId));
            } else {
                pomodoroDao.updateCompletedWorks(pomodoroDao.getCompletedWorks(pomodoroId) + 1, pomodoroId);
                pomodoroDao.updateCompletedWorkTime(pomodoroDao.getCompletedWorkTime(pomodoroId) + time, pomodoroId);
            }
        });
    }

    public static void updateDatabaseIncompleteWorks(Context context, int time, int activityId) {
        PomodoroDao pomodoroDao = Database.getInstance(context).pomodoroDao();

        String currentDate = LocalDate.now().toString();

        Database.databaseExecutor.execute(() -> {
            int pomodoroId = pomodoroDao.getId(currentDate, activityId);

            if (pomodoroId == 0) {
                pomodoroDao.insertPomodoro(new Pomodoro(currentDate, 0, 0, 1, time, 0, 0, activityId));
            } else {
                pomodoroDao.updateIncompleteWorks(pomodoroDao.getIncompleteWorks(pomodoroId) + 1, pomodoroId);
                pomodoroDao.updateIncompleteWorkTime(pomodoroDao.getIncompleteWorkTime(pomodoroId) + time, pomodoroId);
            }
        });
    }

    public static String formatPieChartLegendPercent(Context context, double percent) {
        if (percent < 1) {
            return context.getString(R.string.activity_legend_percent_1, percent);
        } else {
            return context.getString(R.string.activity_legend_percent_2, percent);
        }
    }

    public static void showSystemUI(Window window) {
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    public static void hideSystemUI(Window window) {
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
}
