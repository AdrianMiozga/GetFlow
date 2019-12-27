package com.wentura.pomodoro;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class NotificationService extends Service {
    private static final String TAG = "Hello";
    private boolean isBreakState;
    private int timeLeft;
    private CountDownTimer countDownTimer;

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
        final NotificationCompat.Builder builder =
                timerNotification.buildNotification(getApplicationContext(), isTimerRunning);

        isBreakState = preferences.getBoolean(Constants.IS_BREAK_STATE, false);

        timeLeft = preferences.getInt(Constants.TIMER_LEFT, 0);

        if (timeLeft == 0) {
            if (isBreakState) {
                timeLeft = Integer.parseInt(preferences.getString(Constants.BREAK_DURATION_SETTING,
                        Constants.DEFAULT_BREAK_TIME));
            } else {
                timeLeft = Integer.parseInt(preferences.getString(Constants.WORK_DURATION_SETTING,
                        Constants.DEFAULT_WORK_TIME));
            }
        }

        preferenceEditor.putInt(Constants.LAST_SESSION_DURATION, timeLeft);

        if (action != null && action.equals(Constants.NOTIFICATION_SERVICE_PAUSE)) {
            cancelCountDownTimer();

            if (isBreakState) {
                builder.setContentText(getApplicationContext().getString(R.string.break_time_left,
                        Utility.formatTime(timeLeft)));
            } else {
                builder.setContentText(getApplicationContext().getString(R.string.work_time_left,
                        Utility.formatTime(timeLeft)));
            }

            startForeground(Constants.TIME_LEFT_NOTIFICATION, builder.build());
        } else {
            countDownTimer = new CountDownTimer(timeLeft, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    timeLeft = (int) millisUntilFinished;

                    preferenceEditor.putInt(Constants.TIMER_LEFT, timeLeft);
                    preferenceEditor.apply();

                    if (isBreakState) {
                        builder.setContentText(getApplicationContext().getString(R.string.break_time_left,
                                Utility.formatTime(millisUntilFinished)));
                    } else {
                        builder.setContentText(getApplicationContext().getString(R.string.work_time_left,
                                Utility.formatTime(millisUntilFinished)));
                    }

                    Intent updateTimer = new Intent(Constants.ON_TICK);
                    updateTimer.putExtra(Constants.TIME_LEFT, timeLeft);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(updateTimer);

                    startForeground(Constants.TIME_LEFT_NOTIFICATION, builder.build());
                }

                @Override
                public void onFinish() {
                    Utility.setDoNotDisturb(getApplicationContext(),
                            AudioManager.RINGER_MODE_NORMAL);

                    preferenceEditor.putBoolean(Constants.IS_TIMER_RUNNING, false);
                    preferenceEditor.putInt(Constants.TIMER_LEFT, 0);

                    preferenceEditor.putBoolean(Constants.IS_STOP_BUTTON_VISIBLE, true);
                    preferenceEditor.putBoolean(Constants.IS_START_BUTTON_VISIBLE, true);
                    preferenceEditor.putBoolean(Constants.IS_PAUSE_BUTTON_VISIBLE, false);

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
                    }
                    preferenceEditor.apply();

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);

                    if (isBreakState) {
                        new UpdateDatabaseBreaks(getApplicationContext(),
                                preferences.getInt(Constants.LAST_SESSION_DURATION, 0)).execute();
                    } else {
                        new UpdateDatabaseCompletedWorks(getApplicationContext(),
                                preferences.getInt(Constants.LAST_SESSION_DURATION, 0)).execute();
                    }

                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                    Intent displayEndNotification = new Intent(getApplicationContext(),
                            EndNotificationService.class);
                    startService(displayEndNotification);

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

        SharedPreferences.Editor preferenceEditor =
                PreferenceManager.getDefaultSharedPreferences(this).edit();

        preferenceEditor.putInt(Constants.TIMER_LEFT, 0);
        preferenceEditor.apply();
    }
}
