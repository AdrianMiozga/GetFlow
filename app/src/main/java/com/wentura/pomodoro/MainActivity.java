package com.wentura.pomodoro;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.wentura.pomodoro.database.Database;
import com.wentura.pomodoro.database.Pomodoro;
import com.wentura.pomodoro.settings.SettingsActivity;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {
    private ImageButton startButton;
    private ImageButton pauseButton;
    private ImageButton stopButton;
    private ImageButton skipButton;
    private ImageView workIcon;
    private ImageView breakIcon;
    private TextView countdownText;
    private Database database;

    private BroadcastReceiver statusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra(Constants.BUTTON_ACTION);

            switch (action) {
                case Constants.BUTTON_SKIP: {
                    SharedPreferences sharedPreferences =
                            PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                    boolean isBreakState = sharedPreferences.getBoolean(Constants.IS_BREAK_STATE, false);
                    boolean isTimerRunning = sharedPreferences.getBoolean(Constants.IS_TIMER_RUNNING,
                            false);

                    if (isBreakState) {
                        workIcon.setVisibility(View.INVISIBLE);
                        breakIcon.setVisibility(View.VISIBLE);
                    } else {
                        workIcon.setVisibility(View.VISIBLE);
                        breakIcon.setVisibility(View.INVISIBLE);
                    }

                    if (isTimerRunning) {
                        startButton.setVisibility(View.INVISIBLE);
                        pauseButton.setVisibility(View.VISIBLE);
                    } else {
                        startButton.setVisibility(View.VISIBLE);
                        pauseButton.setVisibility(View.INVISIBLE);
                    }
                    break;
                }
                case Constants.BUTTON_STOP:
                    stopTimerUI();
                    break;
                case Constants.BUTTON_START: {
                    startButton.setVisibility(View.INVISIBLE);
                    pauseButton.setVisibility(View.VISIBLE);
                    break;
                }
                case Constants.BUTTON_PAUSE: {
                    startButton.setVisibility(View.VISIBLE);
                    pauseButton.setVisibility(View.INVISIBLE);
                    break;
                }
            }
        }
    };

    private BroadcastReceiver updateTimerTextView = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long time = intent.getLongExtra(Constants.TIME_LEFT, 0);
            updateTimerTextView(time);
        }
    };

    private static int getLastSessionDuration(WeakReference<MainActivity> weakReference) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(weakReference.get());

        int lastWorkSessionDuration = sharedPreferences.getInt(Constants.LAST_SESSION_DURATION, 0);

        if (lastWorkSessionDuration == 0) {
            return Integer.parseInt(sharedPreferences.getString(Constants.WORK_DURATION_SETTING,
                    Constants.DEFAULT_WORK_TIME));
        } else {
            return lastWorkSessionDuration;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utility.toggleKeepScreenOn(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        countdownText = findViewById(R.id.countdown_text_view);
        startButton = findViewById(R.id.start_button);
        pauseButton = findViewById(R.id.pause_button);
        stopButton = findViewById(R.id.stop_button);
        workIcon = findViewById(R.id.work_icon);
        breakIcon = findViewById(R.id.break_icon);
        skipButton = findViewById(R.id.skip_button);

        setupUI();

        LocalBroadcastManager.getInstance(this).registerReceiver(
                statusReceiver, new IntentFilter(Constants.BUTTON_CLICKED));

        LocalBroadcastManager.getInstance(this).registerReceiver(
                updateTimerTextView, new IntentFilter(Constants.ON_TICK));

        setupNotificationChannels();

        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                if (sharedPreferences.getLong(Constants.TIMER_LEFT_IN_MILLISECONDS, 0) == 0) {
                    finish();
                } else {
                    moveTaskToBack(true);
                }
            }
        };

        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimer();
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseTimer();
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
    protected void onDestroy() {
        super.onDestroy();

        SharedPreferences.Editor editor =
                PreferenceManager.getDefaultSharedPreferences(this).edit();

        if (stopButton.getVisibility() == View.VISIBLE) {
            editor.putBoolean(Constants.IS_STOP_BUTTON_VISIBLE, true);
        } else {
            editor.putBoolean(Constants.IS_STOP_BUTTON_VISIBLE, false);
        }

        if (skipButton.getVisibility() == View.VISIBLE) {
            editor.putBoolean(Constants.IS_SKIP_BUTTON_VISIBLE, true);
        } else {
            editor.putBoolean(Constants.IS_SKIP_BUTTON_VISIBLE, false);
        }

        if (startButton.getVisibility() == View.VISIBLE) {
            editor.putBoolean(Constants.IS_START_BUTTON_VISIBLE, true);
            editor.putBoolean(Constants.IS_PAUSE_BUTTON_VISIBLE, false);
        } else {
            editor.putBoolean(Constants.IS_START_BUTTON_VISIBLE, false);
            editor.putBoolean(Constants.IS_PAUSE_BUTTON_VISIBLE, true);
        }

        if (workIcon.getVisibility() == View.VISIBLE) {
            editor.putBoolean(Constants.IS_WORK_ICON_VISIBLE, true);
            editor.putBoolean(Constants.IS_BREAK_ICON_VISIBLE, false);
        } else {
            editor.putBoolean(Constants.IS_WORK_ICON_VISIBLE, false);
            editor.putBoolean(Constants.IS_BREAK_ICON_VISIBLE, true);
        }

        editor.apply();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(statusReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateTimerTextView);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        String key = intent.getStringExtra(Constants.UPDATE_DATABASE_INTENT);
        if (key != null) {
            database = Database.getInstance(this);

            Intent displayEndNotification = new Intent(this, EndNotificationService.class);
            startService(displayEndNotification);

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

    private void setupUI() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        boolean isBreakState = sharedPreferences.getBoolean(Constants.IS_BREAK_STATE, false);
        long timerLeftInMilliseconds = sharedPreferences.getLong(Constants.TIMER_LEFT_IN_MILLISECONDS, 0);

        if (timerLeftInMilliseconds == 0) {
            if (isBreakState) {
                updateTimerTextView(Integer.parseInt(sharedPreferences.getString(Constants.BREAK_DURATION_SETTING,
                        Constants.DEFAULT_BREAK_TIME)) * 60000);
            } else {
                updateTimerTextView(Integer.parseInt(sharedPreferences.getString(Constants.WORK_DURATION_SETTING,
                        Constants.DEFAULT_WORK_TIME)) * 60000);
            }
        } else {
            updateTimerTextView(timerLeftInMilliseconds);
        }

        if (sharedPreferences.getBoolean(Constants.IS_SKIP_BUTTON_VISIBLE, false)) {
            skipButton.setVisibility(View.VISIBLE);
        } else {
            skipButton.setVisibility(View.INVISIBLE);
        }

        if (sharedPreferences.getBoolean(Constants.IS_STOP_BUTTON_VISIBLE, false)) {
            stopButton.setVisibility(View.VISIBLE);
        } else {
            stopButton.setVisibility(View.INVISIBLE);
        }

        if (sharedPreferences.getBoolean(Constants.IS_START_BUTTON_VISIBLE, true)) {
            startButton.setVisibility(View.VISIBLE);
        } else {
            startButton.setVisibility(View.INVISIBLE);
        }

        if (sharedPreferences.getBoolean(Constants.IS_PAUSE_BUTTON_VISIBLE, false)) {
            pauseButton.setVisibility(View.VISIBLE);
        } else {
            pauseButton.setVisibility(View.INVISIBLE);
        }

        if (sharedPreferences.getBoolean(Constants.IS_WORK_ICON_VISIBLE, true)) {
            workIcon.setVisibility(View.VISIBLE);
        } else {
            workIcon.setVisibility(View.INVISIBLE);
        }

        if (sharedPreferences.getBoolean(Constants.IS_BREAK_ICON_VISIBLE, false)) {
            breakIcon.setVisibility(View.VISIBLE);
        } else {
            breakIcon.setVisibility(View.INVISIBLE);
        }
    }

    private void stopTimer() {
        Intent stopIntent = new Intent(this, NotificationButtonReceiver.class);
        stopIntent.putExtra(Constants.BUTTON_ACTION, Constants.BUTTON_STOP);
        sendBroadcast(stopIntent);
    }

    private void stopTimerUI() {
        stopButton.setVisibility(View.INVISIBLE);
        startButton.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.INVISIBLE);
        skipButton.setVisibility(View.INVISIBLE);
        workIcon.setVisibility(View.VISIBLE);
        breakIcon.setVisibility(View.INVISIBLE);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        updateTimerTextView(Integer.parseInt(sharedPreferences.getString(Constants.WORK_DURATION_SETTING,
                Constants.DEFAULT_WORK_TIME)) * 60000);
    }

    private void skipTimer() {
        Intent intent = new Intent(this, NotificationButtonReceiver.class);
        intent.putExtra(Constants.BUTTON_ACTION, Constants.BUTTON_SKIP);
        sendBroadcast(intent);
    }

    private void startTimer() {
        stopButton.setVisibility(View.VISIBLE);
        skipButton.setVisibility(View.VISIBLE);
        startButton.setVisibility(View.INVISIBLE);
        pauseButton.setVisibility(View.VISIBLE);

        Intent intent = new Intent(this, NotificationButtonReceiver.class);
        intent.putExtra(Constants.BUTTON_ACTION, Constants.BUTTON_START);
        sendBroadcast(intent);
    }

    private void pauseTimer() {
        stopButton.setVisibility(View.VISIBLE);
        skipButton.setVisibility(View.VISIBLE);
        startButton.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.INVISIBLE);

        Intent intent = new Intent(this, NotificationButtonReceiver.class);
        intent.putExtra(Constants.BUTTON_ACTION, Constants.BUTTON_PAUSE);
        sendBroadcast(intent);
    }

    private void updateTimerTextView(long timeInMilliseconds) {
        countdownText.setText(Utility.formatTime(this, timeInMilliseconds));
    }

    private void setupNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

    private static class UpdateDatabaseWorks extends AsyncTask<Void, Void, Void> {
        private WeakReference<MainActivity> weakReference;

        UpdateDatabaseWorks(MainActivity context) {
            this.weakReference = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String currentDate = Utility.getCurrentDate();

            if (weakReference.get().database.pomodoroDao().getLatestDate().equals(currentDate)) {
                weakReference.get().database.pomodoroDao().updateCompletedWorks(weakReference.get().database.pomodoroDao().getCompletedWorks(currentDate) + 1, currentDate);
                weakReference.get().database.pomodoroDao().updateCompletedWorksTime(weakReference.get().database.pomodoroDao().getCompletedWorksTime(currentDate) + getLastSessionDuration(weakReference), currentDate);
            } else {
                weakReference.get().database.pomodoroDao().insertPomodoro(new Pomodoro(currentDate, 1, 0, getLastSessionDuration(weakReference), 0));
            }
            return null;
        }
    }
}