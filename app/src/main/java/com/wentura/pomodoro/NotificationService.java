package com.wentura.pomodoro;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class NotificationService extends Service {
    static final String TAG = NotificationService.class.getSimpleName();

    private boolean isBreakState;
    private long timeLeft;
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
                timerNotification.buildNotification(getApplicationContext(), 0, isBreakState,
                        isTimerRunning, false);

        isBreakState = preferences.getBoolean(Constants.IS_BREAK_STATE, false);

        if (isBreakState) {
            timeLeft = preferences.getLong(Constants.BREAK_LEFT_IN_MILLISECONDS, 0);
            Log.d(TAG, "onStartCommand: isBreakState");
        } else {
            timeLeft = preferences.getLong(Constants.WORK_LEFT_IN_MILLISECONDS, 0);
            Log.d(TAG, "onStartCommand: !isBreakState");
        }
        Log.d(TAG, "onStartCommand: timeLeft " + timeLeft);

        if (action != null && action.equals(Constants.NOTIFICATION_SERVICE)) {
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            if (isBreakState) {
                builder.setContentText(getApplicationContext().getString(R.string.break_time_left,
                        Utility.formatTime(getApplicationContext(), timeLeft)));
            } else {
                builder.setContentText(getApplicationContext().getString(R.string.work_time_left,
                        Utility.formatTime(getApplicationContext(), timeLeft)));
            }
            startForeground(Constants.TIME_LEFT_NOTIFICATION, builder.build());
            Log.d(TAG, "onStartCommand: NOTIFICATION_SERVICE");
        } else {
            if (isTimerRunning) {
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }
                countDownTimer = new CountDownTimer(timeLeft, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        timeLeft = millisUntilFinished;

                        if (isBreakState) {
                            preferenceEditor.putLong(Constants.BREAK_LEFT_IN_MILLISECONDS, timeLeft);
                            builder.setContentText(getApplicationContext().getString(R.string.break_time_left, Utility.formatTime(getApplicationContext(), millisUntilFinished)));
                        } else {
                            preferenceEditor.putLong(Constants.WORK_LEFT_IN_MILLISECONDS, timeLeft);
                            builder.setContentText(getApplicationContext().getString(R.string.work_time_left, Utility.formatTime(getApplicationContext(), millisUntilFinished)));
                        }

                        preferenceEditor.apply();

                        startForeground(Constants.TIME_LEFT_NOTIFICATION, builder.build());

                        Log.d(TAG, "onTick: " + timeLeft);
                    }

                    @Override
                    public void onFinish() {
                        preferenceEditor.putBoolean(Constants.IS_TIMER_RUNNING, false);
                        preferenceEditor.putBoolean(Constants.IS_BREAK_STARTED, false);
                        preferenceEditor.putBoolean(Constants.IS_WORK_STARTED, false);
                        if (isBreakState) {
                            preferenceEditor.putBoolean(Constants.IS_BREAK_STATE, false);
                        } else {
                            preferenceEditor.putBoolean(Constants.IS_BREAK_STATE, true);
                        }
                        preferenceEditor.apply();

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        if (isBreakState) {
                            intent.putExtra(Constants.UPDATE_DATABASE_INTENT, Constants.UPDATE_BREAKS);
                        } else {
                            intent.putExtra(Constants.UPDATE_DATABASE_INTENT, Constants.UPDATE_WORKS);
                        }
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        Log.d(TAG, "onFinish: ");

                        Intent displayEndNotification = new Intent(getApplicationContext(),
                                EndNotificationService.class);
                        startService(displayEndNotification);
                    }
                }.start();
            } else {
                if (isBreakState) {
                    builder.setContentText(getApplicationContext().getString(R.string.break_time_left, Utility.formatTime(getApplicationContext(), timeLeft)));
                } else {
                    builder.setContentText(getApplicationContext().getString(R.string.work_time_left, Utility.formatTime(getApplicationContext(), timeLeft)));
                }
                startForeground(Constants.TIME_LEFT_NOTIFICATION, builder.build());
            }
        }
        return START_STICKY;
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
