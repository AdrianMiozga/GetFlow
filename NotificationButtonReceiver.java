package com.wentura.pomodoro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static android.media.AudioManager.RINGER_MODE_NORMAL;
import static android.media.AudioManager.RINGER_MODE_SILENT;
import static com.wentura.pomodoro.Constants.BREAK_DURATION_SETTING;
import static com.wentura.pomodoro.Constants.BREAK_LEFT_IN_MILLISECONDS;
import static com.wentura.pomodoro.Constants.IS_BREAK_STARTED;
import static com.wentura.pomodoro.Constants.IS_BREAK_STATE;
import static com.wentura.pomodoro.Constants.IS_TIMER_RUNNING;
import static com.wentura.pomodoro.Constants.IS_WORK_STARTED;
import static com.wentura.pomodoro.Constants.WORK_DURATION_SETTING;
import static com.wentura.pomodoro.Constants.WORK_LEFT_IN_MILLISECONDS;

public class NotificationButtonReceiver extends BroadcastReceiver {

    private static final String TAG = NotificationButtonReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getStringExtra(Constants.BUTTON_ACTION);
        boolean isNotificationOpenedFromActivity =
                intent.getBooleanExtra(Constants.IS_NOTIFICATION_OPENED_FROM_ACTIVITY, false);

        if (isNotificationOpenedFromActivity) {
            Intent localIntent = new Intent(Constants.BUTTON_CLICKED);
            localIntent.putExtra(Constants.BUTTON_ACTION, action);
            LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
        } else {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editPreferences = preferences.edit();

            switch (action) {
                case Constants.BUTTON_STOP: {
                    stopNotificationService(context);
                    cancelAllNotifications(context);

                    editPreferences.putBoolean(IS_WORK_STARTED, false);
                    editPreferences.putBoolean(IS_BREAK_STARTED, false);
                    editPreferences.putBoolean(IS_TIMER_RUNNING, false);
                    editPreferences.putBoolean(IS_BREAK_STATE, false);
                    editPreferences.apply();
                    Utility.toggleDoNotDisturb(context, RINGER_MODE_NORMAL);

                    Log.d(TAG, "onReceive: STOP");
                    break;
                }
                case Constants.BUTTON_SKIP: {
                    stopNotificationService(context);

                    boolean isBreakState = preferences.getBoolean(Constants.IS_BREAK_STATE, false);

                    if (isBreakState) {
                        editPreferences.putBoolean(IS_BREAK_STARTED, false);
                        editPreferences.putBoolean(IS_WORK_STARTED, true);
                        editPreferences.putBoolean(IS_BREAK_STATE, false);
                        editPreferences.putLong(BREAK_LEFT_IN_MILLISECONDS,
                                Integer.parseInt(preferences.getString(BREAK_DURATION_SETTING,
                                        Constants.DEFAULT_BREAK_TIME)) * 60000);
                        Log.d(TAG, "onReceive: breakState");
                        Utility.toggleDoNotDisturb(context, RINGER_MODE_SILENT);
                    } else {
                        editPreferences.putBoolean(IS_BREAK_STARTED, true);
                        editPreferences.putBoolean(IS_WORK_STARTED, false);
                        editPreferences.putBoolean(IS_BREAK_STATE, true);
                        editPreferences.putLong(WORK_LEFT_IN_MILLISECONDS,
                                Integer.parseInt(preferences.getString(WORK_DURATION_SETTING,
                                        Constants.DEFAULT_WORK_TIME)) * 60000);
                        Log.d(TAG, "onReceive: !breakState");
                        Utility.toggleDoNotDisturb(context, RINGER_MODE_NORMAL);
                    }
                    editPreferences.putBoolean(IS_TIMER_RUNNING, true);
                    editPreferences.apply();

                    startNotificationService(context);
                    break;
                }
                case Constants.BUTTON_PAUSE_RESUME: {
                    boolean isTimerRunning = preferences.getBoolean(Constants.IS_TIMER_RUNNING, false);
                    boolean isBreakState = preferences.getBoolean(Constants.IS_BREAK_STATE, false);
                    long workLeftInMilliseconds =
                            preferences.getLong(Constants.WORK_LEFT_IN_MILLISECONDS, 0);
                    long breakLeftInMilliseconds =
                            preferences.getLong(Constants.BREAK_LEFT_IN_MILLISECONDS, 0);

                    Log.d(TAG, "onReceive: isTimerRunning " + isTimerRunning);
                    if (isTimerRunning) {
                        stopNotificationService(context);
                        editPreferences.putBoolean(IS_TIMER_RUNNING, false);

                        Notification notification = new Notification();

                        if (isBreakState) {
                            notification.buildNotification(context, breakLeftInMilliseconds,
                                    true, false, false).build();
                            Log.d(TAG, "onReceive: isBreakState");
                        } else {
                            notification.buildNotification(context, workLeftInMilliseconds,
                                    false, false, false).build();
                            Utility.toggleDoNotDisturb(context, RINGER_MODE_NORMAL);
                            Log.d(TAG, "onReceive: !isBreakState");
                        }
                    } else {
                        startNotificationService(context);
                        editPreferences.putBoolean(IS_TIMER_RUNNING, true);
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

    private void cancelAllNotifications(Context context) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancelAll();
    }

    private void stopNotificationService(Context context) {
        Intent stopService = new Intent(context, NotificationService.class);
        context.stopService(stopService);
    }

    private void startNotificationService(Context context) {
        Intent startService = new Intent(context, NotificationService.class);
        context.startService(startService);
    }
}
