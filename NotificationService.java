package com.wentura.pomodoroapp;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class NotificationService extends Service {
    static final String TAG = NotificationService.class.getSimpleName();

    private boolean isTimerRunning;
    private boolean isBreakState;
    private long timeLeft;
    private CountDownTimer countDownTimer;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        SharedPreferences preferences = getSharedPreferences(Constants.MY_PREFERENCES, MODE_PRIVATE);
        Log.d(TAG, "onStartCommand: ");

        preferences.getBoolean(Constants.TIME_LEFT_NOTIFICATION_FIRST_TIME,
                true);
        isBreakState = preferences.getBoolean(Constants.IS_BREAK_STATE, false);
        isTimerRunning = preferences.getBoolean(Constants.IS_TIMER_RUNNING, false);

        if (isBreakState) {
            timeLeft = preferences.getLong(Constants.BREAK_LEFT_IN_MILLISECONDS, 0);
            Log.d(TAG, "onStartCommand: isBreakState");
        } else {
            timeLeft = preferences.getLong(Constants.WORK_LEFT_IN_MILLISECONDS, 0);
            Log.d(TAG, "onStartCommand: !isBreakState");
        }

        Log.d(TAG, "onStartCommand: timeLeft " + timeLeft);

        final Notification notification = new Notification();

        if (isTimerRunning) {
            countDownTimer = new CountDownTimer(timeLeft, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    timeLeft = millisUntilFinished;

                    SharedPreferences.Editor preferences =
                            getSharedPreferences(Constants.MY_PREFERENCES, MODE_PRIVATE).edit();

                    if (isBreakState) {
                        preferences.putLong(Constants.BREAK_LEFT_IN_MILLISECONDS, timeLeft);
                    } else {
                        preferences.putLong(Constants.WORK_LEFT_IN_MILLISECONDS, timeLeft);
                    }

                    preferences.apply();

                    notification.buildNotification(getApplicationContext(), millisUntilFinished,
                            isBreakState, isTimerRunning, false);
                    Log.d(TAG, "onTick: " + timeLeft);
                }

                @Override
                public void onFinish() {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    Log.d(TAG, "onFinish: ");
                }
            }.start();
        } else {
            SharedPreferences sharedPreferences =
                    getSharedPreferences(Constants.MY_PREFERENCES, MODE_PRIVATE);
            if (isBreakState) {
                notification.buildNotification(getApplicationContext(),
                        sharedPreferences.getLong(Constants.BREAK_LEFT_IN_MILLISECONDS, 0),
                        isBreakState, isTimerRunning, false);
            } else {
                notification.buildNotification(getApplicationContext(),
                        sharedPreferences.getLong(Constants.WORK_LEFT_IN_MILLISECONDS, 0),
                        isBreakState, isTimerRunning, false);
            }
        }
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
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        Log.d(TAG, "onDestroy: ");
    }
}
