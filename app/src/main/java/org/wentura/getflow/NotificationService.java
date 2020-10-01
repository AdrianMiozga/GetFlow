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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import java.time.LocalDateTime;

public class NotificationService extends Service {

    private boolean isBreakState;
    private int timeLeft;
    private CountDownTimer countDownTimer;
    private PowerManager.WakeLock wakeLock = null;
    private final Handler handler = new Handler();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = null;

        if (intent != null) {
            action = intent.getStringExtra(Constants.NOTIFICATION_SERVICE);
        }

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor preferenceEditor = preferences.edit();
        final TimerNotification timerNotification = new TimerNotification();
        boolean isTimerRunning = preferences.getBoolean(Constants.IS_TIMER_RUNNING, false);
        boolean areLongBreaksEnabled = false;
        int sessionsBeforeLongBreak = Constants.DEFAULT_SESSIONS_BEFORE_LONG_BREAK;

        if (intent != null) {
            areLongBreaksEnabled = intent.getBooleanExtra(Constants.ARE_LONG_BREAKS_ENABLED_INTENT, false);
            sessionsBeforeLongBreak =
                    intent.getIntExtra(Constants.SESSIONS_BEFORE_LONG_BREAK_INTENT,
                            Constants.DEFAULT_SESSIONS_BEFORE_LONG_BREAK);
        }

        int workSessionCounter = preferences.getInt(Constants.WORK_SESSION_COUNTER, 0);
        final NotificationCompat.Builder builder =
                timerNotification.buildNotification(getApplicationContext(), isTimerRunning);

        isBreakState = preferences.getBoolean(Constants.IS_BREAK_STATE, false);

        timeLeft = preferences.getInt(Constants.TIME_LEFT, 0);

        LocalDateTime lastWorkSession =
                LocalDateTime.parse(preferences.getString(Constants.TIMESTAMP_OF_LAST_WORK_SESSION,
                        LocalDateTime.now().toString()));

        if (timeLeft == 0 && intent != null) {
            // If last work session was over specified time, reset the work counter so that for long break to occur, you
            // have to again complete all sessions.
            if (LocalDateTime.now()
                    .isAfter(lastWorkSession.plusHours(Constants.HOURS_BEFORE_WORK_SESSION_COUNT_RESETS))) {
                workSessionCounter = 0;
                preferenceEditor.putInt(Constants.WORK_SESSION_COUNTER, 0);
            }

            if (isBreakState) {
                if (workSessionCounter >= sessionsBeforeLongBreak && areLongBreaksEnabled) {
                    timeLeft = intent.getIntExtra(Constants.LONG_BREAK_DURATION_INTENT, 0) * 60_000;
                    preferenceEditor.putInt(Constants.WORK_SESSION_COUNTER, 0);
                } else {
                    timeLeft = intent.getIntExtra(Constants.BREAK_DURATION_INTENT, 0) * 60_000;
                }
            } else {
                timeLeft = intent.getIntExtra(Constants.WORK_DURATION_INTENT, 0) * 60_000;
            }
        }

        if (preferences.getInt(Constants.LAST_SESSION_DURATION, 0) == 0) {
            preferenceEditor.putInt(Constants.LAST_SESSION_DURATION, timeLeft);
        }

        if (action != null && action.equals(Constants.NOTIFICATION_SERVICE_PAUSE)) {
            cancelCountDownTimer();
            cancelAlarm();

            handler.removeCallbacksAndMessages(null);

            if (wakeLock != null) {
                wakeLock.release();
            }

            if (isBreakState) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    builder.setContentText(getApplicationContext().getString(R.string.break_time_left,
                            Utility.formatTimeForNotification(timeLeft)));
                } else {
                    builder.setContentTitle(getApplicationContext().getString(R.string.break_time_left,
                            Utility.formatTimeForNotification(timeLeft)));
                }
            } else {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    builder.setContentText(getApplicationContext().getString(R.string.work_time_left,
                            Utility.formatTimeForNotification(timeLeft)));
                } else {
                    builder.setContentTitle(getApplicationContext().getString(R.string.work_time_left,
                            Utility.formatTimeForNotification(timeLeft)));
                }
            }

            startForeground(Constants.TIME_LEFT_NOTIFICATION, builder.build());
        } else {
            // AlarmManager doesn't work when the time is less than 5 seconds,
            // so I have to use another method to trigger the end notification.
            if (timeLeft < 6000) {
                PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);

                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                            Constants.WAKE_LOCK_TAG);
                }

                if (wakeLock != null) {
                    wakeLock.acquire(timeLeft + 1000);
                }

                handler.postDelayed(() -> startService(new Intent(getApplicationContext(),
                        EndNotificationService.class)), timeLeft);
            } else {
                AlarmManager alarmManager =
                        (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);

                if (alarmManager == null) {
                    return START_STICKY;
                }

                Intent displayEndNotification = new Intent(getApplicationContext(),
                        EndNotificationService.class);

                PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(),
                        Constants.PENDING_INTENT_END_REQUEST_CODE,
                        displayEndNotification, 0);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            SystemClock.elapsedRealtime() +
                                    timeLeft, pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            SystemClock.elapsedRealtime() +
                                    timeLeft, pendingIntent);
                }
            }

            countDownTimer = new CountDownTimer(timeLeft, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    timeLeft = (int) millisUntilFinished;

                    preferenceEditor.putInt(Constants.TIME_LEFT, timeLeft);
                    preferenceEditor.apply();

                    if (isBreakState) {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                            builder.setContentText(getApplicationContext().getString(R.string.break_time_left,
                                    Utility.formatTimeForNotification(millisUntilFinished)));
                        } else {
                            builder.setContentTitle(getApplicationContext().getString(R.string.break_time_left,
                                    Utility.formatTimeForNotification(millisUntilFinished)));
                        }
                    } else {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                            builder.setContentText(getApplicationContext().getString(R.string.work_time_left,
                                    Utility.formatTimeForNotification(millisUntilFinished)));
                        } else {
                            builder.setContentTitle(getApplicationContext().getString(R.string.work_time_left,
                                    Utility.formatTimeForNotification(millisUntilFinished)));
                        }
                    }

                    Intent updateTimer = new Intent(Constants.ON_TICK);
                    updateTimer.putExtra(Constants.TIME_LEFT_INTENT, timeLeft);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(updateTimer);

                    startForeground(Constants.TIME_LEFT_NOTIFICATION, builder.build());
                }

                @Override
                public void onFinish() {
                    stopSelf();
                }
            }.start();
        }
        return START_STICKY;
    }

    private void cancelCountDownTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelCountDownTimer();

        cancelAlarm();

        handler.removeCallbacksAndMessages(null);

        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }

        SharedPreferences.Editor preferenceEditor =
                PreferenceManager.getDefaultSharedPreferences(this).edit();

        preferenceEditor.putInt(Constants.TIME_LEFT, 0);
        preferenceEditor.apply();
    }

    private void cancelAlarm() {
        AlarmManager alarmManager =
                (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) {
            return;
        }

        Intent displayEndNotification = new Intent(getApplicationContext(),
                EndNotificationService.class);

        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(),
                Constants.PENDING_INTENT_END_REQUEST_CODE,
                displayEndNotification, 0);

        alarmManager.cancel(pendingIntent);
    }
}
