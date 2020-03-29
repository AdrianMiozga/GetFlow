package com.wentura.pomodoro;

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
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class NotificationService extends Service {
    private static final String TAG = NotificationService.class.getSimpleName();
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
        final NotificationCompat.Builder builder =
                timerNotification.buildNotification(getApplicationContext(), isTimerRunning);

        isBreakState = preferences.getBoolean(Constants.IS_BREAK_STATE, false);

        timeLeft = preferences.getInt(Constants.TIME_LEFT, 0);

        if (timeLeft == 0) {
            if (isBreakState) {
                timeLeft = Integer.parseInt(preferences.getString(Constants.BREAK_DURATION_SETTING,
                        Constants.DEFAULT_BREAK_TIME)) /* * 60000 */;
            } else {
                timeLeft = Integer.parseInt(preferences.getString(Constants.WORK_DURATION_SETTING,
                        Constants.DEFAULT_WORK_TIME) /* * 60000 */);
            }
        }

        Log.d(TAG, "onStartCommand: timeLeft = " + timeLeft);

        preferenceEditor.putInt(Constants.LAST_SESSION_DURATION, timeLeft);

        if (action != null && action.equals(Constants.NOTIFICATION_SERVICE_PAUSE)) {
            cancelCountDownTimer();
            cancelAlarm();

            handler.removeCallbacksAndMessages(null);

            if (wakeLock != null) {
                wakeLock.release();
            }

            if (isBreakState) {
                builder.setContentText(getApplicationContext().getString(R.string.break_time_left,
                        Utility.formatTimeForNotification(timeLeft)));
            } else {
                builder.setContentText(getApplicationContext().getString(R.string.work_time_left,
                        Utility.formatTimeForNotification(timeLeft)));
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

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(getApplicationContext(),
                                EndNotificationService.class);
                        startService(intent);
                    }
                }, timeLeft);
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
                        builder.setContentText(getApplicationContext().getString(R.string.break_time_left,
                                Utility.formatTimeForNotification(millisUntilFinished)));
                    } else {
                        builder.setContentText(getApplicationContext().getString(R.string.work_time_left,
                                Utility.formatTimeForNotification(millisUntilFinished)));
                    }

                    Intent updateTimer = new Intent(Constants.ON_TICK);
                    updateTimer.putExtra(Constants.TIME_LEFT_INTENT, timeLeft);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(updateTimer);

                    startForeground(Constants.TIME_LEFT_NOTIFICATION, builder.build());
                }

                @Override
                public void onFinish() {
                    Log.d(TAG, "onFinish: ");
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
