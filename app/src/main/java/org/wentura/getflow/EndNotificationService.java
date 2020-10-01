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

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import org.wentura.getflow.database.Database;

import java.time.LocalDateTime;

public class EndNotificationService extends Service {

    private CountDownTimer reminderCountDownTimer;
    private PowerManager.WakeLock wakeLock = null;
    private final long[] vibrationPattern = new long[]{0, 500, 250, 500};
    private final long vibrationPatternLength = sumArrayElements(vibrationPattern);

    private long sumArrayElements(long[] vibrationPattern) {
        int sum = 0;

        for (Long element : vibrationPattern) {
            sum += element;
        }
        return sum;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor preferenceEditor = preferences.edit();

        boolean isBreakState = preferences.getBoolean(Constants.IS_BREAK_STATE, false);

        int activityId = preferences.getInt(Constants.CURRENT_ACTIVITY_ID, 1);

        Database database = Database.getInstance(this);
        Database.databaseExecutor.execute(() -> {
            if (!database.activityDao().isDNDKeptOnBreaks(activityId)) {
                Utility.setDoNotDisturb(getApplicationContext(),
                        AudioManager.RINGER_MODE_NORMAL, activityId);
            }
        });

        preferenceEditor.putBoolean(Constants.IS_TIMER_RUNNING, false);
        preferenceEditor.putInt(Constants.TIME_LEFT, 0);

        preferenceEditor.putBoolean(Constants.IS_STOP_BUTTON_VISIBLE, true);
        preferenceEditor.putBoolean(Constants.IS_START_BUTTON_VISIBLE, true);
        preferenceEditor.putBoolean(Constants.IS_PAUSE_BUTTON_VISIBLE, false);
        preferenceEditor.putBoolean(Constants.IS_TIMER_BLINKING, true);

        if (isBreakState) {
            preferenceEditor.putBoolean(Constants.IS_SKIP_BUTTON_VISIBLE, false);
            preferenceEditor.putBoolean(Constants.IS_WORK_ICON_VISIBLE, true);
            preferenceEditor.putBoolean(Constants.IS_BREAK_ICON_VISIBLE, false);
            preferenceEditor.putBoolean(Constants.IS_BREAK_STATE, false);
            preferenceEditor.putBoolean(Constants.CENTER_BUTTONS, true);
        } else {
            preferenceEditor.putBoolean(Constants.IS_SKIP_BUTTON_VISIBLE, true);
            preferenceEditor.putBoolean(Constants.IS_WORK_ICON_VISIBLE, false);
            preferenceEditor.putBoolean(Constants.IS_BREAK_ICON_VISIBLE, true);
            preferenceEditor.putBoolean(Constants.IS_BREAK_STATE, true);
            preferenceEditor.putBoolean(Constants.CENTER_BUTTONS, false);

            preferenceEditor.putString(Constants.TIMESTAMP_OF_LAST_WORK_SESSION, LocalDateTime.now().toString());
        }

        preferenceEditor.putInt(Constants.LAST_SESSION_DURATION, 0);

        if (isBreakState) {
            Utility.updateDatabaseBreaks(getApplicationContext(),
                    preferences.getInt(Constants.LAST_SESSION_DURATION, 0), activityId);
        } else {
            Utility.updateDatabaseCompletedWorks(getApplicationContext(),
                    preferences.getInt(Constants.LAST_SESSION_DURATION, 0), activityId);

            preferenceEditor.putInt(Constants.WORK_SESSION_COUNTER,
                    preferences.getInt(Constants.WORK_SESSION_COUNTER, 0) + 1);
        }
        preferenceEditor.apply();

        Intent displayMainActivity = new Intent(getApplicationContext(), MainActivity.class);
        displayMainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(displayMainActivity);

        showEndNotification();
        vibrate();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void showEndNotification() {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext(), Constants.CHANNEL_TIMER_COMPLETED)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setOngoing(true)
                        .setDefaults(Notification.DEFAULT_SOUND)
                        .setLights(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary),
                                500, 2000);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            builder.setContentTitle(getString(R.string.app_name));
        }

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                Constants.PENDING_INTENT_OPEN_APP_REQUEST_CODE, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        builder.setContentIntent(pendingIntent);

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        boolean isBreakState = preferences.getBoolean(Constants.IS_BREAK_STATE, false);

        if (isBreakState) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                builder.setContentText(getString(R.string.break_time));
            } else {
                builder.setContentTitle(getString(R.string.break_time));
            }
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                builder.setContentText(getString(R.string.work_time));
            } else {
                builder.setContentTitle(getString(R.string.work_time));
            }
        }

        startForeground(Constants.ON_FINISH_NOTIFICATION, builder.build());
    }

    private void vibrate() {
        final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (vibrator == null) {
            return;
        }

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);

        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    Constants.WAKE_LOCK_TAG);
        }

        if (wakeLock != null) {
            wakeLock.acquire(Constants.VIBRATION_REMINDER_FREQUENCY + vibrationPatternLength);
        }

        reminderCountDownTimer = new CountDownTimer(Constants.VIBRATION_REMINDER_FREQUENCY, 1000) {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(vibrationPattern,
                            VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(500);
                }

                if (wakeLock != null) {
                    wakeLock.acquire(Constants.VIBRATION_REMINDER_FREQUENCY + vibrationPatternLength);
                }
                start();
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (reminderCountDownTimer != null) {
            reminderCountDownTimer.cancel();
        }

        if (wakeLock != null) {
            wakeLock.release();
        }
    }
}
