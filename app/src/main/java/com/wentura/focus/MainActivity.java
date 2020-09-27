/*
 * Copyright (C) 2020 Adrian Miozga <AdrianMiozga@outlook.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.wentura.focus;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.PopupMenu;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.google.android.material.snackbar.Snackbar;
import com.wentura.focus.activities.Activities;
import com.wentura.focus.database.Activity;
import com.wentura.focus.database.Database;
import com.wentura.focus.settings.SettingsActivity;
import com.wentura.focus.statistics.StatisticsActivity;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int AUTO_HIDE_FULL_SCREEN_AFTER = 3000;
    //    private Button skipButton;
    private ImageView workIcon;
    private ImageView breakIcon;
    private TextView timerTextView;
    private Button activityTextView;
    private Database database;
    private boolean isScaleAnimationDone = false;
    private boolean isTimerTextViewActionUpCalled = false;
    private Handler fullScreenHandler = new Handler();
    private Runnable enterFullScreen = () -> Utility.hideSystemUI(getWindow());
    private ImageButton menuButton;

    private final BroadcastReceiver statusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra(Constants.UPDATE_UI_ACTION);

            if (action == null) {
                return;
            }

            switch (action) {
                case Constants.BUTTON_SKIP:
                case Constants.BUTTON_START: {
                    SharedPreferences sharedPreferences =
                            PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                    boolean isBreakState = sharedPreferences.getBoolean(Constants.IS_BREAK_STATE, false);

//                    skipButton.setVisibility(View.VISIBLE);

                    if (isBreakState) {
                        workIcon.setVisibility(View.INVISIBLE);
                        breakIcon.setVisibility(View.VISIBLE);
                    } else {
                        workIcon.setVisibility(View.VISIBLE);
                        breakIcon.setVisibility(View.INVISIBLE);
                    }

                    break;
                }
                case Constants.BUTTON_STOP:
                    stopTimerUI();
                    break;
                case Constants.BUTTON_PAUSE:
//                    skipButton.setVisibility(View.VISIBLE);
                    startBlinkingAnimation();
                    break;
            }
        }
    };

    private final BroadcastReceiver updateTimerTextView = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            // Very rarely, this broadcast delivers a little late. If the user presses the stop
            // button, and this happens, it will update the timer text view after the stop timer method.
            // This condition prevents it.
            if (sharedPreferences.getBoolean(Constants.IS_STOP_BUTTON_VISIBLE, false)) {
                updateTimerTextView(intent.getIntExtra(Constants.TIME_LEFT_INTENT, 0));
            }
        }
    };

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (sharedPreferences.getBoolean(Constants.FULL_SCREEN_MODE, false)) {
            if (hasFocus) {
                Utility.hideSystemUI(getWindow());
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        if (event.getAction() != MotionEvent.ACTION_DOWN) {
            return false;
        }

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (sharedPreferences.getBoolean(Constants.FULL_SCREEN_MODE, false)) {
            View decorView = getWindow().getDecorView();
            boolean isFullScreenOff = (decorView.getSystemUiVisibility() & View.SYSTEM_UI_FLAG_FULLSCREEN) != 0;

            if (isFullScreenOff) {
                Utility.showSystemUI(getWindow());

                fullScreenHandler.removeCallbacks(enterFullScreen);
                fullScreenHandler.postDelayed(enterFullScreen, AUTO_HIDE_FULL_SCREEN_AFTER);
            } else {
                Utility.hideSystemUI(getWindow());
            }
        }

        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (sharedPreferences.getBoolean(Constants.FULL_SCREEN_MODE, false)) {
            Utility.hideSystemUI(getWindow());
        }

        setupUI();

        Utility.toggleKeepScreenOn(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                updateTimerTextView, new IntentFilter(Constants.ON_TICK));

        LocalBroadcastManager.getInstance(this).registerReceiver(
                statusReceiver, new IntentFilter(Constants.UPDATE_UI));
    }

    @Override
    public void onBackPressed() {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (sharedPreferences.getInt(Constants.TIME_LEFT, 0) == 0) {
            finish();
        } else {
            moveTaskToBack(true);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timerTextView = findViewById(R.id.countdown_text_view);
        activityTextView = findViewById(R.id.current_activity);
        workIcon = findViewById(R.id.work_icon);
        breakIcon = findViewById(R.id.break_icon);
//        skipButton = findViewById(R.id.skip_button);
        menuButton = findViewById(R.id.menu);

        setupNotificationChannels();

        database = Database.getInstance(this);

        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.hide();
        }

        activityTextView.setOnClickListener(view -> {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

            if (preferences.getBoolean(Constants.FULL_SCREEN_MODE, false)) {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
                    // On older APIs, without disabling full screen before entering Activities the animation
                    // would be bugged out.
                    Utility.showSystemUI(getWindow());
                }

                fullScreenHandler.removeCallbacks(enterFullScreen);
                Utility.hideSystemUI(getWindow());
            }

            startActivity(new Intent(this, Activities.class));
        });

        timerTextView.setOnTouchListener(new OnTouchListener(this) {

            @Override
            public void onUp() {
                isTimerTextViewActionUpCalled = true;

                if (isScaleAnimationDone) {
                    revertTimerAnimation();
                    isScaleAnimationDone = false;
                }
            }

            @Override
            public void onDown() {
                isTimerTextViewActionUpCalled = false;
                AnimatorSet animatorSet = startTimerAnimation();

                animatorSet.addListener(new Animator.AnimatorListener() {

                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        isScaleAnimationDone = true;

                        if (isTimerTextViewActionUpCalled) {
                            revertTimerAnimation();
                            isTimerTextViewActionUpCalled = false;
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                });
            }

            @Override
            public void onTap() {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                if (sharedPreferences.getBoolean(Constants.IS_TIMER_RUNNING, false)) {
                    pauseTimer();
                } else {
                    startTimer();
                }
            }

            @Override
            public void onMyLongPress() {
                stopTimer();
                isScaleAnimationDone = true;
            }

            @Override
            public void onSwipeLeft() {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                if (sharedPreferences.getBoolean(Constants.IS_TIMER_RUNNING, false) ||
                        sharedPreferences.getBoolean(Constants.IS_BREAK_STATE, false)) {

                    skipTimer();
                }
            }
        });

//        skipButton.setOnClickListener(view -> skipTimer());

        menuButton.setOnClickListener(view -> {
            if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Constants.FULL_SCREEN_MODE, false)) {
                // Showing pop up menu doesn't show status bar
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }

            PopupMenu popup = new PopupMenu(MainActivity.this, menuButton);

            popup.getMenuInflater().inflate(R.menu.menu, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

                switch (item.getItemId()) {
                    case R.id.settings:
                        startSettingsActivity();
                        return true;
                    case R.id.statistics:
                        startStatisticsActivity();
                        return true;
                }
                return false;
            });

            popup.show();
        });

        showHelpingSnackbars();
    }

    private void showHelpingSnackbars() {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);

        final int currentStep = sharedPreferences.getInt(Constants.TUTORIAL_STEP, 0);

        List<String> messages = Arrays.asList(
                getString(R.string.press_on_timer_snackbar_message),
                getString(R.string.swipe_timer_snackbar_message),
                getString(R.string.long_press_on_timer_snackbar_message));

        if (currentStep >= messages.size()) {
            return;
        }

        final SharedPreferences.Editor editPreferences =
                sharedPreferences.edit();

        final Snackbar snackbar = Snackbar.make(findViewById(R.id.main_activity), messages.get(currentStep),
                Snackbar.LENGTH_INDEFINITE);

        snackbar.setAction(getString(R.string.OK), view -> {
            snackbar.dismiss();

            editPreferences.putInt(Constants.TUTORIAL_STEP, currentStep + 1).apply();
            showHelpingSnackbars();
        });

        snackbar.setTextColor(Color.WHITE);
        snackbar.getView().setBackgroundColor(getResources().getColor(R.color.dark_grey));
        snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimary));
        snackbar.show();
    }

    private AnimatorSet startTimerAnimation() {
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(timerTextView,
                "scaleX", 0.95f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(timerTextView,
                "scaleY", 0.95f);
        scaleDownX.setDuration(100);
        scaleDownY.setDuration(100);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(scaleDownX).with(scaleDownY);
        animatorSet.start();
        return animatorSet;
    }

    private void revertTimerAnimation() {
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(
                timerTextView, "scaleX", 1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(
                timerTextView, "scaleY", 1f);
        scaleUpX.setDuration(100);
        scaleUpY.setDuration(100);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(scaleUpX).with(scaleUpY);
        animatorSet.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        fullScreenHandler.removeCallbacks(enterFullScreen);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(statusReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateTimerTextView);
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
        int workSessionCounter = sharedPreferences.getInt(Constants.WORK_SESSION_COUNTER, 0);
        int timeLeft = sharedPreferences.getInt(Constants.TIME_LEFT, 0);
        int activityId = sharedPreferences.getInt(Constants.CURRENT_ACTIVITY_ID, 1);

        Database.databaseExecutor.execute(() -> {
            int numberOfActivities = database.activityDao().getNumberOfActivities();

            if (numberOfActivities < 1) {
                database.activityDao().insertActivity(new Activity(getString(R.string.default_activity_name)));
            }

            String activityName = database.activityDao().getName(activityId);
            runOnUiThread(() -> activityTextView.setText(activityName));

            boolean areLongBreaksEnabled = database.activityDao().areLongBreaksEnabled(activityId);

            int duration;

            if (timeLeft == 0) {
                if (isBreakState) {
                    // TODO: 26.09.2020 Update this to reflect long break revamp
                    if (workSessionCounter != 0 && workSessionCounter % 4 == 0 && areLongBreaksEnabled) {
                        duration = database.activityDao().getLongBreakDuration(activityId);
                    } else {
                        duration = database.activityDao().getBreakDuration(activityId);
                    }
                } else {
                    duration = database.activityDao().getWorkDuration(activityId);
                }
                runOnUiThread(() -> updateTimerTextView(duration * 60_000));
            } else {
                runOnUiThread(() -> updateTimerTextView(timeLeft));
            }
        });

        if (sharedPreferences.getBoolean(Constants.IS_SKIP_BUTTON_VISIBLE, false)) {
//            skipButton.setVisibility(View.VISIBLE);
        } else {
//            skipButton.setVisibility(View.INVISIBLE);
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

        if (sharedPreferences.getBoolean(Constants.IS_TIMER_BLINKING, false)) {
            startBlinkingAnimation();
        }
    }

    private void stopTimer() {
        Intent stopIntent = new Intent(this, TimerActionReceiver.class);
        stopIntent.putExtra(Constants.BUTTON_ACTION, Constants.BUTTON_STOP);
        stopIntent.putExtra(Constants.CURRENT_ACTIVITY_ID_INTENT, getCurrentActivityId());
        sendBroadcast(stopIntent);
    }

    private void stopTimerUI() {
//        skipButton.setVisibility(View.INVISIBLE);
        workIcon.setVisibility(View.VISIBLE);
        breakIcon.setVisibility(View.INVISIBLE);

        revertTimerAnimation();
        timerTextView.clearAnimation();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        Database.databaseExecutor.execute(() -> {
            int duration =
                    database.activityDao().getWorkDuration(sharedPreferences.getInt(Constants.CURRENT_ACTIVITY_ID, 1));

            runOnUiThread(() -> updateTimerTextView(duration * 60_000));
        });
    }

    private void skipTimer() {
        revertTimerAnimation();
        timerTextView.clearAnimation();

        Intent intent = new Intent(this, TimerActionReceiver.class);
        intent.putExtra(Constants.BUTTON_ACTION, Constants.BUTTON_SKIP);
        intent.putExtra(Constants.CURRENT_ACTIVITY_ID_INTENT, getCurrentActivityId());
        sendBroadcast(intent);
    }

    private int getCurrentActivityId() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getInt(Constants.CURRENT_ACTIVITY_ID, 1);
    }

    private void startTimer() {
        timerTextView.clearAnimation();

        Intent intent = new Intent(this, TimerActionReceiver.class);
        intent.putExtra(Constants.BUTTON_ACTION, Constants.BUTTON_START);
        intent.putExtra(Constants.CURRENT_ACTIVITY_ID_INTENT, getCurrentActivityId());
        sendBroadcast(intent);
    }

    private void pauseTimer() {
        Intent intent = new Intent(this, TimerActionReceiver.class);
        intent.putExtra(Constants.BUTTON_ACTION, Constants.BUTTON_PAUSE);
        intent.putExtra(Constants.CURRENT_ACTIVITY_ID_INTENT, getCurrentActivityId());
        sendBroadcast(intent);
    }

    private void startBlinkingAnimation() {
        Animation blinkingAnimation = new AlphaAnimation(1.0f, 0.5f);
        blinkingAnimation.setDuration(1000);
        blinkingAnimation.setRepeatMode(Animation.REVERSE);
        blinkingAnimation.setRepeatCount(Animation.INFINITE);
        timerTextView.startAnimation(blinkingAnimation);
    }

    private void updateTimerTextView(long time) {
        timerTextView.setText(Utility.formatTime(time));
    }

    private void setupNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationManager notificationManager = getSystemService(NotificationManager.class);

        if (notificationManager == null) {
            return;
        }

        NotificationChannel timerCompletedChannel = new NotificationChannel(Constants.CHANNEL_TIMER_COMPLETED,
                Constants.CHANNEL_TIMER_COMPLETED, NotificationManager.IMPORTANCE_HIGH);

        NotificationChannel timerChannel = new NotificationChannel(Constants.CHANNEL_TIMER,
                Constants.CHANNEL_TIMER, NotificationManager.IMPORTANCE_LOW);

        timerCompletedChannel.setShowBadge(false);
        timerCompletedChannel.enableLights(true);
        timerCompletedChannel.setBypassDnd(true);

        timerChannel.setShowBadge(false);

        notificationManager.createNotificationChannel(timerCompletedChannel);
        notificationManager.createNotificationChannel(timerChannel);
    }
}