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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import org.wentura.getflow.database.Database;

import java.time.LocalDateTime;

import static android.media.AudioManager.RINGER_MODE_NORMAL;
import static android.media.AudioManager.RINGER_MODE_SILENT;

/**
 * Receives actions like stop, pause, skip taken on the timer either from the notification buttons or inside of the
 * app.
 */
public final class TimerActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getStringExtra(Constants.BUTTON_ACTION);
        int activityId = intent.getIntExtra(Constants.CURRENT_ACTIVITY_ID_INTENT, 1);

        Bundle extras = intent.getExtras();

        if (extras == null) {
            throw new AssertionError("Provide Activity ID and Button Action");
        }

        if (!extras.containsKey(Constants.CURRENT_ACTIVITY_ID_INTENT)) {
            throw new AssertionError("No Activity ID");
        }

        if (action == null) {
            throw new AssertionError("Provide Button Action");
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editPreferences = preferences.edit();

        switch (action) {
            case Constants.BUTTON_STOP: {
                stopNotificationService(context);
                stopEndNotificationService(context);

                Utility.setWifiEnabled(context, true, activityId);

                boolean isBreakState = preferences.getBoolean(Constants.IS_BREAK_STATE, false);

                int lastSessionDuration = preferences.getInt(Constants.LAST_SESSION_DURATION, 0);
                int timeLeft = preferences.getInt(Constants.TIME_LEFT, 0);

                if (timeLeft != 0 && lastSessionDuration - timeLeft > Constants.MINIMUM_SESSION_TIME) {
                    editPreferences.putString(Constants.TIMESTAMP_OF_LAST_WORK_SESSION, LocalDateTime.now().toString());

                    if (isBreakState) {
                        Utility.updateDatabaseBreaks(context, lastSessionDuration - timeLeft, activityId);
                    } else {
                        Utility.updateDatabaseIncompleteWorks(context, lastSessionDuration - timeLeft, activityId);
                    }
                }

                editPreferences.putBoolean(Constants.IS_TIMER_RUNNING, false);
                editPreferences.putBoolean(Constants.IS_BREAK_STATE, false);
                editPreferences.putBoolean(Constants.IS_SKIP_BUTTON_VISIBLE, false);
                editPreferences.putBoolean(Constants.IS_START_BUTTON_VISIBLE, true);
                editPreferences.putBoolean(Constants.IS_PAUSE_BUTTON_VISIBLE, false);
                editPreferences.putBoolean(Constants.IS_STOP_BUTTON_VISIBLE, false);
                editPreferences.putBoolean(Constants.CENTER_BUTTONS, false);
                editPreferences.putBoolean(Constants.IS_WORK_ICON_VISIBLE, true);
                editPreferences.putBoolean(Constants.IS_BREAK_ICON_VISIBLE, false);
                editPreferences.putInt(Constants.TIME_LEFT, 0);
                editPreferences.putBoolean(Constants.IS_TIMER_BLINKING, false);
                editPreferences.putInt(Constants.LAST_SESSION_DURATION, 0);
                editPreferences.apply();

                Intent updateUI = new Intent(Constants.UPDATE_UI);
                updateUI.putExtra(Constants.UPDATE_UI_ACTION, Constants.BUTTON_STOP);
                LocalBroadcastManager.getInstance(context).sendBroadcast(updateUI);

                Utility.setDoNotDisturb(context, RINGER_MODE_NORMAL, activityId);
                break;
            }
            case Constants.BUTTON_SKIP: {
                boolean isBreakState = preferences.getBoolean(Constants.IS_BREAK_STATE, false);
                stopEndNotificationService(context);
                stopNotificationService(context);

                int lastSessionDuration = preferences.getInt(Constants.LAST_SESSION_DURATION, 0);

                int timeLeft = preferences.getInt(Constants.TIME_LEFT, 0);

                editPreferences.putInt(Constants.TIME_LEFT, 0);
                editPreferences.putBoolean(Constants.IS_TIMER_BLINKING, false);

                if (isBreakState) {
                    editPreferences.putBoolean(Constants.IS_BREAK_STATE, false);
                    editPreferences.putBoolean(Constants.IS_WORK_ICON_VISIBLE, true);
                    editPreferences.putBoolean(Constants.IS_BREAK_ICON_VISIBLE, false);

                    Utility.setDoNotDisturb(context, RINGER_MODE_SILENT, activityId);
                    Utility.setWifiEnabled(context, false, activityId);
                } else {
                    editPreferences.putBoolean(Constants.IS_BREAK_STATE, true);
                    editPreferences.putBoolean(Constants.IS_WORK_ICON_VISIBLE, false);
                    editPreferences.putBoolean(Constants.IS_BREAK_ICON_VISIBLE, true);

                    if (lastSessionDuration - timeLeft > Constants.MINIMUM_SESSION_TIME) {
                        editPreferences.putInt(Constants.WORK_SESSION_COUNTER,
                                preferences.getInt(Constants.WORK_SESSION_COUNTER, 0) + 1);
                        editPreferences.putString(Constants.TIMESTAMP_OF_LAST_WORK_SESSION,
                                LocalDateTime.now().toString());
                    }

                    Database database = Database.getInstance(context);
                    Database.databaseExecutor.execute(() -> {
                        if (!database.activityDao().isDNDKeptOnBreaks(activityId)) {
                            Utility.setDoNotDisturb(context, AudioManager.RINGER_MODE_NORMAL, activityId);
                        }
                    });

                    Utility.setWifiEnabled(context, true, activityId);
                }

                if (timeLeft != 0) {
                    if (isBreakState) {
                        Utility.updateDatabaseBreaks(context, lastSessionDuration - timeLeft, activityId);
                    } else {
                        Utility.updateDatabaseIncompleteWorks(context, lastSessionDuration - timeLeft, activityId);
                    }
                }

                editPreferences.putBoolean(Constants.IS_PAUSE_BUTTON_VISIBLE, true);
                editPreferences.putBoolean(Constants.IS_START_BUTTON_VISIBLE, false);
                editPreferences.putBoolean(Constants.IS_TIMER_RUNNING, true);
                editPreferences.putBoolean(Constants.CENTER_BUTTONS, false);
                editPreferences.putInt(Constants.LAST_SESSION_DURATION, 0);
                editPreferences.apply();

                Intent updateUI = new Intent(Constants.UPDATE_UI);
                updateUI.putExtra(Constants.UPDATE_UI_ACTION, Constants.BUTTON_SKIP);
                LocalBroadcastManager.getInstance(context).sendBroadcast(updateUI);

                startNotificationService(context);
                break;
            }
            case Constants.BUTTON_START: {
                boolean isBreakState = preferences.getBoolean(Constants.IS_BREAK_STATE, false);
                editPreferences.putBoolean(Constants.IS_TIMER_RUNNING, true);
                editPreferences.putBoolean(Constants.IS_SKIP_BUTTON_VISIBLE, true);
                editPreferences.putBoolean(Constants.IS_START_BUTTON_VISIBLE, false);
                editPreferences.putBoolean(Constants.IS_PAUSE_BUTTON_VISIBLE, true);
                editPreferences.putBoolean(Constants.IS_STOP_BUTTON_VISIBLE, true);
                editPreferences.putBoolean(Constants.CENTER_BUTTONS, false);
                editPreferences.putBoolean(Constants.IS_TIMER_BLINKING, false);
                editPreferences.apply();

                stopEndNotificationService(context);
                startNotificationService(context);

                Intent updateUI = new Intent(Constants.UPDATE_UI);
                updateUI.putExtra(Constants.UPDATE_UI_ACTION, Constants.BUTTON_START);
                LocalBroadcastManager.getInstance(context).sendBroadcast(updateUI);

                if (!isBreakState) {
                    Utility.setDoNotDisturb(context, RINGER_MODE_SILENT, activityId);
                    Utility.setWifiEnabled(context, false, activityId);
                } else {
                    Utility.setWifiEnabled(context, true, activityId);
                }
                break;
            }
            case Constants.BUTTON_PAUSE: {
                boolean isBreakState = preferences.getBoolean(Constants.IS_BREAK_STATE, false);
                editPreferences.putBoolean(Constants.IS_TIMER_RUNNING, false);
                editPreferences.putBoolean(Constants.IS_SKIP_BUTTON_VISIBLE, true);
                editPreferences.putBoolean(Constants.IS_START_BUTTON_VISIBLE, true);
                editPreferences.putBoolean(Constants.IS_PAUSE_BUTTON_VISIBLE, false);
                editPreferences.putBoolean(Constants.IS_STOP_BUTTON_VISIBLE, true);
                editPreferences.putBoolean(Constants.CENTER_BUTTONS, false);
                editPreferences.putBoolean(Constants.IS_TIMER_BLINKING, true);
                editPreferences.apply();

                Utility.setWifiEnabled(context, true, activityId);

                if (!isBreakState) {
                    Utility.setDoNotDisturb(context, RINGER_MODE_NORMAL, activityId);
                }

                Database.databaseExecutor.execute(() -> {
                    Database database = Database.getInstance(context);

                    int workDuration = database.activityDao().getWorkDuration(activityId);
                    int breakDuration = database.activityDao().getBreakDuration(activityId);
                    int longBreakDuration = database.activityDao().getLongBreakDuration(activityId);
                    int sessionsBeforeLongBreak = database.activityDao().getSessionsBeforeLongBreak(activityId);
                    boolean areLongBreaksEnabled = database.activityDao().areLongBreaksEnabled(activityId);

                    Intent serviceIntent = new Intent(context, NotificationService.class);
                    serviceIntent.putExtra(Constants.WORK_DURATION_INTENT, workDuration);
                    serviceIntent.putExtra(Constants.BREAK_DURATION_INTENT, breakDuration);
                    serviceIntent.putExtra(Constants.LONG_BREAK_DURATION_INTENT, longBreakDuration);
                    serviceIntent.putExtra(Constants.ARE_LONG_BREAKS_ENABLED_INTENT, areLongBreaksEnabled);
                    serviceIntent.putExtra(Constants.SESSIONS_BEFORE_LONG_BREAK_INTENT, sessionsBeforeLongBreak);
                    serviceIntent.putExtra(Constants.NOTIFICATION_SERVICE, Constants.NOTIFICATION_SERVICE_PAUSE);
                    context.startService(serviceIntent);
                });

                Intent updateUI = new Intent(Constants.UPDATE_UI);
                updateUI.putExtra(Constants.UPDATE_UI_ACTION, Constants.BUTTON_PAUSE);
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
        Database database = Database.getInstance(context);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        int activityId = sharedPreferences.getInt(Constants.CURRENT_ACTIVITY_ID, 1);

        Database.databaseExecutor.execute(() -> {
            int workDuration = database.activityDao().getWorkDuration(activityId);
            int breakDuration = database.activityDao().getBreakDuration(activityId);
            int longBreakDuration = database.activityDao().getLongBreakDuration(activityId);
            int sessionsBeforeLongBreak = database.activityDao().getSessionsBeforeLongBreak(activityId);
            boolean areLongBreaksEnabled = database.activityDao().areLongBreaksEnabled(activityId);

            Intent startService = new Intent(context, NotificationService.class);
            startService.putExtra(Constants.WORK_DURATION_INTENT, workDuration);
            startService.putExtra(Constants.BREAK_DURATION_INTENT, breakDuration);
            startService.putExtra(Constants.LONG_BREAK_DURATION_INTENT, longBreakDuration);
            startService.putExtra(Constants.SESSIONS_BEFORE_LONG_BREAK_INTENT, sessionsBeforeLongBreak);
            startService.putExtra(Constants.ARE_LONG_BREAKS_ENABLED_INTENT, areLongBreaksEnabled);
            context.startService(startService);
        });
    }
}
