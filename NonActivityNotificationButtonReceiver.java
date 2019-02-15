package com.wentura.pomodoroapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import static android.content.Context.MODE_PRIVATE;
import static android.media.AudioManager.RINGER_MODE_NORMAL;
import static android.media.AudioManager.RINGER_MODE_SILENT;
import static com.wentura.pomodoroapp.Constants.BREAK_DURATION_SETTING;
import static com.wentura.pomodoroapp.Constants.BREAK_LEFT_IN_MILLISECONDS;
import static com.wentura.pomodoroapp.Constants.IS_BREAK_STARTED;
import static com.wentura.pomodoroapp.Constants.IS_BREAK_STATE;
import static com.wentura.pomodoroapp.Constants.IS_TIMER_RUNNING;
import static com.wentura.pomodoroapp.Constants.IS_WORK_STARTED;
import static com.wentura.pomodoroapp.Constants.MY_PREFERENCES;
import static com.wentura.pomodoroapp.Constants.WORK_DURATION_SETTING;
import static com.wentura.pomodoroapp.Constants.WORK_LEFT_IN_MILLISECONDS;

public class NonActivityNotificationButtonReceiver extends BroadcastReceiver {

    private static final String TAG = NonActivityNotificationButtonReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getStringExtra(Constants.BUTTON_ACTION);

        SharedPreferences.Editor editPreferences =
                context.getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE).edit();

        switch (action) {
            case Constants.BUTTON_STOP: {
                Intent stopService = new Intent(context, NotificationService.class);
                context.stopService(stopService);

                Log.d(TAG, "onReceive: STOP");

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.cancelAll();

                editPreferences.putBoolean(IS_WORK_STARTED, false);
                editPreferences.putBoolean(IS_BREAK_STARTED, false);
                editPreferences.putBoolean(IS_TIMER_RUNNING, false);
                editPreferences.putBoolean(IS_BREAK_STATE, false);
                editPreferences.apply();
                Utility.toggleDoNotDisturb(context, RINGER_MODE_NORMAL);
                break;
            }
            case Constants.BUTTON_SKIP: {
                Intent stopService = new Intent(context, NotificationService.class);
                context.stopService(stopService);

                SharedPreferences preferences =
                        context.getSharedPreferences(Constants.MY_PREFERENCES, MODE_PRIVATE);

                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

                boolean breakState = preferences.getBoolean(Constants.IS_BREAK_STATE, false);

                if (breakState) {
                    editPreferences.putBoolean(IS_BREAK_STARTED, false);
                    editPreferences.putBoolean(IS_WORK_STARTED, true);
                    editPreferences.putBoolean(IS_BREAK_STATE, false);
                    editPreferences.putLong(BREAK_LEFT_IN_MILLISECONDS,
                            Integer.parseInt(settings.getString(BREAK_DURATION_SETTING,
                                    Constants.DEFAULT_BREAK_TIME)) * 60000);
                    Log.d(TAG, "onReceive: breakState");
                    Utility.toggleDoNotDisturb(context, RINGER_MODE_SILENT);
                } else {
                    editPreferences.putBoolean(IS_BREAK_STARTED, true);
                    editPreferences.putBoolean(IS_WORK_STARTED, false);
                    editPreferences.putBoolean(IS_BREAK_STATE, true);
                    editPreferences.putLong(WORK_LEFT_IN_MILLISECONDS,
                            Integer.parseInt(settings.getString(WORK_DURATION_SETTING,
                                    Constants.DEFAULT_WORK_TIME)) * 60000);
                    Log.d(TAG, "onReceive: !breakState");
                    Utility.toggleDoNotDisturb(context, RINGER_MODE_NORMAL);
                }
                editPreferences.putBoolean(IS_TIMER_RUNNING, true);
                editPreferences.apply();

                Intent startService = new Intent(context, NotificationService.class);
                context.startService(startService);
                break;
            }
            case Constants.BUTTON_PAUSE_RESUME: {
                SharedPreferences preferences =
                        context.getSharedPreferences(Constants.MY_PREFERENCES, MODE_PRIVATE);

                boolean isTimerRunning = preferences.getBoolean(Constants.IS_TIMER_RUNNING, false);
                boolean isBreakState = preferences.getBoolean(Constants.IS_BREAK_STATE, false);
                long workLeftInMilliseconds =
                        preferences.getLong(Constants.WORK_LEFT_IN_MILLISECONDS, 0);
                long breakLeftInMilliseconds =
                        preferences.getLong(Constants.BREAK_LEFT_IN_MILLISECONDS, 0);

                Log.d(TAG, "onReceive: isTimerRunning " + isTimerRunning);
                if (isTimerRunning) {
                    editPreferences.putBoolean(IS_TIMER_RUNNING, false);

                    Notification notification = new Notification();

                    if (isBreakState) {
                        notification.buildNotification(context, breakLeftInMilliseconds,
                                true, false, false);
                    } else {
                        notification.buildNotification(context, workLeftInMilliseconds,
                                false, false, false);
                        Utility.toggleDoNotDisturb(context, RINGER_MODE_NORMAL);
                    }
                    Intent stopService = new Intent(context, NotificationService.class);
                    context.stopService(stopService);
                } else {
                    editPreferences.putBoolean(IS_TIMER_RUNNING, true);
                    Intent startService = new Intent(context, NotificationService.class);
                    context.startService(startService);
                    if (!isBreakState) {
                        Utility.toggleDoNotDisturb(context, RINGER_MODE_SILENT);
                    }
                }
                editPreferences.apply();
                break;
            }
        }
    }
}
