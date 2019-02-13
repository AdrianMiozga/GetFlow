package com.wentura.pomodoroapp;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class NotificationService extends Service {
    static final String TAG = "NotificationService";

    private boolean timeLeftNotificationFirstTime;
    private boolean timerIsRunning;
    private boolean breakState;
    private long timeLeft;
    private CountDownTimer countDownTimer;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        SharedPreferences preferences = getSharedPreferences(Constants.MY_PREFERENCES, MODE_PRIVATE);
        Log.d(TAG, "onStartCommand: ");

        timeLeftNotificationFirstTime = preferences.getBoolean(Constants.TIME_LEFT_NOTIFICATION_FIRST_TIME,
                true);
        breakState = preferences.getBoolean(Constants.IS_BREAK_STATE, false);
        timerIsRunning = preferences.getBoolean(Constants.TIMER_IS_RUNNING, false);

        if (breakState) {
            timeLeft = preferences.getLong(Constants.BREAK_LEFT_IN_MILLISECONDS, 0);
        } else {
            timeLeft = preferences.getLong(Constants.WORK_LEFT_IN_MILLISECONDS, 0);
        }

        Log.d(TAG, "onStartCommand: timeLeft " + timeLeft);

        final Notification notification = new Notification();

        countDownTimer = new CountDownTimer(timeLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = millisUntilFinished;

                SharedPreferences.Editor preferences =
                        getSharedPreferences(Constants.MY_PREFERENCES, MODE_PRIVATE).edit();

                if (breakState) {
                    preferences.putLong(Constants.BREAK_LEFT_IN_MILLISECONDS, timeLeft);
                } else {
                    preferences.putLong(Constants.WORK_LEFT_IN_MILLISECONDS, timeLeft);
                }

                preferences.apply();

                notification.buildNotification(getApplicationContext(), millisUntilFinished,
                        timeLeftNotificationFirstTime, breakState, timerIsRunning, false);
                Log.d(TAG, "onTick: " + timeLeft);
            }

            @Override
            public void onFinish() {

            }
        }.start();
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        countDownTimer.cancel();
        Log.d(TAG, "onDestroy: ");
    }
}
