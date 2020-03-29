package com.wentura.pomodoro;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;

public class EndNotificationService extends Service {

    private static final String TAG = EndNotificationService.class.getSimpleName();
    private CountDownTimer reminderCountDownTimer;
    private PowerManager.WakeLock wakeLock = null;
    private long[] vibrationPattern = new long[]{0, 500, 250, 500};
    private long vibrationPatternLength = sumArrayElements(vibrationPattern);

    private long sumArrayElements(long[] vibrationPattern) {
        int sum = 0;

        for (Long element : vibrationPattern) {
            sum += element;
        }
        return sum;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor preferenceEditor = preferences.edit();

        boolean isBreakState = preferences.getBoolean(Constants.IS_BREAK_STATE, false);

        if (!preferences.getBoolean(Constants.DO_NOT_DISTURB_BREAK_SETTING, false)) {
            Utility.setDoNotDisturb(getApplicationContext(),
                    AudioManager.RINGER_MODE_NORMAL);
        }

        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(getApplicationContext(),
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

            if (Build.VERSION.SDK_INT >= 21) {
                mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build());
            } else {
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            }
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException exception) {
            exception.printStackTrace();
        }

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
        }
        preferenceEditor.apply();

        if (isBreakState) {
            new UpdateDatabaseBreaks(getApplicationContext(),
                    preferences.getInt(Constants.LAST_SESSION_DURATION, 0)).execute();
        } else {
            new UpdateDatabaseCompletedWorks(getApplicationContext(),
                    preferences.getInt(Constants.LAST_SESSION_DURATION, 0)).execute();
        }

        Intent displayMainActivity = new Intent(getApplicationContext(), MainActivity.class);
        displayMainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(displayMainActivity);

        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(vibrationPattern,
                        VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(500);
            }
        }

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
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), Constants.CHANNEL_TIMER_COMPLETED)
                .setSmallIcon(R.drawable.notification_icon)
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))
                .setContentTitle(getString(R.string.app_name))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setOngoing(true)
                .setLights(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary),
                        500, 2000);

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                Constants.PENDING_INTENT_OPEN_APP_REQUEST_CODE, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        builder.setContentIntent(pendingIntent);

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        boolean isBreakState = preferences.getBoolean(Constants.IS_BREAK_STATE, false);

        if (isBreakState) {
            builder.setContentText(getString(R.string.break_time));
        } else {
            builder.setContentText(getString(R.string.work_time));
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
                    Log.d(TAG, "vibrate: wakeLock != null");
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
