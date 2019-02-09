package com.wentura.pomodoroapp;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.arch.lifecycle.ViewModelProviders;
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
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
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

import static android.media.AudioManager.RINGER_MODE_NORMAL;
import static android.media.AudioManager.RINGER_MODE_SILENT;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String CHANNEL_TIMER_COMPLETED = "Pomodoro Timer Completed";
    private static final String CHANNEL_TIMER = "Pomodoro Timer";
    private static final String DEFAULT_WORK_TIME = "25";
    private static final String DEFAULT_BREAK_TIME = "5";

    private static final int ON_FINISH_NOTIFICATION = 0;
    private static final int TIME_LEFT_NOTIFICATION = 1;

    private static final int PENDING_INTENT_OPEN_APP_REQUEST_CODE = 0;
    private static final int PENDING_INTENT_SKIP_REQUEST_CODE = 1;
    private static final int PENDING_INTENT_PAUSE_RESUME_REQUEST_CODE = 2;
    private static final int PENDING_INTENT_STOP_REQUEST_CODE = 3;
    private static final String WORK_DURATION_SETTING = "work_duration_setting";
    private static final String BREAK_DURATION_SETTINGS = "break_duration_setting";

    private TextView countdownText;
    private ImageButton startPauseButton;
    private ImageButton stopButton;
    private ImageButton skipButton;
    private ImageView workBreakIcon;

    private MyViewModel viewModel;

    private BroadcastReceiver statusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra(ButtonConstants.BUTTON_ACTION);
            switch (action) {
                case ButtonConstants.BUTTON_SKIP:
                    skipTimer();
                    break;
                case ButtonConstants.BUTTON_PAUSE_RESUME:
                    startPauseTimer();
                    if (viewModel.isBreakState()) {
                        buildTimeLeftNotification(viewModel.getBreakLeftInMilliseconds());
                    } else {
                        buildTimeLeftNotification(viewModel.getWorkLeftInMilliseconds());
                    }
                    break;
                case ButtonConstants.BUTTON_STOP:
                    stopTimer();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = ViewModelProviders.of(this).get(MyViewModel.class);

        countdownText = findViewById(R.id.countdown_text_view);
        startPauseButton = findViewById(R.id.start_pause_button);
        stopButton = findViewById(R.id.stop_button);
        workBreakIcon = findViewById(R.id.work_break_icon);
        skipButton = findViewById(R.id.skip_button);

        Log.d(TAG, "onCreate: " + viewModel.isWorkStarted());

        if (savedInstanceState != null) {
            Log.d(TAG, "onCreate: savedInstanceState != null");
            Log.d(TAG, "onCreate: " + savedInstanceState.getBoolean("isWorkStarted"));
        }

        if (!viewModel.isWorkStarted() && !viewModel.isBreakStarted()) {
            skipButton.setVisibility(View.INVISIBLE);
            workBreakIcon.setVisibility(View.INVISIBLE);
            stopButton.setVisibility(View.INVISIBLE);
            Log.d(TAG, "onCreate: !viewModel.isWorkStarted() && !viewModel.isBreakStarted()");
        }

        if (viewModel.isTimerIsRunning()) {
            startPauseButton.setBackgroundResource(R.drawable.ic_pause_button);
        }

        if (viewModel.isBreakStarted() && viewModel.isTimerIsRunning()) {
            startTimer(viewModel.getBreakLeftInMilliseconds());
        }

        if (viewModel.isWorkStarted() && viewModel.isTimerIsRunning()) {
            startTimer(viewModel.getWorkLeftInMilliseconds());
        }

        if (viewModel.isWorkStarted() && !viewModel.isTimerIsRunning()) {
            updateTimerTextView(viewModel.getWorkLeftInMilliseconds());
        }

        if (viewModel.isBreakStarted() && !viewModel.isTimerIsRunning()) {
            updateTimerTextView(viewModel.getBreakLeftInMilliseconds());
        }

        if (!viewModel.isWorkStarted() && !viewModel.isBreakState()) {
            viewModel.setWorkLeftInMilliseconds(getMillisecondsFromSettings(WORK_DURATION_SETTING));
            updateTimerTextView(getMillisecondsFromSettings(WORK_DURATION_SETTING));
        }

        if (!viewModel.isBreakStarted()) {
            viewModel.setBreakLeftInMilliseconds(getMillisecondsFromSettings
                    (BREAK_DURATION_SETTINGS));
        }

        if (viewModel.isBreakStarted()) {
            workBreakIcon.setImageResource(R.drawable.break_icon);
        }

        if (viewModel.isWorkStarted()) {
            workBreakIcon.setImageResource(R.drawable.work_icon);
        }

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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState: ");
        outState.putBoolean("isWorkStarted", viewModel.isWorkStarted());
    }

    @Override
    protected void onResume() {
        super.onResume();
        toggleKeepScreenOn();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(statusReceiver);
        cancelAllNotifications();
        if (viewModel.getCountDownTimer() != null) {
            viewModel.getCountDownTimer().cancel();
        }
    }

    private void skipTimer() {
        viewModel.getCountDownTimer().cancel();
        if (viewModel.isBreakState()) {
            startTimer(getMillisecondsFromSettings(WORK_DURATION_SETTING));
            viewModel.setBreakLeftInMilliseconds(getMillisecondsFromSettings
                    (BREAK_DURATION_SETTINGS));
            toggleDoNotDisturb(this, RINGER_MODE_SILENT);
            workBreakIcon.setImageResource(R.drawable.work_icon);
            viewModel.setBreakState(false);
            viewModel.setBreakStarted(false);
            viewModel.setWorkStarted(true);
        } else {
            startTimer(getMillisecondsFromSettings(BREAK_DURATION_SETTINGS));
            toggleDoNotDisturb(this, RINGER_MODE_NORMAL);
            workBreakIcon.setImageResource(R.drawable.break_icon);
            viewModel.setBreakState(true);
            viewModel.setBreakStarted(true);
            viewModel.setWorkStarted(false);
            Log.d(TAG, "skipTimer: work");
        }
        startPauseButton.setBackgroundResource(R.drawable.ic_pause_button);
    }

    private long getMillisecondsFromSettings(String durationSetting) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String value = null;
        if (durationSetting.equals(WORK_DURATION_SETTING)) {
            value = sharedPreferences.getString(WORK_DURATION_SETTING, DEFAULT_WORK_TIME);
        }

        if (durationSetting.equals(BREAK_DURATION_SETTINGS)) {
            value = sharedPreferences.getString(BREAK_DURATION_SETTINGS, DEFAULT_BREAK_TIME);
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
        if (viewModel.isTimerIsRunning()) {
            pauseTimer();
            if (viewModel.isBreakState()) {
                buildTimeLeftNotification(viewModel.getBreakLeftInMilliseconds());
            } else {
                buildTimeLeftNotification(viewModel.getWorkLeftInMilliseconds());
            }
        } else {
            workBreakIcon.setVisibility(View.VISIBLE);
            if (viewModel.isBreakState()) {
                startTimer(viewModel.getBreakLeftInMilliseconds());
                viewModel.setBreakStarted(true);
                workBreakIcon.setImageResource(R.drawable.break_icon);
            } else {
                startTimer(viewModel.getWorkLeftInMilliseconds());
                toggleDoNotDisturb(this, RINGER_MODE_SILENT);
                buildTimeLeftNotification(viewModel.getWorkLeftInMilliseconds());
                viewModel.setWorkStarted(true);
                workBreakIcon.setImageResource(R.drawable.work_icon);
            }
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
            notificationManager.cancel(ON_FINISH_NOTIFICATION);
            startPauseButton.setBackgroundResource(R.drawable.ic_pause_button);
        }
        viewModel.setTimeLeftNotificationFirstTime(false);
    }

    private void stopTimer() {
        stopButton.setVisibility(View.INVISIBLE);
        workBreakIcon.setVisibility(View.INVISIBLE);
        startPauseButton.setVisibility(View.VISIBLE);
        skipButton.setVisibility(View.INVISIBLE);
        pauseTimer();
        updateTimerTextView(getMillisecondsFromSettings(WORK_DURATION_SETTING));
        viewModel.setBreakLeftInMilliseconds(getMillisecondsFromSettings(BREAK_DURATION_SETTINGS));
        viewModel.setWorkLeftInMilliseconds(getMillisecondsFromSettings(WORK_DURATION_SETTING));
        viewModel.setBreakState(false);
        viewModel.setWorkStarted(false);
        Log.d(TAG, "stopTimer: work");
        viewModel.setBreakStarted(false);
        viewModel.setTimeLeftNotificationFirstTime(true);
        cancelAllNotifications();
    }

    private void cancelAllNotifications() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        notificationManager.cancelAll();
    }

    private void startTimer(long timeInMilliseconds) {
        viewModel.setTimerIsRunning(true);

        viewModel.setCountDownTimer(new CountDownTimer(timeInMilliseconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (viewModel.isBreakState()) {
                    viewModel.setBreakLeftInMilliseconds(millisUntilFinished);
                } else {
                    viewModel.setWorkLeftInMilliseconds(millisUntilFinished);
                }
                updateTimerTextView(millisUntilFinished);
                buildTimeLeftNotification(millisUntilFinished);
                Log.d(TAG, "onTick: " + viewModel.isWorkStarted());
            }

            @Override
            public void onFinish() {
                cancelAllNotifications();
                showEndNotification();
                if (viewModel.isBreakState()) {
                    updateTimerTextView(getMillisecondsFromSettings(WORK_DURATION_SETTING));
                    workBreakIcon.setBackgroundResource(R.drawable.work_icon);
                    viewModel.setBreakState(false);
                    viewModel.setBreakStarted(false);
                } else {
                    toggleDoNotDisturb(getApplicationContext(), RINGER_MODE_NORMAL);
                    updateTimerTextView(getMillisecondsFromSettings(BREAK_DURATION_SETTINGS));
                    workBreakIcon.setBackgroundResource(R.drawable.break_icon);
                    viewModel.setBreakState(true);
                    viewModel.setWorkStarted(false);
                    Log.d(TAG, "onFinish: work");
                }
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                viewModel.setTimerIsRunning(false);
                viewModel.setTimeLeftNotificationFirstTime(true);
                startPauseButton.setBackgroundResource(R.drawable.ic_play_button);
                skipButton.setVisibility(View.INVISIBLE);
            }
        }.start());
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
        viewModel.getCountDownTimer().cancel();
        viewModel.setTimerIsRunning(false);
        toggleDoNotDisturb(this, RINGER_MODE_NORMAL);
        startPauseButton.setBackgroundResource(R.drawable.ic_play_button);
    }

    private void buildTimeLeftNotification(long millisUntilFinished) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_TIMER)
                .setSmallIcon(R.drawable.ic_logo)
                .setColor(getColor(R.color.colorPrimary))
                .setContentTitle("Pomodoro")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true)
                .setShowWhen(false);

        addButtonsToNotification(mBuilder);
        createIntentToOpenApp(mBuilder);
        setTimeLeftNotificationContent(millisUntilFinished, mBuilder);
        displayNotification(mBuilder, TIME_LEFT_NOTIFICATION);
    }

    private void displayNotification(NotificationCompat.Builder mBuilder, int notificationId) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        notificationManagerCompat.notify(notificationId, mBuilder.build());
    }

    private void setTimeLeftNotificationContent(long millisUntilFinished, NotificationCompat.Builder mBuilder) {
        if (!viewModel.isTimeLeftNotificationFirstTime()) {
            if (viewModel.isBreakState()) {
                mBuilder.setContentText(getString(R.string.break_time_left) + " " + calculateTimeLeft
                        (millisUntilFinished));
            } else {
                mBuilder.setContentText(getString(R.string.work_time_left) + " " + calculateTimeLeft
                        (millisUntilFinished));
            }
        }
    }

    private void createIntentToOpenApp(NotificationCompat.Builder mBuilder) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                PENDING_INTENT_OPEN_APP_REQUEST_CODE, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        mBuilder.setContentIntent(pendingIntent);
    }

    private void addButtonsToNotification(NotificationCompat.Builder mBuilder) {
        addPauseResumeButton(mBuilder);
        addSkipButton(mBuilder);
        addStopButton(mBuilder);
    }

    private void addStopButton(NotificationCompat.Builder mBuilder) {
        mBuilder.addAction(R.drawable.ic_stop_button, getString(R.string.stop),
                createButtonPendingIntent(ButtonConstants.BUTTON_STOP));
    }

    private void addSkipButton(NotificationCompat.Builder mBuilder) {
        mBuilder.addAction(R.drawable.ic_skip_button, getString(R.string.skip),
                createButtonPendingIntent(ButtonConstants.BUTTON_SKIP));
    }

    private void addPauseResumeButton(NotificationCompat.Builder mBuilder) {
        if (viewModel.isTimerIsRunning()) {
            mBuilder.addAction(R.drawable.ic_play_button, getString(R.string.pause),
                    createButtonPendingIntent(ButtonConstants.BUTTON_PAUSE_RESUME));
        } else {
            mBuilder.addAction(R.drawable.ic_play_button, getString(R.string.resume),
                    createButtonPendingIntent(ButtonConstants.BUTTON_PAUSE_RESUME));
        }
    }

    private PendingIntent createButtonPendingIntent(String actionValue) {
        return PendingIntent.getBroadcast(this, getRequestCode(actionValue),
                createButtonIntent(actionValue), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private int getRequestCode(String actionValue) {
        switch (actionValue) {
            case ButtonConstants.BUTTON_SKIP:
                return PENDING_INTENT_SKIP_REQUEST_CODE;
            case ButtonConstants.BUTTON_PAUSE_RESUME:
                return PENDING_INTENT_PAUSE_RESUME_REQUEST_CODE;
            case ButtonConstants.BUTTON_STOP:
                return PENDING_INTENT_STOP_REQUEST_CODE;
            default:
                return -1;
        }
    }

    @NonNull
    private Intent createButtonIntent(String actionValue) {
        Intent buttonIntent = new Intent(this, ActionReceiver.class);
        buttonIntent.putExtra(ButtonConstants.BUTTON_ACTION, actionValue);
        return buttonIntent;
    }

    private void showEndNotification() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_TIMER_COMPLETED)
                .setSmallIcon(R.drawable.ic_logo)
                .setColor(getColor(R.color.colorPrimary))
                .setContentTitle(getString(R.string.app_name))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setOngoing(true);

        createIntentToOpenApp(mBuilder);
        showEndNotificationContent(mBuilder);
        displayNotification(mBuilder, ON_FINISH_NOTIFICATION);
    }

    private void showEndNotificationContent(NotificationCompat.Builder mBuilder) {
        if (viewModel.isBreakState()) {
            mBuilder.setContentText(getString(R.string.work_time));
        } else {
            mBuilder.setContentText(getString(R.string.break_time));
        }
    }

    private void setupNotificationChannels() {
        if (isAndroidAtLeastOreo()) {
            NotificationChannel timerCompletedChannel = new NotificationChannel(CHANNEL_TIMER_COMPLETED,
                    "Pomodoro Timer Completed", NotificationManager.IMPORTANCE_HIGH);
            NotificationChannel timerChannel = new NotificationChannel(CHANNEL_TIMER,
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

        assert notificationManager != null;
        if (notificationManager.isNotificationPolicyAccessGranted()) {
            AudioManager audioManager = context.getSystemService(AudioManager.class);
            assert audioManager != null;
            audioManager.setRingerMode(mode);
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
