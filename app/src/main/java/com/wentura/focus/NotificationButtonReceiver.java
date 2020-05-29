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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import static android.media.AudioManager.RINGER_MODE_NORMAL;
import static android.media.AudioManager.RINGER_MODE_SILENT;

public class NotificationButtonReceiver extends BroadcastReceiver {

    private static final String TAG = NotificationButtonReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getStringExtra(Constants.BUTTON_ACTION);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editPreferences = preferences.edit();

        if (action == null) {
            return;
        }

        switch (action) {
            case Constants.BUTTON_STOP: {
                stopNotificationService(context);
                stopEndNotificationService(context);

                boolean isBreakState = preferences.getBoolean(Constants.IS_BREAK_STATE, false);

                int lastSessionDuration = preferences.getInt(Constants.LAST_SESSION_DURATION, 0);
                int timeLeft = preferences.getInt(Constants.TIME_LEFT, 0);

                if (timeLeft != 0) {
                    if (isBreakState) {
                        new UpdateDatabaseBreaks(context, lastSessionDuration - timeLeft).execute();
                    } else {
                        new UpdateDatabaseIncompleteWorks(context, lastSessionDuration - timeLeft).execute();
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
                editPreferences.apply();

                Intent updateUI = new Intent(Constants.UPDATE_UI);
                updateUI.putExtra(Constants.UPDATE_UI_ACTION, Constants.BUTTON_STOP);
                LocalBroadcastManager.getInstance(context).sendBroadcast(updateUI);

                Log.d(TAG, "onReceive: Button Stop");

                Utility.setDoNotDisturb(context, RINGER_MODE_NORMAL);
                break;
            }
            case Constants.BUTTON_SKIP: {
                Log.d("Pomodoro", "onReceive: SKIP");
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

                    Utility.setDoNotDisturb(context, RINGER_MODE_SILENT);
                } else {
                    editPreferences.putBoolean(Constants.IS_BREAK_STATE, true);
                    editPreferences.putBoolean(Constants.IS_WORK_ICON_VISIBLE, false);
                    editPreferences.putBoolean(Constants.IS_BREAK_ICON_VISIBLE, true);

                    if (!preferences.getBoolean(Constants.DO_NOT_DISTURB_BREAK_SETTING, false)) {
                        Utility.setDoNotDisturb(context, AudioManager.RINGER_MODE_NORMAL);
                    }
                }

                if (timeLeft != 0) {
                    if (isBreakState) {
                        new UpdateDatabaseBreaks(context, lastSessionDuration - timeLeft).execute();
                    } else {
                        new UpdateDatabaseIncompleteWorks(context, lastSessionDuration - timeLeft).execute();
                    }
                }

                editPreferences.putBoolean(Constants.IS_PAUSE_BUTTON_VISIBLE, true);
                editPreferences.putBoolean(Constants.IS_START_BUTTON_VISIBLE, false);
                editPreferences.putBoolean(Constants.IS_TIMER_RUNNING, true);
                editPreferences.putBoolean(Constants.CENTER_BUTTONS, false);
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
                    Utility.setDoNotDisturb(context, RINGER_MODE_SILENT);
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
                editPreferences.apply();

                if (!isBreakState) {
                    Utility.setDoNotDisturb(context, RINGER_MODE_NORMAL);
                }

                Intent serviceIntent = new Intent(context, NotificationService.class);
                serviceIntent.putExtra(Constants.NOTIFICATION_SERVICE,
                        Constants.NOTIFICATION_SERVICE_PAUSE);
                context.startService(serviceIntent);

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
        Intent startService = new Intent(context, NotificationService.class);
        context.startService(startService);
    }
}
