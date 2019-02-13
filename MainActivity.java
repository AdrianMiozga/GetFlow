package com.wentura.pomodoroapp;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.wentura.pomodoroapp.settings.SettingsActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static android.media.AudioManager.RINGER_MODE_NORMAL;
import static android.media.AudioManager.RINGER_MODE_SILENT;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private CountDownTimer countDownTimer;
    private Notification notification;
    private ImageButton startPauseButton;
    private ImageButton stopButton;
    private ImageButton skipButton;
    private ImageView workBreakIcon;
    private TextView countdownText;

    private boolean breakState = false;
    private boolean workStarted = false;
    private boolean breakStarted = false;
    private boolean timeLeftNotificationFirstTime = true;
    private boolean timerIsRunning = true;
    private long breakLeftInMilliseconds = 0;
    private long workLeftInMilliseconds = 0;

    private BroadcastReceiver statusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra(Constants.BUTTON_ACTION);
            switch (action) {
                case Constants.BUTTON_SKIP:
                    skipTimer();
                    break;
                case Constants.BUTTON_PAUSE_RESUME:
                    startPauseTimer();
                    Log.d(TAG, "onReceive: PAUSE");
                    break;
                case Constants.BUTTON_STOP:
                    stopTimer();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        countdownText = findViewById(R.id.countdown_text_view);
        startPauseButton = findViewById(R.id.start_pause_button);
        stopButton = findViewById(R.id.stop_button);
        workBreakIcon = findViewById(R.id.work_break_icon);
        skipButton = findViewById(R.id.skip_button);

        notification = new Notification();

        Log.d(TAG, "onCreate: ");

        LocalBroadcastManager.getInstance(this).registerReceiver(
                statusReceiver, new IntentFilter(ActionReceiver.BUTTON_CLICKED));

        setupNotificationChannels();

        startPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPauseTimer();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setMessage(R.string.dialog_stop)
                        .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                stopTimer();
                            }
                        })
                        .setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        }).show();
            }
        });

        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                skipTimer();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        toggleKeepScreenOn();

        Intent intent = new Intent(this, NotificationService.class);
        this.stopService(intent);

        SharedPreferences preferences = getSharedPreferences(Constants.MY_PREFERENCES, MODE_PRIVATE);

        workStarted = preferences.getBoolean(Constants.IS_WORK_STARTED, false);
        breakStarted = preferences.getBoolean(Constants.IS_BREAK_STARTED, false);
        workLeftInMilliseconds = preferences.getLong(Constants.WORK_LEFT_IN_MILLISECONDS, 0);
        breakLeftInMilliseconds = preferences.getLong(Constants.BREAK_LEFT_IN_MILLISECONDS, 0);
        breakState = preferences.getBoolean(Constants.IS_BREAK_STATE, false);
        timeLeftNotificationFirstTime = preferences.getBoolean(Constants.TIME_LEFT_NOTIFICATION_FIRST_TIME,
                true);
        timerIsRunning = preferences.getBoolean(Constants.TIMER_IS_RUNNING, false);

        if (timerIsRunning && !breakState) {
            startTimer(workLeftInMilliseconds);
        }

        if (timerIsRunning && breakState) {
            startTimer(breakLeftInMilliseconds);
        }

        if (!workStarted && !breakStarted) {
            skipButton.setVisibility(View.INVISIBLE);
            workBreakIcon.setVisibility(View.INVISIBLE);
            stopButton.setVisibility(View.INVISIBLE);
        }

        if (timerIsRunning) {
            startPauseButton.setBackgroundResource(R.drawable.ic_pause_button);
        } else {
            startPauseButton.setBackgroundResource(R.drawable.ic_play_button);
        }

        if (workStarted && !timerIsRunning) {
            updateTimerTextView(workLeftInMilliseconds);
            Log.d(TAG, "onCreate: workStarted && !timerIsRunning");
        }

        if (breakStarted && !timerIsRunning) {
            updateTimerTextView(breakLeftInMilliseconds);
            Log.d(TAG, "onCreate: breakStarted && !timerIsRunning");
        }

        if (!workStarted && !breakState) {
            workLeftInMilliseconds = getMillisecondsFromSettings(Constants.WORK_DURATION_SETTING);
            updateTimerTextView(getMillisecondsFromSettings(Constants.WORK_DURATION_SETTING));
            Log.d(TAG, "onCreate: !workStarted && !breakState");
        }

        if (!breakStarted) {
            breakLeftInMilliseconds = getMillisecondsFromSettings(Constants.BREAK_DURATION_SETTINGS);
            Log.d(TAG, "onCreate: !breakStarted");
        }

        if (breakStarted) {
            workBreakIcon.setImageResource(R.drawable.break_icon);
            Log.d(TAG, "onCreate: breakStarted");
        }

        if (workStarted) {
            workBreakIcon.setImageResource(R.drawable.work_icon);
            Log.d(TAG, "onCreate: workStarted");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (countDownTimer != null) {
            countDownTimer.cancel();
            Log.d(TAG, "onPause: countCancel");
        }

        if (timerIsRunning) {
            Intent intent = new Intent(this, NotificationService.class);
            this.startService(intent);
            Log.d(TAG, "onPause: timerIsRunning");
        }

        SharedPreferences.Editor preferences =
                getSharedPreferences(Constants.MY_PREFERENCES, MODE_PRIVATE).edit();

        preferences.putBoolean(Constants.IS_WORK_STARTED, workStarted);
        preferences.putBoolean(Constants.IS_BREAK_STARTED, breakStarted);
        preferences.putLong(Constants.WORK_LEFT_IN_MILLISECONDS, workLeftInMilliseconds);
        preferences.putLong(Constants.BREAK_LEFT_IN_MILLISECONDS, breakLeftInMilliseconds);
        preferences.putBoolean(Constants.TIMER_IS_RUNNING, timerIsRunning);
        preferences.putBoolean(Constants.IS_BREAK_STATE, breakState);
        preferences.putBoolean(Constants.TIME_LEFT_NOTIFICATION_FIRST_TIME, timeLeftNotificationFirstTime);
        preferences.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");

        LocalBroadcastManager.getInstance(this).unregisterReceiver(statusReceiver);
    }

    private void skipTimer() {
        Log.d(TAG, "skipTimer: ");
        countDownTimer.cancel();

        if (breakState) {
            startTimer(getMillisecondsFromSettings(Constants.WORK_DURATION_SETTING));
            breakLeftInMilliseconds = getMillisecondsFromSettings
                    (Constants.BREAK_DURATION_SETTINGS);
            toggleDoNotDisturb(this, RINGER_MODE_SILENT);
            workBreakIcon.setImageResource(R.drawable.work_icon);
            breakState = false;
            breakStarted = false;
            workStarted = true;
        } else {
            startTimer(getMillisecondsFromSettings(Constants.BREAK_DURATION_SETTINGS));
            toggleDoNotDisturb(this, RINGER_MODE_NORMAL);
            workBreakIcon.setImageResource(R.drawable.break_icon);
            breakState = true;
            breakStarted = true;
            workStarted = false;
            Log.d(TAG, "skipTimer: work");
        }
        startPauseButton.setBackgroundResource(R.drawable.ic_pause_button);
    }

    private long getMillisecondsFromSettings(String durationSetting) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String value = null;
        if (durationSetting.equals(Constants.WORK_DURATION_SETTING)) {
            value = sharedPreferences.getString(Constants.WORK_DURATION_SETTING, Constants.DEFAULT_WORK_TIME);
        }

        if (durationSetting.equals(Constants.BREAK_DURATION_SETTINGS)) {
            value = sharedPreferences.getString(Constants.BREAK_DURATION_SETTINGS, Constants.DEFAULT_BREAK_TIME);
        }

        if (value != null) {
            return (Integer.parseInt(value) * 60000);
        } else {
            return 0;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                startSettingsActivity();
                return true;
        }
        return false;
    }

    private void startSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void startPauseTimer() {
        stopButton.setVisibility(View.VISIBLE);
        skipButton.setVisibility(View.VISIBLE);
        if (timerIsRunning) {
            pauseTimer();
            if (breakState) {
                notification.buildNotification(this, breakLeftInMilliseconds,
                        timeLeftNotificationFirstTime, true, timerIsRunning, true);
            } else {
                notification.buildNotification(this, workLeftInMilliseconds,
                        timeLeftNotificationFirstTime, false, timerIsRunning, true);
            }
        } else {
            workBreakIcon.setVisibility(View.VISIBLE);
            if (breakState) {
                startTimer(breakLeftInMilliseconds);
                breakStarted = true;
                workBreakIcon.setImageResource(R.drawable.break_icon);
            } else {
                startTimer(workLeftInMilliseconds);
                toggleDoNotDisturb(this, RINGER_MODE_SILENT);
                notification.buildNotification(this, workLeftInMilliseconds,
                        timeLeftNotificationFirstTime, breakState, timerIsRunning, true);
                workStarted = true;
                workBreakIcon.setImageResource(R.drawable.work_icon);
            }
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
            notificationManager.cancel(Constants.ON_FINISH_NOTIFICATION);
            startPauseButton.setBackgroundResource(R.drawable.ic_pause_button);
        }
        timeLeftNotificationFirstTime = false;
    }

    private void stopTimer() {
        stopButton.setVisibility(View.INVISIBLE);
        workBreakIcon.setVisibility(View.INVISIBLE);
        startPauseButton.setVisibility(View.VISIBLE);
        skipButton.setVisibility(View.INVISIBLE);
        pauseTimer();
        updateTimerTextView(getMillisecondsFromSettings(Constants.WORK_DURATION_SETTING));
        breakLeftInMilliseconds = getMillisecondsFromSettings(Constants.BREAK_DURATION_SETTINGS);
        workLeftInMilliseconds = getMillisecondsFromSettings(Constants.WORK_DURATION_SETTING);
        breakState = false;
        workStarted = false;
        breakStarted = false;
        timeLeftNotificationFirstTime = true;
        Log.d(TAG, "stopTimer: ");
        cancelAllNotifications();
    }

    private void cancelAllNotifications() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        notificationManager.cancelAll();
    }

    private void startTimer(long timeInMilliseconds) {
        timerIsRunning = true;

        countDownTimer = new CountDownTimer(timeInMilliseconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (breakState) {
                    breakLeftInMilliseconds = millisUntilFinished;
                } else {
                    workLeftInMilliseconds = millisUntilFinished;
                }
                updateTimerTextView(millisUntilFinished);
                notification.buildNotification(getApplicationContext(), millisUntilFinished,
                        timeLeftNotificationFirstTime, breakState, timerIsRunning, true);
            }

            @Override
            public void onFinish() {
                cancelAllNotifications();
                showEndNotification();
                if (breakState) {
                    updateTimerTextView(getMillisecondsFromSettings(Constants.WORK_DURATION_SETTING));
                    workBreakIcon.setBackgroundResource(R.drawable.work_icon);
                    breakState = false;
                    breakStarted = false;
                } else {
                    toggleDoNotDisturb(getApplicationContext(), RINGER_MODE_NORMAL);
                    updateTimerTextView(getMillisecondsFromSettings(Constants.BREAK_DURATION_SETTINGS));
                    workBreakIcon.setBackgroundResource(R.drawable.break_icon);
                    breakState = true;
                    workStarted = false;
                    Log.d(TAG, "onFinish: work");
                }
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                timerIsRunning = false;
                timeLeftNotificationFirstTime = true;
                startPauseButton.setBackgroundResource(R.drawable.ic_play_button);
                skipButton.setVisibility(View.INVISIBLE);
            }
        }.start();
    }

    private String calculateTimeLeft(long milliseconds) {
        return formatTime(getMinutes(milliseconds), getSeconds(milliseconds));
    }

    @NonNull
    private String formatTime(int minutes, int seconds) {
        String timeLeft;

        timeLeft = "" + minutes;
        timeLeft += ":";
        if (seconds < 10) {
            timeLeft += "0";
        }
        timeLeft += "" + seconds;
        return timeLeft;
    }

    private int getSeconds(long milliseconds) {
        return (int) (milliseconds % 60000 / 1000);
    }

    private int getMinutes(long milliseconds) {
        return (int) (milliseconds / 60000);
    }

    private void updateTimerTextView(long timeInMilliseconds) {
        countdownText.setText(calculateTimeLeft(timeInMilliseconds));

        Log.d(TAG, "updateTimerTextView: " + calculateTimeLeft(timeInMilliseconds));
    }

    private void pauseTimer() {
        Log.d(TAG, "pauseTimer: ");
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        timerIsRunning = false;
        toggleDoNotDisturb(this, RINGER_MODE_NORMAL);
        startPauseButton.setBackgroundResource(R.drawable.ic_play_button);
    }

    private void displayNotification(NotificationCompat.Builder mBuilder) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        notificationManagerCompat.notify(Constants.ON_FINISH_NOTIFICATION, mBuilder.build());
    }

    private void createIntentToOpenApp(NotificationCompat.Builder mBuilder) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                Constants.PENDING_INTENT_OPEN_APP_REQUEST_CODE, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        mBuilder.setContentIntent(pendingIntent);
    }

    private void showEndNotification() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), Constants.CHANNEL_TIMER_COMPLETED)
                .setSmallIcon(R.drawable.ic_logo)
                .setColor(getColor(R.color.colorPrimary))
                .setContentTitle(getString(R.string.app_name))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setOngoing(true);

        createIntentToOpenApp(mBuilder);
        showEndNotificationContent(mBuilder);
        displayNotification(mBuilder);
    }

    private void showEndNotificationContent(NotificationCompat.Builder mBuilder) {
        if (breakState) {
            mBuilder.setContentText(getString(R.string.work_time));
        } else {
            mBuilder.setContentText(getString(R.string.break_time));
        }
    }

    private void setupNotificationChannels() {
        if (isAndroidAtLeastOreo()) {
            NotificationChannel timerCompletedChannel = new NotificationChannel(Constants.CHANNEL_TIMER_COMPLETED,
                    "Pomodoro Timer Completed", NotificationManager.IMPORTANCE_HIGH);
            NotificationChannel timerChannel = new NotificationChannel(Constants.CHANNEL_TIMER,
                    "Pomodoro Timer", NotificationManager.IMPORTANCE_LOW);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                timerCompletedChannel.setShowBadge(false);
                timerChannel.setShowBadge(false);
                notificationManager.createNotificationChannel(timerCompletedChannel);
                notificationManager.createNotificationChannel(timerChannel);
            }
        }
    }

    private boolean isAndroidAtLeastOreo() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    private void toggleDoNotDisturb(Context context, int mode) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean("do_not_disturb_setting", false)) {
            setRingerMode(context, mode);
        }
    }

    private void setRingerMode(Context context, int mode) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null && notificationManager.isNotificationPolicyAccessGranted()) {
            AudioManager audioManager = context.getSystemService(AudioManager.class);
            if (audioManager != null) {
                audioManager.setRingerMode(mode);
            }
        }
    }

    private void toggleKeepScreenOn() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean("keep_screen_on_setting", false)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }
}
