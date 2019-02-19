package com.wentura.pomodoro;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.wentura.pomodoro.database.Database;
import com.wentura.pomodoro.database.Pomodoro;
import com.wentura.pomodoro.settings.SettingsActivity;

import java.lang.ref.WeakReference;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
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
    private Database database;

    private boolean isBreakState = false;
    private boolean isWorkStarted = false;
    private boolean isBreakStarted = false;
    private boolean isTimerRunning = true;
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

//        database = Database.getInstance(getApplicationContext());
//
//        database.pomodoroDao().insertPomodoro(new Pomodoro("2019-02-17", 1, 2));
//        database.pomodoroDao().insertPomodoro(new Pomodoro("2019-02-16", 1, 2));
//        database.pomodoroDao().insertPomodoro(new Pomodoro("2019-02-06", 23, 2));
//        database.pomodoroDao().insertPomodoro(new Pomodoro("2019-02-14", 1, 2));
//        database.pomodoroDao().insertPomodoro(new Pomodoro("2019-02-01", 6, 6));
//        database.pomodoroDao().insertPomodoro(new Pomodoro("2019-02-12", 15, 2));
//        database.pomodoroDao().insertPomodoro(new Pomodoro("2018-02-11", 5, 2));
//        database.pomodoroDao().insertPomodoro(new Pomodoro("2019-02-10", 3, 2));

        Log.d(TAG, "onCreate: ");

        LocalBroadcastManager.getInstance(this).registerReceiver(
                statusReceiver, new IntentFilter(Constants.BUTTON_CLICKED));

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

        Utility.toggleKeepScreenOn(this);

        loadData();
        setupUI();
        stopNotificationService();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        String key = intent.getStringExtra(Constants.UPDATE_DATABASE_INTENT);
        if (key != null) {
            database = Database.getInstance(this);
            switch (key) {
                case Constants.UPDATE_BREAKS:
                    new UpdateDatabaseBreaks(this).execute();
                    break;
                case Constants.UPDATE_WORKS:
                    new UpdateDatabaseWorks(this).execute();
                    break;
            }
        }
    }

    private void stopNotificationService() {
        Intent intent = new Intent(this, NotificationService.class);
        this.stopService(intent);
    }

    private void setupUI() {
        stopButton.setVisibility(View.INVISIBLE);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            StatusBarNotification[] notifications = notificationManager.getActiveNotifications();

            for (StatusBarNotification notification : notifications) {
                if (notification.getId() == Constants.ON_FINISH_NOTIFICATION || notification.getId() == Constants.TIME_LEFT_NOTIFICATION) {
                    stopButton.setVisibility(View.VISIBLE);

                    if (isBreakState) {
                        Log.d(TAG, "setupUI: isBreakState");
                        updateTimerTextView(breakLeftInMilliseconds);
                    }

                    if (!isWorkStarted && !isBreakStarted && !isBreakState) {
                        switchToWaitingStateLayout();
                    }
                    Log.d(TAG, "setupUI: StopVisible");
                } else {
                    stopButton.setVisibility(View.INVISIBLE);
                }
            }
        }

        if (isTimerRunning && !isBreakState) {
            startTimer(this, workLeftInMilliseconds);
            Log.d(TAG, "setupUI: isTimerRunning && !isBreakState");
        }

        if (isTimerRunning && isBreakState) {
            startTimer(this, breakLeftInMilliseconds);
            Log.d(TAG, "setupUI: isTimerRunning && isBreakState");
        }

        if (!isWorkStarted && !isBreakStarted && !isBreakState) {
            skipButton.setVisibility(View.INVISIBLE);
            Log.d(TAG, "setupUI: !isWorkStarted && !isBreakStarted && !isBreakState");
        }

        if (isTimerRunning) {
            startPauseButton.setBackgroundResource(R.drawable.ic_pause_button);
            Log.d(TAG, "setupUI: isTimerRunning");
        } else {
            startPauseButton.setBackgroundResource(R.drawable.ic_play_button);
            Log.d(TAG, "setupUI: !isTimerRunning");
        }

        if (isWorkStarted && !isTimerRunning) {
            updateTimerTextView(workLeftInMilliseconds);
            Log.d(TAG, "setupUI: isWorkStarted && !isTimerRunning");
        }

        if (isBreakStarted && !isTimerRunning) {
            updateTimerTextView(breakLeftInMilliseconds);
            Log.d(TAG, "setupUI: isBreakStarted && !isTimerRunning");
        }

        if (!isWorkStarted && !isBreakState) {
            workLeftInMilliseconds = getMillisecondsFromSettings(Constants.WORK_DURATION_SETTING);
            updateTimerTextView(getMillisecondsFromSettings(Constants.WORK_DURATION_SETTING));
            Log.d(TAG, "setupUI: !isWorkStarted && !isBreakState");
        }

        if (isBreakStarted) {
            workBreakIcon.setImageResource(R.drawable.break_icon);
            Log.d(TAG, "setupUI: isBreakStarted");
        } else {
            breakLeftInMilliseconds = getMillisecondsFromSettings(Constants.BREAK_DURATION_SETTING);
            workBreakIcon.setImageResource(R.drawable.work_icon);
            Log.d(TAG, "setupUI: !isBreakStarted");
        }

        if (isBreakState) {
            workBreakIcon.setImageResource(R.drawable.break_icon);
            Log.d(TAG, "setupUI: isBreakState");
        }

        if (isWorkStarted) {
            workBreakIcon.setImageResource(R.drawable.work_icon);
            Log.d(TAG, "setupUI: isWorkStarted");
        }
    }

    private void loadData() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        isWorkStarted = preferences.getBoolean(Constants.IS_WORK_STARTED, false);
        isBreakStarted = preferences.getBoolean(Constants.IS_BREAK_STARTED, false);
        workLeftInMilliseconds = preferences.getLong(Constants.WORK_LEFT_IN_MILLISECONDS, 0);
        breakLeftInMilliseconds = preferences.getLong(Constants.BREAK_LEFT_IN_MILLISECONDS, 0);
        isBreakState = preferences.getBoolean(Constants.IS_BREAK_STATE, false);
        isTimerRunning = preferences.getBoolean(Constants.IS_TIMER_RUNNING, false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (countDownTimer != null) {
            countDownTimer.cancel();
            Log.d(TAG, "onPause: countCancel");
        }

        if (isWorkStarted || isBreakStarted) {
            Intent intent = new Intent(this, NotificationService.class);
            ContextCompat.startForegroundService(this, intent);
            Log.d(TAG, "onPause: isWorkStarted || isBreakStarted");
        }

        SharedPreferences.Editor preferences =
                PreferenceManager.getDefaultSharedPreferences(this).edit();

        preferences.putBoolean(Constants.IS_WORK_STARTED, isWorkStarted);
        preferences.putBoolean(Constants.IS_BREAK_STARTED, isBreakStarted);
        preferences.putLong(Constants.WORK_LEFT_IN_MILLISECONDS, workLeftInMilliseconds);
        preferences.putLong(Constants.BREAK_LEFT_IN_MILLISECONDS, breakLeftInMilliseconds);
        preferences.putBoolean(Constants.IS_TIMER_RUNNING, isTimerRunning);
        preferences.putBoolean(Constants.IS_BREAK_STATE, isBreakState);
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
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        notificationManager.cancel(Constants.ON_FINISH_NOTIFICATION);

        if (isBreakState) {
            startTimer(this, getMillisecondsFromSettings(Constants.WORK_DURATION_SETTING));
            breakLeftInMilliseconds = getMillisecondsFromSettings
                    (Constants.BREAK_DURATION_SETTING);
            Utility.toggleDoNotDisturb(this, RINGER_MODE_SILENT);
            workBreakIcon.setImageResource(R.drawable.work_icon);
            isBreakState = false;
            isBreakStarted = false;
            isWorkStarted = true;
        } else {
            startTimer(this, getMillisecondsFromSettings(Constants.BREAK_DURATION_SETTING));
            Utility.toggleDoNotDisturb(this, RINGER_MODE_NORMAL);
            workBreakIcon.setImageResource(R.drawable.break_icon);
            isBreakState = true;
            isBreakStarted = true;
            isWorkStarted = false;
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

        if (durationSetting.equals(Constants.BREAK_DURATION_SETTING)) {
            value = sharedPreferences.getString(Constants.BREAK_DURATION_SETTING, Constants.DEFAULT_BREAK_TIME);
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
            case R.id.statistics:
                startStatisticsActivity();
                return true;
        }
        return false;
    }

    private void startSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void startStatisticsActivity() {
        Intent intent = new Intent(this, StatisticsActivity.class);
        startActivity(intent);
    }

    private void startPauseTimer() {
        Log.d(TAG, "startPauseTimer: ");
        stopButton.setVisibility(View.VISIBLE);
        skipButton.setVisibility(View.VISIBLE);
        if (isTimerRunning) {
            pauseTimer();
            if (isBreakState) {
                notification.buildNotification(this, breakLeftInMilliseconds,
                        true, isTimerRunning, true);
            } else {
                notification.buildNotification(this, workLeftInMilliseconds,
                        false, isTimerRunning, true);
            }
        } else {
            workBreakIcon.setVisibility(View.VISIBLE);
            if (isBreakState) {
                startTimer(this, breakLeftInMilliseconds);
                isBreakStarted = true;
                workBreakIcon.setImageResource(R.drawable.break_icon);
                Log.d(TAG, "startPauseTimer: !isTimerRunning && isBreakState");
            } else {
                startTimer(this, workLeftInMilliseconds);
                Utility.toggleDoNotDisturb(this, RINGER_MODE_SILENT);
                notification.buildNotification(this, workLeftInMilliseconds,
                        isBreakState, isTimerRunning, true);
                isWorkStarted = true;
                workBreakIcon.setImageResource(R.drawable.work_icon);
                switchToNormalLayout();
                Log.d(TAG, "startPauseTimer: !isTimerRunning && !isBreakState");
            }
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
            notificationManager.cancel(Constants.ON_FINISH_NOTIFICATION);
            startPauseButton.setBackgroundResource(R.drawable.ic_pause_button);
        }
    }

    private void stopTimer() {
        switchToNormalLayout();
        stopButton.setVisibility(View.INVISIBLE);
        startPauseButton.setVisibility(View.VISIBLE);
        skipButton.setVisibility(View.INVISIBLE);
        pauseTimer();
        workBreakIcon.setImageResource(R.drawable.work_icon);
        updateTimerTextView(getMillisecondsFromSettings(Constants.WORK_DURATION_SETTING));
        breakLeftInMilliseconds = getMillisecondsFromSettings(Constants.BREAK_DURATION_SETTING);
        workLeftInMilliseconds = getMillisecondsFromSettings(Constants.WORK_DURATION_SETTING);
        isBreakState = false;
        isWorkStarted = false;
        isBreakStarted = false;
        Log.d(TAG, "stopTimer: ");
        cancelAllNotifications();
    }

    private void cancelAllNotifications() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        notificationManager.cancelAll();
    }

    private void startTimer(final MainActivity context, long timeInMilliseconds) {
        isTimerRunning = true;

        countDownTimer = new CountDownTimer(timeInMilliseconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (isBreakState) {
                    breakLeftInMilliseconds = millisUntilFinished;
                } else {
                    workLeftInMilliseconds = millisUntilFinished;
                }
                updateTimerTextView(millisUntilFinished);
                notification.buildNotification(getApplicationContext(), millisUntilFinished,
                        isBreakState, isTimerRunning, true);
            }

            @Override
            public void onFinish() {
                cancelAllNotifications();
                showEndNotification();
                database = Database.getInstance(context);
                if (isBreakState) {
                    updateTimerTextView(getMillisecondsFromSettings(Constants.WORK_DURATION_SETTING));
                    breakLeftInMilliseconds =
                            getMillisecondsFromSettings(Constants.BREAK_DURATION_SETTING);
                    workBreakIcon.setImageResource(R.drawable.work_icon);
                    isBreakState = false;
                    isBreakStarted = false;
                    Log.d(TAG, "onFinish: isBreakState");

                    skipButton.setVisibility(View.GONE);
                    new UpdateDatabaseBreaks(context).execute();

                    switchToWaitingStateLayout();
                } else {
                    Utility.toggleDoNotDisturb(getApplicationContext(), RINGER_MODE_NORMAL);
                    updateTimerTextView(getMillisecondsFromSettings(Constants.BREAK_DURATION_SETTING));
                    workLeftInMilliseconds = getMillisecondsFromSettings(Constants.WORK_DURATION_SETTING);
                    workBreakIcon.setImageResource(R.drawable.break_icon);
                    isBreakState = true;
                    isWorkStarted = false;
                    Log.d(TAG, "onFinish: !isBreakState");
                    new UpdateDatabaseWorks(context).execute();
                }
                isTimerRunning = false;
                startPauseButton.setBackgroundResource(R.drawable.ic_play_button);
            }
        }.start();
    }

    private static class UpdateDatabaseBreaks extends AsyncTask<Void, Void, Void> {
        private WeakReference<MainActivity> weakReference;

        UpdateDatabaseBreaks(MainActivity context) {
            this.weakReference = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String currentDate = Utility.getCurrentDate();

            if (weakReference.get().database.pomodoroDao().getLatestDate() != null) {
                weakReference.get().database.pomodoroDao().updateCompletedBreaks(weakReference.get().database.pomodoroDao().getCompletedBreaks(currentDate) + 1, currentDate);
            } else {
                weakReference.get().database.pomodoroDao().insertPomodoro(new Pomodoro(currentDate, 0, 1));
            }
            return null;
        }
    }

    private static class UpdateDatabaseWorks extends AsyncTask<Void, Void, Void> {
        private WeakReference<MainActivity> weakReference;

        UpdateDatabaseWorks(MainActivity context) {
            this.weakReference = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String currentDate = Utility.getCurrentDate();

            Log.d(TAG, "doInBackground: currentDate " + currentDate);

            if (weakReference.get().database.pomodoroDao().getLatestDate() != null) {
                Log.d(TAG, "doInBackground: database.pomodoroDao().getLatestDate() != null");
                weakReference.get().database.pomodoroDao().updateCompletedWorks(weakReference.get().database.pomodoroDao().getCompletedWorks(currentDate) + 1, currentDate);
            } else {
                Log.d(TAG, "doInBackground: database.pomodoroDao().getLatestDate() == null");
                weakReference.get().database.pomodoroDao().insertPomodoro(new Pomodoro(currentDate, 1, 0));
            }
            return null;
        }
    }

    private void switchToWaitingStateLayout() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(getApplicationContext(), R.layout.activity_main_waiting_state);
        constraintSet.applyTo((ConstraintLayout) findViewById(R.id.constraint_layout));
    }

    private void switchToNormalLayout() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(getApplicationContext(), R.layout.activity_main);
        constraintSet.applyTo((ConstraintLayout) findViewById(R.id.constraint_layout));
    }

    private void updateTimerTextView(long timeInMilliseconds) {
        countdownText.setText(Utility.formatTime(this, timeInMilliseconds));

        Log.d(TAG, "updateTimerTextView: " + Utility.formatTime(this, timeInMilliseconds));
    }

    private void pauseTimer() {
        Log.d(TAG, "pauseTimer: ");
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = false;
        Utility.toggleDoNotDisturb(this, RINGER_MODE_NORMAL);
        startPauseButton.setBackgroundResource(R.drawable.ic_play_button);
    }

    private void showEndNotification() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), Constants.CHANNEL_TIMER_COMPLETED)
                .setSmallIcon(R.drawable.ic_logo)
                .setColor(getColor(R.color.colorPrimary))
                .setContentTitle(getString(R.string.app_name))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true);

        createIntentToOpenApp(mBuilder);
        showEndNotificationContent(mBuilder);
        displayNotification(mBuilder);
    }

    private void showEndNotificationContent(NotificationCompat.Builder mBuilder) {
        if (isBreakState) {
            mBuilder.setContentText(getString(R.string.work_time));
        } else {
            mBuilder.setContentText(getString(R.string.break_time));
        }
    }

    private void createIntentToOpenApp(NotificationCompat.Builder mBuilder) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                Constants.PENDING_INTENT_OPEN_APP_REQUEST_CODE, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        mBuilder.setContentIntent(pendingIntent);
    }

    private void displayNotification(NotificationCompat.Builder mBuilder) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        notificationManagerCompat.notify(Constants.ON_FINISH_NOTIFICATION, mBuilder.build());
    }

    private void setupNotificationChannels() {
        if (isAndroidAtLeastOreo()) {
            NotificationChannel timerCompletedChannel = new NotificationChannel(Constants.CHANNEL_TIMER_COMPLETED,
                    Constants.CHANNEL_TIMER_COMPLETED, NotificationManager.IMPORTANCE_HIGH);
            NotificationChannel timerChannel = new NotificationChannel(Constants.CHANNEL_TIMER,
                    Constants.CHANNEL_TIMER, NotificationManager.IMPORTANCE_LOW);

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
}
