package com.wentura.pomodoro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static android.media.AudioManager.RINGER_MODE_NORMAL;
import static android.media.AudioManager.RINGER_MODE_SILENT;

public class NotificationButtonReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getStringExtra(Constants.BUTTON_ACTION);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editPreferences = preferences.edit();

        switch (action) {
            case Constants.BUTTON_STOP: {
                stopNotificationService(context);
                stopEndNotificationService(context);

                boolean isBreakState = preferences.getBoolean(Constants.IS_BREAK_STATE, false);

                Intent updateUI = new Intent(Constants.UPDATE_DATABASE_INTENT);
                if (isBreakState) {
                    updateUI.putExtra(Constants.BUTTON_ACTION, Constants.UPDATE_BREAKS);
                } else {
                    updateUI.putExtra(Constants.BUTTON_ACTION, Constants.UPDATE_WORKS);
                }
                LocalBroadcastManager.getInstance(context).sendBroadcast(updateUI);

                editPreferences.putBoolean(Constants.IS_TIMER_RUNNING, false);
                editPreferences.putBoolean(Constants.IS_BREAK_STATE, false);
                editPreferences.apply();

                updateUI = new Intent(Constants.BUTTON_CLICKED);
                updateUI.putExtra(Constants.BUTTON_ACTION, Constants.BUTTON_STOP);
                LocalBroadcastManager.getInstance(context).sendBroadcast(updateUI);

                Utility.toggleDoNotDisturb(context, RINGER_MODE_NORMAL);
                break;
            }
            case Constants.BUTTON_SKIP: {
                boolean isBreakState = preferences.getBoolean(Constants.IS_BREAK_STATE, false);
                stopEndNotificationService(context);
                stopNotificationService(context);
                Intent updateUI = new Intent(Constants.UPDATE_DATABASE_INTENT);

                if (isBreakState) {
                    updateUI.putExtra(Constants.BUTTON_ACTION, Constants.UPDATE_BREAKS);

                    editPreferences.putBoolean(Constants.IS_BREAK_STATE, false);

                    editPreferences.putInt(Constants.LAST_SESSION_DURATION,
                            Integer.parseInt(preferences.getString(Constants.WORK_DURATION_SETTING,
                                    Constants.DEFAULT_WORK_TIME)));

                    if (BuildConfig.BUILD_TYPE.equalsIgnoreCase("myDebug")) {
                        editPreferences.putLong(Constants.TIMER_LEFT_IN_MILLISECONDS,
                                Integer.parseInt(preferences.getString(Constants.WORK_DURATION_SETTING,
                                        Constants.DEFAULT_WORK_TIME)));
                    } else {
                        editPreferences.putLong(Constants.TIMER_LEFT_IN_MILLISECONDS,
                                Integer.parseInt(preferences.getString(Constants.WORK_DURATION_SETTING,
                                        Constants.DEFAULT_WORK_TIME)) * 60000);
                    }

                    Utility.toggleDoNotDisturb(context, RINGER_MODE_SILENT);
                } else {
                    updateUI.putExtra(Constants.BUTTON_ACTION, Constants.UPDATE_WORKS);

                    editPreferences.putBoolean(Constants.IS_BREAK_STATE, true);

                    editPreferences.putInt(Constants.LAST_SESSION_DURATION,
                            Integer.parseInt(preferences.getString(Constants.BREAK_DURATION_SETTING,
                                    Constants.DEFAULT_BREAK_TIME)));

                    if (BuildConfig.BUILD_TYPE.equalsIgnoreCase("myDebug")) {
                        editPreferences.putLong(Constants.TIMER_LEFT_IN_MILLISECONDS,
                                Integer.parseInt(preferences.getString(Constants.WORK_DURATION_SETTING,
                                        Constants.DEFAULT_WORK_TIME)));
                    } else {
                        editPreferences.putLong(Constants.TIMER_LEFT_IN_MILLISECONDS,
                                Integer.parseInt(preferences.getString(Constants.WORK_DURATION_SETTING,
                                        Constants.DEFAULT_WORK_TIME)) * 60000);
                    }
                    Utility.toggleDoNotDisturb(context, RINGER_MODE_NORMAL);
                }
                editPreferences.putBoolean(Constants.IS_TIMER_RUNNING, true);
                editPreferences.apply();

                LocalBroadcastManager.getInstance(context).sendBroadcast(updateUI);

                updateUI = new Intent(Constants.BUTTON_CLICKED);
                updateUI.putExtra(Constants.BUTTON_ACTION, Constants.BUTTON_SKIP);
                LocalBroadcastManager.getInstance(context).sendBroadcast(updateUI);

                startNotificationService(context);
                break;
            }
            case Constants.BUTTON_START: {
                boolean isBreakState = preferences.getBoolean(Constants.IS_BREAK_STATE, false);
                editPreferences.putBoolean(Constants.IS_TIMER_RUNNING, true);
                editPreferences.apply();

                stopEndNotificationService(context);
                startNotificationService(context);

                Intent updateUI = new Intent(Constants.BUTTON_CLICKED);
                updateUI.putExtra(Constants.BUTTON_ACTION, Constants.BUTTON_START);
                LocalBroadcastManager.getInstance(context).sendBroadcast(updateUI);

                if (!isBreakState) {
                    Utility.toggleDoNotDisturb(context, RINGER_MODE_SILENT);
                }
                break;
            }
            case Constants.BUTTON_PAUSE: {
                boolean isBreakState = preferences.getBoolean(Constants.IS_BREAK_STATE, false);
                long timerLeftInMilliseconds =
                        preferences.getLong(Constants.TIMER_LEFT_IN_MILLISECONDS, 0);

                editPreferences.putBoolean(Constants.IS_TIMER_RUNNING, false);
                editPreferences.putInt(Constants.LAST_SESSION_DURATION, (int) timerLeftInMilliseconds / 60000);
                editPreferences.apply();

                if (!isBreakState) {
                    Utility.toggleDoNotDisturb(context, RINGER_MODE_NORMAL);
                }

                Intent serviceIntent = new Intent(context, NotificationService.class);
                serviceIntent.putExtra(Constants.NOTIFICATION_SERVICE,
                        Constants.NOTIFICATION_SERVICE_PAUSE);
                context.startService(serviceIntent);

                Intent updateUI = new Intent(Constants.BUTTON_CLICKED);
                updateUI.putExtra(Constants.BUTTON_ACTION, Constants.BUTTON_PAUSE);
                LocalBroadcastManager.getInstance(context).sendBroadcast(updateUI);
                break;
            }
        }
    }

    private void stopNotificationService(Context context) {
        Intent stopService = new Intent(context, NotificationService.class);
        context.stopService(stopService);
    }

    private void stopEndNotificationService(Context context) {
        Intent stopService = new Intent(context, EndNotificationService.class);
        context.stopService(stopService);
    }

    private void startNotificationService(Context context) {
        Intent startService = new Intent(context, NotificationService.class);
        context.startService(startService);
    }
}
